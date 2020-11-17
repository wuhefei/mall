package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {

    /**
     * 调用ware服务，网es中存入数据。
     * @param esModels
     * @return
     */
    @PostMapping("/search/save/product")
    R productStstusUp(@RequestBody List<SkuEsModel> esModels);
}
