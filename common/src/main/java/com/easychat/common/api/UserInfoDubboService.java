package com.easychat.common.api;

import com.easychat.common.entity.vo.PageResultVO;

import java.util.concurrent.CompletableFuture;

public interface UserInfoDubboService {

    PageResultVO getUserList(Integer pageNo, Integer pageSize);

    Boolean updateUserStatus(String userId, Integer status);

    CompletableFuture<Boolean> updateUserLastLoginTime(String userId, Long lastLoginTime);

    Long getUserLastOffTime(String userId);

    String hi();

}
