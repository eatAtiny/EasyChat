package com.easychat.common.entity.po;

import lombok.Data;

import java.io.Serializable;


/**
 * 靓号
 */
@Data
public class UserInfoBeauty implements Serializable {


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


    @Override
    public String toString() {
        return "自增ID:" + (id == null ? "空" : id) + "，邮箱:" + (email == null ? "空" : email) + "，用户ID:" + (userId == null ? "空" : userId) + "，状态0:未使用 1:已使用:" + (status == null ? "空" : status);
    }
}
