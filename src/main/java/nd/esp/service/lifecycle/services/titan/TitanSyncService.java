package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanRepositoryOperation;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanRepositoryOperationPatch;

import java.util.Set;

/**
 * Created by liuran on 2016/7/6.
 */
public interface TitanSyncService {
    boolean deleteResource(String primaryCategory, String identifier);
    boolean reportResource(String primaryCategory, String identifier, TitanSyncType titanSyncType);
    boolean batchDeleteResource(Set<Resource> resourceSet);
    boolean syncEducation(String primaryCategory, String identifier);
    boolean patch(TitanRepositoryOperationPatch patch);
    boolean script(TitanRepositoryOperation script);
    boolean syncTechInfoAndEducation(String primaryCategory, String identifier);
}
