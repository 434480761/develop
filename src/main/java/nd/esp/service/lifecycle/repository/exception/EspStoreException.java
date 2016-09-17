package nd.esp.service.lifecycle.repository.exception;

public class EspStoreException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String code;

	// private String message;
	// private Exception e;
	public EspStoreException() {
		super();
	}

	public EspStoreException(String msg) {
		super(msg);
	}

	public EspStoreException(Exception e) {
		super(e.getMessage());
	}

	public EspStoreException(String code, String message) {
		// TODO Auto-generated constructor stub
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
