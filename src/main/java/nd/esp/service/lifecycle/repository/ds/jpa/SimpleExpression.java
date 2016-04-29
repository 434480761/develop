
/**   
 * @Title: SimpleExpression.java 
 * @Package: com.nd.esp.repository.ds.jpa 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:54:30 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.ds.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.util.StringUtils;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月4日 下午2:54:30 
 * @version V1.0
 */

public class SimpleExpression implements Criterion{  
    
    private String fieldName;       //属性名  
    private Object value;           //对应值  
    private Operator operator;      //计算符  
    private MatchMode matchMode;	//模糊查询匹配模式
  
    protected SimpleExpression(String fieldName, Object value, Operator operator) {  
        this.fieldName = fieldName;  
        this.value = value;  
        this.operator = operator;  
    }  
  
    public SimpleExpression(String fieldName, Object value, Operator operator,
			MatchMode matchMode) {
		super();
		this.fieldName = fieldName;
		this.value = value;
		this.operator = operator;
		this.matchMode = matchMode;
	}

	public String getFieldName() {  
        return fieldName;  
    }  
    public Object getValue() {  
        return value;  
    }  
    public Operator getOperator() {  
        return operator;  
    }  
    @SuppressWarnings({ "rawtypes", "unchecked" })  
    public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query,  
            CriteriaBuilder builder) {  
        Path expression = null;  
        if(fieldName.contains(".")){  
            String[] names = StringUtils.split(fieldName, ".");  
            expression = root.get(names[0]);  
            for (int i = 1; i < names.length; i++) {  
                expression = expression.get(names[i]);  
            }  
        }else{  
            expression = root.get(fieldName);  
        }  
          
        switch (operator) {  
        case EQ:  
            return builder.equal(expression, value);  
        case NE:  
            return builder.notEqual(expression, value);  
        case LIKE:
        	if(matchMode ==null){
        		return builder.like((Expression<String>) expression, "%" + value + "%");  
        	}else{
        		return builder.like((Expression<String>) expression, matchMode.toMatchString(String.valueOf(value)));
        	}
        case LT:  
            return builder.lessThan(expression, (Comparable) value);  
        case GT:  
            return builder.greaterThan(expression, (Comparable) value);  
        case LTE:  
            return builder.lessThanOrEqualTo(expression, (Comparable) value);  
        case GTE:  
            return builder.greaterThanOrEqualTo(expression, (Comparable) value);  
        default:  
            return null;  
        }  
    }  
      
}  
