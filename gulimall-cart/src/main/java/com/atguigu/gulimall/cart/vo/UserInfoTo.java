package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author yuhl
 * @Date 2020/9/15 22:30
 * @Classname UserInfoTo
 * @Description 传输对象
 */
@ToString
@Data
public class UserInfoTo {

    private Long userId; // 用户登录状态 - 用户的id

    private String userKey; // 用户未登录状态 - 临时的标识

    private boolean tempUser = false; // cookie是否有临时用户
}
