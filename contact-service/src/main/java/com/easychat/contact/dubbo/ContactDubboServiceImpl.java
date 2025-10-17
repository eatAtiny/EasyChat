package com.easychat.contact.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easychat.common.api.ContactDubboService;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.enums.ContactApplyStatusEnum;
import com.easychat.common.entity.po.Contact;
import com.easychat.common.entity.po.ContactApply;
import com.easychat.common.utils.RedisComponet;
import com.easychat.contact.mapper.ContactApplyMapper;
import com.easychat.contact.mapper.ContactMapper;
import com.easychat.contact.service.ContactService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@DubboService
@Service
public class ContactDubboServiceImpl implements ContactDubboService {

    @Autowired
    private ContactMapper contactMapper;

    @Autowired
    private ContactApplyMapper contactApplyMapper;

    @Autowired
    private RedisComponet redisComponet;
    @Autowired
    private ContactService contactService;

    @Override
    public void addContactsToRedis(String userId) {
        // 1. 从数据库中获取用户联系人/群聊
        List<Contact> contactList = contactMapper.selectList(
                new QueryWrapper<Contact>()
                        .eq("user_id", userId)
                        .eq("status", 1)
        );
        // 2. 将联系人/群聊添加到redis中
        redisComponet.cleanUserContact(userId);
        if (!contactList.isEmpty()) {
            redisComponet.addUserContactBatch(userId, contactList.stream().map(Contact::getContactId).collect(Collectors.toList()));
        }
    }

    @Override
    public ContactDTO getContactInfo(String userId, String contactId) {
        ContactDTO contactDTO = new ContactDTO();
        Contact contact = contactMapper.selectOne(
                new QueryWrapper<Contact>()
                        .eq("user_id", userId)
                        .eq("contact_id", contactId)
        );
        if (contact == null) {
            return null;
        }
        BeanUtils.copyProperties(contact, contactDTO);
        return contactDTO;
    }

    @Override
    public void createContact(ContactDTO contactDTO) {
        Contact contact = new Contact();
        BeanUtils.copyProperties(contactDTO, contact);
        contactMapper.insert(contact);
    }

    @Override
    public List<String> getGroupIdList(String userId) {
        // 1. 从数据库中获取用户所在群组列表
        List<Contact> contactList = contactMapper.selectList(
                new QueryWrapper<Contact>()
                        .eq("user_id", userId)
                        .eq("contact_type", 1)
                        .eq("status", 1)
        );
        // 2. 返回群组ID列表
        return contactList.stream().map(Contact::getContactId).collect(Collectors.toList());
    }

    @Override
    public int countFriendApply(String userId, Long timestamp) {
        // 1. 从数据库中获取用户好友申请数量
        QueryWrapper<ContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receive_user_id", userId)
                .eq("status", ContactApplyStatusEnum.PENDING.getStatus());
        if (timestamp != -1) {
            queryWrapper.gt("last_apply_time", timestamp);
        }
        return contactApplyMapper.selectCount(queryWrapper);
    }

    @Override
    public void addRobotFriend(String userId) {
        contactService.addRobotFriend(userId);
    }
}
