package com.easychat.user.userservice.api;

import com.easychat.user.userservice.entity.dto.ContactDTO;
import lombok.Getter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("contact-service")
public interface ContactClient {

    @GetMapping("/contact")
    ContactDTO getContactInfo(@RequestParam("contactId") String contactId);
}