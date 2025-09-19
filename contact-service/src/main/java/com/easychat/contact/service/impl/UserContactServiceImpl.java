package com.easychat.contact.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.api.UserClient;
import com.easychat.contact.constant.Constants;
import com.easychat.contact.entity.dto.UserContactApplyDTO;
import com.easychat.contact.entity.dto.UserInfoDTO;
import com.easychat.contact.entity.po.GroupInfo;
import com.easychat.contact.entity.po.UserContact;
import com.easychat.contact.entity.po.UserContactApply;
import com.easychat.contact.entity.vo.SearchResultVO;
import com.easychat.contact.mapper.GroupInfoMapper;
import com.easychat.contact.mapper.UserContactApplyMapper;
import com.easychat.contact.mapper.UserContactMapper;
import com.easychat.contact.service.UserContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements UserContactService {

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Autowired
    private UserContactApplyMapper userContactApplyMapper;

    @Autowired
    private UserClient userClient;

    @Override
    public SearchResultVO search(String contactId) {
        // 1. 判断ID是否合法（以G或者U开头的12位字符串）
        if (!contactId.matches(Constants.CONTACT_ID_REGEX)){
            return null;
        }

        // 2. 根据ID从数据库查询用户/群组信息
        SearchResultVO searchResultVO = new SearchResultVO();
        if (contactId.charAt(0) == Constants.CONTACT_TYPE_GROUP_OPEN){
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            if (groupInfo != null){
                searchResultVO.setContactType(Constants.CONTACT_TYPE_GROUP_NAME);
                searchResultVO.setNickName(groupInfo.getGroupName());
                searchResultVO.setContactId(groupInfo.getGroupId());
                searchResultVO.setStatusName(groupInfo.getStatus() == Constants.GROUP_STATUS_NORMAL ? "正常" : "解散");
            }
        } else {
            UserInfoDTO userInfoDTO = userClient.search(contactId);
            if (userInfoDTO != null){
                searchResultVO.setContactType(Constants.CONTACT_TYPE_USER_NAME);
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

    @Override
    public void applyAdd(UserContactApplyDTO userContactApplyDTO) {
        // 1. 查询所申请的联系人/群组信息
        UserContact userContact = baseMapper.selectOne(new LambdaQueryWrapper<UserContact>()
                            .eq(UserContact::getUserId, UserContext.getUser())
                            .eq(UserContact::getContactId, userContactApplyDTO.getContactId()));
        if (userContactApplyDTO.getContactType().equals(Constants.CONTACT_TYPE_USER_NAME)){
            // 1.1 联系人类型为用户
            // 1.1.1 检查用户好友关系
            if (userContact != null && userContact.getStatus().equals(Constants.CONTACT_USER_STATUS_BLOCKED_BY_FRIEND)){
                throw new BusinessException(Constants.CONTACT_USER_STATUS_BLOCKED);
            }
            // 1.1.2 检查用户是否存在 (需要调用user-service,所以先查UserContact表)
            UserInfoDTO userInfoDTO = userClient.search(userContactApplyDTO.getContactId());
            if (userInfoDTO == null){
                throw new IllegalArgumentException(Constants.USER_NOT_EXIST);
            }
            // 1.1.3 检查用户申请权限
            if (userInfoDTO.getJoinType().equals(Constants.CONTACT_APPLY_PERMISSION_DIRECT)){
                // 1.1.3.1 无需同意，直接加入
                if(userContact!=null){
                    // 更新好友关系
                    userContact.setStatus(Constants.CONTACT_USER_STATUS_FRIEND);
                    userContact.setCreateTime(DateTime.now());
                    userContact.setLastUpdateTime(DateTime.now());
                    baseMapper.updateById(userContact);
                    // 更新对方好友关系
                    userContact.setUserId(userInfoDTO.getUserId());
                    userContact.setContactId(UserContext.getUser());
                    baseMapper.updateById(userContact);
                }else {
                    // 插入好友关系
                    userContact = new UserContact();
                    userContact.setUserId(UserContext.getUser());
                    userContact.setContactId(userContactApplyDTO.getContactId());
                    userContact.setContactType(Constants.CONTACT_TYPE_USER);
                    userContact.setCreateTime(DateTime.now());
                    userContact.setLastUpdateTime(DateTime.now());
                    userContact.setStatus(Constants.CONTACT_USER_STATUS_FRIEND);
                    baseMapper.insert(userContact);
                    // 插入对方好友关系
                    userContact.setUserId(userInfoDTO.getUserId());
                    userContact.setContactId(UserContext.getUser());
                    baseMapper.insert(userContact);
                }
            } else if (userInfoDTO.getJoinType().equals(Constants.CONTACT_APPLY_PERMISSION_AUDIT)){
                // 1.1.3.2 需要同意，加入申请表
                UserContactApply userContactApply = new UserContactApply();
                userContactApply.setApplyUserId(UserContext.getUser());
                userContactApply.setReceiveUserId(userContactApplyDTO.getContactId());
                userContactApply.setContactType(Constants.CONTACT_TYPE_USER);
                userContactApply.setContactId(userContactApplyDTO.getContactId());
                userContactApply.setApplyInfo(userContactApplyDTO.getApplyInfo());
                userContactApply.setLastApplyTime(DateTime.now().getTime());
                userContactApply.setStatus(Constants.CONTACT_APPLY_STATUS_PENDING);
                userContactApply.setApplyInfo(userContactApplyDTO.getApplyInfo());
                userContactApplyMapper.insert(userContactApply);
            }
        } else if (userContactApplyDTO.getContactType().equals(Constants.CONTACT_TYPE_GROUP_NAME)){
            // 1.2 联系人类型为群组
            // 1.2.1 检查群组是否存在
            GroupInfo groupInfo = groupInfoMapper.selectById(userContactApplyDTO.getContactId());
            if (groupInfo == null){
                throw new BusinessException(Constants.GROUP_NOT_EXIST);
            }
            // 1.2.2 检查群组人数是否达到上限
            Integer groupNum = baseMapper.selectCount(new LambdaQueryWrapper<UserContact>()
                    .eq(UserContact::getContactId, userContactApplyDTO.getContactId())
            );
            if (groupNum >=/* TODO 使用管理系统参数维护 */ 10){
                throw new BusinessException(Constants.GROUP_FULL);
            }
            // 1.2.3 检查群组申请权限
            if (groupInfo.getJoinType().equals(Constants.CONTACT_APPLY_PERMISSION_DIRECT)){
                // 1.2.3.1 直接加入
                if(userContact!=null){
                    userContact.setStatus(Constants.CONTACT_USER_STATUS_FRIEND);
                    userContact.setCreateTime(DateTime.now());
                    userContact.setLastUpdateTime(DateTime.now());
                    baseMapper.updateById(userContact);
                }else{
                    userContact = new UserContact();
                    userContact.setUserId(userContactApplyDTO.getContactId());
                    userContact.setContactId(userContactApplyDTO.getContactId());
                    userContact.setContactType(Constants.CONTACT_TYPE_GROUP);
                    userContact.setCreateTime(DateTime.now());
                    userContact.setLastUpdateTime(DateTime.now());
                    userContact.setStatus(Constants.CONTACT_USER_STATUS_FRIEND);
                    baseMapper.insert(userContact);
                }
            }else{
                // 1.2.3.2 需要同意，加入申请表，通知群主
                UserContactApply userContactApply = new UserContactApply();
                userContactApply.setApplyUserId(UserContext.getUser());
                userContactApply.setReceiveUserId(groupInfo.getGroupOwnerId());
                userContactApply.setContactType(Constants.CONTACT_TYPE_GROUP);
                userContactApply.setContactId(userContactApplyDTO.getContactId());
                userContactApply.setApplyInfo(userContactApplyDTO.getApplyInfo());
                userContactApply.setLastApplyTime(DateTime.now().getTime());
                userContactApply.setStatus(Constants.CONTACT_APPLY_STATUS_PENDING);
                userContactApply.setApplyInfo(userContactApplyDTO.getApplyInfo());
                userContactApplyMapper.insert(userContactApply);
            }
        } else {
            throw new BusinessException(Constants.CONTACT_APPLY_INFO_ERROR);
        }
        // 2. TODO 发消息通知群主/用户

    }
}
