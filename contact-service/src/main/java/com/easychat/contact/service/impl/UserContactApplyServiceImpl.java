package com.easychat.contact.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.exception.BusinessException;
import com.easychat.contact.constant.Constants;
import com.easychat.contact.entity.dto.UserContactApplyDTO;
import com.easychat.contact.entity.dto.UserContactDTO;
import com.easychat.contact.entity.enums.ContactApplyStatusEnum;
import com.easychat.contact.entity.enums.ContactStatusEnum;
import com.easychat.contact.entity.enums.ContactTypeEnum;
import com.easychat.contact.entity.po.UserContact;
import com.easychat.contact.entity.po.UserContactApply;
import com.easychat.contact.mapper.UserContactApplyMapper;
import com.easychat.contact.mapper.UserContactMapper;
import com.easychat.contact.service.UserContactApplyService;
import com.easychat.contact.service.UserContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements UserContactApplyService {

    @Autowired
    private UserContactMapper userContactMapper;

    @Override
    public void createContactApply(UserContactApplyDTO userContactApplyDTO) {
        UserContactApply userContactApply = new UserContactApply();
        BeanUtils.copyProperties(userContactApplyDTO,userContactApply);
        userContactApply.setLastApplyTime(System.currentTimeMillis());
        userContactApply.setStatus(ContactApplyStatusEnum.PENDING.getStatus());
        baseMapper.insert(userContactApply);
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
        UserContactApply apply = baseMapper.selectById(applyId);
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
            UserContactDTO userContactDTO = new UserContactDTO();
            userContactDTO.setUserId(apply.getApplyUserId());
            userContactDTO.setContactId(apply.getContactId());
            userContactDTO.setContactType(apply.getContactType());
            userContactDTO.setStatus(ContactStatusEnum.FRIEND.getStatus());
            createContact(userContactDTO);
            // 4.1.1 若是用户还需要创建双向好友关系
            if (apply.getContactType().equals(ContactTypeEnum.USER.getStatus())){
                String userId = apply.getApplyUserId();
                userContactDTO.setUserId(apply.getContactId());
                userContactDTO.setContactId(userId);
                createContact(userContactDTO);
            }
        } else if (status.equals(ContactApplyStatusEnum.REFUSE.getStatus())) {
            // 4.2 拒绝申请，无需后续处理

        } else if (status.equals(ContactApplyStatusEnum.BLOCKED.getStatus())) {
            // 4.3 拉黑申请，创建单向好友拉黑关系
            UserContactDTO userContactDTO = new UserContactDTO();
            userContactDTO.setUserId(apply.getApplyUserId());
            userContactDTO.setContactId(apply.getContactId());
            userContactDTO.setContactType(apply.getContactType());
            userContactDTO.setStatus(ContactStatusEnum.BLOCK_FRIEND.getStatus());
            createContact(userContactDTO);
        }

    }

    /**
     * 添加/拉黑好友|加入/拉黑群聊
     * @param userContactDTO 添加好友DTO
     */
    public void createContact(UserContactDTO userContactDTO) {
        // 1. 查询所申请的联系人/群组信息
        UserContact userContact = userContactMapper.selectOne(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getUserId, userContactDTO.getUserId())
                .eq(UserContact::getContactId, userContactDTO.getContactId()));
        if (userContact == null){
            // 2.1 关系表中没有记录，添加好友/加入群组
            userContact = new UserContact();
            BeanUtils.copyProperties(userContactDTO,userContact);
            userContact.setCreateTime(DateTime.now());
            userContact.setLastUpdateTime(DateTime.now());
            userContactMapper.insert(userContact);
        } else {
            // 2.2 关系表中存在记录，根据申请类型更新状态
            // 2.2.1 好友关系表中存在记录，根据申请类型更新状态
            userContact.setStatus(userContactDTO.getStatus());
            userContact.setLastUpdateTime(DateTime.now());
            userContactMapper.updateById(userContact);
        }

        // 2. TODO 发消息通知申请人和接收人

    }


}
