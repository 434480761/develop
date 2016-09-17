package nd.esp.service.lifecycle.repository.ds.support;

import java.io.Serializable;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月19日 下午1:41:57 
 * @version V1.0
 * @param <T>
 */ 
  	
public class Pair<T> implements Serializable {
	
	private static final long serialVersionUID = 211613925559016612L;

	private T first;
	
	private T second;
	
	public T getFirst() {
		return first;
	}
	
	public void setFirst(T first) {
		this.first = first;
	}
	
	public T getSecond() {
		return second;
	}
	
	public void setSecond(T second) {
		this.second = second;
	}
}
