package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yuhl
 * @Date 2020/9/4 18:42
 * @Classname CouponFeignService
 * @Description TODO
 * 用于远程调用Coupon服务的接口
 */
@FeignClient("gulimall-coupon") //告诉//告诉spring cloud这个接口是一个远程客户端，要调用coupon服务，再去调用coupon服务/coupon/coupon/member/list对应的方法
public interface CouponFeignService {

    /**
     * 直接俄把CouponController中的对镜接口粘贴过来，加个双引号就可以了
     * 后去远程调用的优惠卷信息
     * @return
     */
    @RequestMapping("/coupon/coupon/member/list") ////注意写全优惠券类上还有映射//注意我们这个地方不熟控制层，所以这个请求映射请求的不是我们服务器上的东西，而是nacos注册中心的
    public R membercoupons();
}
