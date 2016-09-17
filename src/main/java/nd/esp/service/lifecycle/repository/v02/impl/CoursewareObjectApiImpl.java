/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.CoursewareObject;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectRepository;
import nd.esp.service.lifecycle.repository.v02.CoursewareObjectApi;

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
@Repository("CoursewareObjectApi")
public class CoursewareObjectApiImpl extends BaseStoreApiImpl<CoursewareObject> implements CoursewareObjectApi {

    private static final Logger logger = LoggerFactory
			.getLogger(CoursewareObjectApiImpl.class);

	@Autowired
	CoursewareObjectRepository  coursewareObjectRepository;
	
	@Override
	protected ResourceRepository<CoursewareObject> getResourceRepository() {
		return coursewareObjectRepository;
	}


}
