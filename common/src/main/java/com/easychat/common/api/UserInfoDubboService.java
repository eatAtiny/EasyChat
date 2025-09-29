package com.easychat.common.api;

import com.easychat.common.entity.po.UserInfo;
import com.easychat.common.entity.vo.PageResultVO;

public interface UserInfoDubboService {

    PageResultVO getUserList(Integer pageNo, Integer pageSize);

    Boolean updateUserStatus(String userId, Integer status);

    String hi();

}
