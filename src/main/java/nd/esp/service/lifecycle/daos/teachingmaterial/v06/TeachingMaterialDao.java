package nd.esp.service.lifecycle.daos.teachingmaterial.v06;

import java.util.List;
import java.util.Map;

/**
 * 教材数据层
 * @author xuzy
 *
 */
public interface TeachingMaterialDao {
	
	/**
	 * 根据维度路径取出教材
	 * 
	 * @author:xuzy
	 * @date:2015年8月25日
	 * @param taxonPath
	 * @param id
	 * @return
	 */
	public List<Map<String,Object>> queryListByCategories(String taxonPath,String id);
}
