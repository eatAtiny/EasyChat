package com.easychat.common.filter;

import com.easychat.common.utils.UserContext;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo用户信息传递过滤器
 * 用于在Dubbo调用链中传递用户信息
 */
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER}, order = -10000)
public class UserInfoDubboFilter implements Filter {

    private static final String USER_ID_KEY = "userId";
    private static final String NICK_NAME_KEY = "nickName";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 消费者端：传递用户信息到Dubbo上下文
        if (RpcContext.getServiceContext().isConsumerSide()) {
            String userId = UserContext.getUser();
            String nickName = UserContext.getNickName();
            
            if (userId != null) {
                // 设置用户信息到Dubbo隐式参数
                RpcContext.getClientAttachment().setAttachment(USER_ID_KEY, userId);
                if (nickName != null) {
                    RpcContext.getClientAttachment().setAttachment(NICK_NAME_KEY, nickName);
                }
                System.out.println("Dubbo消费者端设置用户信息: userId=" + userId + ", nickName=" + nickName);
            }
        }

        // 提供者端：从Dubbo上下文获取用户信息并设置到ThreadLocal
        if (RpcContext.getServiceContext().isProviderSide()) {
            String userId = invocation.getAttachment(USER_ID_KEY);
            String nickName = invocation.getAttachment(NICK_NAME_KEY);
            
            if (userId != null) {
                // 保存原始用户信息（用于嵌套调用场景）
                String originalUserId = UserContext.getUser();
                String originalNickName = UserContext.getNickName();
                
                // 设置新的用户信息
                UserContext.setUser(userId);
                if (nickName != null) {
                    UserContext.setNickName(nickName);
                }
                System.out.println("Dubbo提供者端获取用户信息: userId=" + userId + ", nickName=" + nickName);
                
                try {
                    // 执行Dubbo调用
                    Result result = invoker.invoke(invocation);
                    return result;
                } finally {
                    // 清理ThreadLocal，恢复原始用户信息
                    if (originalUserId != null) {
                        UserContext.setUser(originalUserId);
                        UserContext.setNickName(originalNickName);
                    } else {
                        UserContext.removeUser();
                        UserContext.removeNickName();
                    }
                    System.out.println("Dubbo提供者端清理用户信息");
                }
            }
        }

        // 如果没有用户信息，直接执行调用
        return invoker.invoke(invocation);
    }
}
