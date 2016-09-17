CREATE TABLE IF NOT EXISTS `resource_tags` (
  `identifier` char(36) NOT NULL,
  `title` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  `resource` char(36) NOT NULL,
  `tag` varchar(100) NOT NULL,
  `count` int(10) NOT NULL,
  `create_time` bigint(20) NOT NULL,
  `last_update` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资源标签统计';

ALTER TABLE `resource_tags`
  ADD KEY `idx_resource_tags_res` (`resource`);