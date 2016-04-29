package nd.esp.service.lifecycle.repository.sdk;


/**
 * 类描述:TeachingActivitiesRepository
 * 创建人:xuzy
 * 创建时间:2016-02-24
 * @version
 */
  
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.TeachingActivities;

import org.springframework.data.repository.Repository;

public interface TeachingActivitiesRepository extends ResourceRepository<TeachingActivities>,
		Repository<TeachingActivities, String> {

}