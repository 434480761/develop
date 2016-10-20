package nd.esp.service.lifecycle.services.coursewareobjects.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;
import nd.esp.service.lifecycle.services.coursewareobjects.v06.CourseWareObjectServiceV06;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.support.busi.PrePackUtil;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课件颗粒业务实现类
 * 
 * @author caocr
 */
@Service("courseWareObjectServiceV06")
@Transactional(value="questionTransactionManager")
public class CourseWareObjectServiceImplV06 implements CourseWareObjectServiceV06 {
    @Autowired
    private PackageService pacakageService;
    
    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    private PrePackUtil prePackUtil;

    @Override
    @TitanTransaction
    public CourseWareObjectModel createCourseWareObject(CourseWareObjectModel model) {
        CourseWareObjectModel rtModel = (CourseWareObjectModel)ndResourceService.create(ResourceNdCode.coursewareobjects.toString(), model,DbName.QUESTION);
        return rtModel;
    }

    @Override
    @TitanTransaction
    public CourseWareObjectModel updateCourseWareObject(CourseWareObjectModel model) {
        CourseWareObjectModel rtModel = (CourseWareObjectModel)ndResourceService.update(ResourceNdCode.coursewareobjects.toString(), model,DbName.QUESTION);
        return rtModel;
    }

    @Override
    @TitanTransaction
    public CourseWareObjectModel patchCourseWareObject(CourseWareObjectModel model, boolean isObvious) {
        return (CourseWareObjectModel)ndResourceService.patch(ResourceNdCode.coursewareobjects.toString(), model, DbName.QUESTION, isObvious);
    }

}
