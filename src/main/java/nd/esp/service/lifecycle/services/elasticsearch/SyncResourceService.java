package nd.esp.service.lifecycle.services.elasticsearch;

import java.util.Set;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
/**
 * 同步操作
 * @author linsm
 *
 */
public interface SyncResourceService {
	
	/**
	 * 返回删除操作是否成功
	 * @param resource
	 * @return
	 */
	boolean syncDelete(Resource resource);
	
	/**
	 * 返回成功操作的个数
	 * @param resourceSet
	 * @return
	 */
	int syncBatchDelete(Set<Resource> resourceSet);
	int syncBatchDeleteForTask(Set<Resource> resourceSet);
	
	int syncBatchDelete(String resourceType, Set<String> uuidSet);
	
	
	boolean syncAdd(Resource resource);
	int syncBatchAdd(Set<Resource> resourceSet);
	int syncBatchAddForTask(Set<Resource> resourceSet);

}
