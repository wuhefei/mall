package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Description TODO
 **/
@Data
public class SubmitOrderRespVo {

    private OrderEntity orderEntity;

    private Integer code; // 0成功 状态码


}
