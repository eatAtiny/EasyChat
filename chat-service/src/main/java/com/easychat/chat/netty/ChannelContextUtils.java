package com.easychat.chat.netty;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easychat.chat.mapper.ChatMessageMapper;
import com.easychat.chat.mapper.ChatSessionMapper;
import com.easychat.chat.mapper.ChatSessionUserMapper;
import com.easychat.common.api.UserInfoDubboService;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.dto.WsInitDataDTO;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.po.ChatSessionUser;
import com.easychat.common.entity.po.UserInfo;
import com.easychat.common.utils.JsonUtils;
import com.easychat.common.utils.RedisComponet;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("channelContextUtils")
public class ChannelContextUtils {

    private static final Logger logger = LoggerFactory.getLogger(ChannelContextUtils.class);

    @Resource
    private RedisComponet redisComponet;

    public static final ConcurrentMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap();

    public static final ConcurrentMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap();

    @DubboReference(check = false)
    private UserInfoDubboService userInfoDubboService;
    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;



    /**
     * 加入通道
     *
     * @param userId
     * @param channel
     */
    public void addContext(String userId, Channel channel) {
        try {
            String channelId = channel.id().toString();
            AttributeKey attributeKey = null;
            if (!AttributeKey.exists(channelId)) {
                attributeKey = AttributeKey.newInstance(channel.id().toString());
            } else {
                attributeKey = AttributeKey.valueOf(channel.id().toString());
            }
            channel.attr(attributeKey).set(userId);

            List<String> contactList = redisComponet.getUserContactList(userId);
            for (String groupId : contactList) {
                if (groupId.startsWith(String.valueOf(ContactTypeEnum.GROUP.getPrefix()))) {
                    addToGroup(groupId, channel);
                }
            }
            USER_CONTEXT_MAP.put(userId, channel);
            redisComponet.saveUserHeartBeat(userId);

            //更新用户最后连接时间
            userInfoDubboService.updateUserLastLoginTime(userId, System.currentTimeMillis());

            //给用户发送一些消息
            //获取用户最后离线时间
            Long sourceLastOffTime = userInfoDubboService.getUserLastOffTime(userId);
            //这里避免毫秒时间差，所以减去1秒的时间
            //如果时间太久，只取最近三天的消息数
            Long lastOffTime = sourceLastOffTime;
            if (sourceLastOffTime != null && System.currentTimeMillis() - Constants.MILLISECOND_3DAYS_AGO > sourceLastOffTime) {
                lastOffTime = System.currentTimeMillis() - Constants.MILLISECOND_3DAYS_AGO;
            }

            /**
             * 1、查询会话信息 查询用户所有会话，避免换设备会话不同步
             */
//            ChatSessionUserQuery sessionUserQuery = new ChatSessionUserQuery();
//            sessionUserQuery.setUserId(userId);
//            sessionUserQuery.setOrderBy("last_receive_time desc");
//            List<ChatSessionUser> chatSessionList = chatSessionUserMapper.selectList(sessionUserQuery);

            List<ChatSessionUser> chatSessionList = chatSessionUserMapper.selectList(
                    new LambdaQueryWrapper<ChatSessionUser>()
                            .eq(ChatSessionUser::getUserId, userId)
                            .orderByDesc(ChatSessionUser::getLastReceiveTime)
            );
            WsInitDataDTO wsInitDataDTO = new WsInitDataDTO();
            wsInitDataDTO.setChatSessionList(chatSessionList);

            /**
             * 2、查询聊天消息
             */
//            //查询用户的联系人
//            UserContactQuery contactQuery = new UserContactQuery();
//            contactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
//            contactQuery.setUserId(userId);
//            List<UserContact> groupContactList = userContactMapper.selectList(contactQuery);
//            List<String> groupIdList = groupContactList.stream().map(item -> item.getContactId()).collect(Collectors.toList());
//            //将自己也加进去
//            groupIdList.add(userId);

            // TODO 这里查询所有会话的消息，可能需要联合查询会话用户表，或者修改会话表
//            List<ChatMessage> chatMessageList = chatMessageMapper.selectList(
//                    new LambdaQueryWrapper<ChatMessage>()
//                            .in(ChatMessage::getContactId, groupIdList)
//                            .orderByDesc(ChatMessage::getCreateTime)
//            );
//
//            ChatMessageQuery messageQuery = new ChatMessageQuery();
//            messageQuery.setContactIdList(groupIdList);
//            messageQuery.setLastReceiveTime(lastOffTime);
//            List<ChatMessage> chatMessageList = chatMessageMapper.selectList(messageQuery);
//            wsInitData.setChatMessageList(chatMessageList);
            wsInitDataDTO.setChatMessageList(new ArrayList<>());
//
//            /**
//             * 3、查询好友申请
//             */
//            UserContactApplyQuery applyQuery = new UserContactApplyQuery();
//            applyQuery.setReceiveUserId(userId);
//            applyQuery.setLastApplyTimestamp(sourceLastOffTime);
//            applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
//            Integer applyCount = userContactApplyMapper.selectCount(applyQuery);
//            wsInitData.setApplyCount(applyCount);
//
            wsInitDataDTO.setApplyCount(0);
            //发送消息
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setMessageType(MessageTypeEnum.INIT.getType());
            messageSendDTO.setContactId(userId);
            messageSendDTO.setExtendData(wsInitDataDTO);

            sendMsg(messageSendDTO, userId);
        } catch (Exception e) {
            logger.error("初始化链接失败", e);
        }
    }

    /**
     * 删除通道连接异常
     *
     * @param channel
     */
    public void removeContext(Channel channel) {
//        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
//        String userId = attribute.get();
//        if (!StringTools.isEmpty(userId)) {
//            USER_CONTEXT_MAP.remove(userId);
//        }
//        redisComponet.removeUserHeartBeat(userId);
//
//        //更新用户最后断线时间
//        UserInfo userInfo = new UserInfo();
//        userInfo.setLastOffTime(System.currentTimeMillis());
//        userInfoMapper.updateByUserId(userInfo, userId);
    }

    public void closeContext(String userId) {
        if (BeanUtil.isEmpty(userId)) {
            return;
        }
        redisComponet.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        USER_CONTEXT_MAP.remove(userId);
        if (channel != null) {
            channel.close();
        }
    }

    public void sendMessage(MessageSendDTO messageSendDTO) {
        ContactTypeEnum contactTypeEnum = ContactTypeEnum.getByPrefix(messageSendDTO.getContactId());
        switch (contactTypeEnum) {
            case USER:
                sendToUser(messageSendDTO);
                break;
            case GROUP:
                sendToGroup(messageSendDTO);
        }
    }

    /**
     * 发送消息给用户
     */
    private void sendToUser(MessageSendDTO messageSendDTO) {
        String contactId = messageSendDTO.getContactId();
        sendMsg(messageSendDTO, contactId);
        //强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDTO.getMessageType())) {
            closeContext(contactId);
        }
    }

    /**
     * 发送消息到组
     */
    private void sendToGroup(MessageSendDTO messageSendDTO) {
        if (messageSendDTO.getContactId() == null) {
            return;
        }

        ChannelGroup group = GROUP_CONTEXT_MAP.get(messageSendDTO.getContactId());
        if (group == null) {
            return;
        }
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDTO)));

        //移除群聊
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDTO.getMessageType());
        if (MessageTypeEnum.LEAVE_GROUP == messageTypeEnum || MessageTypeEnum.REMOVE_GROUP == messageTypeEnum) {
            String userId = (String) messageSendDTO.getExtendData();
            redisComponet.removeUserContact(userId, messageSendDTO.getContactId());
            Channel channel = USER_CONTEXT_MAP.get(userId);
            if (channel == null) {
                return;
            }
            group.remove(channel);
        }

        if (MessageTypeEnum.DISSOLUTION_GROUP == messageTypeEnum) {
            GROUP_CONTEXT_MAP.remove(messageSendDTO.getContactId());
            group.close();
        }
    }


    private static void sendMsg(MessageSendDTO messageSendDTO, String reciveId) {
        if (reciveId == null) {
            return;
        }
        Channel sendChannel = USER_CONTEXT_MAP.get(reciveId);
        if (sendChannel == null) {
            return;
        }
        //相当于客户而言，联系人就是发送人，所以这里转换一下再发送,好友打招呼信息发送给自己需要特殊处理
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDTO.getMessageType())) {
            UserInfo userInfo = (UserInfo) messageSendDTO.getExtendData();
            messageSendDTO.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDTO.setContactId(userInfo.getUserId());
            messageSendDTO.setContactName(userInfo.getNickName());
            messageSendDTO.setExtendData(null);
        } else {
            messageSendDTO.setContactId(messageSendDTO.getSendUserId());
            messageSendDTO.setContactName(messageSendDTO.getSendUserNickName());
        }
        sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDTO)));
    }

    private void addToGroup(String groupId, Channel context) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (context == null) {
            return;
        }
        group.add(context);
    }

    public void addUserToGroup(String userId, String groupId) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        addToGroup(groupId, channel);
    }
}
