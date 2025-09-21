package com.easychat.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.contact.entity.dto.UserContactApplyDTO;
import com.easychat.contact.entity.po.UserContactApply;

public interface UserContactApplyService extends IService<UserContactApply> {

    /**
     * 添加申请
     * @param userContactApplyDTO 申请DTO
     */
    void createContactApply(UserContactApplyDTO userContactApplyDTO);


    /**
     * 处理申请
     * @param applyId 申请ID
     * @param status 处理操作 0:拒绝 1:同意 2:拉黑
     */
    void dealWithApply(Integer applyId, Integer status);
}
