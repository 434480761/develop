--增加m_identifier字段
ALTER TABLE `ndresource` ADD `m_identifier` CHAR(36) NULL AFTER `identifier`, ADD INDEX `idx_ndresource_mid` (`m_identifier`) ;
--新建`user_coverage_mapping` 表, 绑定用户和公私有库的关系
CREATE TABLE IF NOT EXISTS `user_coverage_mapping` (
  `id` bigint(20) NOT NULL COMMENT '自增ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `coverage` varchar(255) NOT NULL COMMENT 'coverage',
  `create_time` date NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `user_coverage_mapping`
  ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `user_coverage_mapping_idx` (`user_id`,`coverage`);

ALTER TABLE `user_coverage_mapping`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID';
  
--新建`user_restype_mapping` 表, 绑定用户和资源类型的关系
CREATE TABLE IF NOT EXISTS `user_restype_mapping` (
  `id` bigint(20) NOT NULL COMMENT '自增ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID ',
  `res_type` varchar(64) NOT NULL COMMENT '资源类型 ',
  `create_time` date NOT NULL COMMENT '创建时间 '
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `user_restype_mapping`
  ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `user_restype_mapping_idx` (`user_id`,`res_type`);

ALTER TABLE `user_restype_mapping`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID';