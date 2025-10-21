package com.easychat.admin;

import com.easychat.common.api.UserInfoDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminServiceApplicationTests {
    @DubboReference
    private UserInfoDubboService userInfoDubboService;

    @Test
    void contextLoads() {
    }

    @Test
    void testDubbo(){
        System.out.println(userInfoDubboService.hi());
    }
}
