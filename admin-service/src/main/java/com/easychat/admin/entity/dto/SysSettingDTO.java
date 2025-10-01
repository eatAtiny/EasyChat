package com.easychat.admin.entity.dto;


import com.easychat.common.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysSettingDTO implements Serializable {
    /**
     * 没人可创建群数
     */
    private Integer maxGroupCount = 5;

    /**
     * 群成员上限
     */
    private Integer maxGroupMemberCount = 500;

    /**
     * 最大图片大小（MB）
     */
    private Integer maxImageSize = 2;

    /**
     * 最大视频大小（MB）
     */
    private Integer maxVideoSize = 5;

     /**
     * 最大文件大小（MB）
     */
    private Integer maxFileSize = 5;

    /**
     * 机器人ID
     */
    private String robotUid = Constants.ROBOT_UID;

    /**
     * 机器人昵称
     */
    private String robotNickName = "EasyChat";

    /**
     *
     */
    private String robotWelcome = "欢迎使用EasyChat";
}
