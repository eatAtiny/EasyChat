package com.easychat.user.userservice.dubbo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.common.api.UserInfoDubboService;
import com.easychat.common.entity.vo.PageResultVO;
import com.easychat.user.userservice.entity.po.UserInfo;
import com.easychat.user.userservice.service.UserInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class UserInfoDubboServiceImpl implements UserInfoDubboService {

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public PageResultVO getUserList(Integer pageNo, Integer pageSize) {
        // 1. 转换 Spring Data PageRequest 为 MyBatis-Plus Page（0基页码转1基）
        Page<UserInfo> mpPage = new Page<>(pageNo, pageSize);

        // 2. 调用 MyBatis-Plus 分页查询（查询所有用户，无过滤条件）
        IPage<UserInfo> userPage = userInfoService.page(mpPage, Wrappers.emptyWrapper());

        // 3. 封装 PageResultVO
        PageResultVO pageResultVO = new PageResultVO();
        pageResultVO.setList(userPage.getRecords()); // 用户数据列表
        pageResultVO.setPageNo((int) userPage.getCurrent()); // 当前页码（1基）
        pageResultVO.setPageSize((int) userPage.getSize()); // 每页条数
        pageResultVO.setPageTotal((int) userPage.getPages()); // 总页数
        pageResultVO.setTotalCount((int) userPage.getTotal());

        return pageResultVO;
    }

    @Override
    public Boolean updateUserStatus(String userId, Integer status) {
        return userInfoService.update(Wrappers.<UserInfo>lambdaUpdate()
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getStatus, status));
    }

    @Override
    public String hi() {
        return "hello";
    }
}
