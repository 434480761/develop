package nd.esp.service.lifecycle.daos.common.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.common.CommonDao;
import nd.esp.service.lifecycle.support.DbName;

import nd.esp.service.lifecycle.support.busi.titan.tranaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
/**
 * 公共DAO实现类
 * @author xuzy
 *
 */
@Repository
public class CommonDaoImpl implements CommonDao {

	
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager em;
	
	@PersistenceContext(unitName="questionEntityManagerFactory")
	EntityManager questionEm;

	@Autowired
	private TitanSubmitTransaction titanSubmitTransaction;
	
	@Override
	public int deleteTechInfoByResource(String resType,String resourceId,DbName name) {
		Query query = null;
		if(name.equals(DbName.DEFAULT)){
			query = em.createNamedQuery("deleteTechInfoByResource");
		}else{
			query = questionEm.createNamedQuery("deleteTechInfoByResource");
		}
		query.setParameter("resourceId", resourceId);

		String script = "DefaultGraphTraversal dgt=g.V().has('identifier',{0}).has('primary_category',{1})" +
				".outE().inV().hasLabel({2});while(dgt.hasNext()){dgt.next().remove()};";
		TitanRepositoryOperation operation = new TitanRepositoryOperationScript(script,resourceId,resType,"tech_info");
		TitanTransaction transaction = new TitanTransaction();
		transaction.addNextStep(operation);
		titanSubmitTransaction.submit(transaction);

		return query.executeUpdate();
	}

	@Override
	public int deleteResourceCategoryByResource(String resType,String resourceId,DbName name) {
		Query query = null;
		if(name.equals(DbName.DEFAULT)){
			query = em.createNamedQuery("deleteResourceCategoryByResource");
		}else{
			query = questionEm.createNamedQuery("deleteResourceCategoryByResource");
		}
		query.setParameter("resourceId", resourceId);
		query.setParameter("rts", resType);

		String script = "DefaultGraphTraversal dgt=g.V().has('identifier',{0}).has('primary_category',{1})" +
				".outE().or(hasLabel('has_categories_path'),hasLabel('has_category_code'));while(dgt.hasNext()){dgt.next().remove()};";
		TitanRepositoryOperation operation = new TitanRepositoryOperationScript(script,resourceId,resType);
		TitanTransaction transaction = new TitanTransaction();
		transaction.addNextStep(operation);
		titanSubmitTransaction.submit(transaction);

		return query.executeUpdate();
	}

}
