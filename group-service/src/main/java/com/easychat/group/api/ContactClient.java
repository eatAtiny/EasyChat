package com.easychat.group.api;

import com.easychat.common.entity.dto.ContactDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("contact-service")
public interface ContactClient {

    @GetMapping("/contact")
    ContactDTO getContactInfo(@RequestParam("contactId") String contactId);

    /**
     * 创建关系
     */
    @PostMapping("/contact")
    void createContact(@RequestBody ContactDTO contactDTO);
}
