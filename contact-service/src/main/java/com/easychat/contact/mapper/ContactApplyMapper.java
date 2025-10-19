package com.easychat.contact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easychat.common.entity.po.ContactApply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContactApplyMapper extends BaseMapper<ContactApply> {
    
    /**
     * 获取申请列表，联合查询用户信息
     * @param receiveUserId 接收人ID
     * @return 申请列表
     */
    List<ContactApply> selectApplyListWithUserInfo(String receiveUserId);

    /**
     * 根据applyId查询申请详情，联合查询用户信息
     * @param applyId 申请ID
     * @return 申请详情
     */
    ContactApply selectByApplyId(Integer applyId);
}