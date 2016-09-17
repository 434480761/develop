/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.CoursewareObjectTemplate;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectTemplateRepository;
import nd.esp.service.lifecycle.repository.v02.CoursewareObjectTemplateApi;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */
@Repository("CoursewareObjectTemplateApi")
public class CoursewareObjectTemplateApiImpl extends BaseStoreApiImpl<CoursewareObjectTemplate> implements CoursewareObjectTemplateApi {

	private static final Logger logger = LoggerFactory
			.getLogger(CoursewareObjectTemplateApiImpl.class);

	@Autowired
	CoursewareObjectTemplateRepository  coursewareObjectTemplateRepository;
	
	@Override
	protected ResourceRepository<CoursewareObjectTemplate> getResourceRepository() {
		return coursewareObjectTemplateRepository;
	}


}
