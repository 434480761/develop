package nd.esp.service.lifecycle.repository.ds;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;


/** 
 * @Description 动态查询的Specification。
 * @author Rainy(yang.lin)  
 * @date 2015年5月19日 下午1:42:42 
 * @version V1.0
 * @param <T>
 */ 
  	
public class ItemsSpecification<T> implements Specification<T> {
	private List<Item<? extends Object>> itemList;

	public ItemsSpecification(List<Item<? extends Object>> itemList) {
		this.itemList = itemList;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		 List<Predicate> predicates = new ArrayList<Predicate>();  
		Predicate predicate = null;
		if (itemList!=null && itemList.size()>0) {
			for (Item<? extends Object> item : itemList) {
				predicate = item.getComparsionOperator().getPredicate(item.getKey(), item.getValue(), root, query, cb);
				if (predicate != null)
					predicate = item.getLogicalOperator().getPredicate(predicate, cb);
				predicates.add(predicate);
			}
		}
		if (predicate == null)
			return cb.conjunction();
		return cb.and(predicates.toArray(new Predicate[predicates.size()]));  
	}
}
