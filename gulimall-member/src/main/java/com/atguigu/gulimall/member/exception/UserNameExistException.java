package com.atguigu.gulimall.member.exception;

/**
 * @Description TODO
 **/
public class UserNameExistException extends RuntimeException {

    public UserNameExistException() {
        super("用户名已存在");
    }
}
