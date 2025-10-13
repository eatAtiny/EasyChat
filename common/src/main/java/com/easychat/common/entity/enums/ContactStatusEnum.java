package com.easychat.common.entity.enums;

import lombok.Getter;

@Getter
public enum ContactStatusEnum {
    NO_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL_FRIEND(2, "已删除好友"),
    DEL_BY_FRIEND(3, "已删除好友"),
    BLOCK_FRIEND(4, "已拉黑好友"),
    BLOCK_BY_FRIEND(5, "被好友拉黑");
    private final Integer status;
    private final String desc;
    ContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static String getDescByStatus(Integer status) {
        ContactStatusEnum contactStatusEnum = getByStatus(status);
        return contactStatusEnum == null ? null : contactStatusEnum.getDesc();
    }

    public static ContactStatusEnum getByStatus(Integer status) {
        for (ContactStatusEnum contactStatusEnum : ContactStatusEnum.values()) {
            if (contactStatusEnum.getStatus().equals(status)) {
                return contactStatusEnum;
            }
        }
        return null;
    }
}
