package com.easychat.common.api;

import com.easychat.common.entity.po.AppUpdate;

public interface AdminDubboService {
    /**
     * 获取更新信息
     */
     AppUpdate getUpdate(String appVersion, String userId);
}
