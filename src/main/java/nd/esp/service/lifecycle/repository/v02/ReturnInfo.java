package nd.esp.service.lifecycle.repository.v02;



/**
 * 
 * 项目名字:nd edu<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年1月27日<br>
 * 修改人:<br>
 * 修改时间:2015年1月27日<br>
 * 修改备注:<br>
 * @version 0.1<br>
 */
public class ReturnInfo<T> extends BaseReturnInfo {
	
	/** @Fields serialVersionUID: */
	  	
	private static final long serialVersionUID = 1L;
	private int code;
	private String message;
	private T data;
	/**
	 * 
	 */
	public ReturnInfo() {
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}

	
}
