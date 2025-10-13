package com.easychat.group.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.common.entity.dto.GroupInfoDTO;
import com.easychat.common.entity.dto.GroupManageDTO;
import com.easychat.common.entity.po.GroupInfo;
import com.easychat.common.entity.vo.SearchResultVO;

public interface GroupInfoService extends IService<GroupInfo> {

    /**
     * 新增群聊
     * @param groupInfoDTO 群聊信息
     * @return 是否新增成功
     */
    public boolean saveGroup(GroupInfoDTO groupInfoDTO);

    /**
     * 搜索群聊
     * @param groupId 群聊ID
     * @return 群聊详情
     */
    public SearchResultVO searchGroup(String groupId);

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
    public void manageGroupUser(GroupManageDTO manageGroupDTO);

    /**
     * 解散群聊
     * @param groupId 群聊ID
     */
    public void dissolutionGroup(String groupId);

}
