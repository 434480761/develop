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
	
	/**
	 * 根据教材Id查找所有章节
	 * @param tmId
	 * @return
	 */
	public List<Map<String,Object>> queryChaptersByTmId(String tmId);
	
	/**
	 * 根据章节id列表查找资源数据
	 * @param cids
	 * @param resType
	 * @return
	 */
	public List<Map<String,Object>> queryResourcesByChapterIds(List<String> cids,String resType,List<String> includes,String coverage);
	
	/**
	 * 根据章节id列表查找资源数据(习题库)
	 * @param cids
	 * @param resType
	 * @return
	 */
	public List<Map<String,Object>> queryResourcesByChapterIds4Question(List<String> cids,String resType,List<String> includes,String coverage);
}
