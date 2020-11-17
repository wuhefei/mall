package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;


/**
 * @author yuhl
 * @Date 2020/9/14 7:16
 * @Classname GulimallWebConfig
 * @Description 做一些web的配置，比如空的视图映射等
 */
@Configuration
public class GulimallWebConfig extends WebMvcConfigurationSupport {


    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        /*registry.addViewController("login.html").setViewName("login");*/
        registry.addViewController("reg.html").setViewName("reg");
    }
}
