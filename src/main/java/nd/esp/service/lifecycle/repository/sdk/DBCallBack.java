package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.IndexMapper;




/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月18日 上午10:24:50 
 * @version V1.0
 * @param <T>
 */ 
  	
public interface DBCallBack {
	
	/**
	 * Execute.
	 *
	 * @param bean the bean
	 * @return true, if successful
	 */
	public boolean execute(IndexMapper bean);
	
	/**
	 * Finish.
	 *
	 * @return the int
	 */
	public int finish();
}
