UPDATE ndresource set record_status = 0;

--资源库
ALTER TABLE ndresource ADD PARTITION (PARTITION p_subInstruction VALUES in ('subInstruction') ENGINE = InnoDB);
ALTER TABLE resource_categories ADD PARTITION (PARTITION p_subInstruction VALUES in ('subInstruction') ENGINE = InnoDB);

ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_0 VALUES IN (('subInstruction','assets')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_1 VALUES IN (('subInstruction','coursewares')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_2 VALUES IN (('subInstruction','coursewareobjects')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_3 VALUES IN (('subInstruction','coursewareobjecttemplates')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_4 VALUES IN (('subInstruction','ebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_5 VALUES IN (('subInstruction','homeworks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_6 VALUES IN (('subInstruction','instructionalobjectives')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_7 VALUES IN (('subInstruction','knowledges')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_8 VALUES IN (('subInstruction','lessonplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_9 VALUES IN (('subInstruction','learningplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_10 VALUES IN (('subInstruction','lessons')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_11 VALUES IN (('subInstruction','questions')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_12 VALUES IN (('subInstruction','teachingmaterials')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_13 VALUES IN (('subInstruction','chapters')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_17 VALUES IN (('subInstruction','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_18 VALUES IN (('subInstruction','tools')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_19 VALUES IN (('subInstruction','guidancebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_20 VALUES IN (('subInstruction','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_21 VALUES IN (('subInstruction','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_22 VALUES IN (('subInstruction','metacurriculums')) ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_assets_22 VALUES IN (('assets','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_coursewares_22 VALUES IN (('coursewares','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwot_22 VALUES IN (('coursewareobjecttemplates','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_ebooks_22 VALUES IN (('ebooks','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_homeworks_22 VALUES IN (('homeworks','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_io_22 VALUES IN (('instructionalobjectives','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_knowledges_22 VALUES IN (('knowledges','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessonplans_22 VALUES IN (('lessonplans','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_learningplans_22 VALUES IN (('learningplans','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessons_22 VALUES IN (('lessons','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tm_22 VALUES IN (('teachingmaterials','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_chapters_22 VALUES IN (('chapters','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_guidancebooks_22 VALUES IN (('guidancebooks','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tools_22 VALUES IN (('tools','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_22 VALUES IN (('teachingactivities','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_examinationpapers_22 VALUES IN (('examinationpapers','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_22 VALUES IN (('metacurriculums','subInstruction')) ENGINE = InnoDB);

--习题库
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwo_22 VALUES IN (('coursewareobjects','subInstruction')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_questions_22 VALUES IN (('questions','subInstruction')) ENGINE = InnoDB);


--除生产环境外，删除所有的教材、章节数据

