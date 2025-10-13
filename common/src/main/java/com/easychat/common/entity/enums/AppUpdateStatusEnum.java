package com.easychat.common.entity.enums;

import lombok.Getter;

@Getter
public enum AppUpdateStatusEnum {
    NO_RELEASE(0, "未发布"),
    GRAY(1, "灰度"),
    ALL(2, "全网发布");
    private final Integer code;
    private final String message;
    AppUpdateStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
