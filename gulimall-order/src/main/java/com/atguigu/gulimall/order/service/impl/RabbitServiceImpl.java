package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@RabbitListener(queues = {"hello_java_queue"})
@Slf4j
@Service("rabbitService")
public class RabbitServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements RabbitService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 接收消息
     * @param message
     */
    /*@RabbitListener(queues = {"hello_java_queue"})
    public void recieveMessage(Object message) {
        System.out.println("接收到的消息"+message);
        System.out.println("接收到的类型"+message.getClass());
    }*/

  /*  @RabbitListener(queues = {"hello_java_queue"})
    public void recieveMessage(Message message,
                               OrderReturnApplyEntity content,
                               Channel channel
                               ) {
        byte[] body = message.getBody();
        //消息头信息
        MessageProperties properties = message.getMessageProperties();
        System.out.println("接收到的消息..."+message+"===>"+content);
      *//*  System.out.println("接收到的消息"+message);
        System.out.println("接收到的类型"+message.getClass());*//*
    }*/

    /**
     * queues 声明需要监听的Queue
     * @param message 监听得到的消息 头+体
     * @param orderReturnReasonEntity 返回的数据 spring会自动类型转换
     * @param channel 通道
     */
    @RabbitHandler
    public void recieveMessage(Message message, OrderReturnReasonEntity orderReturnReasonEntity, Channel channel) {
     /*   log.info("接受到了消息[{}]", orderReturnReasonEntity);*/
        log.info("信息处理完成=》"+orderReturnReasonEntity.getName());
    }

    @RabbitHandler
    public void recieveMessage(OrderEntity orderEntity, Channel channel) {
        log.info("接受到了消息[{}]", orderEntity);

    }

    /**
     *
     .   ____          _            __ _ _
     /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
     ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
     \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
     '  |____| .__|_| |_|_| |_\__, | / / / /
     =========|_|==============|___/=/_/_/_/
     :: Spring Boot ::        (v2.2.9.RELEASE)

     2020-09-16 22:51:50.452  INFO 25012 --- [           main] c.a.n.c.c.impl.LocalConfigInfoProcessor  : LOCAL_SNAPSHOT_PATH:C:\Users\yuhl\nacos\config
     2020-09-16 22:51:50.478  INFO 25012 --- [           main] c.a.nacos.client.config.impl.Limiter     : limitTime:5.0
     2020-09-16 22:51:50.501  WARN 25012 --- [           main] c.a.c.n.c.NacosPropertySourceBuilder     : Ignore the empty nacos configuration and get it based on dataId[null.properties] & group[DEFAULT_GROUP]
     2020-09-16 22:51:50.503  INFO 25012 --- [           main] b.c.PropertySourceBootstrapConfiguration : Located property source: [BootstrapPropertySource {name='bootstrapProperties-null.properties,DEFAULT_GROUP'}]
     2020-09-16 22:51:50.506  INFO 25012 --- [           main] c.a.g.order.GulimallOrderApplication     : No active profile set, falling back to default profiles: default
     2020-09-16 22:51:51.148  INFO 25012 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=d0e45efc-1f0e-3f32-8d0a-fd9a9e05df91
     2020-09-16 22:51:51.494  INFO 25012 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 9000 (http)
     2020-09-16 22:51:51.505  INFO 25012 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
     2020-09-16 22:51:51.505  INFO 25012 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.37]
     2020-09-16 22:51:51.697  INFO 25012 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
     2020-09-16 22:51:51.697  INFO 25012 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1177 ms
     Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
     _ _   |_  _ _|_. ___ _ |    _
     | | |\/|_)(_| | |_\  |_)||_|_\
     /               |
     3.3.2
     2020-09-16 22:51:55.728  WARN 25012 --- [           main] o.s.c.n.a.ArchaiusAutoConfiguration      : No spring.application.name found, defaulting to 'application'
     2020-09-16 22:51:55.731  WARN 25012 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
     2020-09-16 22:51:55.731  INFO 25012 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
     2020-09-16 22:51:55.735  WARN 25012 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
     2020-09-16 22:51:55.735  INFO 25012 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
     2020-09-16 22:51:55.860  INFO 25012 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
     2020-09-16 22:51:57.903  INFO 25012 --- [           main] o.s.s.c.ThreadPoolTaskScheduler          : Initializing ExecutorService 'Nacso-Watch-Task-Scheduler'
     2020-09-16 22:51:57.908  INFO 25012 --- [           main] com.alibaba.nacos.client.naming          : initializer namespace from System Property :null
     2020-09-16 22:51:57.908  INFO 25012 --- [           main] com.alibaba.nacos.client.naming          : initializer namespace from System Environment :null
     2020-09-16 22:51:57.908  INFO 25012 --- [           main] com.alibaba.nacos.client.naming          : initializer namespace from System Property :null
     2020-09-16 22:52:00.045  INFO 25012 --- [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [192.168.56.10:5672]
     2020-09-16 22:52:00.074  INFO 25012 --- [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#26e8ff8c:0/SimpleConnection@464ede1f [delegate=amqp://guest@192.168.56.10:5672/, localPort= 59247]
     2020-09-16 22:52:00.141  INFO 25012 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 9000 (http) with context path ''
     2020-09-16 22:52:00.142  WARN 25012 --- [           main] c.a.c.n.registry.NacosServiceRegistry    : No service to register for nacos client...
     2020-09-16 22:52:02.190  INFO 25012 --- [           main] c.a.g.order.GulimallOrderApplication     : Started GulimallOrderApplication in 16.83 seconds (JVM running for 20.038)
     2020-09-16 22:52:02.193  INFO 25012 --- [           main] c.a.n.client.config.impl.ClientWorker    : [fixed-localhost_8848] [subscribe] null.properties+DEFAULT_GROUP
     2020-09-16 22:52:02.194  INFO 25012 --- [           main] c.a.nacos.client.config.impl.CacheData   : [fixed-localhost_8848] [add-listener] ok, tenant=, dataId=null.properties, group=DEFAULT_GROUP, cnt=1
     2020-09-16 22:52:09.990  INFO 25012 --- [nio-9000-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
     2020-09-16 22:52:09.990  INFO 25012 --- [nio-9000-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
     2020-09-16 22:52:09.997  INFO 25012 --- [nio-9000-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 6 ms
     2020-09-16 22:52:10.073  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderReturnReasonEntity消息发送成功！
     2020-09-16 22:52:10.078  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderEntity消息发送成功！
     2020-09-16 22:52:10.079  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderReturnReasonEntity消息发送成功！
     2020-09-16 22:52:10.079  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderEntity消息发送成功！
     2020-09-16 22:52:10.079  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderReturnReasonEntity消息发送成功！
     2020-09-16 22:52:10.080  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderEntity消息发送成功！
     2020-09-16 22:52:10.080  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderReturnReasonEntity消息发送成功！
     2020-09-16 22:52:10.081  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderEntity消息发送成功！
     2020-09-16 22:52:10.081  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderReturnReasonEntity消息发送成功！
     2020-09-16 22:52:10.081  INFO 25012 --- [nio-9000-exec-1] c.a.g.order.controller.RabbitController  : OrderEntity消息发送成功！
     2020-09-16 22:52:10.142  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 信息处理完成=》于红亮
     2020-09-16 22:52:10.152  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 接受到了消息[OrderEntity(id=null, memberId=null, orderSn=1e64396c-da42-4365-b350-c711620ce934, couponId=null, createTime=null, memberUsername=null, totalAmount=null, payAmount=null, freightAmount=null, promotionAmount=null, integrationAmount=null, couponAmount=null, discountAmount=null, payType=null, sourceType=null, status=null, deliveryCompany=null, deliverySn=null, autoConfirmDay=null, integration=null, growth=null, billType=null, billHeader=null, billContent=null, billReceiverPhone=null, billReceiverEmail=null, receiverName=null, receiverPhone=null, receiverPostCode=null, receiverProvince=null, receiverCity=null, receiverRegion=null, receiverDetailAddress=null, note=null, confirmStatus=null, deleteStatus=null, useIntegration=null, paymentTime=null, deliveryTime=null, receiveTime=null, commentTime=null, modifyTime=null)]
     2020-09-16 22:52:10.153  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 信息处理完成=》于红亮
     2020-09-16 22:52:10.153  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 接受到了消息[OrderEntity(id=null, memberId=null, orderSn=298e55af-afa7-4c89-9b0f-8e300417fb8d, couponId=null, createTime=null, memberUsername=null, totalAmount=null, payAmount=null, freightAmount=null, promotionAmount=null, integrationAmount=null, couponAmount=null, discountAmount=null, payType=null, sourceType=null, status=null, deliveryCompany=null, deliverySn=null, autoConfirmDay=null, integration=null, growth=null, billType=null, billHeader=null, billContent=null, billReceiverPhone=null, billReceiverEmail=null, receiverName=null, receiverPhone=null, receiverPostCode=null, receiverProvince=null, receiverCity=null, receiverRegion=null, receiverDetailAddress=null, note=null, confirmStatus=null, deleteStatus=null, useIntegration=null, paymentTime=null, deliveryTime=null, receiveTime=null, commentTime=null, modifyTime=null)]
     2020-09-16 22:52:10.154  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 信息处理完成=》于红亮
     2020-09-16 22:52:10.154  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 接受到了消息[OrderEntity(id=null, memberId=null, orderSn=769a1324-53ba-4c73-8cdc-e92511c7d5ea, couponId=null, createTime=null, memberUsername=null, totalAmount=null, payAmount=null, freightAmount=null, promotionAmount=null, integrationAmount=null, couponAmount=null, discountAmount=null, payType=null, sourceType=null, status=null, deliveryCompany=null, deliverySn=null, autoConfirmDay=null, integration=null, growth=null, billType=null, billHeader=null, billContent=null, billReceiverPhone=null, billReceiverEmail=null, receiverName=null, receiverPhone=null, receiverPostCode=null, receiverProvince=null, receiverCity=null, receiverRegion=null, receiverDetailAddress=null, note=null, confirmStatus=null, deleteStatus=null, useIntegration=null, paymentTime=null, deliveryTime=null, receiveTime=null, commentTime=null, modifyTime=null)]
     2020-09-16 22:52:10.154  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 信息处理完成=》于红亮
     2020-09-16 22:52:10.155  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 接受到了消息[OrderEntity(id=null, memberId=null, orderSn=75b4446b-adf6-41a3-847b-ae261a037652, couponId=null, createTime=null, memberUsername=null, totalAmount=null, payAmount=null, freightAmount=null, promotionAmount=null, integrationAmount=null, couponAmount=null, discountAmount=null, payType=null, sourceType=null, status=null, deliveryCompany=null, deliverySn=null, autoConfirmDay=null, integration=null, growth=null, billType=null, billHeader=null, billContent=null, billReceiverPhone=null, billReceiverEmail=null, receiverName=null, receiverPhone=null, receiverPostCode=null, receiverProvince=null, receiverCity=null, receiverRegion=null, receiverDetailAddress=null, note=null, confirmStatus=null, deleteStatus=null, useIntegration=null, paymentTime=null, deliveryTime=null, receiveTime=null, commentTime=null, modifyTime=null)]
     2020-09-16 22:52:10.155  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 信息处理完成=》于红亮
     2020-09-16 22:52:10.155  INFO 25012 --- [ntContainer#0-1] c.a.g.o.s.impl.OrderItemServiceImpl      : 接受到了消息[OrderEntity(id=null, memberId=null, orderSn=b754fa7e-f778-45a8-8d8a-cf30581812d0, couponId=null, createTime=null, memberUsername=null, totalAmount=null, payAmount=null, freightAmount=null, promotionAmount=null, integrationAmount=null, couponAmount=null, discountAmount=null, payType=null, sourceType=null, status=null, deliveryCompany=null, deliverySn=null, autoConfirmDay=null, integration=null, growth=null, billType=null, billHeader=null, billContent=null, billReceiverPhone=null, billReceiverEmail=null, receiverName=null, receiverPhone=null, receiverPostCode=null, receiverProvince=null, receiverCity=null, receiverRegion=null, receiverDetailAddress=null, note=null, confirmStatus=null, deleteStatus=null, useIntegration=null, paymentTime=null, deliveryTime=null, receiveTime=null, commentTime=null, modifyTime=null)]

     */
}