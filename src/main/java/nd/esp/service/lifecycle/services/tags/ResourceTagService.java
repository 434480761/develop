package nd.esp.service.lifecycle.services.tags;

import java.util.Map;

public interface ResourceTagService {
	public Map<String,String> addResourceTags(String cid,String category,Map<String,Integer> params);
	public Map<String,Object> queryResourceTagsByCid(String cid,String category,String limit);
	public int deleteResourceTagsByCid(String cid);
	public int deleteResourceTagsById(String id);
}
