package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;

import java.util.Set;

/**
 * Created by liuran on 2016/7/6.
 */
public interface TitanSyncService {
    boolean deleteResource(String primaryCategory, String identifier);
    boolean reportResource(String primaryCategory, String identifier);
    boolean batchDeleteResource(Set<Resource> resourceSet);
    boolean syncEducation(String primaryCategory, String identifier);
}
