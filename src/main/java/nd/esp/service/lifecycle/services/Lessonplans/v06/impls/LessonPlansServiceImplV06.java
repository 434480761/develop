package nd.esp.service.lifecycle.services.Lessonplans.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.v06.LessonPlanModel;
import nd.esp.service.lifecycle.services.Lessonplans.v06.LessonPlansServiceV06;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LessonPlansServiceImplV06 implements LessonPlansServiceV06 {

	@Autowired
	private NDResourceService ndResourceService;
	
	/**
	 * @Title: create
	 * @Description:
	 * @throws
	 */
	@Override
	@TitanTransaction
	public LessonPlanModel create(LessonPlanModel lessonPlansModel) {
		return (LessonPlanModel)ndResourceService.create(ResourceNdCode.lessonplans.toString(), lessonPlansModel);
	}

	/**
	 * @Title: update
	 * @Description:
	 * @throws
	 */
	@Override
	@TitanTransaction
	public LessonPlanModel update(LessonPlanModel lessonPlansModel) {
		return (LessonPlanModel)ndResourceService.update(ResourceNdCode.lessonplans.toString(), lessonPlansModel);
	}

	/**
	 * @Title: patch
	 * @Description:
	 * @throws
	 */
	@Override
	@TitanTransaction
	public LessonPlanModel patch(LessonPlanModel lessonPlansModel) {
		return (LessonPlanModel)ndResourceService.patch(ResourceNdCode.lessonplans.toString(), lessonPlansModel);
	}
}
