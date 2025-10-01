package com.easychat.user.userservice.entity.dto;

import lombok.Data;

@Data
public class UserInfoBeautyDTO {
    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 状态0:未使用 1:已使用
     */
    private Integer status;
}
