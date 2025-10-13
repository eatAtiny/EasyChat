package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.easychat.common.entity.enums.DateTimePatternEnum;
import com.easychat.common.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 联系人
 */
@Data
public class Contact implements Serializable {


    /**
     * 用户ID
     */
    private String userId;

    /**
     * 联系人ID或者群组ID
     */
    private String contactId;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private Integer contactType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 状态 0:非好友 1:好友 2:已删除 3:被删除 4:已拉黑 5:被拉黑
     */
    private Integer status;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

    /**
     * 联系人名称
     */
    @TableField(exist = false)
    private String contactName;

    /**
     * 联系人性别
     */
    @TableField(exist = false)
    private Integer sex;



    @Override
    public String toString() {
        return "用户ID:" + (userId == null ? "空" : userId) + "，联系人ID或者群组ID:" + (contactId == null ? "空" : contactId) + "，联系人类型 0:好友 1:群组:" + (contactType == null ? "空" :
                contactType) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，状态 0:非好友 " +
                "1:好友 2:已删除 3:拉黑:" + (status == null ? "空" : status) + "，最后更新时间:" + (lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime,
                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
    }
}
