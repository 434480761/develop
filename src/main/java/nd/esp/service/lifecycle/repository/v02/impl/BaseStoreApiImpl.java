/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.v02.ReturnInfo;
import nd.esp.service.lifecycle.repository.v02.SearchApi;
import nd.esp.service.lifecycle.repository.v02.StoreApi;


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
abstract public class BaseStoreApiImpl<T extends EspEntity> implements
		StoreApi<T>, SearchApi<T> {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseStoreApiImpl.class);
	
	abstract protected ResourceRepository<T> getResourceRepository();

	@Override
	public ReturnInfo<T> getDetail(String id) throws EspStoreException {
		T tvalue = getResourceRepository().get(id);
		ReturnInfo<T> returnInfo = new ReturnInfo<T>();
		returnInfo.setCode(1);
		returnInfo.setData(tvalue);
		return returnInfo;
	}

	@Override
	public ReturnInfo<T> add(T bean) throws EspStoreException {
		T tvalue = getResourceRepository().add(bean);
		ReturnInfo<T> returnInfo = new ReturnInfo<T>();
		returnInfo.setCode(1);
		returnInfo.setData(tvalue);
		return returnInfo;
	}

	@Override
	public boolean delete(String id) throws EspStoreException {
		getResourceRepository().del(id);
		return true;
	}

	@Override
	public ReturnInfo<T> update(T bean) throws EspStoreException {
		T tvalue = getResourceRepository().update(bean);
		ReturnInfo<T> returnInfo = new ReturnInfo<T>();
		returnInfo.setCode(1);
		returnInfo.setData(tvalue);
		return returnInfo;
	}

	@Override
	public List<T> getList(List<String> ids) throws EspStoreException {
		return getResourceRepository().getAll(ids);
	}
	
	@Override
	public QueryResponse<T> searchByExample(
			AdaptQueryRequest<T> queryRequest)
			throws EspStoreException {		
		return getResourceRepository().searchByExample(queryRequest);
	}
	
	@Override
	public QueryResponse<T> search(QueryRequest queryRequest)
			throws EspStoreException {
		return getResourceRepository().search(queryRequest);
		
	}

}
