package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.easychat.common.entity.enums.ContactApplyStatusEnum;
import lombok.Data;

import java.io.Serializable;


/**
 * 联系人申请
 */
@Data
@TableName("user_contact_apply")
public class UserContactApply implements Serializable {


    /**
     * 自增ID
     */
    private Integer applyId;

    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 接收人ID
     */
    private String receiveUserId;

    /**
     * 状态0:待处理 1:已同意  2:已拒绝 3:已拉黑
     */
    private Integer contactType;

    /**
     * 联系人群组ID
     */
    private String contactId;

    /**
     * 最后申请时间
     */
    private Long lastApplyTime;

    /**
     * 状态0:待同意 1:同意 2:已拒绝 3:拉黑
     */
    private Integer status;

    /**
     * 申请信息
     */
    private String applyInfo;

    /**
     * 联系人名称
     */
    @TableField(exist = false)
    private String contactName;
    /**
     * 申请状态名称
     */
    @TableField(exist = false)
    private String statusName;

    public String getStatusName() {
        ContactApplyStatusEnum statusEnum = ContactApplyStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }
}
