ALTER TABLE `tech_infos` ADD `printable` TINYINT(1) NULL DEFAULT '0' AFTER `resource`, ADD INDEX `idx_techInfo_printable` (`printable`) ;