package nd.esp.service.lifecycle.services.coursewareobjects.v06;

import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;

/**
 * 课件颗粒业务层接口（v0.6）
 * 
 * @author caocr
 * @since 
 *
 */
public interface CourseWareObjectServiceV06 {
	/**
	 * 课件颗粒创建
	 * 
	 * @param cwom 课件颗粒业务模型
	 * @return CourseWareObjectModel
	 */
	public CourseWareObjectModel createCourseWareObject(CourseWareObjectModel cwom);
	
	/**
	 * 课件颗粒修改
	 * 
	 * @param cwom 课件颗粒业务模型
	 * @return CourseWareObjectModel
	 */
	public CourseWareObjectModel updateCourseWareObject(CourseWareObjectModel cwom);

	CourseWareObjectModel patchCourseWareObject(CourseWareObjectModel model, boolean isObvious);
}