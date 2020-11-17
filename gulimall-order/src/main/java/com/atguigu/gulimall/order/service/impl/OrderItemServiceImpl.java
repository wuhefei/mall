package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;

@RabbitListener(queues = {"hello_java_queue"})
@Slf4j
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

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

        /**
         * ack 手动签收，或者拒签
         * 其他方法也可以加上手动确认模式
         *
         */
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();//消息的唯一表示，从1开始自增，用于恢复mq签收成功，或者拒签的id。
        System.out.println("deliveryTag===>"+deliveryTag);

        try {
            if (deliveryTag % 2 == 0) {
                channel.basicAck(deliveryTag, false); //false：非批量签收
                System.out.println("签收货物。。"+deliveryTag);
            } else {
                channel.basicNack(deliveryTag,false,false);//最后false:快递不要了，别再放进mq中了。如果为true，表示拒绝签收，重新放回mq中
                System.out.println("拒签货物。。"+deliveryTag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RabbitHandler
    public void recieveMessage(OrderEntity orderEntity, Channel channel) {
        log.info("接受到了消息[{}]", orderEntity);

    }
}