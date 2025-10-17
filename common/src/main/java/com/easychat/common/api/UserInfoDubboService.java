package com.easychat.common.api;

import com.easychat.common.entity.dto.UserInfoDTO;
import com.easychat.common.entity.vo.PageResultVO;

import java.util.concurrent.CompletableFuture;

public interface UserInfoDubboService {

    PageResultVO getUserList(Integer pageNo, Integer pageSize);

    UserInfoDTO getUserInfo(String userId);

    Boolean updateUserStatus(String userId, Integer status);

    CompletableFuture<Boolean> updateUserLastOffTime(String userId, Long lastOffTime);

    Long getUserLastOffTime(String userId);

    String hi();

}
