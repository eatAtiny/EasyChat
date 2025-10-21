create table group_info
(
    group_id       varchar(12) collate utf8mb4_general_ci  not null comment '群ID'
        primary key,
    group_name     varchar(20) collate utf8mb4_general_ci  null comment '群组名',
    group_owner_id varchar(12) collate utf8mb4_general_ci  null comment '群主id',
    create_time    datetime                                null comment '创建时间',
    group_notice   varchar(500) collate utf8mb4_general_ci null comment '群公告',
    join_type      tinyint(1)                              null comment '0:直接加入 1:管理员同意后加入',
    status         tinyint(1) default 1                    null comment '状态 1:正常 0:解散'
)
    charset = utf8mb4;

INSERT INTO group.group_info (group_id, group_name, group_owner_id, create_time, group_notice, join_type, status) VALUES ('G58884090062', '嘻嘻哈哈', 'U61031033269', '2025-09-18 12:20:31', '随机公告', 1, 0);
INSERT INTO group.group_info (group_id, group_name, group_owner_id, create_time, group_notice, join_type, status) VALUES ('G72028774254', '108', 'U61031033269', '2025-09-21 22:10:35', '没啥', 0, 1);
