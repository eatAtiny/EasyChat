package com.easychat.user.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "user-service")
@Data
public class UserServiceConfig {
    private String adminEmails;
}
