package com.easychat.user.userservice.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.exception.BusinessException;

import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.StringTools;
import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.common.entity.kafka.UserInfoMessage;
import com.easychat.user.userservice.api.ContactClient;
import com.easychat.user.userservice.config.UserServiceConfig;
import com.easychat.user.userservice.constant.Constants;
import com.easychat.user.userservice.entity.dto.ContactDTO;
import com.easychat.user.userservice.entity.dto.UserFormDTO;
import com.easychat.user.userservice.entity.dto.UserInfoDTO;
import com.easychat.user.userservice.entity.enums.ContactStatusEnum;
import com.easychat.user.userservice.entity.enums.ContactTypeEnum;
import com.easychat.user.userservice.entity.po.UserInfoBeauty;
import com.easychat.user.userservice.entity.vo.SearchResultVO;
import com.easychat.user.userservice.entity.vo.UserInfoVO;
import com.easychat.user.userservice.kafka.KafkaMessageService;
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

    @Autowired
    private ContactClient contactClient;
    
    @Autowired
    private KafkaMessageService kafkaMessageService;

    /**
     * 注册用户
     * @param userFormDTO 用户注册信息
     */
    @Override
    public void register(UserFormDTO userFormDTO) {
        // 1. 查看用户是否存在
        UserInfo userInfo = baseMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, userFormDTO.getEmail()));
        if (userInfo != null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_EXIST);
        }
        // 2. 查询靓号
        String userId;
        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectOne(new LambdaQueryWrapper<UserInfoBeauty>().eq(UserInfoBeauty::getEmail, userFormDTO.getEmail()));
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
        userInfo.setEmail(userFormDTO.getEmail());
        userInfo.setNickName(userFormDTO.getNickName());
        userInfo.setJoinType(Constants.USER_DEFAULT_JOIN_TYPE);
        userInfo.setSex(Constants.USER_DEFAULT_SEX);
        userInfo.setPassword(StringTools.encodeByMD5(userFormDTO.getPassword()));
        userInfo.setPersonalSignature(Constants.USER_DEFAULT_PERSONAL_SIGNATURE);
        userInfo.setStatus(Constants.USER_DEFAULT_STATUS);
        userInfo.setCreateTime(DateTime.now());
        userInfo.setLastLoginTime(DateTime.now());
        userInfo.setAreaName(Constants.USER_DEFAULT_AREA_NAME);
        userInfo.setAreaCode(Constants.USER_DEFAULT_AREA_CODE);
        userInfo.setLastOffTime(DateTime.now().getTime());
        baseMapper.insert(userInfo);
        
        // 发送用户创建事件到Kafka
        UserInfoMessage event = new UserInfoMessage();
        BeanUtils.copyProperties(userInfo, event);
        event.setEventType(UserInfoMessage.EventType.CREATE);
        kafkaMessageService.sendUserInfoChangeEvent(event);
        
        // 4. TODO 添加机器人好友

    }

    @Override
    public UserInfoVO login(UserFormDTO userFormDTO) {
        // 1. 校验用户信息
        // 1.1 检查用户是否存在
        UserInfo userInfo = baseMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, userFormDTO.getEmail()));
        if (userInfo == null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_NOT_EXIST);
        }
        // 1.2 校验密码
        if (!Objects.equals(userFormDTO.getPassword(), userInfo.getPassword())) {
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
        // 1.5 更新用户登录时间
        userInfo.setLastLoginTime(DateTime.now());
        baseMapper.updateById(userInfo);

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

    /**
     * 搜索用户信息
     */
    @Override
    public SearchResultVO searchUserInfo(String contactId) {
        // 1. 获取用户信息
        UserInfo userInfo = baseMapper.selectById(contactId);
        if (userInfo == null) {
            return null;
        }
        SearchResultVO searchResultVO = new SearchResultVO();
        BeanUtils.copyProperties(userInfo, searchResultVO);
        searchResultVO.setContactId(contactId);
        searchResultVO.setContactType(ContactTypeEnum.USER.getName());
        // 2. 获取用户关系信息
        ContactDTO contactDTO = contactClient.getContactInfo(contactId);
        if (contactDTO != null) {
            searchResultVO.setStatus(contactDTO.getStatus());
            searchResultVO.setStatusName(ContactStatusEnum.getDescByStatus(contactDTO.getStatus()));
        }else{
            searchResultVO.setStatus(ContactStatusEnum.NO_FRIEND.getStatus());
            searchResultVO.setStatusName(ContactStatusEnum.NO_FRIEND.getDesc());
        }

        return searchResultVO;
    }

    /**
     * 更新用户信息
     */
    @Override
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        // 1. 检查用户是否存在
        UserInfo userInfo = baseMapper.selectById(userInfoDTO.getUserId());
        if (userInfo == null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_NOT_EXIST);
        }
        
        // 2. 更新用户信息
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        baseMapper.updateById(userInfo);
        
        // 3. 发送用户更新事件到Kafka
        UserInfoMessage event = new UserInfoMessage();
        BeanUtils.copyProperties(userInfo, event);
        event.setEventType(UserInfoMessage.EventType.UPDATE);
        kafkaMessageService.sendUserInfoChangeEvent(event);
    }
}