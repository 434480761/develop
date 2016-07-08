package nd.esp.service.lifecycle.services.coursewareobjecttemplate.v06;

import nd.esp.service.lifecycle.models.v06.CourseWareObjectTemplateModel;

/**
 * 课件颗粒service接口
 * @author liur
 * */
public interface CourseWareObjectTemplateServiceV06 {
    /**
     * 创建课件颗粒模板
     * */
    public CourseWareObjectTemplateModel createCourseWareObjectTemplate(CourseWareObjectTemplateModel ctm);

    /**
     * 修改课件颗粒模板
     * */
    public CourseWareObjectTemplateModel updateCourseWareObjectTemplate(CourseWareObjectTemplateModel ctm);

    CourseWareObjectTemplateModel patchCourseWareObjectTemplate(CourseWareObjectTemplateModel ctm);
}
