package nd.esp.service.lifecycle.repository.ds.support;

import nd.esp.service.lifecycle.repository.ds.Value;


/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月19日 下午1:41:52 
 * @version V1.0
 * @param <T>
 */ 
  	
public class GenericValue<T> implements Value<T> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8776418271286892897L;
	
	/** The value. */
	private T value;
	
	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.ds.Value#getValue() 
	 */ 
		
	@Override
	public T getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	
}
