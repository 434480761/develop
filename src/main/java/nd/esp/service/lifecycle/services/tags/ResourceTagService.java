package nd.esp.service.lifecycle.services.tags;

import java.util.Map;

public interface ResourceTagService {
	public Map<String,String> addResourceTags(String cid,Map<String,Integer> params);
	public Map<String,Object> queryResourceTagsByCid(String cid,String limit);
	public int deleteResourceTagsByCid(String cid);
	public int deleteResourceTagsById(String id);
}
