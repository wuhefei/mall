package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * @Description TODO
 **/
@Data
public class SocialUserVo {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
