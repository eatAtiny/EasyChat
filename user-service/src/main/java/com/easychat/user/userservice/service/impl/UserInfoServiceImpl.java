package com.easychat.user.userservice.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.config.AvatarConfig;
import com.easychat.common.exception.BusinessException;

import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.StringTools;
import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.common.entity.kafka.UserInfoMessage;
import com.easychat.common.utils.UserContext;
import com.easychat.user.userservice.api.ContactClient;
import com.easychat.user.userservice.config.UserServiceConfig;
import com.easychat.user.userservice.constant.Constants;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.dto.UserFormDTO;
import com.easychat.common.entity.dto.UserInfoDTO;
import com.easychat.common.entity.enums.ContactStatusEnum;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.po.UserInfoBeauty;
import com.easychat.common.entity.vo.SearchResultVO;
import com.easychat.common.entity.vo.UserInfoVO;
import com.easychat.common.entity.po.UserInfo;
import com.easychat.user.userservice.kafka.KafkaMessageService;
import com.easychat.user.userservice.mapper.UserInfoBeautyMapper;
import com.easychat.user.userservice.mapper.UserInfoMapper;
import com.easychat.user.userservice.service.UserInfoService;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.marshalling.TraceInformation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    @Autowired
    private AvatarConfig avatarConfig;

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
    @Transactional(rollbackFor = Exception.class)
    public UserInfo updateUserInfo(UserInfoDTO userInfoDTO) {
        // 1. 检查用户是否存在
        UserInfo userInfo = baseMapper.selectById(userInfoDTO.getUserId());
        if (userInfo == null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_NOT_EXIST);
        }
        
        // 2. 保存原始的创建时间和最后登录时间，避免被覆盖
        Date originalCreateTime = userInfo.getCreateTime();
        Date originalLastLoginTime = userInfo.getLastLoginTime();
        
        // 3. 更新用户信息（只更新非空字段）
        BeanUtils.copyProperties(userInfoDTO, userInfo, getNullPropertyNames(userInfoDTO));
        
        // 4. 恢复原始的创建时间和最后登录时间
        userInfo.setCreateTime(originalCreateTime);
        userInfo.setLastLoginTime(originalLastLoginTime);
        
        // 5. 更新数据库
        baseMapper.updateById(userInfo);
        
        // 6. 发送用户更新事件到Kafka
        UserInfoMessage event = new UserInfoMessage();
        BeanUtils.copyProperties(userInfo, event);
        event.setEventType(UserInfoMessage.EventType.UPDATE);
        kafkaMessageService.sendUserInfoChangeEvent(event);

        // 7. 处理头像
        if (userInfoDTO.getAvatarFile() != null) {
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
                userInfoDTO.getAvatarFile().transferTo(new File(avatarPath + File.separator + userInfoDTO.getUserId() + avatarConfig.getSuffix()));
                // 3.1.2 群封面
                userInfoDTO.getAvatarCover().transferTo(new File(avatarCoverPath + File.separator + userInfoDTO.getUserId() + avatarConfig.getCoverSuffix()));

            } catch (IOException e) {
                // 添加详细错误日志
                log.error("头像保存失败");
                throw new BusinessException("头像上传失败: " + e.getMessage());
            }
        }

        return userInfo;
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(UserInfoDTO userInfoDTO) {
        // 1. 检查用户是否存在
        UserInfo userInfo = baseMapper.selectById(UserContext.getUser());
        if (userInfo == null) {
            throw new BusinessException(Constants.ERROR_MSG_USER_NOT_EXIST);
        }
        // 2. 加密新密码
        userInfoDTO.setPassword(StringTools.encodeByMD5(userInfoDTO.getPassword()));
        // 3. 更新密码
        BeanUtils.copyProperties(userInfoDTO, userInfo, getNullPropertyNames(userInfoDTO));
        baseMapper.updateById(userInfo);
    }
    
    /**
     * 获取对象中值为null的属性名数组
     */
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        
        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}