package com.easychat.admin.dubbo;

import com.easychat.admin.service.AppUpdateService;
import com.easychat.common.api.AdminDubboService;

import com.easychat.common.entity.po.AppUpdate;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class AdminDubboServiceImpl implements AdminDubboService {
    @Autowired
    private AppUpdateService appUpdateService;

    @Override
     public AppUpdate getUpdate(String appVersion, String userId) {
        return appUpdateService.getUpdate(appVersion, userId);
    }
}
