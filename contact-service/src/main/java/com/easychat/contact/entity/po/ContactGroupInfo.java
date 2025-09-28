package com.easychat.contact.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;

/**
 * 群信息副本，用于存储群组的基本信息
 */
@Data
public class ContactGroupInfo implements Serializable {

    /**
     * 群ID
     */
    @TableId
    private String groupId;

    /**
     * 群组名
     */
    private String groupName;

    /**
     * 群主id
     */
    private String groupOwnerId;

    /**
     * 群公告
     */
    private String groupNotice;
}
