package nd.esp.service.lifecycle.repository.index;


public class Success {
	private ResponseHeader response_header;

	public ResponseHeader getResponse_header() {
		return response_header;
	}

	public void setResponse_header(ResponseHeader response_header) {
		this.response_header = response_header;
	}

	public Success(ResponseHeader response_header) {
		super();
		this.response_header = response_header;
	}
}
