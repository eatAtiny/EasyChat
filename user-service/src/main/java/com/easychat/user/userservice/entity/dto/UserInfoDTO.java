package com.easychat.user.userservice.entity.dto;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class UserInfoDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0:直接加入 1:同意后加好友
     */
    private Integer joinType;

    /**
     * 0:女 1:男
     */
    private Integer sex;

    /**
     * 密码
     */
    private String password;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 省份
     */
    private String areaName;

    /**
     * 城市
     */
    private String areaCode;

    /**
     * 最后离开时间
     */
    private Long lastOffTime;
}
