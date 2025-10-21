create table chat_session
(
    session_id        varchar(32) collate utf8mb4_general_ci  not null comment '会话ID'
        primary key,
    last_message      varchar(500) collate utf8mb4_general_ci null comment '最后接受的消息',
    last_receive_time bigint                                  null comment '最后接受消息时间毫秒'
)
    comment '会话信息' charset = utf8mb4;

