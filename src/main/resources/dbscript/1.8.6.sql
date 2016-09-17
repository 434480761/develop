ALTER TABLE `static_datas_update` ADD `taskId` INT(20) NOT NULL FIRST, ADD `description` VARCHAR(250) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `taskId`, ADD PRIMARY KEY (`taskId`) ;

--
-- 转存表中的数据 `static_datas_update`
--

INSERT INTO `static_datas_update` (`taskId`, `description`, `last_update`) VALUES
(1, '开关静态变量的更新时间', 914197561),
(2, '访问控制策略的更新时间', 1000);

ALTER TABLE `static_datas_update` CHANGE `last_update` `last_update` BIGINT NOT NULL;

--
-- 表的结构 `third_party_bsys`  第三方服务系统注册表
--

CREATE TABLE IF NOT EXISTS `third_party_bsys` (
  `identifier` VARCHAR(255) NOT NULL PRIMARY KEY,
  `description` VARCHAR(255) DEFAULT NULL,
  `record_status` INT(11) DEFAULT '0',
  `title` VARCHAR(255) DEFAULT NULL,
  `bsysname` VARCHAR(255) DEFAULT NULL,
  `bsysadmin` VARCHAR(255) DEFAULT NULL,
  `bsyskey` VARCHAR(255) DEFAULT NULL,
  `bsysivcconfig` TEXT,
  `update_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `ndcode` VARCHAR(255) DEFAULT NULL,
  `subtype` VARCHAR(255) DEFAULT NULL,
  `total_res` VARCHAR(255) DEFAULT NULL,
  `total_time` DATETIME DEFAULT NULL,
  `total_type` VARCHAR(255) DEFAULT NULL,
  `total_value` INT(11) DEFAULT NULL
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `third_party_bsys` (`identifier`,`description`, `record_status`, `title`, `bsysname`, `bsysadmin`,`bsyskey`, `bsysivcconfig`, `update_time`, `create_time`, `ndcode`,`subtype`, `total_res`, `total_time`, `total_type`, `total_value`) VALUES('a5e379d4-70d8-471d-926a-e403d52d3cfd', NULL, '0', NULL, 'LCMS(DEFAULT SERVICE)', 'johnny(830917)', 'DEFAULT_SERVICE_KEY', '{"global_load":{"max_rps":"200","max_dpr":"500"}}', '2016-03-22 18:45:49', '2016-03-22 18:45:49', NULL, NULL, NULL, NULL, NULL, NULL);