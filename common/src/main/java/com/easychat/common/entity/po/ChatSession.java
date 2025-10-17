package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;


/**
 * 会话信息
 */
@Data
public class ChatSession implements Serializable {


    /**
     * 会话ID
     */
    @TableId(type = IdType.INPUT)
    private String sessionId;

    /**
     * 最后接受的消息
     */
    private String lastMessage;

    /**
     * 最后接受消息时间毫秒
     */
    private Long lastReceiveTime;


    @Override
    public String toString() {
        return "会话ID:" + (sessionId == null ? "空" : sessionId) + "，最后接受的消息:" + (lastMessage == null ? "空" : lastMessage) + "，最后接受消息时间毫秒:" + (lastReceiveTime == null ?
                "空" : lastReceiveTime);
    }
}
