package nd.esp.service.lifecycle.services.elasticsearch;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
/**
 * elasticsearch 查询接口
 * @author linsm
 *
 */
public interface ES_Search {

	/**
	 * 
	 * @param resType 资源类型
	 * @param includes 返回数据范围
	 * @param params 查询条件
	 * @param orderMap 排序
	 * @param from  从几个开始
	 * @param size  个数
	 * @return
	 */
	ListViewModel<ResourceModel> searchByES(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size);

}
