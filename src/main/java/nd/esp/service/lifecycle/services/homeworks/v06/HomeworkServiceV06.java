package nd.esp.service.lifecycle.services.homeworks.v06;

import nd.esp.service.lifecycle.models.v06.HomeworkModel;

/**
 * 作业业务层接口
 * @author xuzy
 * @version 0.6
 * @created 2015-07-16
 */
public interface HomeworkServiceV06{
	/**
	 * 作业创建
	 * @param rm
	 * @return
	 */
	public HomeworkModel createHomework(HomeworkModel am);
	
	/**
	 * 作业修改
	 * @param rm
	 * @return
	 */
	public HomeworkModel updateHomework(HomeworkModel am);

	HomeworkModel patchHomework(HomeworkModel hm, boolean isObvious);
}