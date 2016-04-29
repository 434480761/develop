package nd.esp.service.lifecycle.repository.index;



public class SearchServerException extends RuntimeException{

	public SearchServerException(String string,Exception e) {
		super(string, e);
	}

	public SearchServerException(String string) {
		super(string);
	}

	/**    
	 * serialVersionUID:TODO（）    
	 *    
	 * @since Ver 1.1    
	 */    
	
	private static final long serialVersionUID = 1L;

}
