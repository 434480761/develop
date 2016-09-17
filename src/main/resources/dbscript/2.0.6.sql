ALTER TABLE `category_datas` ADD `preview` VARCHAR(255) NULL COMMENT '预览图' ;

--新建titan_sync表（esp）
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

ALTER TABLE `titan_sync` ADD PRIMARY KEY (`identifier`);

--创建索引

ALTER TABLE `titan_sync` ADD INDEX `create_time` (`identifier`, `description`) COMMENT '增加索引';

ALTER TABLE `titan_sync` ADD INDEX `primary_category` (`identifier`, `description`) COMMENT '增加索引';

ALTER TABLE `titan_sync` ADD INDEX `resource` (`identifier`, `description`) COMMENT '增加索引';

--static_datas添加一条数据

insert INTO `static_datas` (`name`, `status`) values('TITAN_SWITCH', '1')