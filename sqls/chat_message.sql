create table chat_message
(
    message_id          bigint auto_increment comment '消息自增ID'
        primary key,
    session_id          varchar(32) collate utf8mb4_general_ci  not null comment '会话ID',
    message_type        tinyint(1)                              not null comment '消息类型',
    message_content     varchar(500) collate utf8mb4_general_ci null comment '消息内容',
    send_user_id        varchar(12) collate utf8mb4_general_ci  null comment '发送人ID',
    send_user_nick_name varchar(20) collate utf8mb4_general_ci  null comment '发送人昵称',
    send_time           bigint                                  null comment '发送时间',
    contact_id          varchar(12) collate utf8mb4_general_ci  not null comment '接收联系人ID',
    contact_type        tinyint(1)                              null comment '联系人类型 0:单聊 1:群聊',
    file_size           bigint                                  null comment '文件大小',
    file_name           varchar(200) collate utf8mb4_general_ci null comment '文件名',
    file_type           tinyint(1)                              null comment '文件类型',
    status              tinyint(1) default 1                    null comment '状态 0:正在发送 1:已发送'
)
    comment '聊天消息表' charset = utf8mb4;

create index idx_receive_contact_id
    on chat_message (contact_id);

create index idx_send_time
    on chat_message (send_time);

create index idx_send_user_id
    on chat_message (send_user_id);

create index idx_session_id
    on chat_message (session_id);

