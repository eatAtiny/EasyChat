create table user_contact_apply
(
    apply_id        int auto_increment comment '自增ID'
        primary key,
    apply_user_id   varchar(12) collate utf8mb4_general_ci  not null comment '申请人id',
    receive_user_id varchar(12) collate utf8mb4_general_ci  not null comment '接收人ID',
    contact_type    tinyint(1)                              not null comment '联系人类型 0:好友 1:群组',
    contact_id      varchar(12) collate utf8mb4_general_ci  null comment '联系人群组ID',
    last_apply_time bigint                                  null comment '最后申请时间',
    status          tinyint(1)                              null comment '状态0:待处理 1:已同意  2:已拒绝 3:已拉黑',
    apply_info      varchar(100) collate utf8mb4_general_ci null comment '申请信息',
    constraint idx_key
        unique (apply_user_id, receive_user_id, contact_id)
)
    comment '联系人申请' charset = utf8mb4;

create index idx_last_apply_time
    on user_contact_apply (last_apply_time);

INSERT INTO contact.contact_apply (apply_id, apply_user_id, receive_user_id, contact_type, contact_id, last_apply_time, status, apply_info) VALUES (136923, 'U34172864469', 'U61031033269', 1, 'G58884090062', 1758276359521, 1, '我是小方');
INSERT INTO contact.contact_apply (apply_id, apply_user_id, receive_user_id, contact_type, contact_id, last_apply_time, status, apply_info) VALUES (136924, 'U61031033269', 'U34172864469', 0, 'U34172864469', 1758276461784, 1, '我是admin');
INSERT INTO contact.contact_apply (apply_id, apply_user_id, receive_user_id, contact_type, contact_id, last_apply_time, status, apply_info) VALUES (136925, 'U34172864469', 'U34172864469', 0, 'U34172864469', 1758436980190, 0, '我是undefined');
