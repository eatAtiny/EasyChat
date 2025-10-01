package com.easychat.user.userservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.utils.UserContext;
import com.easychat.user.userservice.entity.dto.UserInfoBeautyDTO;
import com.easychat.user.userservice.entity.enums.UserInfoBeautyStatus;
import com.easychat.user.userservice.entity.po.UserInfoBeauty;
import com.easychat.user.userservice.mapper.UserInfoBeautyMapper;
import com.easychat.user.userservice.service.UserInfoBeautyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Provider;

@Service
public class UserInfoBeautyServiceImpl extends ServiceImpl<UserInfoBeautyMapper, UserInfoBeauty> implements UserInfoBeautyService {

    /**
     * 保存靓号
     *
     * @param userInfoBeautyDTO 靓号信息DTO
     */
    @Override
    public void saveUserInfoBeauty(UserInfoBeautyDTO userInfoBeautyDTO) {
        UserInfoBeauty userInfoBeauty = new UserInfoBeauty();
        BeanUtils.copyProperties(userInfoBeautyDTO,userInfoBeauty);
        userInfoBeauty.setStatus(UserInfoBeautyStatus.UNUSE.getValue());
        baseMapper.insert(userInfoBeauty);
    }


}
