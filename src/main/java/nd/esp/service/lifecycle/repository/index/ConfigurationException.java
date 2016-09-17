package nd.esp.service.lifecycle.repository.index;




/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月15日 下午6:10:49 
 * @version V1.0
 */ 
  	
public class ConfigurationException extends RuntimeException{

	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param string the string
	 * @param e the e
	 */
	public ConfigurationException(String string,Exception e) {
		super(string, e);
	}

	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param string the string
	 */
	public ConfigurationException(String string) {
		super(string);
	}

	/**    
	 * serialVersionUID:TODO（）    
	 *    
	 * @since Ver 1.1    
	 */    
	
	private static final long serialVersionUID = 1L;

}
