
CREATE TABLE `titan_sync` (
  `identifier` varchar(50) NOT NULL,
  `description` varchar(100) NOT NULL,
  `title` varchar(100) NOT NULL,
  `resource` varchar(50) NOT NULL,
  `level` int(10) NOT NULL,
  `primary_category` varchar(50) NOT NULL,
  `execute_times` int(10) NOT NULL,
  `type` varchar(50) NOT NULL,
  `create_time` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `titan_sync`
  ADD PRIMARY KEY (`identifier`);

ALTER TABLE `prepub_mysql_esp`.`titan_sync` DROP INDEX `create_time`, ADD INDEX `create_time` (`identifier`, `description`) COMMENT '';
ALTER TABLE `prepub_mysql_esp`.`titan_sync` DROP INDEX `primary_category`, ADD UNIQUE `primary_category` (`identifier`, `description`) COMMENT '';
ALTER TABLE `prepub_mysql_esp`.`titan_sync` DROP INDEX `resource`, ADD INDEX `resource` (`identifier`, `description`) COMMENT '';