package nd.esp.service.lifecycle.repository.index;



import com.fasterxml.jackson.annotation.JsonProperty;


/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年4月28日 下午2:48:05 
 * @version V1.0
 * @param <T>
 */ 
  	
public class QueryResponse<T> {
	
	/** The response header. */
	@JsonProperty("response_header")
	private ResponseHeader responseHeader;
	
	/** The response body. */
	@JsonProperty("response_body")
	private Response responseBody;
	
	/**
	 * Instantiates a new query response.
	 *
	 * @param responseHeader the response header
	 * @param responseBody the response body
	 */
	public QueryResponse(ResponseHeader responseHeader, Response responseBody) {
		super();
		this.responseHeader = responseHeader;
		this.responseBody = responseBody;
	}

	/** The hits. */
	private Hits<T> hits;
	
	/**
	 * Gets the response header.
	 *
	 * @return the response header
	 */
	public ResponseHeader getResponseHeader() {
		return responseHeader;
	}

	/**
	 * Sets the response header.
	 *
	 * @param responseHeader the new response header
	 */
	public void setResponseHeader(ResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	/**
	 * Instantiates a new query response.
	 */
	public QueryResponse() {
		super();
	}

	/**
	 * Instantiates a new query response.
	 *
	 * @param responseHeader the response header
	 * @param hits the hits
	 */
	public QueryResponse(ResponseHeader responseHeader, Hits<T> hits) {
		super();
		this.responseHeader = responseHeader;
		this.hits = hits;
	}

	/**
	 * Gets the hits.
	 *
	 * @return the hits
	 */
	public Hits<T> getHits() {
		return hits;
	}

	/**
	 * Sets the hits.
	 *
	 * @param hits the new hits
	 */
	public void setHits(Hits<T> hits) {
		this.hits = hits;
	}


	/**
	 * Gets the response body.
	 *
	 * @return the response body
	 */
	public Response getResponseBody() {
		return responseBody;
	}

	/**
	 * Sets the response body.
	 *
	 * @param responseBody the new response body
	 */
	public void setResponseBody(Response responseBody) {
		this.responseBody = responseBody;
	}

	/**
	 * Description 
	 * @return 
	 * @see java.lang.Object#toString() 
	 */ 
		
	@Override
	public String toString() {
		return "QueryResponse [responseHeader=" + responseHeader
				+ ", responseBody=" + responseBody + ", hits=" + hits + "]";
	}
	
}
