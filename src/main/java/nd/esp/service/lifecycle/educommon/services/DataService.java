package nd.esp.service.lifecycle.educommon.services;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.repository.model.FullModel;
/**
 * 数据操作接口
 * @author xuzy
 *
 */
public interface DataService {
	/**
	 * 获取记录总数
	 * @return
	 */
	public int queryCount(Map<String,Object> params);
	
	/**
	 * 获取列表数据（500条）
	 * @return
	 */
	public List<FullModel> queryResult(Map<String,Object> params);
}
