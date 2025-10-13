package com.easychat.common.entity.enums;

import lombok.Getter;

@Getter
public enum ContactApplyStatusEnum {
    PENDING(0, "待处理"),
    AGREE(1, "同意"),
    REFUSE(2, "拒绝"),
    BLOCKED(3, "拉黑");
    private final Integer status;
    private final String desc;

    ContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static ContactApplyStatusEnum getByStatus(Integer status) {
        for (ContactApplyStatusEnum item : ContactApplyStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
