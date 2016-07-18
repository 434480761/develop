package nd.esp.service.lifecycle.daos.statisticals.imps;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.daos.statisticals.ResourceStatisticalsDao;

@Repository("ResourceStatisticalsDao4QuestionDBImpl")
public class ResourceStatisticalsDao4QuestionDBImpl implements ResourceStatisticalsDao {

    @PersistenceContext(unitName="questionEntityManagerFactory")
    EntityManager questionEm;
    
    public List<ResourceStatistical> getAllRsByReousrceId(String resourceId) {
        
        Query query = questionEm.createNamedQuery("getStatisticalBuResource") ;
        query.setParameter("resourceId", resourceId) ;
        
        List<ResourceStatistical> list =  query.getResultList() ;
        
        return list;
    }

	@Override
	public double getMaxTopValue(String resType) {
		String sql = "SELECT max(key_value) FROM resource_statisticals where res_type='"+resType+"' and key_title='top'";
		Query query = questionEm.createNativeQuery(sql);
		List<Double> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			return list.get(0) != null ? list.get(0) : 0;
		}
		return 0;
	}

}
