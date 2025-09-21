package com.easychat.contact.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.entity.dto.GroupInfoDTO;
import com.easychat.contact.entity.dto.ManageGroupDTO;
import com.easychat.contact.entity.po.GroupInfo;
import com.easychat.contact.service.GroupInfoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/group")
@Api(tags = "群聊接口")
@Slf4j
public class GroupController extends BaseController {
    @Autowired
    private GroupInfoService groupInfoService;


    /**
     * 创建群聊
     * @param groupInfoDTO 群聊信息
     * @return 是否创建成功
     */
    @PostMapping("/saveGroup")
    public ResponseVO saveGroup(GroupInfoDTO groupInfoDTO) {
        log.info("创建群聊, groupInfoDTO: {}", groupInfoDTO);
        groupInfoDTO.setGroupOwnerId(UserContext.getUser());
        groupInfoService.saveGroup(groupInfoDTO);
        return getSuccessResponseVO("创建成功");
    }

    /**
     * 加载群聊信息
     */
    @PostMapping("/loadMyGroup")
    public ResponseVO loadMyGroup() {
        String userId = UserContext.getUser();
        List<GroupInfo> groupInfoList = groupInfoService.list(
                new QueryWrapper<GroupInfo>()
                        .eq("group_owner_id", userId)
                        .eq("status", 1)
        );
        return getSuccessResponseVO(groupInfoList);
    }

    /**
     * 获取群聊详情
     * @param groupId 群聊ID
     * @return 群聊详情
     */
    @PostMapping("/getGroupInfo")
    public ResponseVO getGroupInfo(@NotNull @RequestParam("groupId") String groupId) {
        GroupInfo groupInfo = groupInfoService.loadGroupDetail(groupId);
        return getSuccessResponseVO(groupInfo);
    }

    /**
     * 管理群员
     */
    @PostMapping("/addOrRemoveGroupUser")
    public ResponseVO manageGroupUser(@ModelAttribute ManageGroupDTO manageGroupDTO) {
        groupInfoService.manageGroupUser(manageGroupDTO);
        return getSuccessResponseVO("操作成功");
    }


    /**
     * 退出群聊
     * @param groupId 群聊ID
     */
    @PostMapping("/leaveGroup")
    public ResponseVO leaveGroup(@NotNull @RequestParam("groupId") String groupId) {
        ManageGroupDTO manageGroupDTO = new ManageGroupDTO();
        manageGroupDTO.setGroupId(groupId);
        manageGroupDTO.setContactIds(UserContext.getUser());
        groupInfoService.leaveGroup(manageGroupDTO);
        return getSuccessResponseVO("退出成功");
    }

    /**
     * 解散群聊
     * @param groupId 群聊ID
     */
    @PostMapping("/dissolutionGroup")
    public ResponseVO dissolutionGroup(@NotNull @RequestParam("groupId") String groupId) {
        groupInfoService.dissolutionGroup(groupId);
        return getSuccessResponseVO("解散成功");
    }



}
