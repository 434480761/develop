package nd.esp.service.lifecycle.educommon.services.titanV07;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.models.AccessModel;

import java.util.List;
import java.util.Set;

/**
 * Created by liuran on 2016/8/1.
 */
public interface NDResourceTitanService {
    ResourceModel getDetail(String resourceType, String uuid, List<String> includeList, Boolean isAll);

    List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList);

    AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew, String coverage);

    AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew);

    AccessModel getDownloadUrl(String resourceType, String uuid, String uid, String key);
}
