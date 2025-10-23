package com.easychat.common.api;

import com.easychat.common.entity.dto.GroupInfoDTO;

public interface GroupInfoDubboService {
    /**
     * 增加群员数量
     * @param groupId 群组ID
     * @param count 增加的群员数量
     */
    void addGroupMemberCount(String groupId, Integer count);

    /**
     * 获取群组信息
     * @param groupId 群组ID
     * @return 群组信息
     */
    GroupInfoDTO getGroupInfo(String groupId);

    /**
     * 解散群组
     * @param groupId 群组ID
     */
    void dissolutionGroup(String groupId);
}
