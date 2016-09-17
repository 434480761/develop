package nd.esp.service.lifecycle.repository.sdk;


/**
 * 类描述:CategoryPatternRepository
 * 创建人:
 * 创建时间:2015-05-20 10:14:30
 * @version
 */
  
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.Ebook;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EbookRepository extends ResourceRepository<Ebook>,
JpaRepository<Ebook, String> {

}