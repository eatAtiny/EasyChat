package com.easychat.user.userservice.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AppUpdateVO {

    private static final long serialVersionUID = 4756060542150096340L;
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新描述
     */
    private List<String> updateList;

    private Long size;

    private String fileName;

    private Integer fileType;

    private String outerLink;
}
