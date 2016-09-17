INSERT INTO `synchronized_table` (`pid`, `value`) VALUES ('4', '0')

--
-- 表的结构 `notify_backups`
--

CREATE TABLE IF NOT EXISTS `notify_backups` (
  `identifier` varchar(36) NOT NULL,
  `description` varchar(20) DEFAULT NULL,
  `title` varchar(2000) DEFAULT NULL COMMENT '教学目标title',
  `teaching_object_id` varchar(36) DEFAULT NULL COMMENT '教学目标id',
  `status` varchar(20) DEFAULT NULL COMMENT '对智能出题而已资源的状态',
  `lesson_period_id` varchar(36) DEFAULT NULL COMMENT '课时id',
  `chapter_id` varchar(36) DEFAULT NULL COMMENT '章节id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `notify_backups`
--
ALTER TABLE `notify_backups`
  ADD PRIMARY KEY (`identifier`);