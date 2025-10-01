package com.easychat.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.admin.entity.dto.AppUpdateDTO;
import com.easychat.common.entity.po.AppUpdate;

public interface AppUpdateService extends IService<AppUpdate> {
    /**
     * 保存更新
     */
    boolean addUpdate(AppUpdateDTO appUpdateDTO);

    /**
     * 发布更新
     */
     void postUpdate(Integer id, Integer status, String grayscaleUid);

    /**
     * 获取最新更新
     */
    AppUpdate getUpdate(String appVersion, String userId);
}
