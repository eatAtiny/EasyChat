package com.easychat.contact.api;

import com.easychat.common.entity.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/user/service/{userId}")
    UserInfoDTO getUserInfo(@PathVariable("userId") String userId);
}