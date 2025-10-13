package com.easychat.contact.kafka;

import com.easychat.common.entity.kafka.GroupInfoMessage;
import com.easychat.common.entity.po.ContactGroupInfo;
import com.easychat.contact.mapper.ContactGroupInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 群组信息变更事件监听器
 * 用于接收来自group-service的群组信息变更事件，并更新contactgroupinfo表
 */
@Slf4j
@Component
public class GroupInfoChangeEventListener {

    @Autowired
    private ContactGroupInfoMapper contactGroupInfoMapper;

    /**
     * 监听群组信息变更主题
     */
    private static final String GROUP_INFO_CHANGE_TOPIC = "group_info_change_topic";


    /**
     * 处理群组信息变更事件
     * @param record Kafka消息记录
     * @param acknowledgment 确认对象
     */
    @KafkaListener(topics = GROUP_INFO_CHANGE_TOPIC, groupId = "contact-service-group")
    public void handleGroupInfoChange(ConsumerRecord<String, GroupInfoMessage> record, Acknowledgment acknowledgment) {
        try {
            GroupInfoMessage event = record.value();
            if (event == null) {
                acknowledgment.acknowledge();
                return;
            }

            log.info("收到消息, event: {}", event);
            // 根据事件类型执行不同的操作
            switch (event.getEventType()) {
                case CREATE:
                    // 创建群聊信息
                    createGroupInfo(event);
                    break;
                case UPDATE:
                    // 更新群聊信息
                    updateGroupInfo(event);
                    break;
                case DISSOLUTION:
                    // 处理群聊解散
                    handleGroupDissolution(event);
                    break;
                case MEMBER_CHANGE:
                case USER_LEAVE:
                    // 成员变更和用户退出事件可以在这里处理，
                    // 根据业务需求决定是否需要更新contactgroupinfo表
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
        } finally {
            // 手动确认消息
            acknowledgment.acknowledge();
        }
    }

    /**
     * 创建群组信息
     */
    private void createGroupInfo(GroupInfoMessage event) {
        ContactGroupInfo contactGroupInfo = new ContactGroupInfo();
        BeanUtils.copyProperties(event, contactGroupInfo);
        contactGroupInfoMapper.insert(contactGroupInfo);
    }

    /**
     * 更新群组信息
     */
    private void updateGroupInfo(GroupInfoMessage event) {
        ContactGroupInfo contactGroupInfo = contactGroupInfoMapper.selectById(event.getGroupId());
        if (contactGroupInfo != null) {
            BeanUtils.copyProperties(event, contactGroupInfo);
            contactGroupInfoMapper.updateById(contactGroupInfo);
        }
    }

    /**
     * 处理群聊解散
     */
    private void handleGroupDissolution(GroupInfoMessage event) {
        // 根据业务需求决定是否删除群组信息或更新状态
        // 这里可以选择删除记录或标记为解散
        contactGroupInfoMapper.deleteById(event.getGroupId());
    }
}