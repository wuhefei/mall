package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * * 1、整合redisson作为分布式锁等功能的框架
 *  *      ① 引入依赖
 *  *          <dependency>
 *  *              <groupId>org.redisson</groupId>
 *  *              <artifactId>redisson</artifactId>
 *  *              <version>3.12.0</version>
 *  *          </dependency>
 *  *      ②配置redisson MyRedissonConfig
 *  *
 *  * 2、整合springcache 简化缓存开发
 *  *      ① 引入依赖
 *  *          1)、 spring-boot-starter-cache
 *  *          2)、spring-boot-starter-data-redis
 *  *      ② 写配置
 *  *          1)、自动配置了
 *  *              CacheAutoConfiguration会导入RedisCacheConfiguration
 *  *              自动配好了缓存管理器
 *  *          2)、配置使用redis缓存
 *  *                  spring.cache.type=redis
 *  *      ③ 测试使用缓存
 *  *              @Cacheable: Triggers cache population. -> 触发将数据保存到缓存的操作
 *  *              @CacheEvict: Triggers cache eviction.  -> 触发将数据从缓存删除的操作
 *  *              @CachePut: Updates the cache without interfering with the method execution. -> 不影响方法执行更新缓存
 *  *              @Caching: Regroups multiple cache operations to be applied on a method.     -> 组合以上多个操作
 *  *              @CacheConfig: Shares some common cache-related settings at class-level.     -> 在类级别共享缓存相同配置
 *  *         1)、开启缓存功能
 *  *              @EnableCaching
 *  *
 *  *         2)、只需要使用注解就可使用缓存
 *
 *
 //	http://localhost:10001/product/category/list/tree
 //	http://127.0.0.1:88/api/product/category/list/tree
 //	http://127.0.0.1:88/api/product/attrgroup/list/1?page=1&key=aa

 //	JSR303最终测试：POSTman :{"name":"aaa","logo":"https://github.com/1046762075","sort":0,"firstLetter":"d","showStatus":0}

 //	http://localhost:10001/index/catalog.json

 //	分布式锁：http://localhost:10001/index/hello

 //	读写锁：http://localhost:10001/index/write
 //			http://localhost:10001/index/read

 //	闭锁：	http://localhost:10001/index/lockDoor
 //			http://localhost:10001/index/go/1

 //	信号量:	http://localhost:10001/index/go/park
 //			http://localhost:10001/index/park
 */
@EnableRedisHttpSession //开启session共享
@EnableCaching //开启缓存，整合redis spring就是好，对什么都做了整合
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign") //不使用basePackage也是可以的。会自动扫描到@FeignClient("gulimall-coupon")这个注解
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.atguigu.gulimall.product.dao")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
