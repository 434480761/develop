--新建`security_key` 表
CREATE TABLE IF NOT EXISTS `security_key` (
  `identifier` char(36) NOT NULL,
  `security_key` varchar(64) NOT NULL,
  `user_id` varchar(36) NOT NULL COMMENT '用户id',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `security_key`
  ADD PRIMARY KEY (`identifier`), ADD UNIQUE KEY `user_key_inx` (`security_key`,`user_id`);