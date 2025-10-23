package com.easychat.group.dubbo;

import com.alibaba.nacos.shaded.org.checkerframework.checker.units.qual.A;
import com.easychat.common.api.GroupInfoDubboService;
import com.easychat.common.entity.dto.GroupInfoDTO;
import com.easychat.common.entity.po.GroupInfo;
import com.easychat.common.utils.UserContext;
import com.easychat.group.mapper.GroupInfoMapper;
import com.easychat.group.service.GroupInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class GroupInfoDubboServiceImpl implements GroupInfoDubboService {

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Autowired
    private GroupInfoService groupInfoService;

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

    @Override
    public void dissolutionGroup(String groupId) {
        System.out.println(UserContext.getUserFromDubbo());
        groupInfoService.dissolutionGroup(groupId);
    }
}