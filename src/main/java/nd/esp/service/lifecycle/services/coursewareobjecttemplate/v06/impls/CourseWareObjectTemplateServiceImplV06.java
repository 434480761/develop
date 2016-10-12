package nd.esp.service.lifecycle.services.coursewareobjecttemplate.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectTemplateModel;
import nd.esp.service.lifecycle.services.coursewareobjecttemplate.v06.CourseWareObjectTemplateServiceV06;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liur
 * */
@Service("CourseWareObjectTemplateServiceImplV06")
@Transactional
public class CourseWareObjectTemplateServiceImplV06 implements CourseWareObjectTemplateServiceV06 {

    @Autowired
    private NDResourceService ndResourceService;

    /**
     * 创建课件颗粒模板
     * @param ctm 课件颗粒模板model
     * */
    public CourseWareObjectTemplateModel createCourseWareObjectTemplate(CourseWareObjectTemplateModel ctm) {

        return (CourseWareObjectTemplateModel) ndResourceService.create(ResourceNdCode.coursewareobjecttemplates.toString(), ctm);
    }

    /**
     * 修改课件颗粒模板
     * @param ctm 课件颗粒模板model
     * */
    public CourseWareObjectTemplateModel updateCourseWareObjectTemplate(CourseWareObjectTemplateModel ctm) {

        return (CourseWareObjectTemplateModel)ndResourceService.update(ResourceNdCode.coursewareobjecttemplates.toString(), ctm);
    }

    @Override
    public CourseWareObjectTemplateModel patchCourseWareObjectTemplate(CourseWareObjectTemplateModel ctm) {
        return (CourseWareObjectTemplateModel)ndResourceService.patch(ResourceNdCode.coursewareobjecttemplates.toString(), ctm);
    }
}
