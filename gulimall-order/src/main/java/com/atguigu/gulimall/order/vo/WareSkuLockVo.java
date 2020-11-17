package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 **/
@Data
public class WareSkuLockVo {

    private String orderSn;

    private List<OrderItemVo> locks; // 需要锁住的所有库存
}
