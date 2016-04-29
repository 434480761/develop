package nd.esp.service.lifecycle.services.tool.v06;

import java.util.List;
import java.util.Map;

/**
 * 工具类service接口类
 * @author xuzy
 *
 */
public interface ToolServiceV06 {
	
	/**
	 * 根据教材id查询维度路径path
	 * 
	 * @author:xuzy
	 * @date:2015年9月25日
	 * @param tmId
	 * @return
	 */
	public List<String> getTmCategories(String tmId);

	/**
	 * 将教材的维度路径copy至目标资源
	 * 
	 * @author:xuzy
	 * @date:2015年9月25日
	 * @param tmCategories
	 * @param resList
	 */
	public void copyTmCategories2Res(List<String> tmCategories, List<Map<String,String>> resList);
}
