package nd.esp.service.lifecycle.daos.statisticals.imps;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
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

}
