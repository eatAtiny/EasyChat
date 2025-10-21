create table user_contact
(
    user_id          varchar(12) collate utf8mb4_general_ci not null comment '用户ID',
    contact_id       varchar(12) collate utf8mb4_general_ci not null comment '联系人ID或者群组ID',
    contact_type     tinyint(1)                             null comment '联系人类型 0:好友 1:群组',
    create_time      datetime                               null comment '创建时间',
    status           tinyint(1)                             null comment '状态 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑',
    last_update_time timestamp                              null on update CURRENT_TIMESTAMP comment '最后更新时间',
    primary key (user_id, contact_id)
)
    comment '联系人' charset = utf8mb4;

create index idx_contact_id
    on user_contact (contact_id);

INSERT INTO contact.contact (user_id, contact_id, contact_type, create_time, status, last_update_time) VALUES ('U34172864469', 'G72028774254', 1, '2025-09-21 22:13:56', 1, '2025-09-21 22:13:56');
INSERT INTO contact.contact (user_id, contact_id, contact_type, create_time, status, last_update_time) VALUES ('U34172864469', 'U61031033269', 0, '2025-09-21 22:21:42', 1, '2025-09-21 22:21:42');
INSERT INTO contact.contact (user_id, contact_id, contact_type, create_time, status, last_update_time) VALUES ('U61031033269', 'G72028774254', 1, '2025-09-21 22:10:35', null, '2025-09-21 22:10:35');
INSERT INTO contact.contact (user_id, contact_id, contact_type, create_time, status, last_update_time) VALUES ('U61031033269', 'U34172864469', 0, '2025-09-21 22:21:10', 1, '2025-09-21 22:21:10');
