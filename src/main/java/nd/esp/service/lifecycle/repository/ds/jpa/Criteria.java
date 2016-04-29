
/**   
 * @Title: Criteria.java 
 * @Package: com.nd.esp.repository.ds.jpa 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:51:48 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.ds.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:51:48 
 * @version V1.0
 */

public class Criteria<T> implements Specification<T>{  
    private List<Criterion> criterions = new ArrayList<Criterion>();  
  
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,  
            CriteriaBuilder builder) {  
        if (!criterions.isEmpty()) {  
            List<Predicate> predicates = new ArrayList<Predicate>();  
            for(Criterion criterion : criterions){  
                predicates.add(criterion.toPredicate(root, query,builder));  
            }  
            //and 联合起来  
            if (predicates.size() > 0) {  
                return builder.and(predicates.toArray(new Predicate[predicates.size()]));  
            }  
        }  
        return builder.conjunction();  
    }
    
    public void add(Criterion criterion){  
        if(criterion!=null){  
            criterions.add(criterion);  
        }  
    }  
}  
