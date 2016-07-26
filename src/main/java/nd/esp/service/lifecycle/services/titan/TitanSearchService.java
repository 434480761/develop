package nd.esp.service.lifecycle.services.titan;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface TitanSearchService {

	public ListViewModel<ResourceModel> search(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size, boolean reverse,String words);
	
	
	public ListViewModel<ResourceModel> searchWithAdditionProperties(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size, boolean reverse,String words);

	public ListViewModel<ResourceModel> searchWithAdditionPropertiesUseES(String resType,
																	 List<String> includes,
																	 Map<String, Map<String, List<String>>> params,
																	 Map<String, String> orderMap, int from, int size, boolean reverse,String words);

	/**
	 *
	 * @param resType
	 * @param fields
	 * @param includes
	 * @param params
	 * @param orderMap
	 * @param from
	 * @param size
	 * @param reverse
     * @param words
     * @return
     */
	public ListViewModel<ResourceModel> searchUseES(String resType,List<String> fields,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size, boolean reverse,String words);
	
	
}
