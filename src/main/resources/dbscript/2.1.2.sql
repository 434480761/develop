---------新增表
CREATE TABLE `resources_sharing` (`identifier` CHAR(36) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NOT NULL , `title` VARCHAR(1024) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL , `description` TEXT CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL , `res_type` VARCHAR(50) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL COMMENT '资源类型' , `resource` CHAR(36) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL COMMENT '资源id' , `protect_passwd` VARCHAR(100) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL COMMENT '分享密码' , `sharer_id` VARCHAR(50) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL COMMENT '分享者userid' , `sharer_name` VARCHAR(50) CHARACTER
                                                 SET utf8 COLLATE utf8_general_ci NULL COMMENT '分享者name' , `sharing_time` BIGINT(20) NULL COMMENT '分享时间') ENGINE = InnoDB CHARACTER
SET utf8 COLLATE utf8_general_ci COMMENT = '资源分享';
-----新增索引
ALTER TABLE `resources_sharing` ADD PRIMARY KEY(`identifier`);
ALTER TABLE `resources_sharing` ADD INDEX `index_res_type_and_resource` (`res_type`, `resource`) COMMENT '';
ALTER TABLE `resources_sharing` ADD INDEX `index_protect_passwd` (`protect_passwd`) COMMENT '';
ALTER TABLE `resources_sharing` ADD INDEX `index_sharing_id` (`sharer_id`) COMMENT '';