package nd.esp.service.lifecycle.services.provider.v06;

import nd.esp.service.lifecycle.models.provider.v06.ResourceProviderModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface ResourceProviderService {
	/**
	 * 创建资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param rpm
	 * @return
	 */
	public ResourceProviderModel createResourceProvider(ResourceProviderModel rpm);
	
	/**
	 * 更新资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param rpm
	 * @return
	 */
	public ResourceProviderModel updateResourceProvider(ResourceProviderModel rpm);
	
	/**
	 * 删除资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param id
	 * @return
	 */
	public boolean deleteResourceProvider(String id);
	
	/**
	 * 查询资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param words
	 * @param limit
	 * @return
	 */
	public ListViewModel<ResourceProviderModel> getResourceProviderList(String words, String limit);
}
