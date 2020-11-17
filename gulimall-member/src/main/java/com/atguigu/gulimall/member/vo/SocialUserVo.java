package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @Description 社交登录vo对象
 **/
@Data
public class SocialUserVo {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
