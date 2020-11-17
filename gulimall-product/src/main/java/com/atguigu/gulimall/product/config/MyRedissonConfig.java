package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author yuhl
 * @Date 2020/9/12 9:38
 * @Classname MyRedissonConfig
 * @Description redis分布式锁配置类
 */
@Component
public class MyRedissonConfig {

    /**
     * 创建redisson bean 和jdis  lettece客户端一样，redsson也是一个哭护短而已
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(@Value("${spring.redis.host}") String url) throws IOException{
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+url+":6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
