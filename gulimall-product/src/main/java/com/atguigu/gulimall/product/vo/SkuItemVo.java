package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 **/
@Data
public class SkuItemVo {

    SkuInfoEntity info; // sku基本信息

    boolean hasStock = true; // 是否有库存

    List<SkuImagesEntity> images; // sku图片信息

    List<SkuItemSaleAttrVo> saleAttrs;  // spu的销售属性组合信息

    SpuInfoDescEntity desp; // spu介绍信息

    List<SpuItemAttrGroupVo> attrGroups; // spu规格参数



    /**
     * 秒杀信息
     */
    SeckillInfoVo seckillInfoVo;
}
