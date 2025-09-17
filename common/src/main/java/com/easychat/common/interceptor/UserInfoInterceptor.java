package com.easychat.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.easychat.common.constants.Constants;
import com.easychat.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的用户信息
        String userInfo = request.getHeader(Constants.USER_ID);
        String nickName = request.getHeader(Constants.USER_NICK_NAME);
        // 2.判断是否为空
        if (StrUtil.isNotBlank(userInfo)) {
            // 不为空，保存到ThreadLocal
            UserContext.setUser(userInfo);
            UserContext.setNickName(nickName);
            System.out.println("用户信息：" + userInfo);
        }
        // 3.放行
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 1.清除ThreadLocal中的数据
        UserContext.removeUser();
        UserContext.removeNickName();
    }
}
