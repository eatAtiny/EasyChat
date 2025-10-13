package com.easychat.contact.api;

import com.easychat.common.entity.dto.GroupInfoDTO;
import org.springframework.stereotype.Component;

/**
 * GroupClient的降级处理类，用于处理服务调用失败的情况
 */
@Component
public class GroupClientFallback implements GroupClient {
    
    @Override
    public GroupInfoDTO getGroupInfo(String groupId) {
        // 当group-server服务不可用时，返回null或抛出异常
        // 在ContactApplyServiceImpl中已经有异常处理逻辑
        return null;
    }
}