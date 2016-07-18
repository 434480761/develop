--新建`resource_security_key` 表
CREATE TABLE IF NOT EXISTS `resource_security_key` (
  `identifier` char(36) NOT NULL,
  `security_key` varchar(64) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `resource_security_key`
  ADD PRIMARY KEY (`identifier`);