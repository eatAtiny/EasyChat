package com.easychat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easychat.common.entity.po.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
