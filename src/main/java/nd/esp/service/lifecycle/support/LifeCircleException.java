package nd.esp.service.lifecycle.support;

import org.springframework.http.HttpStatus;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;

/**
 * @title 生命管理周期业务异常处理
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年1月26日 上午11:33:38
 */
public class LifeCircleException extends WafSimpleException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7171590667016717608L;
	public LifeCircleException(HttpStatus status, String code, String message) {
		super(status, code, message);
		// TODO Auto-generated constructor stub
	}
	public LifeCircleException(HttpStatus status, MessageMapper messageMapper) {
		super(status, messageMapper.getCode(), messageMapper.getMessage());
		// TODO Auto-generated constructor stub
	}

	public LifeCircleException(String code, String message) {
		this(HttpStatus.OK,code, message);
		// TODO Auto-generated constructor stub
	}

	public LifeCircleException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	public LifeCircleException(MessageMapper messageMapper) {
		this(messageMapper.getCode(),messageMapper.getMessage());
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	
	
	

}
