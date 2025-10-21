package com.easychat.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.common.config.FileConfig;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.AppUpdateDTO;
import com.easychat.common.entity.enums.AppUpdateFileTypeEnum;
import com.easychat.common.entity.enums.AppUpdateStatusEnum;
import com.easychat.admin.mapper.AppUpdateMapper;
import com.easychat.admin.service.AppUpdateService;
import com.easychat.common.entity.po.AppUpdate;
import com.easychat.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate> implements AppUpdateService {

    @Autowired
    private FileConfig fileConfig;

    /**
     * 保存更新
     */
    @Override
    public boolean addUpdate(AppUpdateDTO appUpdateDTO) {
        AppUpdate appUpdate = baseMapper.selectOne(new LambdaQueryWrapper<AppUpdate>()
                .eq(AppUpdate::getVersion, appUpdateDTO.getVersion())
        );
        if (Objects.isNull(appUpdate)) {
            appUpdate = new AppUpdate();
            BeanUtil.copyProperties(appUpdateDTO, appUpdate);
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateStatusEnum.NO_RELEASE.getCode());
            baseMapper.insert(appUpdate);
        }else{
            BeanUtil.copyProperties(appUpdateDTO, appUpdate,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreProperties("createTime")
            );
            baseMapper.updateById(appUpdate);
        }
        if(Objects.equals(appUpdateDTO.getFileType(), AppUpdateFileTypeEnum.LOCAL.getCode())) {
            // 本地文件
            // 获取当前项目根目录
            String projectPath = System.getProperty("user.dir");

            // 构建完整的文件夹路径
            String filePath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getUpdateFolder();

            try {
                File avatarDir = new File(filePath);
                // 如果文件夹不存在则创建
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs(); // 递归创建文件夹
                }
                appUpdateDTO.getFile().transferTo(new File(filePath + File.separator + appUpdateDTO.getVersion() + Constants.APP_EXE_SUFFIX));
            } catch (IOException e) {
                // 添加详细错误日志
                log.error("文件保存失败");
                throw new BusinessException("文件上传失败: " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * 发布更新
     */
    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdate appUpdate = baseMapper.selectById(id);
        if(Objects.isNull(appUpdate)){
            throw new BusinessException("更新不存在");
        }
        appUpdate.setStatus(status);
        appUpdate.setGrayscaleUid(grayscaleUid);
        baseMapper.updateById(appUpdate);
    }

    /**
     * 获取最新更新
     */
    @Override
    public AppUpdate getUpdate(String appVersion, String userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<AppUpdate>()
                .gt(AppUpdate::getVersion, appVersion)
                .and(wrapper -> wrapper
                        .eq(AppUpdate::getStatus, AppUpdateStatusEnum.ALL.getCode())
                        .or(
                                grayWrapper -> grayWrapper
                                        .eq(AppUpdate::getStatus, AppUpdateStatusEnum.GRAY.getCode())
                                        .like(AppUpdate::getGrayscaleUid, userId)
                        )
                )
                .orderByDesc(AppUpdate::getVersion)
                .last("LIMIT 1")
        );
    }

}
