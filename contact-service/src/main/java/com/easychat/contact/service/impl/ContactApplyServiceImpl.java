package com.easychat.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.api.GroupInfoDubboService;
import com.easychat.common.api.SessionDubboService;
import com.easychat.common.api.UserInfoDubboService;
import com.easychat.common.entity.dto.*;
import com.easychat.common.entity.enums.*;
import com.easychat.common.entity.po.*;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.StringTools;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.constant.Constants;
import com.easychat.contact.kafka.KafkaMessageService;
import com.easychat.contact.mapper.ContactApplyMapper;
import com.easychat.contact.mapper.ContactUserInfoMapper;
import com.easychat.contact.service.ContactApplyService;
import com.easychat.contact.service.ContactService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ContactApplyServiceImpl extends ServiceImpl<ContactApplyMapper, ContactApply> implements ContactApplyService {

    @Autowired
    private ContactService contactService;

    @DubboReference(check = false)
    private SessionDubboService sessionDubboService;

    @DubboReference(check = false)
    private GroupInfoDubboService groupInfoDubboService;

    @DubboReference(check = false)
    private UserInfoDubboService userInfoDubboService;

    @Autowired
    private RedisComponet redisComponet;

    @Autowired
    private KafkaMessageService kafkaMessageService;
    @Autowired
    private ContactUserInfoMapper contactUserInfoMapper;


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
        GroupInfoDTO groupInfoDTO = null;
        UserInfoDTO userInfoDTO = null;
        if (contactApplyDTO.getContactType().equals(ContactTypeEnum.GROUP.getStatus())){
            // 3.1 检查群组是否存在
            try {
                groupInfoDTO = groupInfoDubboService.getGroupInfo(contactApplyDTO.getContactId());
                if (groupInfoDTO == null){
                    throw new BusinessException(Constants.GROUP_NOT_EXIST);
                }
                joinType = groupInfoDTO.getJoinType();
                // 将申请接收人设为群主
                contactApplyDTO.setReceiveUserId(groupInfoDTO.getGroupOwnerId());
            } catch (Exception e) {
                // 处理群组服务调用失败的情况，包括UnknownHostException
                throw new BusinessException(Constants.GROUP_INFO_SERVICE_ERROR);
            }
        }else if (contactApplyDTO.getContactType().equals(ContactTypeEnum.USER.getStatus())){
            // 3.2 检查用户是否存在
            try {
                userInfoDTO = userInfoDubboService.getUserInfo(contactApplyDTO.getContactId());
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
            addOrUpdateContact(contactDTO);

            String sessionId = null;
            String lastMessage = null;
            String contactName = null;
            Integer messageType = null;
            if (ContactTypeEnum.USER.getStatus().equals(contactApplyDTO.getContactType())) {
                sessionId = StringTools.getChatSessionId4User(new String[]{contactApplyDTO.getApplyUserId(), contactApplyDTO.getContactId()});
                lastMessage = contactApplyDTO.getApplyInfo();
                contactName = userInfoDTO.getNickName();
                messageType = MessageTypeEnum.ADD_FRIEND.getType();
            } else {
                // 更新群组人数
                groupInfoDubboService.addGroupMemberCount(contactApplyDTO.getContactId(), 1);
                // 将user加入groupChannel
                sessionDubboService.addGroupToChannel(contactApplyDTO.getApplyUserId(), contactApplyDTO.getContactId());
                sessionId = StringTools.getChatSessionId4Group(contactApplyDTO.getContactId());
                lastMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), UserContext.getNickName());
                contactName = groupInfoDTO.getGroupName();
                messageType = MessageTypeEnum.ADD_GROUP.getType();
            }
            redisComponet.addUserContact(contactApplyDTO.getApplyUserId(), contactApplyDTO.getContactId());
            // 4.3 创建会话

            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            chatSession.setLastMessage(lastMessage);
            sessionDubboService.addSession(chatSession);

            // 4.4 创建用户会话
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(contactApplyDTO.getApplyUserId());
            chatSessionUser.setContactId(contactApplyDTO.getApplyUserId());
            chatSessionUser.setContactName(contactName);
            sessionDubboService.addSessionUser(chatSessionUser);
            if(ContactTypeEnum.USER.getStatus().equals(contactApplyDTO.getContactType())) {
                // 4.4.1 若申请对象为用户，则还需要为对方创建会话
                ChatSessionUser friendChatSessionUser = new ChatSessionUser();
                friendChatSessionUser.setSessionId(sessionId);
                friendChatSessionUser.setUserId(contactApplyDTO.getContactId());
                friendChatSessionUser.setContactId(contactApplyDTO.getApplyUserId());
                friendChatSessionUser.setContactName(UserContext.getNickName());
                sessionDubboService.addSessionUser(friendChatSessionUser);
                redisComponet.addUserContact(contactApplyDTO.getContactId(), contactApplyDTO.getApplyUserId());
            }

            // 4.5 增加聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(messageType);
            chatMessage.setMessageContent(lastMessage);
            chatMessage.setSendUserId(UserContext.getUser());
            chatMessage.setSendUserNickName(UserContext.getNickName());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setContactId(contactApplyDTO.getContactId());
            chatMessage.setContactType(contactApplyDTO.getContactType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            // 添加消息到数据库，接收返回的包含自增ID的chatMessage对象
            chatMessage = sessionDubboService.addChatMessage(chatMessage);

            // 4.6 向客户端发送消息
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setExtendData(chatMessage);
            BeanUtils.copyProperties(chatMessage, messageSendDTO);
            if(groupInfoDTO != null){
                messageSendDTO.setMemberCount(groupInfoDTO.getMemberCount() + 1);
            }
            kafkaMessageService.sendMessageToChannel(messageSendDTO);

            if(ContactTypeEnum.USER.getStatus().equals(contactApplyDTO.getContactType())){
                // 4.6.1 若申请对象为用户，则还需要给自身发送消息
                messageSendDTO.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
                messageSendDTO.setContactId(UserContext.getUser());
                kafkaMessageService.sendMessageToChannel(messageSendDTO);
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
            // 给对方发送信息通知申请
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            BeanUtils.copyProperties(contactApplyDTO, messageSendDTO);
            messageSendDTO.setSendUserId(UserContext.getUser());
            messageSendDTO.setSendUserNickName(UserContext.getNickName());
            messageSendDTO.setContactId(contactApplyDTO.getContactId());
            messageSendDTO.setContactType(contactApplyDTO.getContactType());
            messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDTO.setMessageContent(contactApplyDTO.getApplyInfo());
            messageSendDTO.setSendTime(System.currentTimeMillis());
            messageSendDTO.setStatus(MessageStatusEnum.SENDED.getStatus());
            kafkaMessageService.sendMessageToChannel(messageSendDTO);
        } else {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }

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
            addOrUpdateContact(contactDTO);

            GroupInfoDTO groupInfoDTO = null;
            UserInfoDTO userInfoDTO = null;
            String sessionId = null;
            String lastMessage = null;
            String contactName = null;
            Integer messageType = null;
            if (ContactTypeEnum.USER.getStatus().equals(apply.getContactType())) {
                userInfoDTO = userInfoDubboService.getUserInfo(apply.getContactId());
                sessionId = StringTools.getChatSessionId4User(new String[]{apply.getApplyUserId(), apply.getContactId()});
                lastMessage = apply.getApplyInfo();
                contactName = apply.getContactName();
                messageType = MessageTypeEnum.ADD_FRIEND.getType();
            } else {
                groupInfoDTO = groupInfoDubboService.getGroupInfo(apply.getContactId());
                // 更新群组人数
                groupInfoDubboService.addGroupMemberCount(apply.getContactId(), 1);
                sessionId = StringTools.getChatSessionId4Group(apply.getContactId());
                lastMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), apply.getApplyUserId());
                contactName = apply.getContactName();
                messageType = MessageTypeEnum.ADD_GROUP.getType();
            }
            // 4.2 创建会话
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            chatSession.setLastMessage(lastMessage);
            sessionDubboService.addSession(chatSession);

            redisComponet.addUserContact(apply.getApplyUserId(), apply.getContactId());

            // 4.4 创建用户会话
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(apply.getApplyUserId());
            chatSessionUser.setContactId(apply.getContactId());
            chatSessionUser.setContactName(contactName);
            sessionDubboService.addSessionUser(chatSessionUser);
            if(ContactTypeEnum.USER.getStatus().equals(apply.getContactType())) {
                // 4.4.1 若申请对象为用户，则还需要为对方创建会话
                ChatSessionUser friendChatSessionUser = new ChatSessionUser();
                friendChatSessionUser.setSessionId(sessionId);
                friendChatSessionUser.setUserId(apply.getContactId());
                friendChatSessionUser.setContactId(apply.getApplyUserId());
                friendChatSessionUser.setContactName(UserContext.getNickName());
                redisComponet.addUserContact(apply.getContactId(), apply.getApplyUserId());
                sessionDubboService.addSessionUser(friendChatSessionUser);
            }

            // 4.5 增加聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(messageType);
            chatMessage.setMessageContent(lastMessage);
            chatMessage.setSendUserId(apply.getApplyUserId());
            chatMessage.setSendUserNickName(contactUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<ContactUserInfo>()
                            .eq(ContactUserInfo::getUserId, apply.getApplyUserId()))
                    .getNickName());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setContactId(apply.getContactId());
            chatMessage.setContactType(apply.getContactType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            // 添加消息到数据库，接收返回的包含自增ID的chatMessage对象
            chatMessage = sessionDubboService.addChatMessage(chatMessage);

            // 4.6 向客户端发送消息
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            BeanUtils.copyProperties(chatMessage, messageSendDTO);
            messageSendDTO.setContactName(contactName);
            messageSendDTO.setLastMessage(lastMessage);
            if(groupInfoDTO != null){
                // 将用户加入群组频道
                sessionDubboService.addGroupToChannel(apply.getApplyUserId(), apply.getContactId());
                messageSendDTO.setMemberCount(groupInfoDTO.getMemberCount() + 1);
            }
            kafkaMessageService.sendMessageToChannel(messageSendDTO);

            if(ContactTypeEnum.USER.getStatus().equals(apply.getContactType())){
                // 4.6.1 若申请对象为用户，则还需要给申请人发送消息
                messageSendDTO.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
                messageSendDTO.setContactId(apply.getReceiveUserId());
//                messageSendDTO.setExtendData(userInfoDTO);
                kafkaMessageService.sendMessageToChannel(messageSendDTO);
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
            addOrUpdateContact(contactDTO);
        } else {
            throw new BusinessException(Constants.ERROR_OPERATION);
        }

    }

    /**
     * 新增或者修改联系关系
     * @param contactDTO 联系关系DTO
     */

    public void addOrUpdateContact(ContactDTO contactDTO){
        // 1. 新增或者修改关系
        contactService.manageContact(contactDTO);
        // 1.1 若是用户还需要创建双向好友关系
        if (contactDTO.getContactType().equals(ContactTypeEnum.USER.getStatus())){
            String userId = contactDTO.getUserId();
            contactDTO.setUserId(contactDTO.getContactId());
            contactDTO.setContactId(userId);
            if(contactDTO.getStatus().equals(ContactStatusEnum.BLOCK_BY_FRIEND.getStatus()))
                contactDTO.setStatus(ContactStatusEnum.BLOCK_FRIEND.getStatus());
            contactService.manageContact(contactDTO);
        }
    }
}