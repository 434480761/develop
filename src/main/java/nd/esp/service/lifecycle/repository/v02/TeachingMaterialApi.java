package nd.esp.service.lifecycle.repository.v02;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;

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
public interface TeachingMaterialApi extends StoreApi<TeachingMaterial>,SearchApi<TeachingMaterial> {
	
	/**
	 * 分页查询
	 * @param request
	 * @return
	 * @throws EspStoreException 
	 */
	public QueryResponse<TeachingMaterial> search(QueryRequest request) throws EspStoreException;
	
	/**
	 * 资源检索
	 * @param phase
	 * @param grade
	 * @param subject
	 * @param edition
	 * @param request
	 * @return
	 * @throws EspStoreException
	 */
	QueryResponse<TeachingMaterial> getListByCondition(String phase, String grade,
			String subject, String edition, QueryRequest request)
			throws EspStoreException;
}