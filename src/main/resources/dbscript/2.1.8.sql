CREATE TABLE IF NOT EXISTS `category_sync` (
  `identifier` char(36) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` text,
  `sync_category` varchar(255) DEFAULT NULL COMMENT '需要同步的维度',
  `category_type` tinyint(4) DEFAULT NULL,
  `operation_type` tinyint(4) DEFAULT NULL,
  `operation_time` bigint(20) DEFAULT NULL,
  `handle` tinyint(4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='维度同步表';

ALTER TABLE `category_sync` ADD PRIMARY KEY (`identifier`);
  

CREATE TABLE `category_sync_error` (`identifier` CHAR(36) CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NOT NULL , `title` VARCHAR(255) CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NULL , `description` TEXT CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NULL , `sync_category` VARCHAR(255) CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NULL , `res_type` VARCHAR(100) CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NULL , `resource` CHAR(36) CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NULL , `message` VARCHAR(1000) CHARACTER
                                    SET utf8 COLLATE utf8_general_ci NULL , `code` TINYINT(4) NULL) ENGINE = InnoDB CHARACTER
SET utf8 COLLATE utf8_general_ci COMMENT = '维度同步错误记录表';

ALTER TABLE `category_sync_error` ADD PRIMARY KEY(`identifier`);

INSERT INTO `synchronized_table` (`pid`, `value`) VALUES ('7', '0');

ALTER TABLE `category_patterns` ADD `gb_code` VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `title`;