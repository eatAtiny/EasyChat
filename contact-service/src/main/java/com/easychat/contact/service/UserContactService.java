package com.easychat.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.contact.entity.dto.UserContactApplyDTO;
import com.easychat.contact.entity.po.UserContact;
import com.easychat.contact.entity.vo.SearchResultVO;

public interface UserContactService extends IService<UserContact> {
    /**
     * 新增好友
     *
     * @param userContact
     * @return
     */
//    boolean addFriend(UserContact userContact);

    /**
     * 搜索
     * @param contactId 群组/用户ID
     */
    SearchResultVO search(String contactId);

    /**
     * 申请添加好友
     * @param userContactApplyDTO 申请添加好友DTO
     */
    void applyAdd(UserContactApplyDTO userContactApplyDTO);
}
