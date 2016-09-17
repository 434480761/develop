--任务表增加优先级字段及索引
ALTER TABLE `task_status_infos` ADD `priority` INT(5) NOT NULL DEFAULT '0' AFTER `task_id`;
ALTER TABLE `task_status_infos` DROP INDEX `status`, ADD INDEX `status` (`status`, `priority`, `update_time`);

--增加教学活动资源
CREATE TABLE `teaching_activities` (
  `identifier` char(36) NOT NULL,
  `description` text,
  `record_status` int(11) DEFAULT '0',
  `title` text,
  `create_time` datetime DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  `categories` text,
  `keywords` text,
  `relations` text,
  `tags` text,
  `enable` tinyint(1) DEFAULT '1',
  `elanguage` varchar(255) DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  `provider` varchar(1024) DEFAULT NULL,
  `provider_source` varchar(1500) DEFAULT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  `estatus` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `md5` text,
  `preview` text,
  `stored_info` text,
  `format` varchar(255) DEFAULT NULL,
  `href` varchar(255) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `subtype` int(11) DEFAULT NULL,
  `lesson_objectives` text,
  `typical_age_range` text,
  `difficulty` varchar(255) DEFAULT NULL,
  `typical_learning_time` varchar(255) DEFAULT NULL,
  `res_coverages` text,
  `author` varchar(255) DEFAULT NULL,
  `cr_description` varchar(255) DEFAULT NULL,
  `cr_right` varchar(255) DEFAULT NULL,
  `age_range` varchar(255) DEFAULT NULL,
  `context` varchar(255) DEFAULT NULL,
  `edu_description` varchar(255) DEFAULT NULL,
  `edu_language` varchar(255) DEFAULT NULL,
  `end_user_type` varchar(255) DEFAULT NULL,
  `interactivity` int(11) DEFAULT NULL,
  `interactivity_level` int(11) DEFAULT NULL,
  `rep_description` varchar(255) DEFAULT NULL,
  `repository_admin` varchar(255) DEFAULT NULL,
  `repository_name` varchar(255) DEFAULT NULL,
  `semantic_density` int(11) DEFAULT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `target` varchar(255) DEFAULT NULL,
  `target_type` varchar(255) DEFAULT NULL,
  `learning_time` varchar(255) DEFAULT NULL,
  `custom_properties` varchar(3000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `teaching_activities`
  ADD PRIMARY KEY (`identifier`);
  

--增加分区
ALTER TABLE ndresource ADD PARTITION (PARTITION p_teachingactivities VALUES in ('teachingactivities') ENGINE = InnoDB);

ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_0 VALUES IN (('teachingactivities','assets')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_1 VALUES IN (('teachingactivities','coursewares')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_2 VALUES IN (('teachingactivities','coursewareobjects')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_3 VALUES IN (('teachingactivities','coursewareobjecttemplates')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_4 VALUES IN (('teachingactivities','ebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_5 VALUES IN (('teachingactivities','homeworks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_6 VALUES IN (('teachingactivities','instructionalobjectives')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_7 VALUES IN (('teachingactivities','knowledges')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_8 VALUES IN (('teachingactivities','lessonplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_9 VALUES IN (('teachingactivities','learningplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_10 VALUES IN (('teachingactivities','lessons')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_11 VALUES IN (('teachingactivities','questions')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_12 VALUES IN (('teachingactivities','teachingmaterials')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_13 VALUES IN (('teachingactivities','chapters')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_17 VALUES IN (('teachingactivities','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_18 VALUES IN (('teachingactivities','tools')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_19 VALUES IN (('teachingactivities','guidancebooks')) ENGINE = InnoDB);

ALTER TABLE resource_relations ADD PARTITION (PARTITION p_assets_19 VALUES IN (('assets','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_coursewares_19 VALUES IN (('coursewares','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwo_19 VALUES IN (('coursewareobjects','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwot_19 VALUES IN (('coursewareobjecttemplates','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_ebooks_19 VALUES IN (('ebooks','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_homeworks_19 VALUES IN (('homeworks','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_io_19 VALUES IN (('instructionalobjectives','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_knowledges_19 VALUES IN (('knowledges','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessonplans_19 VALUES IN (('lessonplans','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_learningplans_19 VALUES IN (('learningplans','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessons_19 VALUES IN (('lessons','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_questions_19 VALUES IN (('questions','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tm_19 VALUES IN (('teachingmaterials','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_chapters_19 VALUES IN (('chapters','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_guidancebooks_19 VALUES IN (('guidancebooks','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tools_19 VALUES IN (('tools','teachingactivities')) ENGINE = InnoDB);