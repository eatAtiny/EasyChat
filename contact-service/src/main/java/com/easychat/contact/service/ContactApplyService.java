package com.easychat.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.contact.entity.dto.ContactApplyDTO;
import com.easychat.contact.entity.po.ContactApply;

public interface ContactApplyService extends IService<ContactApply> {

    /**
     * 添加申请
     * @param contactApplyDTO 申请DTO
     */
    void createContactApply(ContactApplyDTO contactApplyDTO);


    /**
     * 处理申请
     * @param applyId 申请ID
     * @param status 处理操作 0:拒绝 1:同意 2:拉黑
     */
    void dealWithApply(Integer applyId, Integer status);
}
