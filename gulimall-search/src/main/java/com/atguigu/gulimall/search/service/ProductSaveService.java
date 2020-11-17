package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author yuhl
 * @Date 2020/9/9 6:37
 * @Classname ProductSaveService
 * @Description TODO
 */
public interface ProductSaveService {

    /**
     * 上传到es
     * @param esModels
     * @return true 有错误 false 无错误
     * @throws IOException
     */
    Boolean productStatusUp(List<SkuEsModel> esModels) throws IOException;
}
