package com.easychat.common.entity.enums;

import lombok.Getter;

@Getter
public enum UserInfoBeautyStatus {
    UNUSE(0,"未使用"),
    USE(1,"已使用");
    private final int value;
    private final String desc;
    UserInfoBeautyStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
