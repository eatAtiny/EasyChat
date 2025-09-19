package com.easychat.common.config;

import cn.hutool.core.util.StrUtil;
import com.easychat.common.constants.Constants;
import com.easychat.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户
                String userId = UserContext.getUser();
                String nickName = UserContext.getNickName();
                if(userId == null) {
                    // 如果为空则直接跳过
                    return;
                }
                // 如果不为空则放入请求头中，传递给下游微服务
                template.header(Constants.USER_ID, userId);
                template.header(Constants.USER_NICK_NAME,  nickName);

            }
        };
    }
}
