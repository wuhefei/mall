package com.atguigu.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * 特别注意，使用配置中心控制层上加@RefreshScope
 *          使用@Value获取远程配置中心值
 *          引入依赖
 *
 * 配置中心原则：namespace为服务名，group为环境名字
 * 生产上最后的结果就是项目中只有bootstrap.properties，确定读那些配置文件的内容，application.yml等文件消失。
 */

@SpringBootApplication
@MapperScan("com.atguigu.gulimall.coupon.dao")
@EnableDiscoveryClient //服务功能开启
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
