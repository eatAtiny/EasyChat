package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.enums.DateTimePatternEnum;
import com.easychat.common.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户信息
 */
@Data
@TableName("user_info")
public class UserInfo implements Serializable {


    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0:直接加入 1:同意后加好友
     */
    private Integer joinType;

    /**
     * 0:女 1:男
     */
    private Integer sex;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 省份
     */
    private String areaName;

    /**
     * 城市
     */
    private String areaCode;

    /**
     * 最后离开时间
     */
    private Long lastOffTime;
    /**
     * 在线状态
     */
    @TableField(exist = false)
    private Integer onlineType;

    public Integer getOnlineType() {
        if (lastLoginTime != null && lastLoginTime.getTime() > lastOffTime) {
            return Constants.ONE;
        } else {
            return Constants.ZERO;
        }
    }

    @Override
    public String toString() {
        return "用户ID:" + (userId == null ? "空" : userId) + "，邮箱:" + (email == null ? "空" : email) + "，昵称:" + (nickName == null ? "空" : nickName) + "，0:直接加入 1:同意后加好友:" + (joinType == null ? "空" : joinType) + "，0:女 1:男:" + (sex == null ? "空" : sex) + "，密码:" + (password == null ? "空" : password) + "，个性签名:" + (personalSignature == null ? "空" : personalSignature) + "，状态:" + (status == null ? "空" : status) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，最后登录时间:" + (lastLoginTime == null ? "空" : lastLoginTime) + "，省份:" + (areaName == null ? "空" : areaName) + "，城市:" + (areaCode == null ? "空" : areaCode) + "，最后离开时间:" + (lastOffTime == null ? "空" : lastOffTime);
    }
}