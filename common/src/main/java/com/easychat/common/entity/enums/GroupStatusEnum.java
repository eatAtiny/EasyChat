package com.easychat.common.entity.enums;

import lombok.Getter;

@Getter
public enum GroupStatusEnum {
    NORMAL(1, "正常"),
    DISABLED(0, "解散");
    private final Integer code;
    private final String msg;

    GroupStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}