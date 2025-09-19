package com.easychat.contact.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserContactApplyDTO {
    /**
     * 申请用户/群组id
     */
    @NotNull
    public String contactId;

    /**
     * 申请类型
     * USER：用户
     * GROUP：群组
     */
    @NotNull
    public String contactType;

    /**
     * 申请信息
     */
    public String applyInfo;
}
