package nd.esp.service.lifecycle.services.updatamediatype.dao;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;

public interface UpdateMediatypeDao {
    
    public Map<String, List<TechInfo>> getAllTechInfo() ;
    
    public Map<String, ResourceCategory> getAllCategory();
    
    public Map<String, List<ResourceCategory>> getAllCategories() ;
}
