package nd.esp.service.lifecycle.services.tags;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.vos.v06.ResourceTagViewModel;

public interface ResourceTagService {
	public Map<String,String> addResourceTags(String cid,Map<String,Integer> params);
	public List<ResourceTagViewModel> queryResourceTagsByCid(String cid,String limit);
}
