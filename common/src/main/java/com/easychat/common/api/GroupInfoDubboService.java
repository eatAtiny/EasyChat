package com.easychat.common.api;

public interface GroupInfoDubboService {
    /**
     * 增加群员数量
     * @param groupId 群组ID
     * @param count 增加的群员数量
     */
    void addGroupMemberCount(String groupId, Integer count);
}
