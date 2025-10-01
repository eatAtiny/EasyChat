package com.easychat.user.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.user.userservice.entity.dto.UserInfoBeautyDTO;
import com.easychat.user.userservice.entity.po.UserInfoBeauty;
import com.easychat.user.userservice.service.impl.UserInfoBeautyServiceImpl;

import java.net.InterfaceAddress;

public interface UserInfoBeautyService extends IService<UserInfoBeauty> {

    /**
     * 保存靓号
     */
     void saveUserInfoBeauty(UserInfoBeautyDTO userInfoBeautyDTO);
}
