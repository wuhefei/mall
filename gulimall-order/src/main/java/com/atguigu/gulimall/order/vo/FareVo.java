package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>Title: FareVo</p>
 */
@Data
public class FareVo {
	private MemberAddressVo memberAddressVo;

	private BigDecimal fare;
}
