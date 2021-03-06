package com.atguigu.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yuhl
 * @Date 2020/9/15 19:19
 * @Classname HelloController
 * @Description TODO
 */

@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;
    /**
     * 无需登录直接访问
     * @return
     */
    @GetMapping({"/", "/hello"})
    @ResponseBody
    public String hello() {
        return "欢迎来到单点登录测试客户端2";
    }


    @GetMapping("/boss")
    public String employess(Model model, HttpSession session,
                            @RequestParam(value = "token", required = false) String token) {

        if (!StringUtils.isEmpty(token)) { // 如果本次请求携带了令牌信息，则获取用户的信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userinfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser", body);
        }

        //
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            // 没登录,跳转到登录服务器
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("张三1");
            emps.add("张三2");
            emps.add("张三3");
            model.addAttribute("emps", emps);

            return "list";
        }

    }


}
