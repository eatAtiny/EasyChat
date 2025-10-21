package com.easychat.common.config;

import lombok.Data;
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 文件配置类，读取头像存储路径等参数
 */
@Configuration
@ConfigurationProperties(prefix = "easychat.file")
@Data
public class FileConfig {
    // 文件存储路径
    private String filePath;
    // 更新文件夹
    private String updateFolder;
    // 头像文件夹
    private String avatarFolder;
    // 消息文件夹
    private String messageFolder;

    @PostConstruct  // 添加初始化验证
    public void checkConfig() {
        System.out.println("FileConfig Path: " + getFilePath());
    }
}
