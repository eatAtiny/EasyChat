package com.easychat.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 文件配置类，读取头像存储路径等参数
 */
@Configuration
@ConfigurationProperties(prefix = "easychat.avatar")
@Data
public class AvatarConfig {
    private String path;
    private String suffix;
    private String coverPath;
    private String coverSuffix;

    @PostConstruct  // 添加初始化验证
    public void checkConfig() {
        System.out.println("AvatarConfig Path: " + getPath());
    }
}
