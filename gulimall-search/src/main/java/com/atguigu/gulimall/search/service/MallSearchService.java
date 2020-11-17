package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @author yuhl
 * @Date 2020/9/12 17:34
 * @Classname MallSearchService
 * @Description es查询的接口方法
 */
public interface MallSearchService {
    /**
     * 检索所有参数
     */
    SearchResult search(SearchParam Param);
}
