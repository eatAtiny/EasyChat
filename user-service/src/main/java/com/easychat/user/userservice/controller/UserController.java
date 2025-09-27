package com.easychat.user.userservice.controller;

import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.dto.SysSettingDTO;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.RedisUtils;

import com.easychat.common.utils.UserContext;
import com.easychat.user.userservice.constant.Constants;
import com.easychat.user.userservice.entity.dto.UserFormDTO;
import com.easychat.user.userservice.entity.po.UserInfo;
import com.easychat.user.userservice.entity.vo.SearchResultVO;
import com.easychat.user.userservice.entity.vo.SysSettingVO;
import com.easychat.user.userservice.entity.vo.UserInfoVO;
import com.easychat.user.userservice.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends BaseController {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisComponet redisComponet;

    /**
     * 验证码
     */
    @ApiOperation("获取验证码")
    @GetMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_CHECK_CODE_KEY_TIME);
        String checkCodeBase64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }

    /**
     * 注册
     * @param userFormDTO 用户信息DTO
     */
    @ApiOperation("注册")
    @PostMapping("/register")
    public ResponseVO register(@ModelAttribute UserFormDTO userFormDTO) {
        try {
            if (!userFormDTO.getCheckCode().equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey()))) {
                throw new BusinessException(Constants.ERROR_MSG_CHECK_CODE);
            }
            userInfoService.register(userFormDTO);
            return getSuccessResponseVO(null);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey());
        }
    }

    /**
     * 登录接口
     */
    @ApiOperation("登录接口")
    @PostMapping("/login")
    public ResponseVO login(@ModelAttribute UserFormDTO userFormDTO) {
        log.info("登录请求参数: {}", userFormDTO);
        try {
            if (!userFormDTO.getCheckCode().equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey()))) {
                throw new BusinessException(Constants.ERROR_MSG_CHECK_CODE);
            }
            UserInfoVO userInfoVO = userInfoService.login(userFormDTO);
            log.info("登录成功{}", userInfoVO);
            return getSuccessResponseVO(userInfoVO);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey());
        }
    }

    /**
     * 获取系统设置
     * @return 系统设置
     */
    @ApiOperation("获取系统设置")
    @GetMapping("/setting")
    public ResponseVO getSysSetting() {
        SysSettingDTO sysSettingDTO = redisComponet.getSysSetting();
        SysSettingVO sysSettingVO = new SysSettingVO();
        BeanUtils.copyProperties(sysSettingDTO, sysSettingVO);
        return getSuccessResponseVO(sysSettingVO);
    }


    /**
     * 根据用户ID搜索用户信息
     * @param contactId 用户id
     * @return 用户信息
     */
    @ApiOperation("根据用户ID搜索用户信息")
    @GetMapping("/search/{contactId}")
    public ResponseVO searchUserInfo(@PathVariable("contactId") String contactId) {
        SearchResultVO searchResultVO = userInfoService.searchUserInfo(contactId);
        return getSuccessResponseVO(searchResultVO);
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("")
    public ResponseVO getUserInfo() {
        // 打印用户ID，用于调试
        String userId = UserContext.getUser();
        System.out.println("UserController getUserInfo - UserContext userId: " + userId);
        
        UserInfo userInfo = userInfoService.getById(userId);
        return getSuccessResponseVO(userInfo);
    }


    /**
     * 获取用户信息(供contact服务使用)
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/service/{userId}")
    public UserInfo ServiceGetUserInfo(@PathVariable("userId") String userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        return userInfo;
    }
}