create table app_update
(
    id            int auto_increment comment '自增ID'
        primary key,
    version       varchar(10) collate utf8mb4_general_ci   null comment '版本号',
    update_desc   varchar(500) collate utf8mb4_general_ci  null comment '更新描述',
    create_time   datetime                                 null comment '创建时间',
    status        tinyint(1)                               null comment '0:未发布 1:灰度发布 2:全网发布',
    grayscale_uid varchar(1000) collate utf8mb4_general_ci null comment '灰度uid',
    file_type     tinyint(1)                               null comment '文件类型0:本地文件 1:外链',
    outer_link    varchar(200) collate utf8mb4_general_ci  null comment '外链地址',
    constraint idx_key
        unique (version)
)
    comment 'app发布' charset = utf8mb4;

