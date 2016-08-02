package nd.esp.service.lifecycle.educommon.services.titanV07;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;

import java.util.List;
import java.util.Set;

/**
 * Created by liuran on 2016/8/1.
 */
public interface NDResourceTitanService {
    ResourceModel getDetail(String resourceType, String uuid, List<String> includeList, Boolean isAll);

    List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList);
}
