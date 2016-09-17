CREATE TABLE `resource_providers` ( `identifier` CHAR(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `title` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `description` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NULL , PRIMARY KEY (`identifier`) ) ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT = '资源提供商';

CREATE TABLE `copyright_owners` ( `identifier` CHAR(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `title` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `description` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NULL , PRIMARY KEY (`identifier`) ) ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT = '资源版权方';
--增加分区
ALTER TABLE ndresource ADD PARTITION (PARTITION p_examinationpapers VALUES in ('examinationpapers') ENGINE = InnoDB);
ALTER TABLE resource_categories ADD PARTITION (PARTITION p_examinationpapers VALUES in ('examinationpapers') ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_0 VALUES IN (('examinationpapers','assets')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_1 VALUES IN (('examinationpapers','coursewares')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_2 VALUES IN (('examinationpapers','coursewareobjects')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_3 VALUES IN (('examinationpapers','coursewareobjecttemplates')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_4 VALUES IN (('examinationpapers','ebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_5 VALUES IN (('examinationpapers','homeworks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_6 VALUES IN (('examinationpapers','instructionalobjectives')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_7 VALUES IN (('examinationpapers','knowledges')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_8 VALUES IN (('examinationpapers','lessonplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_9 VALUES IN (('examinationpapers','learningplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_10 VALUES IN (('examinationpapers','lessons')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_11 VALUES IN (('examinationpapers','questions')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_12 VALUES IN (('examinationpapers','teachingmaterials')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_13 VALUES IN (('examinationpapers','chapters')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_17 VALUES IN (('examinationpapers','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_18 VALUES IN (('examinationpapers','tools')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_19 VALUES IN (('examinationpapers','guidancebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_20 VALUES IN (('examinationpapers','teachingactivities')) ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_assets_20 VALUES IN (('assets','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_coursewares_20 VALUES IN (('coursewares','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwot_20 VALUES IN (('coursewareobjecttemplates','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_ebooks_20 VALUES IN (('ebooks','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_homeworks_20 VALUES IN (('homeworks','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_io_20 VALUES IN (('instructionalobjectives','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_knowledges_20 VALUES IN (('knowledges','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessonplans_20 VALUES IN (('lessonplans','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_learningplans_20 VALUES IN (('learningplans','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessons_20 VALUES IN (('lessons','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tm_20 VALUES IN (('teachingmaterials','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_chapters_20 VALUES IN (('chapters','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_guidancebooks_20 VALUES IN (('guidancebooks','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tools_20 VALUES IN (('tools','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_20 VALUES IN (('teachingactivities','examinationpapers')) ENGINE = InnoDB);

--习题库
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwo_20 VALUES IN (('coursewareobjects','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_questions_20 VALUES IN (('questions','examinationpapers')) ENGINE = InnoDB);

