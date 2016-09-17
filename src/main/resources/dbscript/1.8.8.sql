--增加密钥字段
ALTER TABLE `tech_infos` ADD `secure_key` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `requirements`;

--表增加primary_category字段
ALTER TABLE `resource_categories` ADD `primary_category` VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'other' COMMENT '资源主分类' ;

--修复primary_category值
UPDATE resource_categories rc,ndresource nd SET rc.primary_category=nd.primary_category WHERE rc.resource = nd.identifier;

--将原来的`resource_categories` 表重命名(`resource_categories_bak`)

--新建`resource_categories` 表，有分区的(debug)
CREATE TABLE IF NOT EXISTS `resource_categories` (
  `identifier` char(36) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  `record_status` int(11) DEFAULT '0',
  `title` varchar(50) DEFAULT NULL,
  `category_code` varchar(30) DEFAULT NULL,
  `category_name` varchar(50) DEFAULT NULL,
  `resource` char(36) DEFAULT NULL,
  `short_name` varchar(100) DEFAULT NULL,
  `taxOnCode` varchar(30) DEFAULT NULL,
  `taxoncodeid` char(36) DEFAULT NULL,
  `taxOnName` varchar(50) DEFAULT NULL,
  `taxonpath` varchar(255) DEFAULT NULL,
  `primary_category` varchar(50) DEFAULT 'other' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
PARTITION BY LIST  COLUMNS(primary_category)
(PARTITION p_assets VALUES IN ('assets') ENGINE = InnoDB,
 PARTITION p_coursewares VALUES IN ('coursewares') ENGINE = InnoDB,
 PARTITION p_cwo VALUES IN ('coursewareobjects') ENGINE = InnoDB,
 PARTITION p_cwot VALUES IN ('coursewareobjecttemplates') ENGINE = InnoDB,
 PARTITION p_ebooks VALUES IN ('ebooks') ENGINE = InnoDB,
 PARTITION p_homeworks VALUES IN ('homeworks') ENGINE = InnoDB,
 PARTITION p_io VALUES IN ('instructionalobjectives') ENGINE = InnoDB,
 PARTITION p_knowledges VALUES IN ('knowledges') ENGINE = InnoDB,
 PARTITION p_lessonplans VALUES IN ('lessonplans') ENGINE = InnoDB,
 PARTITION p_learningplans VALUES IN ('learningplans') ENGINE = InnoDB,
 PARTITION p_lessons VALUES IN ('lessons') ENGINE = InnoDB,
 PARTITION p_questions VALUES IN ('questions') ENGINE = InnoDB,
 PARTITION p_tm VALUES IN ('teachingmaterials') ENGINE = InnoDB,
 PARTITION p_chapters VALUES IN ('chapters') ENGINE = InnoDB,
 PARTITION p_ipt VALUES IN ('instructionalprototypes') ENGINE = InnoDB,
 PARTITION p_ipta VALUES IN ('instructionalprototypeactivities') ENGINE = InnoDB,
 PARTITION p_iptas VALUES IN ('instructionalprototypeactivitysteps') ENGINE = InnoDB,
 PARTITION p_guidancebooks VALUES IN ('guidancebooks') ENGINE = InnoDB,
 PARTITION p_tools VALUES IN ('tools') ENGINE = InnoDB,
 PARTITION p_other VALUES IN ('other') ENGINE = InnoDB,
 PARTITION p_teachingactivities VALUES IN ('teachingactivities') ENGINE = InnoDB);
ALTER TABLE `resource_categories`
  ADD KEY (`identifier`), ADD KEY `resource` (`resource`,`taxOnCode`,`taxonpath`), ADD KEY `ind_resource_categories_taxOnPath` (`taxonpath`), ADD KEY `ind_resource_categories_taxOnCode` (`taxOnCode`), ADD KEY `ind_resource_categories_resource` (`resource`);

--新建`resource_categories` 表，有分区的(pre)
CREATE TABLE IF NOT EXISTS `resource_categories` (
  `identifier` char(36) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  `record_status` int(11) DEFAULT '0',
  `title` varchar(50) DEFAULT NULL,
  `category_code` varchar(30) DEFAULT NULL,
  `category_name` varchar(50) DEFAULT NULL,
  `resource` char(36) DEFAULT NULL,
  `short_name` varchar(100) DEFAULT NULL,
  `taxOnCode` varchar(30) DEFAULT NULL,
  `taxoncodeid` char(36) DEFAULT NULL,
  `taxOnName` varchar(50) DEFAULT NULL,
  `taxonpath` varchar(255) DEFAULT NULL,
  `primary_category` varchar(50) DEFAULT 'other' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
PARTITION BY LIST  COLUMNS(primary_category)
(PARTITION p_assets VALUES IN ('assets') ENGINE = InnoDB,
 PARTITION p_coursewares VALUES IN ('coursewares') ENGINE = InnoDB,
 PARTITION p_cwo VALUES IN ('coursewareobjects') ENGINE = InnoDB,
 PARTITION p_cwot VALUES IN ('coursewareobjecttemplates') ENGINE = InnoDB,
 PARTITION p_ebooks VALUES IN ('ebooks') ENGINE = InnoDB,
 PARTITION p_homeworks VALUES IN ('homeworks') ENGINE = InnoDB,
 PARTITION p_io VALUES IN ('instructionalobjectives') ENGINE = InnoDB,
 PARTITION p_knowledges VALUES IN ('knowledges') ENGINE = InnoDB,
 PARTITION p_lessonplans VALUES IN ('lessonplans') ENGINE = InnoDB,
 PARTITION p_learningplans VALUES IN ('learningplans') ENGINE = InnoDB,
 PARTITION p_lessons VALUES IN ('lessons') ENGINE = InnoDB,
 PARTITION p_questions VALUES IN ('questions') ENGINE = InnoDB,
 PARTITION p_tm VALUES IN ('teachingmaterials') ENGINE = InnoDB,
 PARTITION p_chapters VALUES IN ('chapters') ENGINE = InnoDB,
 PARTITION p_ipt VALUES IN ('instructionalprototypes') ENGINE = InnoDB,
 PARTITION p_ipta VALUES IN ('instructionalprototypeactivities') ENGINE = InnoDB,
 PARTITION p_iptas VALUES IN ('instructionalprototypeactivitysteps') ENGINE = InnoDB,
 PARTITION p_guidancebooks VALUES IN ('guidancebooks') ENGINE = InnoDB,
 PARTITION p_tools VALUES IN ('tools') ENGINE = InnoDB,
 PARTITION p_other VALUES IN ('other') ENGINE = InnoDB,
 PARTITION p_teachingactivities VALUES IN ('teachingactivities') ENGINE = InnoDB);
ALTER TABLE `resource_categories`
  ADD KEY (`identifier`), ADD KEY `resource` (`resource`,`taxOnCode`,`taxonpath`), ADD KEY `ind_resource_categories_taxOnPath` (`taxonpath`), ADD KEY `ind_resource_categories_taxOnCode` (`taxOnCode`), ADD KEY `ind_resource_categories_resource` (`resource`);


--新建`resource_categories` 表，有分区的(pre_product)
CREATE TABLE IF NOT EXISTS `resource_categories` (
  `identifier` varchar(255) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `record_status` int(11) DEFAULT '0',
  `title` varchar(255) DEFAULT NULL,
  `resource` varchar(255) DEFAULT NULL,
  `taxOnCode` varchar(255) DEFAULT NULL,
  `taxOnName` varchar(255) DEFAULT NULL,
  `taxOnPath` varchar(255) DEFAULT NULL,
  `category_code` varchar(100) DEFAULT NULL,
  `category_name` varchar(100) DEFAULT NULL,
  `short_name` varchar(255) DEFAULT NULL,
  `taxoncodeid` varchar(255) DEFAULT NULL,
  `primary_category` varchar(50) DEFAULT 'other' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
PARTITION BY LIST  COLUMNS(primary_category)
(PARTITION p_assets VALUES IN ('assets') ENGINE = InnoDB,
 PARTITION p_coursewares VALUES IN ('coursewares') ENGINE = InnoDB,
 PARTITION p_cwo VALUES IN ('coursewareobjects') ENGINE = InnoDB,
 PARTITION p_cwot VALUES IN ('coursewareobjecttemplates') ENGINE = InnoDB,
 PARTITION p_ebooks VALUES IN ('ebooks') ENGINE = InnoDB,
 PARTITION p_homeworks VALUES IN ('homeworks') ENGINE = InnoDB,
 PARTITION p_io VALUES IN ('instructionalobjectives') ENGINE = InnoDB,
 PARTITION p_knowledges VALUES IN ('knowledges') ENGINE = InnoDB,
 PARTITION p_lessonplans VALUES IN ('lessonplans') ENGINE = InnoDB,
 PARTITION p_learningplans VALUES IN ('learningplans') ENGINE = InnoDB,
 PARTITION p_lessons VALUES IN ('lessons') ENGINE = InnoDB,
 PARTITION p_questions VALUES IN ('questions') ENGINE = InnoDB,
 PARTITION p_tm VALUES IN ('teachingmaterials') ENGINE = InnoDB,
 PARTITION p_chapters VALUES IN ('chapters') ENGINE = InnoDB,
 PARTITION p_ipt VALUES IN ('instructionalprototypes') ENGINE = InnoDB,
 PARTITION p_ipta VALUES IN ('instructionalprototypeactivities') ENGINE = InnoDB,
 PARTITION p_iptas VALUES IN ('instructionalprototypeactivitysteps') ENGINE = InnoDB,
 PARTITION p_guidancebooks VALUES IN ('guidancebooks') ENGINE = InnoDB,
 PARTITION p_tools VALUES IN ('tools') ENGINE = InnoDB,
 PARTITION p_other VALUES IN ('other') ENGINE = InnoDB,
 PARTITION p_teachingactivities VALUES IN ('teachingactivities') ENGINE = InnoDB);
ALTER TABLE `resource_categories`
  ADD KEY (`identifier`), ADD KEY `resource` (`resource`,`taxOnCode`,`taxonpath`), ADD KEY `ind_resource_categories_taxOnPath` (`taxonpath`), ADD KEY `ind_resource_categories_taxOnCode` (`taxOnCode`), ADD KEY `ind_resource_categories_resource` (`resource`);

--新建`resource_categories` 表，有分区的(product)
CREATE TABLE IF NOT EXISTS `resource_categories` (
  `identifier` varchar(255) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `record_status` int(11) DEFAULT '0',
  `title` varchar(255) DEFAULT NULL,
  `resource` varchar(255) DEFAULT NULL,
  `taxOnCode` varchar(255) DEFAULT NULL,
  `taxOnName` varchar(255) DEFAULT NULL,
  `taxOnPath` varchar(255) DEFAULT NULL,
  `category_code` varchar(100) DEFAULT NULL,
  `category_name` varchar(100) DEFAULT NULL,
  `short_name` varchar(255) DEFAULT NULL,
  `taxoncodeid` varchar(255) DEFAULT NULL,
  `primary_category` varchar(50) DEFAULT 'other' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
PARTITION BY LIST  COLUMNS(primary_category)
(PARTITION p_assets VALUES IN ('assets') ENGINE = InnoDB,
 PARTITION p_coursewares VALUES IN ('coursewares') ENGINE = InnoDB,
 PARTITION p_cwo VALUES IN ('coursewareobjects') ENGINE = InnoDB,
 PARTITION p_cwot VALUES IN ('coursewareobjecttemplates') ENGINE = InnoDB,
 PARTITION p_ebooks VALUES IN ('ebooks') ENGINE = InnoDB,
 PARTITION p_homeworks VALUES IN ('homeworks') ENGINE = InnoDB,
 PARTITION p_io VALUES IN ('instructionalobjectives') ENGINE = InnoDB,
 PARTITION p_knowledges VALUES IN ('knowledges') ENGINE = InnoDB,
 PARTITION p_lessonplans VALUES IN ('lessonplans') ENGINE = InnoDB,
 PARTITION p_learningplans VALUES IN ('learningplans') ENGINE = InnoDB,
 PARTITION p_lessons VALUES IN ('lessons') ENGINE = InnoDB,
 PARTITION p_questions VALUES IN ('questions') ENGINE = InnoDB,
 PARTITION p_tm VALUES IN ('teachingmaterials') ENGINE = InnoDB,
 PARTITION p_chapters VALUES IN ('chapters') ENGINE = InnoDB,
 PARTITION p_ipt VALUES IN ('instructionalprototypes') ENGINE = InnoDB,
 PARTITION p_ipta VALUES IN ('instructionalprototypeactivities') ENGINE = InnoDB,
 PARTITION p_iptas VALUES IN ('instructionalprototypeactivitysteps') ENGINE = InnoDB,
 PARTITION p_guidancebooks VALUES IN ('guidancebooks') ENGINE = InnoDB,
 PARTITION p_tools VALUES IN ('tools') ENGINE = InnoDB,
 PARTITION p_other VALUES IN ('other') ENGINE = InnoDB,
 PARTITION p_teachingactivities VALUES IN ('teachingactivities') ENGINE = InnoDB);
ALTER TABLE `resource_categories`
  ADD KEY (`identifier`), ADD KEY `resource` (`resource`,`taxOnCode`,`taxOnPath`), ADD KEY `category_code` (`category_code`,`resource`,`taxOnCode`,`taxOnPath`), ADD KEY `ind_resource_categories_taxOnPath` (`taxOnPath`), ADD KEY `ind_resource_categories_taxOnCode` (`taxOnCode`), ADD KEY `ind_resource_categories_resource` (`resource`);


--数据迁移
insert into `resource_categories` select * from `resource_categories_bak`; 



--习题库数据迁移

--(debug)from:qa_mysql_esp  to:qa_mysql_esp_quetion
--(integration)from :prepub_mysql_esp  to:prepub_mysql_esp_quetion
--(预生产) from:preproduction_mysql_esp  to:preproduction_mysql_esp_question
--(生产) from:esp_product  to:esp_product_question

--需要导数据的表分别为ndresource、questions、resource_categories、resource_relations、resource_statisticals、res_coverages、tech_infos、contributes、resource_annotations
--只需要导习题类型和课件颗粒的数据即可，查询的sql为



SELECT * from ndresource where primary_category='questions';

SELECT * from questions q;

SELECT q.* from resource_categories q where q.primary_category='questions';

SELECT q.* from resource_relations q,ndresource nd where nd.primary_category='questions' and q.res_type='questions' and q.source_uuid = nd.identifier;

SELECT q.* from resource_statisticals q,ndresource nd where nd.primary_category='questions' and q.res_type='questions' and q.resource = nd.identifier;

SELECT q.* from res_coverages q,ndresource nd where nd.primary_category='questions' and q.resource = nd.identifier;

SELECT q.* from tech_infos q,ndresource nd where nd.primary_category='questions' and q.res_type='questions' and q.resource = nd.identifier;

SELECT q.* from contributes q,ndresource nd where nd.primary_category='questions' and q.res_type='questions' and q.resource = nd.identifier;

SELECT q.* from resource_annotations q,ndresource nd where nd.primary_category='questions' and q.res_type='questions' and q.resource = nd.identifier;





SELECT * from ndresource where primary_category='coursewareobjects';

SELECT * from courseware_objects q;

SELECT q.* from resource_categories q where q.primary_category='coursewareobjects';

SELECT q.* from resource_relations q,ndresource nd where nd.primary_category='coursewareobjects' and q.res_type='coursewareobjects' and q.source_uuid = nd.identifier;

SELECT q.* from resource_statisticals q,ndresource nd where nd.primary_category='coursewareobjects' and q.res_type='coursewareobjects' and q.resource = nd.identifier;

SELECT q.* from res_coverages q,ndresource nd where nd.primary_category='coursewareobjects' and q.resource = nd.identifier;

SELECT q.* from tech_infos q,ndresource nd where nd.primary_category='coursewareobjects' and q.res_type='coursewareobjects' and q.resource = nd.identifier;

SELECT q.* from contributes q,ndresource nd where nd.primary_category='coursewareobjects' and q.res_type='coursewareobjects' and q.resource = nd.identifier;

SELECT q.* from resource_annotations q,ndresource nd where nd.primary_category='coursewareobjects' and q.res_type='coursewareobjects' and q.resource = nd.identifier;


--删除原先库里面的数据
--删除contributes表中的相关数据
DELETE from contributes where res_type in ('questions','coursewareobjects');

--删除res_coverages表中的相关数据
DELETE FROM `res_coverages` where res_type in ('questions','coursewareobjects');

--删除tech_infos表中的相关数据
DELETE FROM `tech_infos` where res_type in ('questions','coursewareobjects');

--删除resource_statisticals表中的相关数据
DELETE FROM `resource_statisticals` where res_type in ('questions','coursewareobjects');

--删除resource_categories相关的分区数据
ALTER TABLE `resource_categories` DROP PARTITION p_questions;
ALTER TABLE `resource_categories` DROP PARTITION p_cwo;

--删除resource_relations相关的分区数据
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_0;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_1;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_2;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_3;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_4;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_5;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_6;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_7;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_8;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_9;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_10;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_11;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_12;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_13;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_14;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_15;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_16;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_17;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_18;
ALTER TABLE `resource_relations` DROP PARTITION p_cwo_19;


ALTER TABLE `resource_relations` DROP PARTITION p_questions_0;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_1;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_2;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_3;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_4;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_5;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_6;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_7;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_8;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_9;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_10;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_11;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_12;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_13;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_14;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_15;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_16;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_17;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_18;
ALTER TABLE `resource_relations` DROP PARTITION p_questions_19;


--清空courseware_objects表数据
TRUNCATE table courseware_objects;

--清空questions表数据
TRUNCATE table questions;

--删除ndresource相关的分区数据
ALTER TABLE `ndresource` DROP PARTITION p_questions;
ALTER TABLE `ndresource` DROP PARTITION p_cwo;



