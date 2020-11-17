package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author yuhl
 * @email fsjwin@163.com
 * @date 2020-09-04 15:57:52
 */
public interface RabbitService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

