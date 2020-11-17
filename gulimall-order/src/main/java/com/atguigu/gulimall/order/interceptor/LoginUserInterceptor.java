package com.atguigu.gulimall.order.interceptor;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description 付款前需要确认登录成功的拦截器
 **/
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    /**
     * 执行目标方法之前
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        String requestURI = request.getRequestURI();
        // 判断本次请求是不是服务间的远程调用，如果是则不需要进行登录判断 - 库存服务调用订单服务获取订单信息
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", requestURI);
        boolean match1 = antPathMatcher.match("/payed/notify", requestURI);
        if (match || match1) {
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            threadLocal.set(attribute);
            return true;
        } else {
            // 未登录
            request.getSession().setAttribute("msg", "请先登录！");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
