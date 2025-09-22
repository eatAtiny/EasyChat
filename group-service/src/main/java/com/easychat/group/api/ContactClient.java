package com.easychat.group.api;

import com.easychat.group.entity.dto.ContactDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("contact-service")
public interface ContactClient {

    /**
     * 创建关系
     */
    @PostMapping("api/contact/createContact")
    void createContact(@ModelAttribute ContactDTO contactDTO);
}
