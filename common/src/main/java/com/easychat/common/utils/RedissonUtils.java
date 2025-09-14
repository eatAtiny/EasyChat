package com.easychat.common.utils;

import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("redissonUtils")
public class RedissonUtils<T> {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 集合添加
     *
     * @param key   键
     * @param value 值
     * @param seconds  时间(秒)
     */
    public void addSet(String key, T value, Long seconds) {
        RSet<T> reset = redissonClient.getSet(key);
        reset.add(value);
        reset.expire(seconds, TimeUnit.SECONDS);
    }
}
