package nd.esp.service.lifecycle.daos.statisticals;

import java.util.List;

import nd.esp.service.lifecycle.repository.model.ResourceStatistical;

public interface ResourceStatisticalsDao {
    
    public List<ResourceStatistical> getAllRsByReousrceId(String resourceId) ;
}
