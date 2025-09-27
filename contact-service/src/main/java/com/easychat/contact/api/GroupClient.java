package com.easychat.contact.api;

import com.easychat.contact.entity.dto.GroupInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "group-service", fallback = GroupClientFallback.class)
public interface GroupClient {
    /**
     * 获取群聊信息
     * @param groupId 群聊ID
     * @return 群聊信息
     */
    @GetMapping("/group/service/{groupId}")
    GroupInfoDTO getGroupInfo(@PathVariable("groupId") String groupId);
}