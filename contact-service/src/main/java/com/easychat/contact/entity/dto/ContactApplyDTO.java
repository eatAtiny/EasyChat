package com.easychat.contact.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ContactApplyDTO {

    /**
     * 自增ID
     */
    private Integer applyId;

    /**
     * 申请人id
     */
    public String applyUserId;

    /**
     * 申请接收人id
     */
    public String receiveUserId;

    /**
     * 申请类型
     * 0：用户  1：群组
     */
    @NotNull
    public Integer contactType;

    /**
     * 申请用户/群组id
     */
    @NotNull
    public String contactId;

    /**
     * 状态0:待同意 1:同意 2:已拒绝 3:拉黑
     */
    public Integer status;

    /**
     * 申请信息
     */
    public String applyInfo;
}
