package com.easychat.group.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.group.entity.dto.GroupInfoDTO;
import com.easychat.group.entity.dto.GroupManageDTO;
import com.easychat.group.entity.enums.GroupStatusEnum;
import com.easychat.group.entity.po.GroupInfo;
import com.easychat.group.entity.vo.SearchResultVO;
import com.easychat.group.service.GroupInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/group")
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
    @ApiOperation("创建群聊")
    @PostMapping("")
    public ResponseVO saveGroup(GroupInfoDTO groupInfoDTO) {
        log.info("创建群聊, groupInfoDTO: {}", groupInfoDTO);
        groupInfoDTO.setGroupOwnerId(UserContext.getUser());
        groupInfoService.saveGroup(groupInfoDTO);
        return getSuccessResponseVO("创建成功");
    }

    /**
     * 加载我的群聊信息
     */
    @ApiOperation("加载我的群聊")
    @GetMapping("")
    public ResponseVO loadMyGroup() {
        List<GroupInfo> groupInfoList = groupInfoService.list(
                new QueryWrapper<GroupInfo>()
                        .eq("group_owner_id", UserContext.getUser())
                        .eq("status", GroupStatusEnum.NORMAL.getCode())
        );
        return getSuccessResponseVO(groupInfoList);
    }

    /**
     * 搜索群聊
     * @param groupId 群聊ID
     * @return 群聊详情
     */
    @ApiOperation("根据群聊ID搜索群聊详情")
    @GetMapping("/search/{groupId}")
    public ResponseVO searchGroup(@NotNull @PathVariable("groupId") String groupId) {
        SearchResultVO searchResultVO = groupInfoService.searchGroup(groupId);
        return getSuccessResponseVO(searchResultVO);
    }

    /**
     * 加载群聊详情
     * @param groupId 群聊ID
     * @return 群聊详情
     */
    @ApiOperation("获取群聊详情")
    @GetMapping("/{groupId}")
    public ResponseVO loadGroupDetail(@NotNull @PathVariable("groupId") String groupId) {
        GroupInfo groupInfo = groupInfoService.loadGroupDetail(groupId);
        return getSuccessResponseVO(groupInfo);
    }

    /**
     * 管理群员
     */
    @ApiOperation("管理群员")
    @PostMapping("/manage")
    public ResponseVO manageGroupUser(@ModelAttribute GroupManageDTO groupManageDTO) {
        groupInfoService.manageGroupUser(groupManageDTO);
        return getSuccessResponseVO("操作成功");
    }


//    /**
//     * 退出群聊
//     * @param groupId 群聊ID
//     */
//    @PostMapping("/leaveGroup")
//    public ResponseVO leaveGroup(@NotNull @RequestParam("groupId") String groupId) {
//        GroupManageDTO groupManageDTO = new GroupManageDTO();
//        groupManageDTO.setGroupId(groupId);
//        groupManageDTO.setContactIds(UserContext.getUser());
//        groupInfoService.leaveGroup(groupManageDTO);
//        return getSuccessResponseVO("退出成功");
//    }

    /**
     * 解散群聊
     * @param groupId 群聊ID
     */
    @DeleteMapping("/{groupId}")
    public ResponseVO dissolutionGroup(@NotNull @PathVariable("groupId") String groupId) {
        groupInfoService.dissolutionGroup(groupId);
        return getSuccessResponseVO("解散成功");
    }



}
