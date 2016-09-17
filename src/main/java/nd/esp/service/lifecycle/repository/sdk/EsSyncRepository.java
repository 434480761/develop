package nd.esp.service.lifecycle.repository.sdk;

/**
 * 类描述:CategoryPatternRepository
 * 创建人:
 * 创建时间:2015-05-20 10:14:30
 * @version
 */

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.EsSync;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EsSyncRepository extends ResourceRepository<EsSync>,
		JpaRepository<EsSync, String> {

}