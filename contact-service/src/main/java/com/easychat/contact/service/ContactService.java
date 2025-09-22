package com.easychat.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.contact.entity.dto.ContactApplyDTO;
import com.easychat.contact.entity.dto.ContactDTO;
import com.easychat.contact.entity.po.Contact;
import com.easychat.contact.entity.vo.SearchResultVO;

import java.util.List;

public interface ContactService extends IService<Contact> {
    /**
     * 新增好友
     *
     * @param userContact
     * @return
     */
//    boolean addFriend(Contact userContact);

    /**
     * 搜索
     * @param contactId 群组/用户ID
     */
    SearchResultVO search(String contactId);

    /**
     * 申请添加好友
     * @param contactApplyDTO 申请添加好友DTO
     */
    void applyAdd(ContactApplyDTO contactApplyDTO);

    /**
     * 添加好友
     * @param contactDTO 添加好友DTO
     */
    void createContact(ContactDTO contactDTO);

    /**
     * 获取联系人列表
     * @param contactType 联系人类型 USER:好友 GROUP:群组
     * @return 联系人列表
     */
    List<Contact> getContactList(String contactType);
}
