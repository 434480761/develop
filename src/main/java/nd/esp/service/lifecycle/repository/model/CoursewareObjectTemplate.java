package nd.esp.service.lifecycle.repository.model;


import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 类描述:bean
 * 创建人:
 * 创建时间:2015-05-13 18:52:3
 * @version
 */
  
@Entity
@Table(name="courseware_object_templates")
public class CoursewareObjectTemplate extends Education {
	
	
	
	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.SourceCourseWareObjectTemplateType.getName());
		return IndexSourceType.SourceCourseWareObjectTemplateType;
	}
	

	
}