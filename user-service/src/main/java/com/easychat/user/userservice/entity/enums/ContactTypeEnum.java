package com.easychat.user.userservice.entity.enums;

import lombok.Getter;

@Getter
public enum ContactTypeEnum {
    USER(0, 'U', "USER", "用户"),
    GROUP(1, 'G', "GROUP","群聊");

    private final Integer status;
    private final Character prefix;
    private final String name;
    private final String  desc;
    ContactTypeEnum(Integer status, Character prefix, String name, String desc) {
        this.status = status;
        this.prefix = prefix;
        this.name = name;
        this.desc = desc;
    }

    public static Integer nameToStatus(String name) {
        for (ContactTypeEnum contactTypeEnum : ContactTypeEnum.values()) {
            if (contactTypeEnum.name.equals(name)) {
                return contactTypeEnum.status;
            }
        }
        return null;
    }
}
