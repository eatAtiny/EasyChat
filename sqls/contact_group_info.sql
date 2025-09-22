create table contact_group_info
(
    group_id       varchar(12) collate utf8mb4_general_ci  not null comment '群ID'
        primary key,
    group_name     varchar(20) collate utf8mb4_general_ci  null comment '群组名',
    group_owner_id varchar(12) collate utf8mb4_general_ci  null comment '群主id',
    group_notice   varchar(500) collate utf8mb4_general_ci null comment '群公告'
)
    charset = utf8mb4;

