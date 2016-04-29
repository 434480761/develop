
/**   
 * @Title: SimpleIndexCallBack.java 
 * @Package: com.nd.esp.store.service.impl 
 * @Description: A Simple Impl CallBack For BaseStore Bean  
 * @author Rainy(yang.lin)  
 * @date 2015年4月10日 上午10:09:31 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.sdk.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.esp.service.lifecycle.repository.IndexMapper;
import nd.esp.service.lifecycle.repository.IndexRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.sdk.DBCallBack;

/**
 *  
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description 
 * @date 2015年4月10日 上午10:09:31
 */

public class SimpleIndexCallBack implements DBCallBack{
	
	/** The search dao. */
	private IndexRepository<?> searchDao;
	
	/** The count. */
	final int count[] = new int[3];;
	
	/** The map idxs. */
	final List<IndexMapper> mapIdxs = new ArrayList<IndexMapper>();
	
	/**
	 * Creates a new instance of SimpleIndexCallBack.
	 * Description 
	 *
	 * @param searchDao the search dao
	 */
	public SimpleIndexCallBack(IndexRepository<?> searchDao) {
		super();
		this.searchDao = searchDao;
	}


	/** Logging. */
	private static Logger logger = LoggerFactory
			.getLogger(SimpleIndexCallBack.class);
	
	/**
	 * Description .
	 *
	 * @param bean the bean
	 * @return true, if successful
	 * @see com.nd.esp.store.dao.IndexCallBack#execute(java.lang.Object)
	 */ 
		
	@Override
	public boolean execute(IndexMapper bean) {
		count[0]++;
		try {

			mapIdxs.add(bean);
			if (mapIdxs.size() > 5000) {
			    
			    if (logger.isInfoEnabled()) {
                    
			        logger.info("question  commit search");
			        
                }
				        
				searchDao.getSolrServer().addBeans(mapIdxs);
				mapIdxs.clear();
			}
			count[1]++;
		} catch (Exception e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("to index error:{}", e);
		        
            }
			        
		}
		return false;
	}
		
	/**
	 * Gets the result message.
	 *
	 * @return the result message
	 */
	public String getResultMessage(){
		StringBuilder rt = new StringBuilder();
		return rt.append("总记录数:").append(count[0]).append(";")
				.append("索引成功数:").append(count[1]).append(";").append("错误数:").append(count[2]).toString();
	}
	
	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public int []  getCount(){
		return count;
	}

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.store.dao.IndexCallBack#finish() 
	 */ 
		
	@Override
	public int finish() {
		try {
			if(mapIdxs!=null&&mapIdxs.size()>0){
				searchDao.batchAddIndex(mapIdxs);
			}
		}catch (EspStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count[0];
	}
}
