package com.easychat.chat.netty;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easychat.chat.mapper.ChatMessageMapper;
import com.easychat.chat.mapper.ChatSessionMapper;
import com.easychat.chat.mapper.ChatSessionUserMapper;
import com.easychat.common.api.ContactDubboService;
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
import com.easychat.common.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component("channelContextUtils")
@Slf4j
public class ChannelContextUtils {


    @Resource
    private RedisComponet redisComponet;

    public static final ConcurrentMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap();

    public static final ConcurrentMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap();

    @DubboReference(check = false)
    private UserInfoDubboService userInfoDubboService;

    @DubboReference(check = false)
    private ContactDubboService contactDubboService;

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

            //给用户发送一些消息
            //获取用户最后离线时间
            Long sourceLastOffTime = userInfoDubboService.getUserLastOffTime(userId);
            System.out.println("sourceLastOffTime = " + sourceLastOffTime);
            //这里避免毫秒时间差，所以减去1秒的时间
            //如果时间太久，只取最近三天的消息数
            Long lastOffTime = sourceLastOffTime;
            if (sourceLastOffTime != null && System.currentTimeMillis() - Constants.MILLISECOND_3DAYS_AGO > sourceLastOffTime) {
                lastOffTime = System.currentTimeMillis() - Constants.MILLISECOND_3DAYS_AGO;
            }
            System.out.println("lastOffTime = " + lastOffTime);
            /**
             * 1、查询会话信息 查询用户所有会话，避免换设备会话不同步
             */
            List<ChatSessionUser> chatSessionList = chatSessionUserMapper.selectListByUserId(userId);
            WsInitDataDTO wsInitDataDTO = new WsInitDataDTO();
            wsInitDataDTO.setChatSessionList(chatSessionList);

            /**
             * 2、查询聊天消息
             */
            // 将用户所在群组，和接收对象为本人的消息，查询出来
            // 获取群组Id
            List<String> groupIdList = contactDubboService.getGroupIdList(userId);
            //将自己也加进去
            groupIdList.add(userId);
            //查询用户的聊天消息
            List<ChatMessage> chatMessageList = chatMessageMapper.selectList(
                    new LambdaQueryWrapper<ChatMessage>()
                            .in(ChatMessage::getContactId, groupIdList)
                            .gt(ChatMessage::getSendTime, lastOffTime)
                            .orderByDesc(ChatMessage::getSendTime)
            );
            wsInitDataDTO.setChatMessageList(chatMessageList);
            /**
             * 3、查询好友申请
             */
            int applyCount = contactDubboService.countFriendApply(userId, lastOffTime);
            wsInitDataDTO.setApplyCount(applyCount);
            //发送消息
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setMessageType(MessageTypeEnum.INIT.getType());
            messageSendDTO.setContactId(userId);
            messageSendDTO.setExtendData(wsInitDataDTO);

            sendMsg(messageSendDTO, userId);
        } catch (Exception e) {
            log.error("初始化链接失败", e);
        }
    }

    /**
     * 删除通道连接异常
     *
     * @param channel
     */
    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (!StringTools.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponet.removeUserHeartBeat(userId);

        //更新用户最后断线时间
        userInfoDubboService.updateUserLastOffTime(userId, System.currentTimeMillis());
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
            log.info("群聊{}不存在，消息发送失败", messageSendDTO.getContactId());
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


    private void sendMsg(MessageSendDTO messageSendDTO, String reciveId) {
        if (reciveId == null) {
            return;
        }
        Channel sendChannel = USER_CONTEXT_MAP.get(reciveId);
        if (sendChannel == null) {
            return;
        }
//        相当于客户而言，联系人就是发送人，所以这里转换一下再发送,好友打招呼信息发送给自己需要特殊处理
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDTO.getMessageType())) {
            Object extendData = messageSendDTO.getExtendData();
            String userId = null;
            String nickName = null;

            if (extendData instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) extendData;
                userId = (String) map.get("userId");
                nickName = (String) map.get("nickName");
            }
            if (userId != null && nickName != null) {
                messageSendDTO.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
                messageSendDTO.setContactId(userId);
                messageSendDTO.setContactName(nickName);
                messageSendDTO.setExtendData(null);
            } else {
                log.warn("无法从extendData中提取userId和nickName: {}", extendData);
                return;
            }
        } else {
            messageSendDTO.setContactId(messageSendDTO.getSendUserId());
            messageSendDTO.setContactName(messageSendDTO.getSendUserNickName());
        }
        log.info("发送消息给用户{}: {}", reciveId, messageSendDTO);
        sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDTO)));
    }

    private void addToGroup(String groupId, Channel context) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (context == null) {
            log.warn("添加用户到群聊失败，用户不存在");
            return;
        }
        group.add(context);
    }

    public void addUserToGroup(String userId, String groupId) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            log.warn("添加用户到群聊失败，用户{}不存在", userId);
            return;
        }
        addToGroup(groupId, channel);
    }
}