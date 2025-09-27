package com.easychat.contact.api;

import com.easychat.contact.entity.dto.UserInfoDTO;
import org.springframework.stereotype.Component;

/**
 * UserClient的降级处理类，用于处理服务调用失败的情况
 */
@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public UserInfoDTO getUserInfo(String userId) {
        // 当user-service服务不可用时，返回null或抛出异常
        // 在ContactApplyServiceImpl中已经有异常处理逻辑
        return null;
    }
}