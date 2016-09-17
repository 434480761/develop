--增加章节标签的来源分类维度
ALTER TABLE `resource_tags` ADD `category` VARCHAR(255) NULL DEFAULT '$RA0101' COMMENT '标签来源的分类维度' ;