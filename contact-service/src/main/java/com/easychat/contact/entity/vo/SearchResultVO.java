package com.easychat.contact.entity.vo;

import lombok.Data;

@Data
public class SearchResultVO {
    /**
     * 群组/用户ID
     */
    private String contactId;
    /**
     * 群组/用户类型
     * USER:用户 GROUP:群组
     */
    private String contactType;
    /**
     * 群组/用户名称
     */
    private String nickName;
    /**
     * 群组/用户头像最后更新时间
     */
    private Long avatarLastUpdate;
    /**
     * 群组/用户状态
     */
    private Integer status;
    /**
     * 群组/用户状态名称
     */
    private String statusName;
    /**
     * 用户性别
     */
    private Integer sex;
    /**
     * 用户地区
     */
    private String areaName;
}
