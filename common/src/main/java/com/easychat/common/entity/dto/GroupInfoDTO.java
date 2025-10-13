package com.easychat.common.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * 群信息
 */
@Data
public class GroupInfoDTO implements Serializable {


    /**
     * 群ID
     */
    private String groupId;

    /**
     * 群组名
     */
    @NotNull
    private String groupName;

    /**
     * 群主id
     */
    private String groupOwnerId;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 0:直接加入 1:管理员同意后加入
     */
    @NotNull
    private Integer joinType;

    /**
     * 群人数
     */
    private Integer memberCount;

    /**
     * 群主昵称
     */
    private String groupOwnerNickName;


    private MultipartFile avatarFile;

    private MultipartFile avatarCover;




}
