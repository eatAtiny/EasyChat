package com.easychat.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.chat.mapper.ChatMessageMapper;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.chat.service.ChatMessageService;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
}
