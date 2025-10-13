package com.easychat.common.entity.enums;

import lombok.Getter;

@Getter
public enum ContactJoinTypeEnum {
    DIRECT(0, "直接加入"),
    AUDIT(1, "需要审核");

    private final Integer status;
    private final String desc;

    ContactJoinTypeEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
