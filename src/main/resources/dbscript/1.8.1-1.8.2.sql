--
-- 表的结构 `dirty_data`
--

CREATE TABLE IF NOT EXISTS `dirty_data` (
  `identifier` char(36) NOT NULL COMMENT '资源id',
  `primary_category` varchar(50) NOT NULL COMMENT '资源主分类'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='QA用例或单元测试产生的垃圾数据';


CREATE TABLE IF NOT EXISTS `synchronized_table` (
  `pid` int(11) NOT NULL,
  `value` int(11) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用于多实例应用间的同步';
ALTER TABLE `synchronized_table`
  ADD PRIMARY KEY (`pid`) COMMENT '主键值';

INSERT INTO `synchronized_table` (`pid`, `value`) VALUES (1, 0);
INSERT INTO `synchronized_table` (`pid`, `value`) VALUES (2, 0);

--添加索引
ALTER TABLE `ndresource` ADD INDEX `idx_ndresource_code` (`code`) COMMENT '';


--
-- 表的结构 `static_datas`
--

CREATE TABLE IF NOT EXISTS `static_datas` (
  `name` varchar(100) NOT NULL COMMENT '静态变量名',
  `status` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 转存表中的数据 `static_datas`
--

INSERT INTO `static_datas` (`name`, `status`) VALUES
('CAN_QUERY_QA_DATA', 0),
('CAN_QUERY_PROVIDER', 1),
('suspendFlag', 0);

--
-- 表的结构 `static_datas_update`
--

CREATE TABLE IF NOT EXISTS `static_datas_update` (
  `last_update` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 转存表中的数据 `static_datas_update`
--

INSERT INTO `static_datas_update` (`last_update`) VALUES
(914197561);