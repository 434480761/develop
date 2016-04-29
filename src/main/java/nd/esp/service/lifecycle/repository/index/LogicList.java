
/**   
 * @Title: LogicList.java 
 * @Package: com.nd.esp.repository.index 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年7月13日 下午5:13:14 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.index;

import java.util.ArrayList;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年7月13日 下午5:13:14 
 * @version V1.0
 * @param <E>
 */

public class LogicList<E> extends ArrayList<E> {

	LogicOperation operation;

	public LogicList(LogicOperation operation) {
		super();
		this.operation = operation;
	}

	public LogicOperation getOperation() {
		return operation;
	}

	public void setOperation(LogicOperation operation) {
		this.operation = operation;
	}
}
