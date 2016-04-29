package nd.esp.service.lifecycle.repository.index;



import java.util.List;



/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月15日 下午6:10:56 
 * @version V1.0
 * @param <T>
 */ 
  	
public class Hits<T> {
	
	/** The total. */
	private long total;
	
	/** The docs. */
	private List<T> docs;	
	
	/**
	 * Gets the total.
	 *
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}
	
	/**
	 * Sets the total.
	 *
	 * @param total the new total
	 */
	public void setTotal(long total) {
		this.total = total;
	}
	
	/**
	 * Gets the docs.
	 *
	 * @return the docs
	 */
	public List<T> getDocs() {
		return docs;
	}
	
	/**
	 * Sets the docs.
	 *
	 * @param docs the new docs
	 */
	public void setDocs(List<T> docs) {
		this.docs = docs;
	}
	
	/**
	 * Description 
	 * @return 
	 * @see java.lang.Object#toString() 
	 */ 
		
	@Override
	public String toString() {
		return "Hits [total=" + total + ", docs=" + docs + "]";
	}
	
}
