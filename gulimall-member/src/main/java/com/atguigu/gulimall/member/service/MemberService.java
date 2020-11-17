package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author yuhl
 * @email fsjwin@163.com
 * @date 2020-09-04 15:33:46
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo memberRegistVo);
    // 校验用户名是否已存在
    void checkUsernameUnique(String userName) throws UserNameExistException;
    // 校验手机是否已存在
    void checkPhonelUnique(String phone) throws PhoneExistException;


    MemberEntity login(MemberLoginVo memberLoginVo);

    MemberEntity login(SocialUserVo socialUserVo) throws Exception;
}

