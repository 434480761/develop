package nd.esp.service.lifecycle.services.coursewares.v06.impl;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.courseware.v06.CoursewareModel;
import nd.esp.service.lifecycle.services.coursewares.v06.CoursewareServiceV06;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 业务实现类
 * @author xuzy
 *
 */
@Service("coursewareServiceV06")
@Transactional
public class CoursewareServiceImplV06 implements CoursewareServiceV06 {
	@Autowired
	private NDResourceService ndResourceService;

	@Override
	public CoursewareModel createCourseware(String resType,CoursewareModel cm) {
		return (CoursewareModel)ndResourceService.create(resType, cm);
	}

	@Override
	public CoursewareModel updateCourseware(String resType,CoursewareModel cm) {
		return (CoursewareModel)ndResourceService.update(resType, cm);
	}

	@Override
	public CoursewareModel patchCourseware(String resType, CoursewareModel cm) {
		return (CoursewareModel)ndResourceService.patch(resType, cm);
	}
}
