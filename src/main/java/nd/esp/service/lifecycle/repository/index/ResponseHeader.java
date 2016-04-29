package nd.esp.service.lifecycle.repository.index;




/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月15日 下午6:11:16 
 * @version V1.0
 */ 
  	
public class ResponseHeader {
	private int status;
	private int QTime;
	
	public ResponseHeader() {
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getQTime() {
		return QTime;
	}

	public void setQTime(int qTime) {
		QTime = qTime;
	}

	public ResponseHeader(int status, int qTime) {
		super();
		this.status = status;
		QTime = qTime;
	}

	@Override
	public String toString() {
		return "ResponseHeader [status=" + status + ", QTime=" + QTime + "]";
	}
}
