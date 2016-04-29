package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;

// TODO: Auto-generated Javadoc
/**
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>.
 *
 * @version 0.2<br>
 */
public interface ChapterApi extends StoreApi<Chapter>,SearchApi<Chapter> {
	
	/**
	 * @desc  通过章节ids批量获取章节信息
	 * @param ids
	 * @return
	 * @throws EspStoreException 
	 * @author liuwx
	 */
	public List<Chapter> getList(List<String> ids) throws EspStoreException;
	 
	
	/**
	 * @desc  通过章节id获取所有的下一节信息
	 * @param ids
	 * @return 
	 * @throws EspStoreException
	 * @author liuwx
	 */
	public List<Chapter> getChildList(String chapterId) throws EspStoreException;
	
	/**
	 * @throws EspStoreException 
	 * 根据教材查询所有章节信息
	* @Title: getChapterBy
	* @Description: TODO
	* @param @param materialId
	* @param @return
	* @return List<Chapter>
	* @throws
	 */
	List<Chapter> getChapterByMaterialId(String materialId) throws EspStoreException;
}