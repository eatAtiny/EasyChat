package com.easychat.user.userservice.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.exception.BusinessException;

import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.StringTools;
import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.user.userservice.config.UserServiceConfig;
import com.easychat.user.userservice.constant.Constants;
import com.easychat.user.userservice.entity.dto.UserInfoDTO;
import com.easychat.user.userservice.entity.po.UserInfoBeauty;
import com.easychat.user.userservice.entity.vo.UserInfoVO;
import com.easychat.user.userservice.mapper.UserInfoBeautyMapper;
import com.easychat.user.userservice.mapper.UserInfoMapper;
import com.easychat.user.userservice.service.UserInfoService;
import com.easychat.user.userservice.entity.po.UserInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Autowired
    private RedisComponet redisComponet;

    @Autowired
    private UserServiceConfig userServiceConfig;

    /**
     * 注册用户
     * @param userInfoDTO 用户注册信息
     */
    @Override
    public void register(UserInfoDTO userInfoDTO) {
        // 1. 查看用户是否存在
        UserInfo userInfo = baseMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, userInfoDTO.getEmail()));
        if (userInfo != null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_EXIST);
        }
        // 2. 查询靓号
        String userId;
        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectOne(new LambdaQueryWrapper<UserInfoBeauty>().eq(UserInfoBeauty::getEmail, userInfoDTO.getEmail()));
        if (userInfoBeauty != null) {
            // 2.1 需要设置靓号
            userId = userInfoBeauty.getUserId();
            // 2.1.1 设置靓号状态为已使用
            userInfoBeauty.setStatus(Constants.USER_BEAUTY_STATUS);
            userInfoBeautyMapper.updateById(userInfoBeauty);
        } else  {
            // 2.2 没有靓号，直接生成随机id
            userId = StringTools.getUserId();
        }
        // 3. 保存用户信息
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(userInfoDTO.getEmail());
        userInfo.setNickName(userInfoDTO.getNickName());
        userInfo.setJoinType(Constants.USER_DEFAULT_JOIN_TYPE);
        userInfo.setSex(Constants.USER_DEFAULT_SEX);
        userInfo.setPassword(StringTools.encodeByMD5(userInfoDTO.getPassword()));
        userInfo.setPersonalSignature(Constants.USER_DEFAULT_PERSONAL_SIGNATURE);
        userInfo.setStatus(Constants.USER_DEFAULT_STATUS);
        userInfo.setCreateTime(DateTime.now());
        userInfo.setLastLoginTime(DateTime.now());
        userInfo.setAreaName(Constants.USER_DEFAULT_AREA_NAME);
        userInfo.setAreaCode(Constants.USER_DEFAULT_AREA_CODE);
        userInfo.setLastOffTime(DateTime.now().getTime());
        baseMapper.insert(userInfo);
        // 4. TODO 添加机器人好友

    }

    @Override
    public UserInfoVO login(UserInfoDTO userInfoDTO) {
        // 1. 校验用户信息
        // 1.1 检查用户是否存在
        UserInfo userInfo = baseMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, userInfoDTO.getEmail()));
        if (userInfo == null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_NOT_EXIST);
        }
        // 1.2 校验密码
        if (!Objects.equals(userInfoDTO.getPassword(), userInfo.getPassword())) {
            throw new BusinessException(Constants.ERROR_MSG_PASSWORD_ERROR);
        }
        // 1.3 检查用户状态
        if (Objects.equals(userInfo.getStatus(), Constants.USER_STATUS_DISABLE)) {
            throw new BusinessException(Constants.ERROR_MSG_USER_DISABLE);
        }
        // 1.4 检查账号登录状态
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfoDTO(userInfo);
        Long lastHeartBeat = redisComponet.getUserHeartBeat(tokenUserInfoDTO.getUserId());
        if (lastHeartBeat != null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_LOGIN);
        }

        // 2. 加载用户信息
        // 2.1 TODO 加载群聊
        // 2.2 TODO 加载好友


        // 3. 生成token,将用户信息保存到redis
        String token = StringTools.encodeByMD5(tokenUserInfoDTO.getUserId() + StringTools.getRandomString(Constants.LENGTH_20));
        tokenUserInfoDTO.setToken(token);
        redisComponet.saveTokenUserInfoDTO(tokenUserInfoDTO);

        // 4. 转换为VO
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, userInfoVO);
        userInfoVO.setToken(token);
        userInfoVO.setAdmin(tokenUserInfoDTO.getAdmin());
        return userInfoVO;
    }

    /**
     * 获取用户信息
     * @param userInfo 用户信息
     * @return 用户信息
     */
    private TokenUserInfoDTO getTokenUserInfoDTO(UserInfo userInfo) {
        // 1. 转换为DTO
        TokenUserInfoDTO tokenUserInfoDTO = new TokenUserInfoDTO();
        tokenUserInfoDTO.setUserId(userInfo.getUserId());
        tokenUserInfoDTO.setNickName(userInfo.getNickName());
        // 2. 检查是否是管理员
        String adminEmails = userServiceConfig.getAdminEmails();
        if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail())) {
            tokenUserInfoDTO.setAdmin(true);
        } else {
            tokenUserInfoDTO.setAdmin(false);
        }
        return tokenUserInfoDTO;
    }
}