package com.atguigu.ssoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yuhl
 * @Date 2020/9/15 19:56
 * @Classname HelloController
 * @Description TODO
 */
@Controller
public class HelloController {
    @GetMapping("/hh")
    @ResponseBody
    public String hh(){
        return "hhh";
    }
}
