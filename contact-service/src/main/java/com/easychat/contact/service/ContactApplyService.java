package com.easychat.contact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.common.entity.dto.ContactApplyDTO;
import com.easychat.common.entity.po.ContactApply;

import java.util.List;

public interface ContactApplyService extends IService<ContactApply> {

    /**
     * 申请添加好友
     * @param contactApplyDTO 申请添加好友DTO
     */
    void applyAdd(ContactApplyDTO contactApplyDTO);

    /**
     * 获取申请列表，联合查询用户信息
     * @param pageNo 页码
     * @return 申请列表
     */
    IPage<ContactApply> getApplyList(Integer pageNo);


    /**
     * 处理申请
     * @param applyId 申请ID
     * @param status 处理操作 0:拒绝 1:同意 2:拉黑
     */
    void dealWithApply(Integer applyId, Integer status);


}