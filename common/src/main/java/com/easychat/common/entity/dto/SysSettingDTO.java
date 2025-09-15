package com.easychat.common.entity.dto;

import com.easychat.common.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SysSettingDTO implements Serializable {


    private Integer maxGroupCount = 5;
    private Integer maxGroupMemberCount = 500;
    private Integer maxImageSize = 2;
    private Integer maxVideoSize = 5;
    private Integer maxFileSize = 5;
    private String robotUid = Constants.ROBOT_UID;
    private String robotNickName = "EasyChat";
    private String robotWelcome = "欢迎使用EasyChat";

}
