package nd.esp.service.lifecycle.services.notify;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.support.enums.OperationType;
/**
 * 通知报表中心系统
 * @author xuzy
 *
 */
public interface NotifyReportService {
	public void addResourceCategory(ResourceModel rm);
	public void updateResourceCategory(ResourceModel rm);
	public void deleteResourceCategory(String resource);
	
	public void addCategory(CategoryModel cm);
	public void updateCategory(CategoryModel cm);
	public void deleteCategory(String identifier);
	
	public void addCategoryData(CategoryDataModel cdm);
	public void updateCategoryData(CategoryDataModel cdm);
	public void deleteCategoryData(String identifier);
	
	public void addResourceRelation(List<ResourceRelation> relationList);
	public void updateResourceRelation(ResourceRelation resourceRelation);
	public void deleteResourceRelation(List<ResourceRelation> relationList);
	public void deleteResourceRelationBySourceId(String sourceId);
	
	public void notifyReport4Resource(String resourceType,ResourceModel rm,OperationType ot);
}
