package com.easychat.group.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.api.ContactDubboService;
import com.easychat.common.api.SessionDubboService;
import com.easychat.common.config.FileConfig;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.enums.MessageStatusEnum;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.kafka.GroupInfoMessage;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.common.entity.po.ChatSessionUser;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.CopyTools;
import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.StringTools;
import com.easychat.common.utils.UserContext;
import com.easychat.common.entity.dto.GroupInfoDTO;
import com.easychat.common.entity.dto.GroupManageDTO;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.enums.ContactStatusEnum;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.po.GroupInfo;
import com.easychat.common.entity.vo.SearchResultVO;
import com.easychat.group.mapper.GroupInfoMapper;
import com.easychat.group.service.GroupInfoService;
import com.easychat.group.kafka.KafkaMessageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService {
    
    @Autowired
    private KafkaMessageService kafkaMessageService;

    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private RedisComponet redisComponet;

    @DubboReference(check = false)
    private SessionDubboService sessionDubboService;

    @DubboReference(check = false)
    private ContactDubboService contactDubboService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveGroup(GroupInfoDTO groupInfoDTO) {
        // 1. 群号为空  创建群聊
        if (StringUtils.isBlank(groupInfoDTO.getGroupId())) {
            // 1.1 根据系统设置判断是否达到群聊上限
            Integer groupCount = baseMapper.selectCount(null);
            if (groupCount >= 10) {
                throw new BusinessException(Constants.GROUP_NUM_EXCEED);
            }
            // 1.2 创建群聊
            GroupInfo groupInfo = new GroupInfo();
            BeanUtils.copyProperties(groupInfoDTO, groupInfo);
            groupInfo.setGroupId(StringTools.getGroupId());
            groupInfo.setCreateTime(DateTime.now());
            groupInfo.setStatus(Constants.GROUP_STATUS_NORMAL);
            groupInfo.setMemberCount(0);
            // 发送群组创建事件到Kafka
            GroupInfoMessage event = new GroupInfoMessage();
            event.setEventType(GroupInfoMessage.EventType.CREATE);
            event.setGroupId(groupInfo.getGroupId());
            event.setGroupName(groupInfo.getGroupName());
            event.setGroupOwnerId(groupInfo.getGroupOwnerId());
            event.setGroupNotice(groupInfo.getGroupNotice());
            kafkaMessageService.sendGroupInfoChangeEvent(event);
            baseMapper.insert(groupInfo);
            
            groupInfoDTO.setGroupId(groupInfo.getGroupId());
            
            // 1.3 将自己加入群聊
            manageGroupContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId(), ContactStatusEnum.FRIEND.getStatus());
            redisComponet.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            // 1.4 创建会话
            String sessionId = StringTools.getChatSessionId4Group(groupInfo.getGroupId());
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(DateTime.now().getTime());
            sessionDubboService.addSession(chatSession);

            //创建群主会话
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactType(ContactTypeEnum.GROUP.getStatus());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(sessionId);
            sessionDubboService.addSessionUser(chatSessionUser);

            // 1.5 创建消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendUserId(null);
            chatMessage.setSendUserNickName(null);
            chatMessage.setSendTime(DateTime.now().getTime());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(ContactTypeEnum.GROUP.getStatus());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            // 添加消息到数据库，接收返回的包含自增ID的chatMessage对象
            chatMessage = sessionDubboService.addChatMessage(chatMessage);
            // 1.6 将群聊加入channelGroup
            sessionDubboService.addGroupToChannel(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            // 发送ws消息
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(chatMessage.getSendTime());
            chatSessionUser.setMemberCount(1);

            MessageSendDTO messageSend = CopyTools.copy(chatMessage, MessageSendDTO.class);
            messageSend.setExtendData(chatSessionUser);
            messageSend.setLastMessage(chatSessionUser.getLastMessage());
            kafkaMessageService.sendWsMessage(messageSend);

        }else// 2. 群号不为空 修改群聊信息
        {
            // 2.1 查询群主信息
            GroupInfo groupInfo = baseMapper.selectById(groupInfoDTO.getGroupId());
            if (groupInfo == null) {
                throw new BusinessException(Constants.GROUP_NOT_EXIST);
            }
            // 2.2 比较群主信息
            if (!StringUtils.equals(groupInfo.getGroupOwnerId(), groupInfoDTO.getGroupOwnerId())) {
                throw new BusinessException(Constants.GROUP_OWNER_ERROR);
            }
            // 2.3 更新群聊信息
            BeanUtils.copyProperties(groupInfoDTO, groupInfo);
            baseMapper.updateById(groupInfo);
            
            // 发送群组更新事件到Kafka
            GroupInfoMessage event = new GroupInfoMessage();
            event.setEventType(GroupInfoMessage.EventType.UPDATE);
            event.setGroupId(groupInfo.getGroupId());
            event.setGroupName(groupInfo.getGroupName());
            event.setGroupOwnerId(groupInfo.getGroupOwnerId());
            event.setGroupNotice(groupInfo.getGroupNotice());
            kafkaMessageService.sendGroupInfoChangeEvent(event);
            
            // 2.4 更新会话
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            sessionDubboService.updateSessionUser(chatSessionUser);
            // 发送ws消息
            MessageSendDTO messageSendDto = new MessageSendDTO();
            messageSendDto.setContactType(ContactTypeEnum.GROUP.getStatus());
            messageSendDto.setContactId(groupInfo.getGroupId());
            messageSendDto.setExtendData(groupInfo.getGroupName());
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            kafkaMessageService.sendWsMessage(messageSendDto);



        }
        // 3. 处理头像
        if (groupInfoDTO.getAvatarFile() != null) {
            // 3.1 保存头像到文件夹
            // 获取当前项目根目录
            String projectPath = System.getProperty("user.dir");

            // 构建完整的文件夹路径
            String avatarPath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getAvatarFolder();
            String avatarCoverPath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getAvatarFolder() + File.separator + "cover";

            try {
                File avatarDir = new File(avatarPath);
                // 如果文件夹不存在则创建
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs(); // 递归创建文件夹
                }
                File avatarCoverDir = new File(avatarCoverPath);
                // 如果文件夹不存在则创建
                if (!avatarCoverDir.exists()) {
                    avatarCoverDir.mkdirs(); // 递归创建文件夹
                }

                // 3.1.1 头像
                groupInfoDTO.getAvatarFile().transferTo(new File(avatarPath + File.separator + groupInfoDTO.getGroupId() + Constants.IMAGE_SUFFIX));
                // 3.1.2 群封面
                groupInfoDTO.getAvatarCover().transferTo(new File(avatarCoverPath + File.separator + groupInfoDTO.getGroupId() + Constants.COVER_IMAGE_SUFFIX));

            } catch (IOException e) {
                // 添加详细错误日志
                log.error("头像保存失败");
                throw new BusinessException("头像上传失败: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    public SearchResultVO searchGroup(String groupId) {
        // 1. 查询群聊信息
        GroupInfo groupInfo = baseMapper.selectById(groupId);
        if (groupInfo == null) {
            return null;
        }
        SearchResultVO searchResultVO = new SearchResultVO();
        searchResultVO.setContactId(groupId);
        searchResultVO.setContactType(ContactTypeEnum.GROUP.getName());
        searchResultVO.setNickName(groupInfo.getGroupName());
        // 2. 查询群聊关系
        ContactDTO contactDTO = contactDubboService.getContactInfo(UserContext.getUser(), groupId);
        if (contactDTO != null) {
            searchResultVO.setStatus(contactDTO.getStatus());
            searchResultVO.setStatusName(ContactStatusEnum.getDescByStatus(contactDTO.getStatus()));
        } else {
            searchResultVO.setStatus(ContactStatusEnum.NO_FRIEND.getStatus());
            searchResultVO.setStatusName(ContactStatusEnum.NO_FRIEND.getDesc());
        }
        return searchResultVO;
    }

    @Override
    public GroupInfo loadGroupDetail(String groupId) {
//        // 1. 判断用户是否在群聊中
//        System.out.println(UserContext.getUser());
//        UserContact userContact = userContactMapper.selectOne(
//                new QueryWrapper<UserContact>()
//                        .eq("user_id", UserContext.getUser())
//                        .eq("contact_id", groupId)
//                        .eq("contact_type", ContactTypeEnum.GROUP.getStatus())
//        );
//        if (userContact == null) {
//            throw new BusinessException(Constants.USER_NOT_IN_GROUP);
//        }
        // 2. 查询群聊信息
        GroupInfo groupInfo = baseMapper.selectById(groupId);
        if (groupInfo == null) {
            throw new BusinessException(Constants.GROUP_NOT_EXIST);
        }
//        // 3. 加载群聊成员数量
//        groupInfo.setMemberCount(userContactMapper.selectCount(
//                new QueryWrapper<UserContact>()
//                        .eq("contact_id", groupId)
//                        .eq("contact_type", ContactTypeEnum.GROUP.getStatus())
//        ));
        return groupInfo;
    }

    @Override
    public void manageGroupUser(GroupManageDTO manageGroupDTO) {
        // 1. 判断操作者是否合法
        // 1.1 查询群聊信息
        GroupInfo groupInfo = baseMapper.selectById(manageGroupDTO.getGroupId());
        if (groupInfo == null) {
            throw new BusinessException(Constants.GROUP_NOT_EXIST);
        }
        // 1.2 判断操作者是否为群聊群主
        if (!StringUtils.equals(groupInfo.getGroupOwnerId(), UserContext.getUser())) {
            throw new BusinessException(Constants.GROUP_OWNER_ERROR);
        }
        // 2. 操作
        String[] contactIds = StringUtils.split(manageGroupDTO.getContactIds(), ",");
        // 2.1 操作类型判断
        if (manageGroupDTO.getOpType() == 1) {
            // 2.1.1 添加成员
            for (String contactId : contactIds) {
                manageGroupContact(contactId, manageGroupDTO.getGroupId(), ContactStatusEnum.FRIEND.getStatus());
            }
        } else if (manageGroupDTO.getOpType() == 2) {
            // 2.1.2 移除成员
            for (String contactId : contactIds) {
                manageGroupContact(contactId, manageGroupDTO.getGroupId(), ContactStatusEnum.DEL_FRIEND.getStatus());
            }
        } else {
            throw new BusinessException(Constants.GROUP_OP_TYPE_UNKNOWN);
        }
        
        // 查询更新后的群聊信息
        GroupInfo updatedGroupInfo = baseMapper.selectById(manageGroupDTO.getGroupId());
        
        // 发送群组成员变更事件到Kafka
        GroupInfoMessage event = new GroupInfoMessage();
        event.setEventType(GroupInfoMessage.EventType.MEMBER_CHANGE);
        event.setGroupId(updatedGroupInfo.getGroupId());
        event.setGroupName(updatedGroupInfo.getGroupName());
        event.setGroupOwnerId(updatedGroupInfo.getGroupOwnerId());
        event.setGroupNotice(updatedGroupInfo.getGroupNotice());
        kafkaMessageService.sendGroupInfoChangeEvent(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String groupId) {
        // 1. 判断群聊是否存在
        GroupInfo groupInfo = baseMapper.selectById(groupId);
        if (groupInfo == null) {
            throw new BusinessException(Constants.GROUP_NOT_EXIST);
        }
        // 2. 判断用户是否为群聊群主
        if (!StringUtils.equals(groupInfo.getGroupOwnerId(), UserContext.getUser())) {
            throw new BusinessException(Constants.GROUP_OWNER_ERROR);
        }
        // 3. 解散群聊 标记状态为解散
        groupInfo.setStatus(Constants.GROUP_STATUS_DISSOLUTION);
        baseMapper.updateById(groupInfo);

        // 4. TODO 删除群聊成员
        // 5. TODO 删除会话
        // 6. TODO 通知成员解散
    }

    /**
     * 加入群聊
     * @param userId
     * @param groupId
     */
    public void manageGroupContact(@NotNull String userId, @NotNull String groupId, @NotNull Integer contactType) {
        // 1. 创建关系
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setUserId(userId);
        contactDTO.setContactId(groupId);
        contactDTO.setContactType(contactType);
        contactDTO.setCreateTime(DateTime.now());
        contactDTO.setLastUpdateTime(DateTime.now());
        contactDTO.setStatus(ContactStatusEnum.FRIEND.getStatus());
        contactDubboService.createContact(contactDTO);
        // 2. 更新群聊成员数量
        if (contactType == ContactStatusEnum.DEL_FRIEND.getStatus()) {
            // 2.1.2 移除成员
            baseMapper.update(null, new LambdaUpdateWrapper<GroupInfo>()
                    .eq(GroupInfo::getGroupId, groupId)
                    .setSql("member_count = member_count - 1"));
        } else {
            // 2.1.1 添加成员
            baseMapper.update(null, new LambdaUpdateWrapper<GroupInfo>()
                    .eq(GroupInfo::getGroupId, groupId)
                    .setSql("member_count = member_count + 1"));
        }
    }


}