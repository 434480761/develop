/* =============================================================
 * Created: [2015年8月27日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源类型与ndCode的对应 ,提供了通过类型获取枚举值的方法
 * 
 * @author linsm
 * @since
 */
public enum ResourceNdCode {

    assets("$RA0100"), // 素材

    // 学研素材
    teachingmaterials("$RA0201"), // 教材
    chapters("$RA0202"), // 章节
    lessons("$RA0203"), // 课时
    instructionalobjectives("$RA0204"), // 教学目标
    knowledges("$RA0205"), // 知识点
    
    guidancebooks("$RA0207"),//教辅
    subInstruction("$RA0212"),//子教学目标

    // 教学资源
    coursewares("$RT0100"), // 课件
    instructionalprototypes("$RT0201"), // 学研原型
    prototypeactivities("$RT0202"), // 学研原型环节
    activitiesteps("$RT0203"), // 学研原型环节-步骤
    coursewareobjects("$RT0204"), // 课件颗粒
    coursewareobjecttemplates("$RT0205"), // 课件颗粒模板
    lessonplans("$RT0206"), // 教案
    ebooks("$RT0207"), // 电子教材
    teachingactivities("$RT0500"),//教学活动

    // 评测资源
    homeworks("$RE0100"), // 作业
    questions("$RE0200"), // 习题 FIXME 游戏化习题
    examinationpapers("$RE0300"), // 试卷


    // 学习资源
    learningplans("$RS0200"), // 学案
 //学科工具
    tools("$RR0000"), 

    ;

    private final String ndCode;

    ResourceNdCode(String ndCode) {
        this.ndCode = ndCode;
    }

    /**
     * 用于保存字符串与枚举的一一对应
     */
    private static final Map<String, ResourceNdCode> stringToEnum = new HashMap<String, ResourceNdCode>();
    static {
        for (ResourceNdCode value : values()) {
            stringToEnum.put(value.toString(), value);
        }
    }

    /**
     * 通过字符串获取 对应资源的枚举
     * 
     * @param resourceType
     * @return
     * @author linsm
     */
    public static ResourceNdCode fromString(String resourceType) {
        return stringToEnum.get(resourceType);
    }

    public String getNdCode() {
        return ndCode;
    }
    
}
