package nd.esp.service.lifecycle.repository.sdk;


/**
 * 类描述:CoursewareObjectRepository
 * 创建人:
 * 创建时间:2015-05-20 10:14:31
 * @version
 */
  
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.CoursewareObject;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursewareObjectRepository extends  ResourceRepository<CoursewareObject>,
JpaRepository<CoursewareObject, String> {

}