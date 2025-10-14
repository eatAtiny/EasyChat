package com.easychat.group.dubbo;

import com.easychat.common.api.GroupInfoDubboService;
import com.easychat.common.entity.po.GroupInfo;
import com.easychat.group.mapper.GroupInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupInfoDubboServiceImpl implements GroupInfoDubboService {

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Override
    public void addGroupMemberCount(String groupId, Integer count) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        groupInfo.setMemberCount(groupInfo.getMemberCount() + count);
        groupInfoMapper.updateById(groupInfo);
    }
}