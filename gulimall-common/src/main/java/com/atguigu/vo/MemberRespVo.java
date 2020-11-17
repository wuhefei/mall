package com.atguigu.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yuhl
 * @Date 2020/9/15 6:57
 * @Classname MemberRespVo
 * @Description TODO
 */

@ToString
@Data
public class MemberRespVo implements Serializable {



    /**
     * id
     */
    private Long id;
    /**
     * 会员等级id
     */
    private Long levelId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 头像
     */
    private String header;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 生日
     */
    private Date birth;
    /**
     * 所在城市
     */
    private String city;
    /**
     * 职业
     */
    private String job;
    /**
     * 个性签名
     */
    private String sign;
    /**
     * 用户来源
     */
    private Integer sourceType;
    /**
     * 积分
     */
    private Integer integration;
    /**
     * 成长值
     */
    private Integer growth;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 注册时间
     */
    private Date createTime;

    /**
     * 会员社交登录识别标识
     */
    private String socialUid;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 访问额令牌过期时间
     */
    private Long expiresIn;
}