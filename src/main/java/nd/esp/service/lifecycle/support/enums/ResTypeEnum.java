/* =============================================================
 * Created: [2015年8月27日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源类型对应 ,提供了通过类型获取枚举值的方法
 * 
 * @author lanyl
 * @since
 */
public enum ResTypeEnum {

    assets("assets"), // 素材

    // 学研素材
    teachingmaterials("teachingmaterials"), // 教材
    chapters("chapters"), // 章节
    lessons("lessons"), // 课时
    instructionalobjectives("instructionalobjectives"), // 教学目标
    knowledges("knowledges"), // 知识点

    guidancebooks("guidancebooks"),//教辅

    // 教学资源
    coursewares("coursewares"), // 课件
    instructionalprototypes("instructionalprototypes"), // 学研原型
    prototypeactivities("prototypeactivities"), // 学研原型环节
    activitiesteps("activitiesteps"), // 学研原型环节-步骤
    coursewareobjects("coursewareobjects"), // 课件颗粒
    coursewareobjecttemplates("coursewareobjecttemplates"), // 课件颗粒模板
    lessonplans("lessonplans"), // 教案
    ebooks("ebooks"), // 电子教材
    teachingactivities("teachingactivities"),//教学活动

    // 评测资源
    homeworks("homeworks"), // 作业
    questions("questions"), // 习题 FIXME 游戏化习题
    examinationpapers("examinationpapers"), // 试卷


    // 学习资源
    learningplans("learningplans"), // 学案
 //学科工具
    tools("tools"),

    ;
    private String value;

    public String getValue() {
        return value;
    }

    private ResTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 获取枚举正则表达式
     * @return
     */
    public static String getRegex(){
        List<String> list = new ArrayList<String>();
        for (ResTypeEnum e : ResTypeEnum.values()){
            list.add(e.getValue());
        }
        return StringUtils.join(list, "|");
    }
}
