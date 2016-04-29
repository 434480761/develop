package nd.esp.service.lifecycle.repository.v02;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.Category;

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
public interface CategoryApi extends StoreApi<Category>,SearchApi<Category> {
	
	/**
	 * Gets the detail.
	 *
	 * @param id the id
	 * @return the detail
	 * @throws EspStoreException the esp store exception
	 */
	public ReturnInfo<Category> getDetailByNdCode(String ndCode) throws EspStoreException;
}