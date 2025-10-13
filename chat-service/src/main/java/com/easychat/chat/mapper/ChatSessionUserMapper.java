package com.easychat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easychat.common.entity.po.ChatSessionUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {
}
