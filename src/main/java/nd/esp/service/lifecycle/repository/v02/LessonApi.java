package nd.esp.service.lifecycle.repository.v02;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.Lesson;

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
public interface LessonApi extends StoreApi<Lesson>,SearchApi<Lesson> {
	
	/**
	 * 
	* @Title: search
	* @Description: 根据关键字查询标题和描述
	* @param @param request
	* @param @return
	* @param @throws EspStoreException
	* @return QueryResponse<Lesson>
	* @throws
	 */
	public QueryResponse<Lesson> search(AdaptQueryRequest<Lesson> request) throws EspStoreException;
	/**
	 * @desc  通过教材id查询相关课时
	 * @param request
	 * @param teachingMaterial
	 * @return
	 * @throws EspStoreException
	 * @author liuwx
	 */
	public QueryResponse<Lesson> search(QueryRequest request,String teachingMaterial) throws EspStoreException;
}