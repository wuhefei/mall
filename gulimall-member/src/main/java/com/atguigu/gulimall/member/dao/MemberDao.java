package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author yuhl
 * @email fsjwin@163.com
 * @date 2020-09-04 15:33:46
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
