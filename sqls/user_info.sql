create table if not exists user_info
(
    user_id            varchar(12) collate utf8mb4_general_ci not null comment '用户ID'
        primary key,
    email              varchar(50) collate utf8mb4_general_ci null comment '邮箱',
    nick_name          varchar(20) collate utf8mb4_general_ci null comment '昵称',
    join_type          tinyint(1)                             null comment '0:直接加入  1:同意后加好友',
    sex                tinyint(1)                             null comment '性别 0:女 1:男',
    password           varchar(32) collate utf8mb4_general_ci null comment '密码',
    personal_signature varchar(50) collate utf8mb4_general_ci null comment '个性签名',
    status             tinyint(1)                             null comment '状态',
    create_time        datetime                               null comment '创建时间',
    last_login_time    datetime                               null comment '最后登录时间',
    area_name          varchar(50) collate utf8mb4_general_ci null comment '地区',
    area_code          varchar(50) collate utf8mb4_general_ci null comment '地区编号',
    last_off_time      bigint                                 null comment '最后离开时间',
    constraint idx_key_email
        unique (email)
)
    comment '用户信息' charset = utf8mb4;

INSERT INTO user.user_info (user_id, email, nick_name, join_type, sex, password, personal_signature, status, create_time, last_login_time, area_name, area_code, last_off_time) VALUES ('U34172864469', 'user01@gmail.com', '小方', 1, 0, '4da49c16db42ca04538d629ef0533fe8', '这个人很懒，没有留下什么~', 1, '2025-09-18 20:05:15', '2025-09-18 20:05:15', '未知', '000000', 1758197115492);
INSERT INTO user.user_info (user_id, email, nick_name, join_type, sex, password, personal_signature, status, create_time, last_login_time, area_name, area_code, last_off_time) VALUES ('U61031033269', 'admin666@qq.com', 'admin', 1, 0, '8a30ec6807f71bc69d096d8e4d501ade', '这个人很懒，没有留下什么~', 1, '2025-09-17 20:41:10', '2025-09-17 20:41:10', '未知', '000000', 1758112869844);