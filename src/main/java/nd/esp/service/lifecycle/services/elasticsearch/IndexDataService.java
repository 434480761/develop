package nd.esp.service.lifecycle.services.elasticsearch;

import java.util.Date;
import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;

/**
 * 用于重建索引，从mysql 数据库取数据
 * 
 * @author linsm
 *
 */
public interface IndexDataService {

	/**
	 * 重建索引
	 * 
	 * @param toDate
	 *            在该时间前
	 * @param fromDate
	 *            在该时间后
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	long index(String resourceType, Date toDate, Date fromDate);

	/**
	 * 删除对应资源索引
	 * 
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@Deprecated
	void deleteResource(String resourceType);
	
	ResourceModel getDetailForES(String resourceType, String uuid) throws EspStoreException;
	
	String getJson(String resourceType, String uuid);

	List<ResourceModel> batchDetailForES(String resourceType,
			Set<String> uuids);

	int index(String resourceType, Set<String> uidSet);

}
