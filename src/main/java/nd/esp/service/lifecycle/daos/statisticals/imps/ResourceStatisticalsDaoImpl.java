package nd.esp.service.lifecycle.daos.statisticals.imps;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.daos.statisticals.ResourceStatisticalsDao;

@Repository("ResourceStatisticalsDaoImpl")
public class ResourceStatisticalsDaoImpl implements ResourceStatisticalsDao {

    @PersistenceContext(unitName="entityManagerFactory")
    EntityManager em;
    
    public List<ResourceStatistical> getAllRsByReousrceId(String resourceId) {
        
        Query query = em.createNamedQuery("getStatisticalBuResource") ;
        query.setParameter("resourceId", resourceId) ;
        
        @SuppressWarnings("unchecked")
		List<ResourceStatistical> list =  query.getResultList() ;
        
        return list;
    }

	@Override
	public double getMaxTopValue(String resType) {
		String sql = "SELECT max(key_value) FROM resource_statisticals where res_type='"+resType+"' and key_title='top'";
		Query query = em.createNativeQuery(sql);
		
		@SuppressWarnings("unchecked")
		List<Double> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			return list.get(0) != null ? list.get(0) : 0;
		}
		return 0;
	}

	@Override
	public ResourceStatistical getResourceStatistical(String resType, String id, String keyTitle) {
		String sql = "SELECT rs.* FROM resource_statisticals rs WHERE "
				+ "rs.res_type=:rt AND rs.resource=:rid AND rs.key_title=:kt for update";
		
		Query query = em.createNativeQuery(sql, ResourceStatistical.class);
		query.setParameter("rt", resType);
		query.setParameter("rid", id);
		query.setParameter("kt", keyTitle);
		
		@SuppressWarnings("unchecked")
		List<ResourceStatistical> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			return list.get(0);
		}
		
		return null;
	}
}
