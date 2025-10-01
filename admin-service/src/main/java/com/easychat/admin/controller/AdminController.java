package com.easychat.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.admin.entity.dto.AppUpdateDTO;
import com.easychat.admin.service.AppUpdateService;
import com.easychat.common.config.AvatarConfig;
import com.easychat.common.entity.dto.SysSettingDTO;
import com.easychat.common.advice.BaseController;
import com.easychat.common.api.UserInfoDubboService;
import com.easychat.common.entity.po.AppUpdate;
import com.easychat.common.entity.vo.PageResultVO;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.RedisComponet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/admin")
@Slf4j
@Api("管理员接口")
public class AdminController extends BaseController {

    @DubboReference(check = false)
    private UserInfoDubboService userInfoDubboService;

    @Autowired
    private RedisComponet redisComponet;

    @Autowired
    private AvatarConfig avatarConfig;
    @Autowired
    private AppUpdateService appUpdateService;

    /**
     * 禁用/启用用户
     * @
     */
    @ApiOperation("禁用/启用用户")
    @PostMapping("/user")
    public ResponseVO updateUserStatus(@RequestParam("userId") String userId, @RequestParam("status") Integer status) {
        if (!userInfoDubboService.updateUserStatus(userId, status)) {
            return getServerErrorResponseVO("更新用户状态失败");
        }
        return getSuccessResponseVO(null);
    }

    /**
     * 强制下线用户
     */
    @ApiOperation("强制下线用户")
    @PostMapping("/user/off")
    public ResponseVO forceLogoutUser(@RequestParam("userId") String userId) {
        // TODO 发送下线消息
        return getServerErrorResponseVO("暂未实现");
    }

    /**
     * 解散群组
     */
    @ApiOperation("解散群组")
    @PostMapping("/group")
    public ResponseVO dismissGroup(@RequestParam("groupId") String groupId) {
        // TODO 调用群组接口
        return getServerErrorResponseVO("暂未实现");
    }

    /**
     * 获取系统设置
     */
    @ApiOperation("获取系统设置")
    @GetMapping("/settings")
    public ResponseVO getSettings() {
        SysSettingDTO sysSettingDTO = redisComponet.getSysSetting();
        return getSuccessResponseVO(sysSettingDTO);
    }

    /**
     * 更新系统设置
     */
    @ApiOperation("更新系统设置")
    @PostMapping("/settings")
    public ResponseVO updateSettings(@ModelAttribute SysSettingDTO sysSettingDTO,
                                     MultipartFile robotFile,
                                     MultipartFile robotCover) {
        if (robotFile != null) {
            // 3.1 保存头像到文件夹
            // 获取当前项目根目录
            String projectPath = System.getProperty("user.dir");

            // 构建完整的文件夹路径
            String avatarPath = projectPath + File.separator + avatarConfig.getPath();
            String avatarCoverPath = projectPath + File.separator + avatarConfig.getCoverPath();

            try {
                File avatarDir = new File(avatarPath);
                // 如果文件夹不存在则创建
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs(); // 递归创建文件夹
                }
                File avatarCoverDir = new File(avatarCoverPath);
                // 如果文件夹不存在则创建
                if (!avatarCoverDir.exists()) {
                    avatarCoverDir.mkdirs(); // 递归创建文件夹
                }

                // 3.1.1 头像
                robotFile.transferTo(new File(avatarPath + File.separator + (sysSettingDTO.getRobotUid() == null ? "Urobot" : sysSettingDTO.getRobotUid()) + avatarConfig.getSuffix()));
                // 3.1.2 群封面
                robotCover.transferTo(new File(avatarCoverPath + File.separator + (sysSettingDTO.getRobotUid() == null ? "Urobot" : sysSettingDTO.getRobotUid()) + avatarConfig.getCoverSuffix()));

            } catch (IOException e) {
                // 添加详细错误日志
                log.error("头像保存失败");
                throw new BusinessException("头像上传失败: " + e.getMessage());
            }
        }
        redisComponet.saveSysSetting(sysSettingDTO);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取更新列表
     */
    @ApiOperation("获取更新列表")
    @GetMapping("/updateList")
    public ResponseVO getUpdateList(@RequestParam(defaultValue = "1") Integer pageNo,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createTimeStart,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createTimeEnd

    ) {
        Page<AppUpdate> page = new Page<>(pageNo, pageSize);
        IPage<AppUpdate> appUpdatePage = appUpdateService.page(page,
                new LambdaQueryWrapper<AppUpdate>()
                        .orderByDesc(AppUpdate::getId)
                        .between(createTimeStart != null && createTimeEnd != null,AppUpdate::getCreateTime, createTimeStart, createTimeEnd)
        );
        PageResultVO pageResultVO = new PageResultVO();
        pageResultVO.setList(appUpdatePage.getRecords()); // 用户数据列表
        pageResultVO.setPageNo((int) appUpdatePage.getCurrent()); // 当前页码
        pageResultVO.setPageSize((int) appUpdatePage.getSize()); // 每页条数
        pageResultVO.setPageTotal((int) appUpdatePage.getPages()); // 总页数
        pageResultVO.setTotalCount((int) appUpdatePage.getTotal()); // 总记录数
        return getSuccessResponseVO(pageResultVO);
    }

    /**
     * 新增更新
     */
    @ApiOperation("新增更新")
    @PostMapping("/update")
    public ResponseVO addUpdate(@ModelAttribute AppUpdateDTO appUpdateDTO) {
        if (!appUpdateService.addUpdate(appUpdateDTO)) {
            return getServerErrorResponseVO("新增更新失败");
        }
        return getSuccessResponseVO(null);
    }

    /**
     * 删除更新
     */
    @ApiOperation("删除更新")
    @DeleteMapping("/update/{updateId}")
    public ResponseVO deleteUpdate(@PathVariable("updateId") String updateId) {
        if (!appUpdateService.removeById(updateId)) {
            return getServerErrorResponseVO("删除更新失败");
        }
        return getSuccessResponseVO(null);
    }

    /**
     * 发布更新
     * @param id
     * @param status
     * @param grayscaleUid
     * @return
     */
    @PostMapping("/update/{updateId}")
    public ResponseVO postUpdate(@PathVariable("updateId") @NotNull Integer id, @NotNull Integer status, String grayscaleUid) {
        appUpdateService.postUpdate(id, status, grayscaleUid);
        return getSuccessResponseVO(null);
    }

}
