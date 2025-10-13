package com.easychat.common.entity.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * 用户注册/登录表单DTO
 */
@Data
public class UserFormDTO {
    @NotEmpty(message = "验证码key不能为空")
    String checkCodeKey;
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    String email;
    @NotEmpty(message = "密码不能为空")
    String password;
    @NotEmpty(message = "昵称不能为空")
    String nickName;
    @NotEmpty(message = "验证码不能为空")
    String checkCode;
}
