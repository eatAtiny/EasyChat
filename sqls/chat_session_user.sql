create table chat_session_user
(
    user_id      varchar(12) collate utf8mb4_general_ci not null comment '用户ID',
    contact_id   varchar(12) collate utf8mb4_general_ci not null comment '联系人ID',
    session_id   varchar(32) collate utf8mb4_general_ci not null comment '会话ID',
    contact_name varchar(20) collate utf8mb4_general_ci null comment '联系人名称',
    primary key (user_id, contact_id)
)
    comment '会话用户' charset = utf8mb4;

create index idx_session_id
    on chat_session_user (session_id);

create index idx_user_id
    on chat_session_user (user_id);

