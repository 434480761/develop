package nd.esp.service.lifecycle.repository.index;



import java.util.Date;


// TODO: Auto-generated Javadoc
/**
 *  
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description 
 * @date 2015年5月15日 下午6:11:06
 */ 
  	
public class Response {
	
	/** The Constant serialVersionUID. */
	public static final long serialVersionUID = 1L;
	
	/** The request_id. */
	private String request_id;
	
	/** The code. */
	private int code;
	
	/** The message. */
	private String message;
	
	/** The url. */
	private String url;
	
	/** The host_id. */
	private String host_id;
	
	/** The server_time. */
	private Date server_time;
	
	/**
	 * The Class Builder.
	 */
	public static class Builder{
		
		/** The request_id. */
		private String request_id;
		
		/** The code. */
		private int code;
		
		/** The message. */
		private String message;
		
		/** The url. */
		private String url;
		
		/** The host_id. */
		private String host_id;
		
		/** The server_time. */
		private Date server_time;
		
		/**
		 * Instantiates a new builder.
		 */
		public Builder(){
			
		}
		
		/**
		 * Instantiates a new builder.
		 *
		 * @param code the code
		 * @param message the message
		 */
		public Builder(int code,String message){
			this.code = code;
			this.message = message;
		}
		
		/**
		 * Request id.
		 *
		 * @param requestId the request id
		 * @return the builder
		 */
		public Builder requestId(String requestId){
			request_id = requestId;
			return this;
		}
		
		/**
		 * Code.
		 *
		 * @param code the code
		 * @return the builder
		 */
		public Builder code(int code){
			this.code = code;
			return this;
		}
		
		/**
		 * Message.
		 *
		 * @param message the message
		 * @return the builder
		 */
		public Builder message(String message){
			this.message = message;
			return this;
		}
		
		/**
		 * Url.
		 *
		 * @param url the url
		 * @return the builder
		 */
		public Builder url(String url){
			this.url = url;
			return this;
		}
		
		/**
		 * Host id.
		 *
		 * @param hostId the host id
		 * @return the builder
		 */
		public Builder hostId(String hostId){
			this.host_id = hostId;
			return this;
		}
		
		/**
		 * Server time.
		 *
		 * @param server_time the server_time
		 * @return the builder
		 */
		public Builder serverTime(Date server_time){
			this.server_time = server_time;
			return this;
		}
		
		/**
		 * Builds the.
		 *
		 * @return the response
		 */
		public Response build(){
			return new Response(this);
		}
	}
	
	/**
	 * Instantiates a new response.
	 *
	 * @param builder the builder
	 */
	public Response(Builder builder) {
		super();
		this.request_id = builder.request_id;
		this.code = builder.code;
		this.message = builder.message;
		this.url = builder.url;
		this.host_id = builder.host_id;
		this.server_time = builder.server_time;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Instantiates a new response.
	 */
	public Response() {
		super();
	}

	/**
	 * Sets the code.
	 *
	 * @param code the new code
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the request_id.
	 *
	 * @return the request_id
	 */
	public String getRequest_id() {
		return request_id;
	}

	/**
	 * Sets the request_id.
	 *
	 * @param request_id the new request_id
	 */
	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}

	/**
	 * Gets the host_id.
	 *
	 * @return the host_id
	 */
	public String getHost_id() {
		return host_id;
	}

	/**
	 * Sets the host_id.
	 *
	 * @param host_id the new host_id
	 */
	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}

	/**
	 * Gets the server_time.
	 *
	 * @return the server_time
	 */
	public Date getServer_time() {
		return server_time;
	}

	/**
	 * Sets the server_time.
	 *
	 * @param server_time the new server_time
	 */
	public void setServer_time(Date server_time) {
		this.server_time = server_time;
	}

	/**
	 * Description 
	 * @return 
	 * @see java.lang.Object#toString() 
	 */ 
		
	@Override
	public String toString() {
		return "Response [request_id=" + request_id + ", code=" + code
				+ ", message=" + message + ", url=" + url + ", host_id="
				+ host_id + ", server_time=" + server_time + "]";
	}
}
