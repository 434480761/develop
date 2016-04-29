
/**   
 * @Title: MatchMode.java 
 * @Package: com.nd.esp.repository.ds.jpa 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:59:02 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.ds.jpa;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:59:02 
 * @version V1.0
 */

public enum MatchMode {

	/**
	 * Match the entire string to the pattern
	 */
	EXACT {
		public String toMatchString(String pattern) {
			return pattern;
		}
	},

	/**
	 * Match the start of the string to the pattern
	 */
	START {
		public String toMatchString(String pattern) {
			return pattern + '%';
		}
	},

	/**
	 * Match the end of the string to the pattern
	 */
	END {
		public String toMatchString(String pattern) {
			return '%' + pattern;
		}
	},

	/**
	 * Match the pattern anywhere in the string
	 */
	ANYWHERE {
		public String toMatchString(String pattern) {
			return '%' + pattern + '%';
		}
	};

	/**
	 * convert the pattern, by appending/prepending "%"
	 */
	public abstract String toMatchString(String pattern);

}