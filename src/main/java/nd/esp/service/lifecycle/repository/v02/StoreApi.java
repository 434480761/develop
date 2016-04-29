/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */
public interface StoreApi<T> {
	public ReturnInfo<T> getDetail(String id) throws EspStoreException;

	public ReturnInfo<T> add(T bean) throws EspStoreException;

	public boolean delete(String id) throws EspStoreException;

	public ReturnInfo<T> update(T bean) throws EspStoreException;

	public List<T> getList(List<String> ids) throws EspStoreException;

	
}
