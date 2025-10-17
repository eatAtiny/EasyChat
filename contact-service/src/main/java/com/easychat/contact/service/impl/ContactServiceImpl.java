package com.easychat.contact.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.api.SessionDubboService;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.enums.MessageStatusEnum;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.common.entity.po.ChatSessionUser;
import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.UserContext;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.enums.ContactStatusEnum;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.po.Contact;
import com.easychat.contact.mapper.ContactMapper;
import com.easychat.contact.service.ContactService;
import org.apache.catalina.User;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements ContactService {

    @DubboReference(check = false)
    private SessionDubboService sessionDubboService;

    private final RedisComponet redisComponet;

    public ContactServiceImpl(RedisComponet redisComponet) {
        this.redisComponet = redisComponet;
    }

    /**
     * 添加/拉黑好友|加入/拉黑群聊
     * @param contactDTO 添加好友DTO
     */
    public void manageContact(ContactDTO contactDTO) {
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
            // 使用条件更新，因为Contact有两个主键(userId和contactId)
            baseMapper.update(contact, new LambdaQueryWrapper<Contact>()
                    .eq(Contact::getUserId, contact.getUserId())
                    .eq(Contact::getContactId, contact.getContactId()));
        }

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

    /**
     * 删除联系人
     * @param contactId 联系人ID或者群组ID
     */
    @Override
    public void deleteContact(String contactId) {
        // 1. 调用manageContact方法删除联系人
        manageContact(new ContactDTO()
                .setUserId(UserContext.getUser())
                        .setContactId(contactId)
                        .setContactType(ContactTypeEnum.USER.getStatus())
                        .setStatus(ContactStatusEnum.DEL_FRIEND.getStatus()));
        redisComponet.removeUserContact(UserContext.getUser(), contactId);
        // 2. 将对方与自己的关系标记为被删除
        manageContact(new ContactDTO()
                .setUserId(contactId)
                        .setContactId(UserContext.getUser())
                        .setContactType(ContactTypeEnum.USER.getStatus())
                        .setStatus(ContactStatusEnum.DEL_BY_FRIEND.getStatus()));
        redisComponet.removeUserContact(contactId, UserContext.getUser());
    }

     /**
      * 拉黑联系人
      * @param contactId 联系人ID或者群组ID
      */
     @Override
     public void blacklistContact(String contactId) {
         // 1. 调用manageContact方法拉黑联系人
         manageContact(new ContactDTO()
                        .setUserId(UserContext.getUser())
                        .setContactId(contactId)
                        .setContactType(ContactTypeEnum.USER.getStatus())
                        .setStatus(ContactStatusEnum.BLOCK_FRIEND.getStatus()));
         redisComponet.removeUserContact(UserContext.getUser(), contactId);
         // 2. 将对方标记为被拉黑
         manageContact(new ContactDTO()
                        .setUserId(contactId)
                        .setContactId(UserContext.getUser())
                        .setContactType(ContactTypeEnum.USER.getStatus())
                        .setStatus(ContactStatusEnum.BLOCK_BY_FRIEND.getStatus()));
         redisComponet.removeUserContact(contactId, UserContext.getUser());
     }

      /**
       * 退出群聊
       * @param groupId 群组ID
       */
     @Override
     public void exitGroup(String groupId) {
         // 1. 调用manageContact方法退出群聊
         manageContact(new ContactDTO()
                        .setUserId(UserContext.getUser())
                        .setContactId(groupId)
                        .setContactType(ContactTypeEnum.GROUP.getStatus())
                        .setStatus(ContactStatusEnum.DEL_FRIEND.getStatus()));
         redisComponet.removeUserContact(UserContext.getUser(), groupId);
     }

    /**
     * 添加机器人为好友
     */
     @Override
     public void addRobotFriend(String Userid) {
         // 1. 调用manageContact方法添加机器人为好友
         manageContact(new ContactDTO()
                        .setUserId(Userid)
                        .setContactId(Constants.ROBOT_ID)
                        .setContactType(ContactTypeEnum.USER.getStatus())
                        .setStatus(ContactStatusEnum.FRIEND.getStatus()));
         // 2. 添加会话，调用远程方法
         String sessionId = Constants.ROBOT_ID + UserContext.getUser();
         ChatSession chatSession = new ChatSession();
         chatSession.setSessionId(sessionId);
         chatSession.setLastMessage("欢迎使用EasyChat");
         chatSession.setLastReceiveTime(DateTime.now().getTime());
         sessionDubboService.addSession(chatSession);

         // 3. 添加会话用户，调用远程方法
         ChatSessionUser chatSessionUser = new ChatSessionUser();
         chatSessionUser.setSessionId(sessionId);
         chatSessionUser.setUserId(Userid);
         chatSessionUser.setContactId(Constants.ROBOT_ID);
         chatSessionUser.setContactName(Constants.ROBOT_NAME);
         sessionDubboService.addSessionUser(chatSessionUser);

         // 3. 添加消息，调用远程方法
         ChatMessage chatMessage = new ChatMessage();
         chatMessage.setSessionId(sessionId);
         chatMessage.setSendUserId(Constants.ROBOT_ID);
         chatMessage.setSendUserNickName(Constants.ROBOT_NAME);
         chatMessage.setContactId(Userid);
         chatMessage.setContactType(ContactTypeEnum.USER.getStatus());
         chatMessage.setMessageContent("欢迎使用EasyChat");
         chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
         chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
         chatMessage.setSendTime(DateTime.now().getTime());
         // 添加消息到数据库，接收返回的包含自增ID的chatMessage对象
         chatMessage = sessionDubboService.addChatMessage(chatMessage);
     }
}