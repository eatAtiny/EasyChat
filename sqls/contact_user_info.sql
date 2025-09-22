create table if not exists contact_user_info
(
    user_id            varchar(12) collate utf8mb4_general_ci not null comment '用户ID'
    primary key,
    nick_name          varchar(20) collate utf8mb4_general_ci null comment '昵称',
    sex                tinyint(1)                             null comment '性别 0:女 1:男',
    personal_signature varchar(50) collate utf8mb4_general_ci null comment '个性签名',
    status             tinyint(1)                             null comment '状态',
    create_time        datetime                               null comment '创建时间',
    last_login_time    datetime                               null comment '最后登录时间',
    area_name          varchar(50) collate utf8mb4_general_ci null comment '地区',
    area_code          varchar(50) collate utf8mb4_general_ci null comment '地区编号',
    last_off_time      bigint                                 null comment '最后离开时间'
    )
    comment '用户信息' charset = utf8mb4;
