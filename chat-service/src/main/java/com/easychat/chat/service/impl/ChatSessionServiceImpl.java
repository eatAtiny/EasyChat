package com.easychat.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.chat.mapper.ChatSessionMapper;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.chat.service.ChatSessionService;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {
}
