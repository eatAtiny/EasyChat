package com.easychat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easychat.common.entity.po.ChatSessionUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {

    /**
     * 根据用户ID查询会话用户列表
     * @param userId 用户ID
     * @return 会话用户列表
     */
    List<ChatSessionUser> selectListByUserId(@Param("userId") String userId);
}
