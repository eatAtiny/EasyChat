package com.easychat.contact.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ContactUserInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 性别 0:女 1:男
     */
    private Integer sex;

    /**
     * 省份
     */
    private String areaName;

    /**
     * 城市
     */
    private String areaCode;
}
