package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/getSpuInfoBySkuId/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 秒杀时调用的获取sku的信息。
     * @param skuId
     * @return
     */
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSkuInfoBySkuId(@PathVariable("id") Long skuId);
}
