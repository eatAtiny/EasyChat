package com.easychat.user.userservice.controller;

import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.dto.SysSettingDTO;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.RedisUtils;

import com.easychat.user.userservice.constant.Constants;
import com.easychat.user.userservice.entity.dto.UserInfoDTO;
import com.easychat.user.userservice.entity.po.UserInfo;
import com.easychat.user.userservice.entity.vo.SysSettingVO;
import com.easychat.user.userservice.entity.vo.UserInfoVO;
import com.easychat.user.userservice.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/user")
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
     * @param userInfoDTO 用户信息DTO
     * @return
     */

    @ApiOperation("注册")
    @PostMapping("/register")
    public ResponseVO register(@ModelAttribute UserInfoDTO userInfoDTO) {
        try {
            if (!userInfoDTO.getCheckCode().equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + userInfoDTO.getCheckCodeKey()))) {
                throw new BusinessException(Constants.ERROR_MSG_CHECK_CODE);
            }
            userInfoService.register(userInfoDTO);
            return getSuccessResponseVO(null);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + userInfoDTO.getCheckCodeKey());
        }
    }

    /**
     * 登录接口
     */
    @ApiOperation("登录接口")
    @PostMapping("")
    public ResponseVO login(@ModelAttribute UserInfoDTO userInfoDTO) {
        try {
            if (!userInfoDTO.getCheckCode().equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + userInfoDTO.getCheckCodeKey()))) {
                throw new BusinessException(Constants.ERROR_MSG_CHECK_CODE);
            }
            UserInfoVO userInfoVO = userInfoService.login(userInfoDTO);
            return getSuccessResponseVO(userInfoVO);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + userInfoDTO.getCheckCodeKey());
        }
    }

    /**
     * 获取系统设置
     * @return
     */
    @GetMapping("/setting")
    public ResponseVO getSysSetting() {
        SysSettingDTO sysSettingDTO = redisComponet.getSysSetting();
        SysSettingVO sysSettingVO = new SysSettingVO();
        BeanUtils.copyProperties(sysSettingDTO, sysSettingVO);
        return getSuccessResponseVO(sysSettingVO);
    }

    @GetMapping("/search")
    public UserInfo search(@RequestParam("contactId") String contactId) {
        return userInfoService.getById(contactId);
    }
}