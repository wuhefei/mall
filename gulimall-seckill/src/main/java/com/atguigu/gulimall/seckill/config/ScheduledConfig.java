package com.atguigu.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <p>Title: ScheduledConfig</p>
 * Description：
 * date：2020/7/6 17:28
 */
//异步开启定时
// 任务
@EnableAsync
@Configuration
@EnableScheduling
public class ScheduledConfig {

}
