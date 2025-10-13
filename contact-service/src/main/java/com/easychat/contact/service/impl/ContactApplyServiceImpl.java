package com.easychat.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.entity.po.Contact;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.api.GroupClient;
import com.easychat.contact.api.UserClient;
import com.easychat.contact.constant.Constants;
import com.easychat.common.entity.dto.ContactApplyDTO;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.dto.GroupInfoDTO;
import com.easychat.common.entity.dto.UserInfoDTO;
import com.easychat.common.entity.enums.ContactApplyStatusEnum;
import com.easychat.common.entity.enums.ContactJoinTypeEnum;
import com.easychat.common.entity.enums.ContactStatusEnum;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.po.ContactApply;
import com.easychat.contact.mapper.ContactApplyMapper;
import com.easychat.contact.service.ContactApplyService;
import com.easychat.contact.service.ContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ContactApplyServiceImpl extends ServiceImpl<ContactApplyMapper, ContactApply> implements ContactApplyService {

    @Autowired
    private ContactService contactService;

    @Autowired
    private GroupClient groupClient;

    @Autowired
    private UserClient userClient;

    /**
     * 申请添加好友关系
     *
     * @param contactApplyDTO 申请添加好友DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyAdd(ContactApplyDTO contactApplyDTO) {
        // 0. 检查是否已存在好友申请
        ContactApply contactApply = baseMapper.selectOne(new LambdaQueryWrapper<ContactApply>()
                .eq(ContactApply::getApplyUserId, contactApplyDTO.getApplyUserId())
                .eq(ContactApply::getContactId, contactApplyDTO.getContactId()));
        if (contactApply != null && contactApply.getStatus().equals(ContactApplyStatusEnum.PENDING.getStatus())){
            throw new BusinessException(Constants.APPLY_INFO_EXIST);
        }
        // 1. 查询所申请的联系人/群组信息
        Contact contact = contactService.getBaseMapper().selectOne(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getUserId, contactApplyDTO.getApplyUserId())
                .eq(Contact::getContactId, contactApplyDTO.getContactId()));
        // 2. 检查是否被拉黑
        if (contact != null && contact.getStatus().equals(ContactStatusEnum.BLOCK_FRIEND.getStatus())){
            throw new BusinessException(Constants.CONTACT_USER_STATUS_BLOCKED);
        }
        // 3. 检查用户/群组是否存在
        Integer joinType =null;
        if (contactApplyDTO.getContactType().equals(ContactTypeEnum.GROUP.getStatus())){
            // 3.1 检查群组是否存在
            GroupInfoDTO groupInfo;
            try {
                groupInfo = groupClient.getGroupInfo(contactApplyDTO.getContactId());
                if (groupInfo == null){
                    throw new BusinessException(Constants.GROUP_NOT_EXIST);
                }
                joinType = groupInfo.getJoinType();
                // 将申请接收人设为群主
                contactApplyDTO.setReceiveUserId(groupInfo.getGroupOwnerId());
            } catch (Exception e) {
                // 处理群组服务调用失败的情况，包括UnknownHostException
                throw new BusinessException(Constants.GROUP_INFO_SERVICE_ERROR);
            }
        }else if (contactApplyDTO.getContactType().equals(ContactTypeEnum.USER.getStatus())){
            // 3.2 检查用户是否存在
            UserInfoDTO userInfoDTO;
            try {
                userInfoDTO = userClient.getUserInfo(contactApplyDTO.getContactId());
                if (userInfoDTO == null){
                    throw new BusinessException(Constants.USER_NOT_EXIST);
                }
                joinType = userInfoDTO.getJoinType();
                // 将申请接收人设为对方
                contactApplyDTO.setReceiveUserId(userInfoDTO.getUserId());
            } catch (Exception e) {
                // 处理用户服务调用失败的情况
                throw new BusinessException(Constants.USER_INFO_SERVICE_ERROR);
            }
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
                contactService.manageContact(contactDTO);
            } else {
                // 4.1.2 用户创建双向好友关系
                contactService.manageContact(contactDTO);
                String contactId =  contactDTO.getContactId();
                contactDTO.setContactId(contactDTO.getUserId());
                contactDTO.setUserId(contactId);
                contactService.manageContact(contactDTO);
            }
        } else if (joinType.equals(ContactJoinTypeEnum.AUDIT.getStatus())) {
            // 4.2 需要审核，加入申请表
            if (contactApply == null) {
                contactApply = new ContactApply();
                BeanUtils.copyProperties(contactApplyDTO, contactApply);
                contactApply.setLastApplyTime(System.currentTimeMillis());
                contactApply.setStatus(ContactApplyStatusEnum.PENDING.getStatus());
                baseMapper.insert(contactApply);
            } else {
                // 更新最后申请时间
                contactApply.setLastApplyTime(System.currentTimeMillis());
                contactApply.setStatus(ContactApplyStatusEnum.PENDING.getStatus());
                baseMapper.updateById(contactApply);
            }
        } else {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }


        // 2. TODO 发消息通知群主/用户

    }

    /**
     * 获取申请列表，包含联系人信息
     * @param pageNo 页码
     * @return 分页结果
     */
    @Override
    public IPage<ContactApply> getApplyList(Integer pageNo) {
        IPage<ContactApply> page = new Page<>(pageNo, 10);

        // 使用自定义SQL进行联合查询
        List<ContactApply> applyList = baseMapper.selectApplyListWithUserInfo(UserContext.getUser());

        // 计算分页
        int startIndex = (pageNo - 1) * 10;
        int endIndex = Math.min(startIndex + 10, applyList.size());

        if (startIndex < applyList.size()) {
            page.setRecords(applyList.subList(startIndex, endIndex));
        } else {
            page.setRecords(new ArrayList<>());
        }

        page.setTotal(applyList.size());
        page.setCurrent(pageNo);
        page.setSize(10);
        page.setPages((applyList.size() + 9) / 10);

        return page;
    }

    /**
     * 处理申请
     * @param applyId 申请ID
     * @param status 处理操作 1:同意 2:拒绝 3:拉黑
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(Integer applyId, Integer status) {
        // 1. 校验申请是否存在
        ContactApply apply = baseMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException(Constants.APPLY_NOT_EXIST);
        }
        // 2. 校验操作是否合法 只能处理待处理状态申请
        if (Objects.equals(status, ContactApplyStatusEnum.PENDING.getStatus()) || !Objects.equals(apply.getStatus(), ContactApplyStatusEnum.PENDING.getStatus())) {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }
        // 3. 处理申请
        apply.setStatus(status);
        baseMapper.updateById(apply);


        // 4. 后续处理
        if (status.equals(ContactApplyStatusEnum.AGREE.getStatus())) {
            // 4.1 同意申请，创建好友/群聊关系
            ContactDTO contactDTO = new ContactDTO();
            contactDTO.setUserId(apply.getApplyUserId());
            contactDTO.setContactId(apply.getContactId());
            contactDTO.setContactType(apply.getContactType());
            contactDTO.setStatus(ContactStatusEnum.FRIEND.getStatus());
            contactService.manageContact(contactDTO);
            // 4.1.1 若是用户还需要创建双向好友关系
            if (apply.getContactType().equals(ContactTypeEnum.USER.getStatus())){
                String userId = apply.getApplyUserId();
                contactDTO.setUserId(apply.getContactId());
                contactDTO.setContactId(userId);
                contactService.manageContact(contactDTO);
            }
        } else if (status.equals(ContactApplyStatusEnum.REFUSE.getStatus())) {
            // 4.2 拒绝申请，无需后续处理

        } else if (status.equals(ContactApplyStatusEnum.BLOCKED.getStatus())) {
            // 4.3 拉黑申请，创建好友拉黑关系
            ContactDTO contactDTO = new ContactDTO();
            contactDTO.setUserId(apply.getApplyUserId());
            contactDTO.setContactId(apply.getContactId());
            contactDTO.setContactType(apply.getContactType());
            contactDTO.setStatus(ContactStatusEnum.BLOCK_FRIEND.getStatus());
            contactService.manageContact(contactDTO);
            // 4.3.1 若是用户还需要创建双向好友拉黑关系
            if (apply.getContactType().equals(ContactTypeEnum.USER.getStatus())){
                String userId = apply.getApplyUserId();
                contactDTO.setUserId(apply.getContactId());
                contactDTO.setContactId(userId);
                contactDTO.setStatus(ContactStatusEnum.BLOCK_BY_FRIEND.getStatus());
                contactService.manageContact(contactDTO);
            }
        } else {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }

    }
}