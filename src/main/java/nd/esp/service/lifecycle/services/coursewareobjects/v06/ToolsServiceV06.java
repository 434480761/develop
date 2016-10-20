package nd.esp.service.lifecycle.services.coursewareobjects.v06;

import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;

/**
 * 学科工具业务层接口（v0.6）
 * 
 * @author linsm
 * @since 
 *
 */
public interface ToolsServiceV06 {
	/**
	 * 学科工具创建
	 * 
	 * @param cwom 学科工具业务模型
	 * @return CourseWareObjectModel
	 */
	public CourseWareObjectModel createTools(CourseWareObjectModel cwom);
	
	/**
	 * 学科工具修改
	 * 
	 * @param cwom 学科工具业务模型
	 * @return CourseWareObjectModel
	 */
	public CourseWareObjectModel updateTools(CourseWareObjectModel cwom);

	CourseWareObjectModel patchTools(CourseWareObjectModel model, boolean isObvious);
}