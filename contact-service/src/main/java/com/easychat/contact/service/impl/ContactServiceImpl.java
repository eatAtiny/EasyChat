package com.easychat.contact.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.api.GroupClient;
import com.easychat.contact.api.UserClient;
import com.easychat.contact.constant.Constants;
import com.easychat.contact.entity.dto.GroupInfoDTO;
import com.easychat.contact.entity.dto.ContactApplyDTO;
import com.easychat.contact.entity.dto.ContactDTO;
import com.easychat.contact.entity.dto.UserInfoDTO;
import com.easychat.contact.entity.enums.ContactJoinTypeEnum;
import com.easychat.contact.entity.enums.ContactStatusEnum;
import com.easychat.contact.entity.enums.ContactTypeEnum;
import com.easychat.contact.entity.po.Contact;
import com.easychat.contact.entity.po.ContactGroupInfo;
import com.easychat.contact.entity.vo.SearchResultVO;

import com.easychat.contact.mapper.ContactGroupInfoMapper;
import com.easychat.contact.mapper.ContactMapper;

import com.easychat.contact.service.ContactApplyService;
import com.easychat.contact.service.ContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements ContactService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private ContactApplyService contactApplyService;
    @Autowired
    private GroupClient groupClient;
    @Autowired
    private ContactGroupInfoMapper contactGroupInfoMapper;

    /**
     * 申请添加好友关系
     *
     * @param contactApplyDTO 申请添加好友DTO
     */
    @Override
    public void applyAdd(ContactApplyDTO contactApplyDTO) {
        // 1. 查询所申请的联系人/群组信息
        Contact contact = baseMapper.selectOne(new LambdaQueryWrapper<Contact>()
                            .eq(Contact::getUserId, contactApplyDTO.getApplyUserId())
                            .eq(Contact::getContactId, contactApplyDTO.getContactId()));
        // 2. 检查是否被拉黑
        if (contact != null && contact.getStatus().equals(Constants.CONTACT_USER_STATUS_BLOCKED_BY_FRIEND)){
            throw new BusinessException(Constants.CONTACT_USER_STATUS_BLOCKED);
        }
        // 3. 检查用户/群组是否存在
        Integer joinType =null;
        if (contactApplyDTO.getContactType().equals(ContactTypeEnum.GROUP.getStatus())){
            // 3.1 检查群组是否存在
            GroupInfoDTO groupInfo = groupClient.getGroupInfo(contactApplyDTO.getContactId());
            if (groupInfo == null){
                throw new BusinessException(Constants.GROUP_NOT_EXIST);
            }
            joinType = groupInfo.getJoinType();
            // 将申请接收人设为群主
            contactApplyDTO.setReceiveUserId(groupInfo.getGroupOwnerId());
        }else if (contactApplyDTO.getContactType().equals(ContactTypeEnum.USER.getStatus())){
            // 3.2 检查用户是否存在
            UserInfoDTO userInfoDTO = userClient.getUserInfo(contactApplyDTO.getContactId());
            if (userInfoDTO == null){
                throw new BusinessException(Constants.USER_NOT_EXIST);
            }
            joinType = userInfoDTO.getJoinType();
            // 将申请接收人设为对方
            contactApplyDTO.setReceiveUserId(userInfoDTO.getUserId());
        }else{
            throw new BusinessException(Constants.ERROR_OPERATION);
        }
        // 4. 检查用户/群组申请权限
        if (joinType.equals(ContactJoinTypeEnum.DIRECT.getStatus())){
            // 4.1 直接加入，无需审核，创建双向好友关系
            ContactDTO contactDTO = new ContactDTO();
            contactDTO.setUserId(contactApplyDTO.getApplyUserId());
            contactDTO.setContactId(contactApplyDTO.getContactId());
            contactDTO.setContactType(contactApplyDTO.getContactType());
            contactDTO.setStatus(ContactStatusEnum.FRIEND.getStatus());
            if (contactApplyDTO.getContactType().equals(ContactTypeEnum.GROUP.getStatus())){
                // 4.1.1 群组直接加入
                createContact(contactDTO);
            } else {
                // 4.1.2 用户创建双向好友关系
                createContact(contactDTO);
                String contactId =  contactDTO.getContactId();
                contactDTO.setContactId(contactDTO.getUserId());
                contactDTO.setUserId(contactId);
                createContact(contactDTO);
            }
        } else if (joinType.equals(ContactJoinTypeEnum.AUDIT.getStatus())) {
            // 4.2 需要审核，加入申请表
            contactApplyService.createContactApply(contactApplyDTO);
        } else {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }


        // 2. TODO 发消息通知群主/用户

    }

    /**
     * 添加/拉黑好友|加入/拉黑群聊
     * @param contactDTO 添加好友DTO
     */
    public void createContact(ContactDTO contactDTO) {
        // 1. 查询所申请的联系人/群组信息
        Contact contact = baseMapper.selectOne(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getUserId, contactDTO.getUserId())
                .eq(Contact::getContactId, contactDTO.getContactId()));
        if (contact == null){
            // 2.1 关系表中没有记录，添加好友/加入群组
            contact = new Contact();
            BeanUtils.copyProperties(contactDTO, contact);
            contact.setCreateTime(DateTime.now());
            contact.setLastUpdateTime(DateTime.now());
            baseMapper.insert(contact);
        } else {
            // 2.2 关系表中存在记录，根据申请类型更新状态
            // 2.2.1 好友关系表中存在记录，根据申请类型更新状态
            contact.setStatus(contactDTO.getStatus());
            contact.setLastUpdateTime(DateTime.now());
            baseMapper.updateById(contact);
        }

        // 2. TODO 发消息通知申请人和接收人

    }

    /**
     * 获取联系人列表
     * @param contactType 联系人类型 USER:好友 GROUP:群组
     * @return 联系人列表
     */
    @Override
    public List<Contact> getContactList(String contactType) {
        return baseMapper.getContactList(UserContext.getUser(), ContactTypeEnum.nameToStatus(contactType));
    }
}
