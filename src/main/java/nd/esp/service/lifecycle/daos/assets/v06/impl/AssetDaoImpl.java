package nd.esp.service.lifecycle.daos.assets.v06.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.assets.v06.AssetDao;
import nd.esp.service.lifecycle.repository.model.Asset;

import org.springframework.stereotype.Repository;

@Repository
public class AssetDaoImpl implements AssetDao{
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager em;
	
	public List<Asset> queryByCategory(String category){
		Query query = em.createNamedQuery("queryAssetsByCategory");
		query.setParameter("category", category);
		List<Asset> assetList = query.getResultList();
		return assetList;
	}
	
	public List<Asset> queryBySourceId(String sourceId,String category){
		Query query = em.createNamedQuery("queryAssetsBySourceId");
		query.setParameter("sourceId", sourceId);
		query.setParameter("category", category);
		List<Asset> assetList = query.getResultList();
		return assetList;
	}
	
	public List<Asset> queryInsTypesByCategory(String likeName,String category){
		Query query = em.createNamedQuery("queryInsTypesByCategory");
		query.setParameter("likeName", likeName+"__");
		query.setParameter("category", category);
		List<Asset> assetList = query.getResultList();
		return assetList;
	}
}
