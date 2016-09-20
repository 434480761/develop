-------------------------------------------------------------------------------------------------------
--暂不执行
--增加分区
ALTER TABLE ndresource ADD PARTITION (PARTITION p_exercisesset VALUES in ('exercisesset') ENGINE = InnoDB);
ALTER TABLE resource_categories ADD PARTITION (PARTITION p_exercisesset VALUES in ('exercisesset') ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_0 VALUES IN (('exercisesset','assets')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_1 VALUES IN (('exercisesset','coursewares')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_2 VALUES IN (('exercisesset','coursewareobjects')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_3 VALUES IN (('exercisesset','coursewareobjecttemplates')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_4 VALUES IN (('exercisesset','ebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_5 VALUES IN (('exercisesset','homeworks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_6 VALUES IN (('exercisesset','instructionalobjectives')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_7 VALUES IN (('exercisesset','knowledges')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_8 VALUES IN (('exercisesset','lessonplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_9 VALUES IN (('exercisesset','learningplans')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_10 VALUES IN (('exercisesset','lessons')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_11 VALUES IN (('exercisesset','questions')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_12 VALUES IN (('exercisesset','teachingmaterials')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_13 VALUES IN (('exercisesset','chapters')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_17 VALUES IN (('exercisesset','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_18 VALUES IN (('exercisesset','tools')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_19 VALUES IN (('exercisesset','guidancebooks')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_20 VALUES IN (('exercisesset','teachingactivities')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_21 VALUES IN (('exercisesset','examinationpapers')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_22 VALUES IN (('exercisesset','metacurriculums')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_exercisesset_23 VALUES IN (('exercisesset','subInstruction')) ENGINE = InnoDB);


ALTER TABLE resource_relations ADD PARTITION (PARTITION p_assets_23 VALUES IN (('assets','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_coursewares_23 VALUES IN (('coursewares','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwot_23 VALUES IN (('coursewareobjecttemplates','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_ebooks_23 VALUES IN (('ebooks','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_homeworks_23 VALUES IN (('homeworks','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_io_23 VALUES IN (('instructionalobjectives','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_knowledges_23 VALUES IN (('knowledges','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessonplans_23 VALUES IN (('lessonplans','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_learningplans_23 VALUES IN (('learningplans','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_lessons_23 VALUES IN (('lessons','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tm_23 VALUES IN (('teachingmaterials','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_chapters_23 VALUES IN (('chapters','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_guidancebooks_23 VALUES IN (('guidancebooks','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_tools_23 VALUES IN (('tools','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_teachingactivities_23 VALUES IN (('teachingactivities','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_metacurriculums_23 VALUES IN (('metacurriculums','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_subInstruction_23 VALUES IN (('subInstruction','exercisesset')) ENGINE = InnoDB);


--习题库
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_cwo_23 VALUES IN (('coursewareobjects','exercisesset')) ENGINE = InnoDB);
ALTER TABLE resource_relations ADD PARTITION (PARTITION p_questions_23 VALUES IN (('questions','exercisesset')) ENGINE = InnoDB);

