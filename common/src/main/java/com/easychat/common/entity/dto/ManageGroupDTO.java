package com.easychat.common.entity.dto;

import lombok.Data;

@Data
public class ManageGroupDTO {

    /**
     * 群聊ID
     */
    private String groupId;
    /**
     * 联系人ID列表，逗号分隔
     */
    private String contactIds;

    /**
     * 操作类型，1：添加成员，2：移除成员
     */
    private Integer opType;
}
