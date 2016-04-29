package nd.esp.service.lifecycle.repository;

import java.util.Map;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;


// TODO: Auto-generated Javadoc
/**
 *  
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description 
 * @date 2015年5月15日 下午6:09:31
 */ 
  	
public interface IndexMapper {
	
	/**
	 * To search doc.
	 *
	 * @return the map
	 * @throws Exception the exception
	 */
	public Map<String, Object> getAdditionSearchFields() throws EspStoreException;
	
	public  IndexSourceType getIndexType();
}
