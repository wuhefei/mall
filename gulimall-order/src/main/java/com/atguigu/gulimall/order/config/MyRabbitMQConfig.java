package com.atguigu.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: MyMQConfig</p>
 * Description：容器中的所有bean都会自动创建到RabbitMQ中 [RabbitMQ没有这个队列、交换机、绑定]
 * date：2020/7/3 17:03
 */
@Slf4j
@Configuration
public class MyRabbitMQConfig {

    @Value("${myRabbitmq.MQConfig.queues}")
    private String queues;

    @Value("${myRabbitmq.MQConfig.eventExchange}")
    private String eventExchange;

    @Value("${myRabbitmq.MQConfig.routingKey}")
    private String routingKey;

    @Value("${myRabbitmq.MQConfig.delayQueue}")
    private String delayQueue;

    @Value("${myRabbitmq.MQConfig.createOrder}")
    private String createOrder;

    @Value("${myRabbitmq.MQConfig.ReleaseOther}")
    private String ReleaseOther;

    @Value("${myRabbitmq.MQConfig.ReleaseOtherKey}")
    private String ReleaseOtherKey;

    @Value("${myRabbitmq.MQConfig.ttl}")
    private Integer ttl; //此处必须使用integer

    /**
     * String name, boolean durable, boolean exclusive, boolean autoDelete,  @Nullable Map<String, Object> arguments
     */
    @Bean
    public Queue orderDelayQueue(){
        log.info("ttl------------->:"+ttl);
        Map<String ,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", eventExchange);
        arguments.put("x-dead-letter-routing-key", routingKey);
        arguments.put("x-message-ttl", ttl); //从配置文件中读取也能都去到，但是就是错误，需要在这里写。 ware 就可以从配置文件中读取 原因是什么呢，原因是原来 private String ttl; 改成Integer就可以了。
        Queue queue = new Queue(delayQueue, true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue(queues, true, false, false);
        return queue;
    }

    /**
     * String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
     * @return
     */
    @Bean
    public Exchange orderEventExchange(){

        return new TopicExchange(eventExchange, true, false);
    }

    /**
     * String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Binding orderCreateOrderBinding(){

        return new Binding(delayQueue, Binding.DestinationType.QUEUE, eventExchange, createOrder, null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){

        return new Binding(queues, Binding.DestinationType.QUEUE, eventExchange, routingKey, null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     */
    @Bean
    public Binding orderReleaseOtherBinding(){

        return new Binding(ReleaseOther, Binding.DestinationType.QUEUE, eventExchange, ReleaseOtherKey + ".#", null);
    }

    /**
     * 秒杀
     * @return
     */
    @Bean
    public Queue orderSecKillQueue(){
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    /**
     * 秒杀的绑定管理和exchange绑定
     * @return
     */
    @Bean
    public Binding orderSecKillQueueBinding(){
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.seckill.order", null);
    }
}
