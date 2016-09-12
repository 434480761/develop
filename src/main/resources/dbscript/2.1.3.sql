----智慧教室 统计表
CREATE TABLE `icrs_resource` (`identifier` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NULL , `title` VARCHAR(500) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL , `description` TEXT CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL , `res_uuid` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源UUID' , `res_type` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源类型' , `school_id` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学校ID' , `teacher_id` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教师ID' , `teacher_name` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教师姓名' , `teachmaterial_uuid` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教材UUID' , `chapter_uuid` CHAR(36) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教材章节UUID' , `grade_code` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '年级CODE' , `subject_code` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学科CODE' , `create_time` BIGINT(20) NOT NULL COMMENT '创建时间' , `create_date` VARCHAR(50) CHARACTER
                                              SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '创建日期' , `create_hour` TINYINT(4) NOT NULL COMMENT '创建时段') ENGINE = InnoDB CHARACTER
SET utf8 COLLATE utf8_general_ci COMMENT = '智慧课堂的数据统计';
----索引
ALTER TABLE `icrs_resource` ADD PRIMARY KEY(`identifier`);