package com.easychat.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.easychat.common.constants.Constants;
import com.easychat.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@Slf4j
@Component
public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.记录所有请求头信息，用于调试
        log.info("UserInfoInterceptor preHandle - 请求URL: {}", request.getRequestURL().toString());
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headersInfo = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headersInfo.append(headerName).append(":").append(headerValue).append(", ");
        }
        log.info("UserInfoInterceptor preHandle - 所有请求头: {}", headersInfo.toString());
        
        // 2.获取请求头中的用户信息
        String userInfo = request.getHeader(Constants.USER_ID);
        String nickName = request.getHeader(Constants.USER_NICK_NAME);
        log.info("UserInfoInterceptor preHandle - userInfo: {}, nickName: {}", userInfo, nickName);
        
        // 3.判断是否为空
        if (StrUtil.isNotBlank(userInfo)) {
            // 不为空，保存到ThreadLocal
            UserContext.setUser(userInfo);
            UserContext.setNickName(nickName);
            log.info("UserInfoInterceptor preHandle - 用户信息已设置到UserContext: userId={}, nickName={}", userInfo, nickName);
        } else {
            log.warn("UserInfoInterceptor preHandle - 请求头中未获取到用户信息(userId为空)");
        }
        
        // 4.放行
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 1.清除ThreadLocal中的数据
        log.info("UserInfoInterceptor afterCompletion - 清除ThreadLocal中的用户数据");
        UserContext.removeUser();
        UserContext.removeNickName();
    }
}