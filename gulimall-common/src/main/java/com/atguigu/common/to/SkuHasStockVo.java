package com.atguigu.common.to;

import lombok.Data;

/**
 * @Description 商品服务远程调用返回的结果集对象
 **/
@Data
public class SkuHasStockVo {

    private Long skuId;
    private Boolean hasStock;
}
