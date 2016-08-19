package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.ResourceStatistical;

/**
 * Created by liuran on 2016/8/17.
 */
public interface TitanStatisticalRepository extends TitanEspRepository<ResourceStatistical> {
    public boolean deleteAllByResource(String primaryCategory, String identifier);
}
