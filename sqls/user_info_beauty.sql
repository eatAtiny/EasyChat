create table user_info_beauty
(
    id      int auto_increment comment '自增ID'
        primary key,
    email   varchar(50) collate utf8mb4_general_ci not null comment '邮箱',
    user_id varchar(12) collate utf8mb4_general_ci not null comment '用户ID',
    status  tinyint(1) default 0                   null comment '0：未使用 1：已使用',
    constraint idx_key_email
        unique (email),
    constraint idx_key_user_id
        unique (user_id)
)
    comment '靓号表' charset = utf8mb4;

