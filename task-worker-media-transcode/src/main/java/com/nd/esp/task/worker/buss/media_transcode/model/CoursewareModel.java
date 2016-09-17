package com.nd.esp.task.worker.buss.media_transcode.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title 课件模型
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午8:02:44
 */
public class CoursewareModel extends ResourceModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 课件的难度
     */
    private String difficulty;

    /**
     * 课件的教学目标的描述
     */
    private String[] objectives;

    /**
     * 建议学习时常，格式为"P0Y0M0DT3H0M"，含义是学习3小时
     */
    private String typicalLearningTime;
    
    
    /**
     * 教学目标(数组)
     */
    private List<String> lessonObjectives;

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String[] getObjectives() {
        return objectives;
    }

    public void setObjectives(String[] objectives) {
        this.objectives = objectives;
    }

    public String getTypicalLearningTime() {
        return typicalLearningTime;
    }

    public void setTypicalLearningTime(String typicalLearningTime) {
        this.typicalLearningTime = typicalLearningTime;
    }

    public List<String> getLessonObjectives() {
        return lessonObjectives;
    }

    public void setLessonObjectives(List<String> lessonObjectives) {
        this.lessonObjectives = lessonObjectives;
    }

 
    
    
    

}
