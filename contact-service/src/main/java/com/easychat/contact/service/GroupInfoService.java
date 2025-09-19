package com.easychat.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.contact.entity.dto.GroupInfoDTO;
import com.easychat.contact.entity.dto.ManageGroupDTO;
import com.easychat.contact.entity.po.GroupInfo;

public interface GroupInfoService extends IService<GroupInfo> {

    /**
     * 新增群聊
     * @param groupInfoDTO 群聊信息
     * @return 是否新增成功
     */
    public boolean saveGroup(GroupInfoDTO groupInfoDTO);

    /**
     * 加载群聊详情
     * @param groupId 群聊ID
     * @return 群聊详情
     */
    public GroupInfo loadGroupDetail(String groupId);

    /**
     * 管理群员
     * @param manageGroupDTO 管理群聊DTO
     */
    public void manageGroupUser(ManageGroupDTO manageGroupDTO);

    /**
     * 退出群聊
     * @param manageGroupDTO 退出群聊DTO
     */
    public void leaveGroup(ManageGroupDTO manageGroupDTO);

    /**
     * 解散群聊
     * @param groupId 群聊ID
     */
    public void dissolutionGroup(String groupId);

}
