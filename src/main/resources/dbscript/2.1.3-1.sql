----智慧教室 统计表
CREATE TABLE `icrs_resource` (`identifier` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL , `title` VARCHAR(500) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL , `description` TEXT CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL , `res_uuid` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '资源UUID' , `res_type` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '资源类型' , `school_id` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '学校ID' , `teacher_id` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '教师ID' , `teacher_name` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '教师姓名' , `teachmaterial_uuid` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '教材UUID' , `chapter_uuid` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '教材章节UUID' , `grade_code` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '年级CODE' , `subject_code` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '学科CODE' , `create_time` BIGINT(20) NULL COMMENT '创建时间' , `create_date` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL COMMENT '创建日期' , `create_hour` TINYINT(4) NULL COMMENT '创建时段') ENGINE = InnoDB CHARACTER
SET utf8 COLLATE utf8_general_ci COMMENT = '智慧课堂的数据统计';
----索引
ALTER TABLE `icrs_resource` ADD PRIMARY KEY(`identifier`);

--------ICRS同步错误记录表
CREATE TABLE `icrs_sync_error` (`identifier` CHAR(36) CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL , `title` VARCHAR(500) CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL , `description` TEXT CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL , `res_type` VARCHAR(20) CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL COMMENT '资源类型' , `res_uuid` CHAR(36) CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL COMMENT '资源UUID' , `create_time` BIGINT(20) NULL COMMENT '资源创建时间' , `target` VARCHAR(255) CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL COMMENT '覆盖范围中的target' , `error_message` TEXT CHARACTER
                                               SET utf8 COLLATE utf8_general_ci NULL COMMENT '错误信息') ENGINE = InnoDB CHARACTER
SET utf8 COLLATE utf8_general_ci COMMENT = 'ICRS同步错误记录表';

ALTER TABLE `icrs_sync_error` ADD PRIMARY KEY(`identifier`);