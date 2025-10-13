package com.easychat.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.chat.mapper.ChatSessionUserMapper;
import com.easychat.common.entity.po.ChatSessionUser;
import com.easychat.chat.service.ChatSessionUserService;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser> implements ChatSessionUserService {
}
