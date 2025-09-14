package com.easychat.common.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class RedissonConfig {

    @Value("${redisson.address}")
    private String address;

    public RedissonClient redissonClient() {
        try {
            // 创建配置 指定redis地址及节点信息
            Config config = new Config();
            config.useSingleServer().setAddress(address);
            // 根据config创建出RedissonClient实例
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (RedisConnectionException e) {
            log.error("redis配置错误，请检查redis配置");
        }
        return null;
    }
}
