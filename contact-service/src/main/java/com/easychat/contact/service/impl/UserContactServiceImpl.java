package com.easychat.contact.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.entity.enums.UserContactTypeEnum;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.api.UserClient;
import com.easychat.contact.constant.Constants;
import com.easychat.contact.entity.dto.UserContactApplyDTO;
import com.easychat.contact.entity.dto.UserContactDTO;
import com.easychat.contact.entity.dto.UserInfoDTO;
import com.easychat.contact.entity.enums.ContactJoinTypeEnum;
import com.easychat.contact.entity.enums.ContactStatusEnum;
import com.easychat.contact.entity.enums.ContactTypeEnum;
import com.easychat.contact.entity.po.GroupInfo;
import com.easychat.contact.entity.po.UserContact;
import com.easychat.contact.entity.vo.SearchResultVO;
import com.easychat.contact.mapper.GroupInfoMapper;

import com.easychat.contact.mapper.UserContactMapper;

import com.easychat.contact.service.UserContactApplyService;
import com.easychat.contact.service.UserContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements UserContactService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private UserContactApplyService userContactApplyService;
    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Override
    public SearchResultVO search(String contactId) {
        // 1. 判断ID是否合法（以G或者U开头的12位字符串）
        if (!contactId.matches(Constants.CONTACT_ID_REGEX)){
            return null;
        }

        // 2. 根据ID从数据库查询用户/群组信息
        SearchResultVO searchResultVO = new SearchResultVO();
        if (contactId.charAt(0) == ContactTypeEnum.GROUP.getPrefix()){
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            if (groupInfo != null){
                searchResultVO.setContactType(ContactTypeEnum.GROUP.getName());
                searchResultVO.setNickName(groupInfo.getGroupName());
                searchResultVO.setContactId(groupInfo.getGroupId());
                searchResultVO.setStatusName(groupInfo.getStatus() == Constants.GROUP_STATUS_NORMAL ? "正常" : "解散");
            }
        } else {
            UserInfoDTO userInfoDTO = userClient.search(contactId);
            if (userInfoDTO != null){
                searchResultVO.setContactType(ContactTypeEnum.USER.getName());
                searchResultVO.setNickName(userInfoDTO.getNickName());
                searchResultVO.setContactId(userInfoDTO.getUserId());
                searchResultVO.setStatusName(userInfoDTO.getStatus() == Constants.GROUP_STATUS_NORMAL ? "正常" : "禁用");
                searchResultVO.setAreaName(userInfoDTO.getAreaName());
                searchResultVO.setSex(userInfoDTO.getSex());
            }
        }
        // 3. 从UserContact表查询是否存在好友关系
        UserContact userContact = baseMapper.selectOne(new LambdaQueryWrapper<UserContact>()
                        .eq(UserContact::getUserId, UserContext.getUser())
                .eq(UserContact::getContactId, contactId));
        searchResultVO.setStatus(userContact == null ? null : userContact.getStatus());

        return searchResultVO;
    }

    /**
     * 申请添加好友关系
     *
     * @param userContactApplyDTO 申请添加好友DTO
     */
    @Override
    public void applyAdd(UserContactApplyDTO userContactApplyDTO) {
        // 1. 查询所申请的联系人/群组信息
        UserContact userContact = baseMapper.selectOne(new LambdaQueryWrapper<UserContact>()
                            .eq(UserContact::getUserId, userContactApplyDTO.getApplyUserId())
                            .eq(UserContact::getContactId, userContactApplyDTO.getContactId()));
        // 2. 检查是否被拉黑
        if (userContact != null && userContact.getStatus().equals(Constants.CONTACT_USER_STATUS_BLOCKED_BY_FRIEND)){
            throw new BusinessException(Constants.CONTACT_USER_STATUS_BLOCKED);
        }
        // 3. 检查用户/群组是否存在
        Integer joinType =null;
        if (userContactApplyDTO.getContactType().equals(ContactTypeEnum.GROUP.getStatus())){
            // 3.1 检查群组是否存在
            GroupInfo groupInfo = groupInfoMapper.selectById(userContactApplyDTO.getContactId());
            if (groupInfo == null){
                throw new BusinessException(Constants.GROUP_NOT_EXIST);
            }
            joinType = groupInfo.getJoinType();
            // 将申请接收人设为群主
            userContactApplyDTO.setReceiveUserId(groupInfo.getGroupOwnerId());
        }else if (userContactApplyDTO.getContactType().equals(ContactTypeEnum.USER.getStatus())){
            // 3.2 检查用户是否存在
            UserInfoDTO userInfoDTO = userClient.search(userContactApplyDTO.getContactId());
            if (userInfoDTO == null){
                throw new BusinessException(Constants.USER_NOT_EXIST);
            }
            joinType = userInfoDTO.getJoinType();
            // 将申请接收人设为对方
            userContactApplyDTO.setReceiveUserId(userInfoDTO.getUserId());
        }else{
            throw new BusinessException(Constants.ERROR_OPERATION);
        }
        // 4. 检查用户/群组申请权限
        if (joinType.equals(ContactJoinTypeEnum.DIRECT.getStatus())){
            // 4.1 直接加入，无需审核，创建双向好友关系
            UserContactDTO userContactDTO = new UserContactDTO();
            userContactDTO.setUserId(userContactApplyDTO.getApplyUserId());
            userContactDTO.setContactId(userContactApplyDTO.getContactId());
            userContactDTO.setContactType(userContactApplyDTO.getContactType());
            userContactDTO.setStatus(ContactStatusEnum.FRIEND.getStatus());
            if (userContactApplyDTO.getContactType().equals(ContactTypeEnum.GROUP.getStatus())){
                // 4.1.1 群组直接加入
                createContact(userContactDTO);
            } else {
                // 4.1.2 用户创建双向好友关系
                createContact(userContactDTO);
                String contactId =  userContactDTO.getContactId();
                userContactDTO.setContactId(userContactDTO.getUserId());
                userContactDTO.setUserId(contactId);
                createContact(userContactDTO);
            }
        } else if (joinType.equals(ContactJoinTypeEnum.AUDIT.getStatus())) {
            // 4.2 需要审核，加入申请表
            userContactApplyService.createContactApply(userContactApplyDTO);
        } else {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }


        // 2. TODO 发消息通知群主/用户

    }

    /**
     * 添加/拉黑好友|加入/拉黑群聊
     * @param userContactDTO 添加好友DTO
     */
    public void createContact(UserContactDTO userContactDTO) {
        // 1. 查询所申请的联系人/群组信息
        UserContact userContact = baseMapper.selectOne(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getUserId, userContactDTO.getUserId())
                .eq(UserContact::getContactId, userContactDTO.getContactId()));
        if (userContact == null){
            // 2.1 关系表中没有记录，添加好友/加入群组
            userContact = new UserContact();
            BeanUtils.copyProperties(userContactDTO,userContact);
            userContact.setCreateTime(DateTime.now());
            userContact.setLastUpdateTime(DateTime.now());
            baseMapper.insert(userContact);
        } else {
            // 2.2 关系表中存在记录，根据申请类型更新状态
            // 2.2.1 好友关系表中存在记录，根据申请类型更新状态
            userContact.setStatus(userContactDTO.getStatus());
            userContact.setLastUpdateTime(DateTime.now());
            baseMapper.updateById(userContact);
        }

        // 2. TODO 发消息通知申请人和接收人

    }

    /**
     * 获取联系人列表
     * @param contactType 联系人类型 USER:好友 GROUP:群组
     * @return 联系人列表
     */
    @Override
    public List<UserContact> getContactList(String contactType) {
        return baseMapper.getContactList(UserContext.getUser(), ContactTypeEnum.nameToStatus(contactType));
    }
}
