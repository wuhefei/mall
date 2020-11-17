package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.atguigu.vo.MemberRespVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${myRabbitmq.MQConfig.eventExchange}")
    private String eventExchange;

    @Value("${myRabbitmq.MQConfig.createOrder}")
    private String createOrder;

    @Value("${myRabbitmq.MQConfig.ReleaseOtherKey}")
    private String ReleaseOtherKey;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取订单确认页需要的数据
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        /**
         * 解决异步任务拿不到ThreadLocal里的数据
         * 获取之前的请求，让每个异步任务的线程共享ThreadLocal数据
         */
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(() -> {

            RequestContextHolder.setRequestAttributes(requestAttributes); // 解决异步任务拿不到ThreadLocal里的数据

            // 1、远程查询所有的收货地址
            List<MemberAddressVo> addresses = memberFeignService.getAddresses(memberRespVo.getId());
            orderConfirmVo.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> getCartTask = CompletableFuture.runAsync(() -> {

            RequestContextHolder.setRequestAttributes(requestAttributes); // 解决异步任务拿不到ThreadLocal里的数据

            // 2、远程查询购物车所有选中的购物项
            List<OrderItemVo> orderItems = cartFeignService.currentUserCartItems();
            orderConfirmVo.setItems(orderItems);
        }, executor).thenRunAsync(() -> {
            // 查询商品是否有库存
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wareFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(map);
            }

        },executor);


        // 3、用户积分信息
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);

        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);


        CompletableFuture.allOf(getAddressTask, getCartTask).get(); // 阻塞等待异步任务完成

        return orderConfirmVo;
    }

    /**
     * 下单
     * @param
     * @return
     */
  /*  @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo) {

        orderSubmitVoThreadLocal.set(orderSubmitVo);

        SubmitOrderRespVo submitOrderRespVo = new SubmitOrderRespVo();
        submitOrderRespVo.setCode(0);

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        // 判断本次下单的token和redis存储的token是否一致
        String redisOrderToken = stringRedisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());

        //  redis+lua脚本 原子验证令牌防止重复提交攻击
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        //  return 0 失败  1 成功
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);

        if (result == 0) { // 令牌验证失败
            submitOrderRespVo.setCode(1);
            return submitOrderRespVo;
        } else { // 令牌验证成功
            OrderCreateTo order = createOrder();

            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 验价通过

                // 保存订单！！！
                saveOrder(order);
                // 锁库存 有异常回滚
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrderEntity().getOrderSn());

                List<OrderItemVo> collect = order.getItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());

                wareSkuLockVo.setLocks(collect);

                // 远程锁库存！！！
                R r = wareFeignService.orderLocKStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    submitOrderRespVo.setOrderEntity(order.getOrderEntity());
                    // TODO 如果使用的积分优惠，扣减积分
                    //int i = 10/0; // 测试分布式事务，远程锁库存业务回滚

                    // 订单创建成功发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrderEntity());

                    return submitOrderRespVo;
                } else {
                    // 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            } else {
                submitOrderRespVo.setCode(2);
                return submitOrderRespVo;
            }
        }



    }*/

    /**
     * 现在我们不用seata来控制事务了。我们使用队列来做。
     * @param vo
     * @return
     */
	//@GlobalTransactional //添加seata的分布式事务。
    @Transactional //订单和库存应该在一个事物里面。
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo vo) {
        // 当条线程共享这个对象

        orderSubmitVoThreadLocal.set(vo);
        SubmitOrderRespVo submitVo = new SubmitOrderRespVo();
        // 0：正常
        submitVo.setCode(0);
        // 去服务器创建订单,验令牌,验价格,所库存
        MemberRespVo memberRsepVo = LoginUserInterceptor.threadLocal.get();
        // 1. 验证令牌 [必须保证原子性] 返回 0 or 1
        // 0 令牌删除失败 1删除成功
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        // 原子验证令牌 删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRsepVo.getId()), orderToken);
        if(result == 0L){
            // 令牌验证失败
            submitVo.setCode(1);
        }else{
            // 令牌验证成功
            // 1 .创建订单等信息
            OrderCreateTo order = createOrder();

            BigDecimal fare =  order.getOrderEntity().getFreightAmount();//运费
            // 2. 验价 的时候需要把运费加上
            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal voPayPrice = vo.getPayPrice();
            if(Math.abs(payAmount.add(fare).subtract(voPayPrice).doubleValue()) < 0.01){
                // 金额对比成功
                // 3.保存订单
                saveOrder(order);
                // 4.库存锁定
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrderEntity().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItemEntities().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    // 锁定的skuId 这个skuId要锁定的数量
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());

                lockVo.setLocks(locks);
                // 远程锁库存
                R r = wareFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0){ //0表示锁定成功。
                    // 库存足够 锁定成功 给MQ发送消息
                    submitVo.setOrderEntity(order.getOrderEntity());
					//TODO 下单成成，远程扣减积分出现异常
                    //int i = 10/0; //假设锁定库存失败，订单已经
                    rabbitTemplate.convertAndSend(this.eventExchange, this.createOrder, order.getOrderEntity());
                }else{
                    // 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg); //只要排除异常，事务就回滚
                }

            }else {
                // 价格验证失败
                submitVo.setCode(2);
            }
        }
        return submitVo;
    }



    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        log.info("\n收到过期的订单信息--准关闭订单:" + entity.getOrderSn());
        // 查询这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if(orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // 发送给MQ告诉它有一个订单被自动关闭了
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                //TODO ：这一点必须保证，在电商业务中，如果消息没有发送出去是灾难性的。
                // 保证消息 100% 发出去 每一个消息在数据库保存详细信息
                // 定期扫描数据库 将失败的消息在发送一遍
                //在一种情况下需要砸解锁订单时，发送一条直接mq信息给解锁锁定库存，这个非常有必要
                //在那种情况下呢：在由于演示原因，库存在更新了扣库存状态，然后一分钟后去检查订单是不是待确认状态
                //一看还没有。就会把消息消费掉，然后库存一直是被锁定的状态，
                //这个时候，订单由于网络演示等原因才在数据库中inset了一条订单的消息为待确认这个时候，被锁定
                //的库存永远也无法被人触及了。所以需要在处理订单的时候直接关联到库存锁定的队列去解一下库存。
                /**
                 *DROP TABLE IF EXISTS `mq_message`;
                 * CREATE TABLE `mq_message`  (
                 *   `message_id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                 *   `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'JSON',
                 *   `to_exchange` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                 *   `class_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                 *   `message_status` int(1) NULL DEFAULT 0 COMMENT '0-新建 1-已发送 2-错误抵达 3-已抵达',
                 *   `create_time` datetime(0) NULL DEFAULT NULL,
                 *   `update_time` datetime(0) NULL DEFAULT NULL,
                 *   PRIMARY KEY (`message_id`) USING BTREE
                 * ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
                 */
                rabbitTemplate.convertAndSend(eventExchange, ReleaseOtherKey , orderTo);
            } catch (AmqpException e) {
                // TODO： 将没发送成功的消息进行重试发送.
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        // 保留2位小数位向上补齐
        payVo.setTotal_amount(order.getTotalAmount().add(order.getFreightAmount()==null?new BigDecimal("0"):order.getFreightAmount()).setScale(2,BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(order.getOrderSn());
        List<OrderItemEntity> entities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
        payVo.setSubject("gulimall");
        payVo.setBody("gulimall");
        if(null != entities.get(0).getSkuName() && entities.get(0).getSkuName().length() > 1){
//			payVo.setSubject(entities.get(0).getSkuName());
//			payVo.setBody(entities.get(0).getSkuName());
            payVo.setSubject("gulimall");
            payVo.setBody("gulimall");
        }
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                // 查询这个用户的最新订单 [降序排序]
                new QueryWrapper<OrderEntity>().eq("member_id",memberRespVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            // 查询这个订单关联的所有订单项
            List<OrderItemEntity> orderSn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(orderSn);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);
        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo vo) {

        // 1.保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        //		TRADE_SUCCESS
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        infoEntity.setSubject(vo.getSubject());
        infoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        infoEntity.setCreateTime(vo.getGmt_create());
        paymentInfoService.save(infoEntity);

        // 2.修改订单状态信息TRADE_SUCCESS  TRADE_FINISHED 都是支付宝的说明文档中。
        if(vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")){
            // 支付成功
            String orderSn = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    /**
     * 秒杀
     * 从度列中获取秒杀成功的订单，去持久化数据库。
     * @param secKillOrderTo
     */
    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrderTo) {
        log.info("\n创建秒杀订单");
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(secKillOrderTo.getOrderSn());
        entity.setMemberId(secKillOrderTo.getMemberId());
        entity.setCreateTime(new Date());
        entity.setPayAmount(secKillOrderTo.getSeckillPrice());
        entity.setTotalAmount(secKillOrderTo.getSeckillPrice());
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setPayType(1);
        // TODO 还有挺多的没设置
        BigDecimal price = secKillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + secKillOrderTo.getNum()));
        entity.setPayAmount(price);//应付价格，要支付的价格。积分啥的不在添加了。

        this.save(entity);

        // 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        itemEntity.setRealAmount(price);
        itemEntity.setOrderId(entity.getId());
        itemEntity.setSkuQuantity(secKillOrderTo.getNum());
        R info = productFeignService.getSkuInfoBySkuId(secKillOrderTo.getSkuId());
        SpuInfoVo spuInfo = info.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());
        itemEntity.setGiftGrowth(secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum())).intValue());
        itemEntity.setGiftIntegration(secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum())).intValue());
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        orderItemService.save(itemEntity);
    }

    /**
     * 创建订单
     */
    private OrderCreateTo createOrder(){

        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1. 生成一个订单号
        String orderSn = IdWorker.getTimeId();
        log.info("档案号码："+orderSn);
        OrderEntity orderEntity = buildOrderSn(orderSn);

        // 2. 获取所有订单项
        List<OrderItemEntity> items = buildOrderItems(orderSn);

        // 3.验价	传入订单 、订单项 计算价格、积分、成长值等相关信息
        computerPrice(orderEntity,items);
        orderCreateTo.setOrderEntity(orderEntity);
        orderCreateTo.setOrderItemEntities(items);
        return orderCreateTo;
    }
    /**
     * 构建一个订单-对应一个订单号
     */
    private OrderEntity buildOrderSn(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setCreateTime(new Date());
        entity.setCommentTime(new Date());
        entity.setReceiveTime(new Date());
        entity.setDeliveryTime(new Date());
        MemberRespVo rsepVo = LoginUserInterceptor.threadLocal.get();
        entity.setMemberId(rsepVo.getId());
        entity.setMemberUsername(rsepVo.getUsername());
        entity.setBillReceiverEmail(rsepVo.getEmail());
        // 2. 获取收获地址信息
        OrderSubmitVo submitVo = orderSubmitVoThreadLocal.get();
        R fare = wareFeignService.getFare(submitVo.getAddrId());
        FareVo resp = fare.getData(new TypeReference<FareVo>() {});
        entity.setFreightAmount(resp.getFare());
        entity.setReceiverCity(resp.getMemberAddressVo().getCity());
        entity.setReceiverDetailAddress(resp.getMemberAddressVo().getDetailAddress());
        entity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setReceiverPhone(resp.getMemberAddressVo().getPhone());
        entity.setReceiverName(resp.getMemberAddressVo().getName());
        entity.setReceiverPostCode(resp.getMemberAddressVo().getPostCode());
        entity.setReceiverProvince(resp.getMemberAddressVo().getProvince());
        entity.setReceiverRegion(resp.getMemberAddressVo().getRegion());
        // 设置订单状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }


    /**
     * 为 orderSn 订单构建订单数据
     */
    /**
     *
     * @param orderSn 为了给订单向中设置订单号。
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 这里是最后一次来确认购物项的价格 这个远程方法还会查询一次数据库
        List<OrderItemVo> cartItems = cartFeignService.currentUserCartItems();
        List<OrderItemEntity> itemEntities = null;
        if(cartItems != null && cartItems.size() > 0){
            itemEntities = cartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);//构建每一个订单项
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return itemEntities;
    }
    /**
     * 构建某一个订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1.订单信息： 订单号

        // 2.商品spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);//最后一个确认价格了。如果再次之后再修改价格也要按这个来结算了。
        SpuInfoVo spuInfo = r.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());
        // 3.商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        // 把一个集合按照指定的字符串进行分割得到一个字符串StringUtils Apring加的。
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        // 4.积分信息 买的数量越多积分越多 成长值越多 价格等于积分
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        // 5.订单项的价格信息 优惠金额
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // 当前订单项的实际金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        // 减去各种优惠的价格 减去优惠-减去打折-减去积分
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getIntegrationAmount());
        //设置真是价格
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }


    /**
     * 计算订单价格等信息
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal totalPrice = new BigDecimal("0.0");
        BigDecimal totalGiftIntegration = new BigDecimal("0.0");
        BigDecimal totalGiftGrowth = new BigDecimal("0.0");

        for (OrderItemEntity itemEntity : orderItemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            totalPrice = totalPrice.add(realAmount);

            totalGiftIntegration = totalGiftIntegration.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
            totalGiftGrowth = totalGiftGrowth.add(new BigDecimal(itemEntity.getGiftGrowth()));
        }

        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setIntegration(totalGiftIntegration.intValue());
        orderEntity.setGrowth(totalGiftGrowth.intValue());
    }

    /**
     * 构建某个订单项数据
     * @param cartItem
     * @return
     */
    private OrderItemEntity buidOrderItem(OrderItemVo cartItem) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 商品spu信息
        R r = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

        // sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());

        // 成长值、积分信息
        // TODO 模拟数据
        int giftGrowth = (cartItem.getTotalPrice().intValue()) / 10;
        orderItemEntity.setGiftGrowth(giftGrowth);
        orderItemEntity.setGiftIntegration(giftGrowth);
        // 该商品经过优惠后的分解金额
        // TODO 实现优惠后的价格
        orderItemEntity.setRealAmount(orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString())));

        return orderItemEntity;

    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {

        OrderEntity orderEntity = order.getOrderEntity();
        orderEntity.setModifyTime(new Date());

        this.save(orderEntity);

        List<OrderItemEntity> items = order.getOrderItemEntities();
        for (OrderItemEntity item : items) {
            orderItemService.getBaseMapper().insert(item);
        }

    }


    @Transactional
    public void a() {
        AopContext.currentProxy();
        b();
        c();
        int i =10/0;
    }

    @Transactional(propagation = Propagation.REQUIRED,timeout = 2)
    public void b() {
        System.out.println("b");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,timeout = 2)
    public void c() {
        System.out.println("c");
    }

}