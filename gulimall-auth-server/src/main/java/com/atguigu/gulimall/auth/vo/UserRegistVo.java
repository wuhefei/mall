package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * @author yuhl
 * @Date 2020/9/14 22:32
 * @Classname UserRegistVo
 * @Description TODO
 */
@Data
public class UserRegistVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 18, message = "用户名必须是6~18位字符")
    private String userName;
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6, max = 18, message = "密码必须是6~18位字符")
    private String password;
    @NotEmpty(message = "手机号必须提交")
//    @Pattern(regexp = "^[1]([3-9])[0-9]{0}$", message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须提交")
    private String code;
}
