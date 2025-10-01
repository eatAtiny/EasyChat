package com.easychat.admin.entity.enums;

import lombok.Getter;

@Getter
public enum AppUpdateFileTypeEnum {
    LOCAL(0, "本地文件"),
    EXTERNAL(1, "外链");

    private final Integer code;
    private final String message;
    AppUpdateFileTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
