package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author yuhl
 * @email fsjwin@163.com
 * @date 2020-09-04 14:12:07
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {
//    更新上架状态
    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);

}
