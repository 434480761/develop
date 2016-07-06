--增加m_identifier字段
ALTER TABLE `ndresource` ADD `m_identifier` CHAR(36) NULL AFTER `identifier`, ADD INDEX `idx_ndresource_mid` (`m_identifier`) ;