
/**   
 * @Title: LogicalExpression.java 
 * @Package: com.nd.esp.repository.ds.jpa 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:55:19 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.ds.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:55:19 
 * @version V1.0
 */

public class LogicalExpression implements Criterion {  
    private Criterion[] criterion;  // 逻辑表达式中包含的表达式  
    private Operator operator;      //计算符  
  
    public LogicalExpression(Criterion[] criterions, Operator operator) {  
        this.criterion = criterions;  
        this.operator = operator;  
    }  
  
    public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query,  
            CriteriaBuilder builder) {  
        List<Predicate> predicates = new ArrayList<Predicate>();  
        for(int i=0;i<this.criterion.length;i++){  
            predicates.add(this.criterion[i].toPredicate(root, query, builder));  
        }  
        switch (operator) {  
        case OR:  
            return builder.or(predicates.toArray(new Predicate[predicates.size()]));  
        default:  
            return null;  
        }  
    }  
  
}  
