package com.easychat.group.dubbo;

import com.easychat.common.api.GroupInfoDubboService;
import com.easychat.common.entity.dto.GroupInfoDTO;
import com.easychat.common.entity.po.GroupInfo;
import com.easychat.group.mapper.GroupInfoMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@DubboService
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

    @Override
    public GroupInfoDTO getGroupInfo(String groupId) {
        GroupInfoDTO groupInfoDTO = new GroupInfoDTO();
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo != null) {
            BeanUtils.copyProperties(groupInfo, groupInfoDTO);
        }
        return groupInfoDTO;
    }
}