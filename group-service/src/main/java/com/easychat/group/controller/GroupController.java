package com.easychat.group.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.PageResultVO;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.common.entity.dto.GroupInfoDTO;
import com.easychat.common.entity.dto.GroupManageDTO;
import com.easychat.common.entity.enums.GroupStatusEnum;
import com.easychat.common.entity.po.GroupInfo;
import com.easychat.common.entity.vo.SearchResultVO;
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

    /**
     * 解散群聊
     * @param groupId 群聊ID
     */
    @DeleteMapping("/{groupId}")
    public ResponseVO dissolutionGroup(@NotNull @PathVariable("groupId") String groupId) {
        groupInfoService.dissolutionGroup(groupId);
        return getSuccessResponseVO("解散成功");
    }

    /**
     * 获取群聊列表
     */
    @ApiOperation("获取群聊列表")
    @GetMapping("/admin/groupList")
    public ResponseVO loadGroupList(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GroupInfo> page = new Page<>(pageNo, pageSize);
        IPage<GroupInfo> groupInfoPage = groupInfoService.page(page, Wrappers.emptyWrapper());
        PageResultVO pageResultVO = new PageResultVO();
        pageResultVO.setList(groupInfoPage.getRecords()); // 用户数据列表
        pageResultVO.setPageNo((int) groupInfoPage.getCurrent()); // 当前页码
        pageResultVO.setPageSize((int) groupInfoPage.getSize()); // 每页条数
        pageResultVO.setPageTotal((int) groupInfoPage.getPages()); // 总页数
        pageResultVO.setTotalCount((int) groupInfoPage.getTotal()); // 总记录数
        return getSuccessResponseVO(pageResultVO);
    }

    /**
     * 加载群聊详情(供contact服务使用)
     * @param groupId 群聊ID
     * @return 群聊详情
     */
    @ApiOperation("获取群聊详情")
    @GetMapping("/service/{groupId}")
    public GroupInfo serviceGetGroupInfo(@NotNull @PathVariable("groupId") String groupId) {
        GroupInfo groupInfo = groupInfoService.loadGroupDetail(groupId);
        return groupInfo;
    }





}
