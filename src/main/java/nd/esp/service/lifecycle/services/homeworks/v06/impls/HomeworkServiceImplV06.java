package nd.esp.service.lifecycle.services.homeworks.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.v06.HomeworkModel;
import nd.esp.service.lifecycle.services.homeworks.v06.HomeworkServiceV06;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 业务实现类
 * @author xuzy
 *
 */
@Service("homeworkServiceV06")
@Transactional
public class HomeworkServiceImplV06 implements HomeworkServiceV06 {
	@Autowired
	private NDResourceService ndResourceService;
	
	@Override
	public HomeworkModel createHomework(HomeworkModel hm) {
		return (HomeworkModel)ndResourceService.create(ResourceNdCode.homeworks.toString(), hm);
	}

	@Override
	public HomeworkModel updateHomework(HomeworkModel hm) {
		return (HomeworkModel)ndResourceService.update(ResourceNdCode.homeworks.toString(), hm);
	}

	@Override
	public HomeworkModel patchHomework(HomeworkModel hm) {
		return (HomeworkModel)ndResourceService.patch(ResourceNdCode.homeworks.toString(), hm);
	}
}
