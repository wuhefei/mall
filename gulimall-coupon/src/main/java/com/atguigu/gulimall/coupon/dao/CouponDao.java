package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author yuhl
 * @email fsjwin@163.com
 * @date 2020-09-04 15:19:55
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
