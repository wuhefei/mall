package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * @author yuhl
 * @Date 2020/9/16 22:35
 * @Classname RabbitController
 * @Description TODO
 */
@Controller
@Slf4j
public class RabbitController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 发送消息测试，controller
     * @param num
     * @return
     */
    @GetMapping("/sendMq")
    @ResponseBody
    public String sendMq(@RequestParam(value = "num", defaultValue = "10") Integer num) {
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setName("于红亮");
                rabbitTemplate.convertAndSend("hello_java_exchange", "hello.java", orderReturnReasonEntity,new CorrelationData(UUID.randomUUID().toString()));
                log.info("OrderReturnReasonEntity消息发送成功！");

            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello_java_exchange","hello.java" ,entity,new CorrelationData(UUID.randomUUID().toString()));
                log.info("OrderEntity消息发送成功！");
            }
        }
        return "ok";
    }
}
