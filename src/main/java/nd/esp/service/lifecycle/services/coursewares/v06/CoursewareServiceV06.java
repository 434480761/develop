package nd.esp.service.lifecycle.services.coursewares.v06;

import nd.esp.service.lifecycle.models.courseware.v06.CoursewareModel;

/**
 * @author xuzy
 * @version 0.6
 * @created 2015-08-15
 */
public interface CoursewareServiceV06{
	/**
	 * 课件创建
	 * @param rm
	 * @return
	 */
	public CoursewareModel createCourseware(String resType,CoursewareModel cm);
	
	/**
	 * 课件修改
	 * @param rm
	 * @return
	 */
	public CoursewareModel updateCourseware(String resType,CoursewareModel cm);

	CoursewareModel patchCourseware(String resType, CoursewareModel cm);
}