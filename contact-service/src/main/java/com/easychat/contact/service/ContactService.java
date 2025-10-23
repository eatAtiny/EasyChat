package com.easychat.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.po.Contact;

import java.util.List;

public interface ContactService extends IService<Contact> {

    /**
     * 管理好友关系 根据DTO中的status去操作contact表
     * @param contactDTO 关系DTO
     */
    void manageContact(ContactDTO contactDTO);

    /**
     * 获取联系人列表
     * @param contactType 联系人类型 USER:好友 GROUP:群组
     * @return 联系人列表
     */
    List<Contact> getContactList(String contactType);

    /**
     * 删除联系人
     * @param contactId 联系人ID或者群组ID
     */
     void deleteContact(String contactId);

     /**
      * 拉黑联系人
      * @param contactId 联系人ID或者群组ID
      */
     void blacklistContact(String contactId);

     /**
      * 退出群聊
      * @param groupId 群组ID
      */
     void exitGroup(String groupId);

    /**
     * 添加机器人为好友
     */
    void addRobotFriend(String userId);

     /**
      * 解散群聊
      * @param groupId 群聊ID
      */
     void dissolutionGroup(String groupId);
}
