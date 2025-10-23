package com.easychat.common.api;

import com.easychat.common.entity.dto.ContactDTO;

import java.util.List;

public interface ContactDubboService {
    /**
     * 将用户联系人/群聊添加到redis中
     * @param userId 用户ID
     */
     void addContactsToRedis(String userId);

    /**
     * 获取contact关系
     * @param userId 用户ID
     * @param contactId 联系人ID
     * @return contact关系
     */
     ContactDTO getContactInfo(String userId, String contactId);

    /**
     * 创建contact关系
     * @param contactDTO contact关系
     */
     void createContact(ContactDTO contactDTO);

    /**
     * 获取用户所在群组列表
     * @param userId 用户ID
     * @return 群组列表
     */
    List<String> getGroupIdList(String userId);

    /**
     * 查询一定范围内待处理好友申请数量
     * @param userId 用户ID
     * @param timestamp 起始时间 若为-1，则查询所有好友申请
     * @return 好友申请数量
     */
     int countFriendApply(String userId, Long timestamp);

     /**
     * 添加机器人好友
     * @param userId 用户ID
     */
     void addRobotFriend(String userId);

     /**
     * 解散群聊
     * @param groupId 群聊ID
     */
     void dissolutionGroup(String groupId);
}
