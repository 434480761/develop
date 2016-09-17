package nd.esp.service.lifecycle.repository.ds;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;


/** 
 * @Description 动态查询的逻辑符操作器。
 * @author Rainy(yang.lin)  
 * @date 2015年5月19日 下午1:42:51 
 * @version V1.0
 */ 
  	
public enum LogicalOperator {
	OR {
		@Override
		public Predicate getPredicate(Predicate predicate, CriteriaBuilder builder) {
			return builder.or(predicate);
		}
	},
	AND {
		@Override
		public Predicate getPredicate(Predicate predicate, CriteriaBuilder builder) {
			return builder.and(predicate);
		}
	},
	NOT {
		@Override
		public Predicate getPredicate(Predicate predicate, CriteriaBuilder builder) {
			return builder.not(predicate);
		}
	};
	
	public abstract Predicate getPredicate(Predicate predicate, CriteriaBuilder builder);
}
