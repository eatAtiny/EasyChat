package com.easychat.gateway.filter;


import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.common.utils.RedisUtils;

import com.easychat.gateway.config.AuthProperties;
import com.easychat.gateway.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final RedisUtils<TokenUserInfoDTO> redisUtils;

    /**
     * 全局过滤器，用于校验请求是否携带token，其中登录注册路径无需拦截
     * @param exchange 服务器Web交换对象，包含请求和响应信息
     * @param chain 网关过滤器链，用于继续处理请求
     * @return 处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取Request
        ServerHttpRequest request = exchange.getRequest();
        // 2.判断是否不需要拦截
        if(isExclude(request.getPath().toString())){
            // 无需拦截，直接放行
            return chain.filter(exchange);
        }
        // 3.获取请求头中的token
        String token = null;
        List<String> headers = request.getHeaders().get("token");
        if (!CollectionUtils.isEmpty(headers)) {
            token = headers.get(0);
        }
        // 4.校验并解析token
        TokenUserInfoDTO tokenUserInfoDTO = redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        log.info("解析token:{}",tokenUserInfoDTO);

        // 4.1 如果token无效，拦截
        if (tokenUserInfoDTO == null) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String message = "{\"code\":401,\"message\":\"Token无效或已过期\"}";
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(message.getBytes());
            return response.writeWith(Mono.just(buffer));
        }
        // 4.2 如果token有效，继续校验能否访问管理员端口
        if (!tokenUserInfoDTO.getAdmin() && isAdminPath(request.getPath().toString())) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String message = "{\"code\":403,\"message\":\"权限不足，需要管理员权限\"}";
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(message.getBytes());
            return response.writeWith(Mono.just(buffer));
        }
        // 5.如果有效，传递用户信息
        ServerWebExchange ex = exchange.mutate()
                .request(builder -> builder.header(Constants.USER_ID, tokenUserInfoDTO.getUserId()))
                .request(builder -> builder.header(Constants.USER_NICK_NAME, tokenUserInfoDTO.getNickName()))
                .build();
        System.out.println("userId = " + tokenUserInfoDTO.getUserId());
        // 6.放行
        return chain.filter(ex);
    }

    private boolean isExclude(String antPath) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }
        return false;
    }

    private boolean isAdminPath(String antPath) {
        for (String pathPattern : authProperties.getAdminPaths()) {
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}