package nd.esp.service.lifecycle.repository.ds;

import java.io.Serializable;


/** 
 * @Description 动态查询的值。
 * @author Rainy(yang.lin)  
 * @date 2015年5月19日 下午1:43:03 
 * @version V1.0
 * @param <T>
 */ 
  	
public interface Value<T> extends Serializable {
	
	T getValue();
}
