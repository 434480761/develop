package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.TechInfo;

/**
 * Created by liuran on 2016/5/30.
 */
public interface TitanTechInfoRepository extends TitanEspRepository<TechInfo>{
	boolean deleteAllByResource(String primaryCategory, String identifier);
}
