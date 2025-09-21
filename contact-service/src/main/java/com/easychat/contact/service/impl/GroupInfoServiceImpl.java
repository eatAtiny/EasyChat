package com.easychat.contact.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.StringTools;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.constant.Constants;
import com.easychat.contact.entity.dto.GroupInfoDTO;
import com.easychat.contact.entity.dto.ManageGroupDTO;
import com.easychat.contact.entity.enums.ContactTypeEnum;
import com.easychat.contact.entity.po.GroupInfo;
import com.easychat.contact.entity.po.UserContact;
import com.easychat.contact.mapper.GroupInfoMapper;
import com.easychat.contact.mapper.UserContactMapper;
import com.easychat.contact.service.GroupInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService {

    @Autowired
    private UserContactMapper userContactMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveGroup(GroupInfoDTO groupInfoDTO) {
        // 1. 群号为空  创建群聊
        if (StringUtils.isBlank(groupInfoDTO.getGroupId())) {
            // 1.1 根据系统设置判断是否达到群聊上限
            Integer groupCount = baseMapper.selectCount(null);
            if (groupCount >= 10) {
                throw new BusinessException(Constants.GROUP_NUM_EXCEED);
            }
            // 1.2 创建群聊
            GroupInfo groupInfo = new GroupInfo();
            BeanUtils.copyProperties(groupInfoDTO, groupInfo);
            groupInfo.setGroupId(StringTools.getGroupId());
            groupInfo.setCreateTime(DateTime.now());
            groupInfo.setStatus(Constants.GROUP_STATUS_NORMAL);
            baseMapper.insert(groupInfo);
            // 1.3 将自己加入群聊
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfoDTO.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setContactType(ContactTypeEnum.GROUP.getStatus());
            userContact.setCreateTime(DateTime.now());
            userContact.setLastUpdateTime(DateTime.now());
//            userContact.setStatus(UserContact.STATUS_FRIEND);
            userContact.setContactName(groupInfoDTO.getGroupName());
            userContactMapper.insert(userContact);
            // 1.4 TODO 创建会话
            // 1.5 TODO 创建消息



        }else// 2. 群号不为空 修改群聊信息
        {
            // 2.1 查询群主信息
            GroupInfo groupInfo = baseMapper.selectById(groupInfoDTO.getGroupId());
            if (groupInfo == null) {
                throw new BusinessException(Constants.GROUP_NOT_EXIST);
            }
            // 2.2 比较群主信息
            if (!StringUtils.equals(groupInfo.getGroupOwnerId(), groupInfoDTO.getGroupOwnerId())) {
                throw new BusinessException(Constants.GROUP_OWNER_ERROR);
            }
            // 2.3 更新群聊信息
            BeanUtils.copyProperties(groupInfoDTO, groupInfo);
            baseMapper.updateById(groupInfo);

            // 2.4 TODO 更新会话

        }
        // 3. 处理头像
        if (groupInfoDTO.getAvatarFile() != null) {
            // 3.1 保存头像到文件夹
            try {
                // 确保目录存在
                File avatarDir = new File(Constants.AVATAR_PATH);
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs();
                }
                File coverDir = new File(Constants.AVATAR_COVER_PATH);
                if (!coverDir.exists()) {
                    coverDir.mkdirs();
                }
                // 3.1.1 头像
                groupInfoDTO.getAvatarFile().transferTo(new File(Constants.AVATAR_PATH + groupInfoDTO.getGroupOwnerId() + Constants.AVATAR_SUFFIX));
                // 3.1.2 群封面
                groupInfoDTO.getAvatarCover().transferTo(new File(Constants.AVATAR_COVER_PATH + groupInfoDTO.getGroupOwnerId() + Constants.AVATAR_COVER_SUFFIX));
            } catch (IOException e) {
                throw new BusinessException("头像上传失败");
            }
        }

        return true;
    }

    @Override
    public GroupInfo loadGroupDetail(String groupId) {
        // 1. 判断用户是否在群聊中
        System.out.println(UserContext.getUser());
        UserContact userContact = userContactMapper.selectOne(
                new QueryWrapper<UserContact>()
                        .eq("user_id", UserContext.getUser())
                        .eq("contact_id", groupId)
                        .eq("contact_type", ContactTypeEnum.GROUP.getStatus())
        );
        if (userContact == null) {
            throw new BusinessException(Constants.USER_NOT_IN_GROUP);
        }
        // 2. 查询群聊信息
        GroupInfo groupInfo = baseMapper.selectById(groupId);
        if (groupInfo == null) {
            throw new BusinessException(Constants.GROUP_NOT_EXIST);
        }
        // 3. 加载群聊成员数量
        groupInfo.setMemberCount(userContactMapper.selectCount(
                new QueryWrapper<UserContact>()
                        .eq("contact_id", groupId)
                        .eq("contact_type", ContactTypeEnum.GROUP.getStatus())
        ));
        return groupInfo;
    }

    @Override
    public void manageGroupUser(ManageGroupDTO manageGroupDTO) {
        // 1. 判断操作者是否合法
        // 1.1 查询群聊信息
        GroupInfo groupInfo = baseMapper.selectById(manageGroupDTO.getGroupId());
        if (groupInfo == null) {
            throw new BusinessException(Constants.GROUP_NOT_EXIST);
        }
        // 1.2 判断操作者是否为群聊群主
        if (!StringUtils.equals(groupInfo.getGroupOwnerId(), UserContext.getUser())) {
            throw new BusinessException(Constants.GROUP_OWNER_ERROR);
        }
        // 2. 操作
        String[] contactIds = StringUtils.split(manageGroupDTO.getContactIds(), ",");
        // 2.1 操作类型判断
        if (manageGroupDTO.getOpType() == 1) {
            // 2.1.1 添加成员
            for (String contactId : contactIds) {
                // TODO
            }

        } else if (manageGroupDTO.getOpType() == 2) {
            // 2.1.2 移除成员
            for (String contactId : contactIds) {
                leaveGroup(new ManageGroupDTO() {{
                    setGroupId(manageGroupDTO.getGroupId());
                    setContactIds(contactId);
                }});
            }
        } else {
            throw new BusinessException(Constants.GROUP_OP_TYPE_UNKNOWN);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(ManageGroupDTO manageGroupDTO) {
        // 1. 判断群聊是否存在
        GroupInfo groupInfo = baseMapper.selectById(manageGroupDTO.getGroupId());
        if (groupInfo == null) {
            return ;
        }
        // 2. 判断用户是否为群聊群主
        if (StringUtils.equals(groupInfo.getGroupOwnerId(), manageGroupDTO.getContactIds())) {
            throw new BusinessException(Constants.GROUP_OWNER_CANT_LEAVE);
        }
        // 3. 移除用户
        userContactMapper.delete(
                new QueryWrapper<UserContact>()
                        .eq("user_id", manageGroupDTO.getContactIds())
                        .eq("contact_id", groupInfo.getGroupId())
                        .eq("contact_type", ContactTypeEnum.GROUP.getStatus())
        );
        // 4. TODO 通知成员退出
        // 5. TODO 更新会话消息
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String groupId) {
        // 1. 判断群聊是否存在
        GroupInfo groupInfo = baseMapper.selectById(groupId);
        if (groupInfo == null) {
            throw new BusinessException(Constants.GROUP_NOT_EXIST);
        }
        // 2. 判断用户是否为群聊群主
        if (!StringUtils.equals(groupInfo.getGroupOwnerId(), UserContext.getUser())) {
            throw new BusinessException(Constants.GROUP_OWNER_ERROR);
        }
        // 3. 解散群聊 标记状态为解散
        groupInfo.setStatus(Constants.GROUP_STATUS_DISSOLUTION);
        baseMapper.updateById(groupInfo);

        // 4. TODO 删除群聊成员
        // 5. TODO 删除会话
        // 6. TODO 通知成员解散
    }


}
