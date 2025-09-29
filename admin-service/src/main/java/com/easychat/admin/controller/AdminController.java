package com.easychat.admin.controller;

import com.easychat.common.advice.BaseController;
import com.easychat.common.api.UserInfoDubboService;
import com.easychat.common.entity.vo.ResponseVO;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @DubboReference
    private UserInfoDubboService userInfoDubboService;

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

}
