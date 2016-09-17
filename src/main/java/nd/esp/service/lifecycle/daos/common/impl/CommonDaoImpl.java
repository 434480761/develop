package nd.esp.service.lifecycle.daos.common.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.common.CommonDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCategoryRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanTechInfoRepository;
import nd.esp.service.lifecycle.support.DbName;

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
	private TitanTechInfoRepository titanTechInfoRepository;

	@Autowired
	private TitanCategoryRepository titanCategoryRepository;
	
	@Override
	public int deleteTechInfoByResource(String resType,String resourceId,DbName name) {
		Query query = null;
		if(name.equals(DbName.DEFAULT)){
			query = em.createNamedQuery("deleteTechInfoByResource");
		}else{
			query = questionEm.createNamedQuery("deleteTechInfoByResource");
		}
		query.setParameter("resourceId", resourceId);
		titanTechInfoRepository.deleteAllByResource(resType,resourceId);
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
		titanCategoryRepository.deleteAll(resType, resourceId);
		return query.executeUpdate();
	}

}
