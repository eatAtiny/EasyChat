package com.easychat.contact.api;

import com.easychat.contact.entity.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping("api/account/search")
    UserInfoDTO search(@RequestParam("contactId") String contactId);
}
