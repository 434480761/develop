package nd.esp.service.lifecycle.services.coursewareobjects.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;
import nd.esp.service.lifecycle.services.coursewareobjects.v06.ToolsServiceV06;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.support.busi.PrePackUtil;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学科工具业务实现类
 * 
 * @author linsm
 */
@Service("toolsServiceV06")
@Transactional
public class ToolsServiceImplV06 implements ToolsServiceV06 {
    @Autowired
    private PackageService pacakageService;
    
    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    private PrePackUtil prePackUtil;

    @Override
    @TitanTransaction
    public CourseWareObjectModel createTools(CourseWareObjectModel model) {
        CourseWareObjectModel rtModel = (CourseWareObjectModel)ndResourceService.create(ResourceNdCode.tools.toString(), model);
        return rtModel;
    }

    @Override
    @TitanTransaction
    public CourseWareObjectModel updateTools(CourseWareObjectModel model) {
        CourseWareObjectModel rtModel = (CourseWareObjectModel)ndResourceService.update(ResourceNdCode.tools.toString(), model);
        return rtModel;
    }

    @Override
    @TitanTransaction
    public CourseWareObjectModel patchTools(CourseWareObjectModel model, boolean isObvious) {
        return (CourseWareObjectModel)ndResourceService.patch(ResourceNdCode.tools.toString(), model, isObvious);
    }
}
