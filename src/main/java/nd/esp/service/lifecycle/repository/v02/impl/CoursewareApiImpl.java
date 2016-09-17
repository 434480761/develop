/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Courseware;
import nd.esp.service.lifecycle.repository.sdk.CoursewareRepository;
import nd.esp.service.lifecycle.repository.v02.CoursewareApi;

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
@Repository("CoursewareApi")
public class CoursewareApiImpl extends BaseStoreApiImpl<Courseware> implements CoursewareApi {

	private static final Logger logger = LoggerFactory
			.getLogger(CoursewareApiImpl.class);

	@Autowired
	CoursewareRepository  coursewareRepository;
	
	@Override
	protected ResourceRepository<Courseware> getResourceRepository() {
		return coursewareRepository;
	}

}
