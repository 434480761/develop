package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.TechInfo;

/**
 * Created by liuran on 2016/5/30.
 */
public interface TitanTechInfoRepository extends TitanEspRepository<TechInfo>{
    void remove(TechInfo techInfo);
    void deleteAll(String primaryCategory, String resource);
}
