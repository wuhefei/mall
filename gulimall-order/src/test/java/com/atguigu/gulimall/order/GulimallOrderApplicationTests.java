package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;//对amqp做管理的类

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * 如果发送的消息是个对象，会使用序列化机制，将对象写出去，所以对象必须实现Serializable
     */
    @Test
    public void sendMessage() {
       for (int i = 0; i < 10; i++) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setMemberId(1111L);
            orderEntity.setMemberUsername("于红亮" );
            rabbitTemplate.convertAndSend("hello_java_exchange", "hello.java", orderEntity);
            log.info("消息发送成功！");
        }

    }

    /**
     * 测试新建交换机
     */
    @Test
    public void createExchange(){
        DirectExchange directExchange = new DirectExchange("hello_java_exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功","hello_java_exchange");
    }

    /**
     * 测试新建队列
     */
    @Test
    public void createQueue(){
        Queue hello_java_queue = new Queue("hello_java_queue", true, false, false);
        amqpAdmin.declareQueue(hello_java_queue);
        log.info("queue[{}]创建成功","hello_java_queue");
    }

    /**
     * 测试交换机和队列的绑定
     */
    @Test
    public void createBinding(){
        Binding binding = new Binding("hello_java_queue",
                Binding.DestinationType.QUEUE,
                "hello_java_exchange",
                "hello.java",
                null
        );
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功","hello_java_bingding");
    }

    @Test
    public void sendMessageTest(){
        //1.发送消息
        String msg = "hello world!";
        rabbitTemplate.convertAndSend("hello_java_exchange","hello.java",msg);
        log.info("消息发送完成：[{}]",msg);
    }
    @Test
    void contextLoads() {
    }

}
