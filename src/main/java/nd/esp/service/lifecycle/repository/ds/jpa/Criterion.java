
/**   
 * @Title: Criterion.java 
 * @Package: com.nd.esp.repository.ds.jpa 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:53:24 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.ds.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:53:24 
 * @version V1.0
 */

public interface Criterion {  
    public enum Operator {  
        EQ, NE, LIKE, GT, LT, GTE, LTE, AND, OR  
    }  
    public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query,  
            CriteriaBuilder builder);  
}
