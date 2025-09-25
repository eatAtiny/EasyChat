package com.easychat.user.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.easychat.user.userservice.entity.dto.UserFormDTO;
import com.easychat.user.userservice.entity.dto.UserInfoDTO;
import com.easychat.user.userservice.entity.po.UserInfo;
import com.easychat.user.userservice.entity.vo.SearchResultVO;
import com.easychat.user.userservice.entity.vo.UserInfoVO;

public interface UserInfoService extends IService<UserInfo> {
    /**
     * 注册
     *
     * @param userFormDTO 用户注册信息
     */
    void register(UserFormDTO userFormDTO);
    /**
     * 登录
     *
     * @param userFormDTO 用户登录信息
     * @return 用户信息VO
     */
    UserInfoVO login(UserFormDTO userFormDTO);

    /**
     * 搜索用户信息
     *
     * @param contactId 用户id
     * @return 搜索结果
     */
    SearchResultVO searchUserInfo(String contactId);
    
    /**
     * 更新用户信息
     *
     * @param userInfoDTO 用户信息DTO
     */
    void updateUserInfo(UserInfoDTO userInfoDTO);
}