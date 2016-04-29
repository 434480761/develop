package nd.esp.service.lifecycle.repository.sdk;


/**
 * 类描述:CategoryDataRepository
 * 创建人:
 * 创建时间:2015-05-20 10:13:42
 * @version
 */
  
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.CategoryData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryDataRepository extends ResourceRepository<CategoryData>,
JpaRepository<CategoryData, String> {

}