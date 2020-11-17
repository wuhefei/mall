package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description TODO
 **/
@Configuration
public class GuliFeignConfig {

    /**
     * 解决fein远程调用丢失请求头 当去cart调用远程方法的时候，由于是从feign调起来的，（如果重定向到页面的话就会
     * 把cookie中的session带着，不存在cart认为未登陆的问题。）cart用户是否登录的拦截器认为用户没有登录。出现问题。
     * 解决的犯法就是在feign远程调用的时候添加一个拦截器，把该增强的给增强上，什么是该增强的呢？
     * 就是把sessin带上，让cart感受到我已经登录了。
     * @return
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1、RequestContextHolder 拿到当前的请求
                //ServletRequestAttributes Spring 加的重封装，底层还是放在treadlocal中。内含有cookie
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    // 原始请求 页面发起的老请求
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        // 获取原始请求的头数据 cookie
                        String cookie = request.getHeader("Cookie");

                        // 给feign生成的心请求设置请求头cookie
                        template.header("Cookie", cookie); //新请求的放入老请求的头信息。远程调用的时候cookie中就有已登录的信息了。
                    }
                }
            }
        };
    }
}
