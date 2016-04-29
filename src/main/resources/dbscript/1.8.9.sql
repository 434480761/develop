--增加es同步表
 CREATE TABLE IF NOT EXISTS `es_sync` (
  `identifier` char(36) NOT NULL COMMENT 'uuid',
  `title` varchar(50) DEFAULT NULL COMMENT '记录标题',
  `description` varchar(50) DEFAULT NULL COMMENT '记录描述',
  `resource` char(36) NOT NULL COMMENT '资源uuid',
  `primary_category` varchar(50) NOT NULL COMMENT '资源主分类',
  `create_time` bigint(20) DEFAULT 0 COMMENT '同步记录创建时间',
  `last_update` bigint(20) DEFAULT 0 COMMENT '同步记录最后更新时间',
  `sync_type` tinyint(1) DEFAULT 0 COMMENT '0表示删除，1表示更新',
  `enable` tinyint(1) DEFAULT 1 COMMENT '0表示已处理，1表示待处理',
  `try_times` int(11) DEFAULT 1 COMMENT '已经尝试同步的次数',
  PRIMARY KEY(identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='同步数据到es中间表';

--增加es任务多实例同步数据
insert into synchronized_table set pid=3,value=0;