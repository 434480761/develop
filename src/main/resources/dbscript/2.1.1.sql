INSERT into synchronized_table SET pid = 5, value =0   //个环境只允许一个任务(@MarkAspect4ImportData)

---库分享表
CREATE TABLE `coverages_sharing` (`identifier` CHAR(36) CHARACTER
                                                  SET utf8 COLLATE utf8_general_ci NOT NULL , `title` VARCHAR(1024) CHARACTER
                                                  SET utf8 COLLATE utf8_general_ci NULL , `description` TEXT CHARACTER
                                                  SET utf8 COLLATE utf8_general_ci NULL , `source_coverage` VARCHAR(500) CHARACTER
                                                  SET utf8 COLLATE utf8_general_ci NULL COMMENT '分享来源' , `target_coverage` VARCHAR(500) CHARACTER
                                                  SET utf8 COLLATE utf8_general_ci NULL COMMENT '分享对象' , `creator` VARCHAR(255) NULL , `create_time` BIGINT(20) NULL) ENGINE = InnoDB CHARACTER
SET utf8 COLLATE utf8_general_ci COMMENT = '库分享';

ALTER TABLE `coverages_sharing` ADD PRIMARY KEY(`identifier`);
ALTER TABLE `coverages_sharing` ADD INDEX `index_source_coverage` (`source_coverage`) COMMENT '';
ALTER TABLE `coverages_sharing` ADD INDEX `index_target_coverage` (`target_coverage`) COMMENT '';
ALTER TABLE `coverages_sharing` ADD INDEX `index_source_and_target` (`source_coverage`, `target_coverage`) COMMENT '';

--增加分区
ALTER TABLE ndresource ADD PARTITION (PARTITION p_metacurriculums VALUES in ('metacurriculums') ENGINE = InnoDB);
ALTER TABLE resource_categories ADD PARTITION (PARTITION p_metacurriculums VALUES in ('metacurriculums') ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_0 VALUES IN (('metacurriculums','assets')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_1 VALUES IN (('metacurriculums','coursewares')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_2 VALUES IN (('metacurriculums','coursewareobjects')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_3 VALUES IN (('metacurriculums','coursewareobjecttemplates')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_4 VALUES IN (('metacurriculums','ebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_5 VALUES IN (('metacurriculums','homeworks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_6 VALUES IN (('metacurriculums','instructionalobjectives')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_7 VALUES IN (('metacurriculums','knowledges')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_8 VALUES IN (('metacurriculums','lessonplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_9 VALUES IN (('metacurriculums','learningplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_10 VALUES IN (('metacurriculums','lessons')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_11 VALUES IN (('metacurriculums','questions')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_12 VALUES IN (('metacurriculums','teachingmaterials')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_13 VALUES IN (('metacurriculums','chapters')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_17 VALUES IN (('metacurriculums','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_18 VALUES IN (('metacurriculums','tools')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_19 VALUES IN (('metacurriculums','guidancebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_20 VALUES IN (('metacurriculums','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_21 VALUES IN (('metacurriculums','examinationpapers')) ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_assets_21 VALUES IN (('assets','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_coursewares_21 VALUES IN (('coursewares','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwot_21 VALUES IN (('coursewareobjecttemplates','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_ebooks_21 VALUES IN (('ebooks','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_homeworks_21 VALUES IN (('homeworks','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_io_21 VALUES IN (('instructionalobjectives','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_knowledges_21 VALUES IN (('knowledges','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessonplans_21 VALUES IN (('lessonplans','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_learningplans_21 VALUES IN (('learningplans','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessons_21 VALUES IN (('lessons','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tm_21 VALUES IN (('teachingmaterials','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_chapters_21 VALUES IN (('chapters','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_guidancebooks_21 VALUES IN (('guidancebooks','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tools_21 VALUES IN (('tools','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_21 VALUES IN (('teachingactivities','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_21 VALUES IN (('examinationpapers','metacurriculums')) ENGINE = InnoDB);

--习题库
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwo_21 VALUES IN (('coursewareobjects','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_questions_21 VALUES IN (('questions','metacurriculums')) ENGINE = InnoDB);

