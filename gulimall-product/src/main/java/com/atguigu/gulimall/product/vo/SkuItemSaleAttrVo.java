package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description spu的销售属性组合信息
 **/
@Data
@ToString
public class SkuItemSaleAttrVo {
    private Long attrId;

    private String attrName;

    private List<AttrValWithSkuIdVo> attrValues;
}
