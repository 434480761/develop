package nd.esp.service.lifecycle.services.notify;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceUsing;
import nd.esp.service.lifecycle.support.enums.OperationType;
/**
 * 通知报表中心系统
 * @author xuzy
 *
 */
public interface NotifyReportService {
	public void addResource(String resType, ResourceModel rm);
	public void updateResource(String resType, ResourceModel rm);
	public void deleteResource(String resType,String uuid);
	
	public void addResourceCategory(ResourceModel rm);
	public void updateResourceCategory(ResourceModel rm);
	public void deleteResourceCategory(String resource);
	
	public void addCategory(CategoryModel cm);
	public void updateCategory(CategoryModel cm);
	public void deleteCategory(String identifier);
	
	public void addCategoryData(CategoryDataModel cdm);
	public void updateCategoryData(CategoryDataModel cdm);
	public void deleteCategoryData(String identifier);
	
	public boolean checkCoverageIsNd(String resourceType,String identifier);
	
	public void addResourceUsing(ReportResourceUsing rru);
	
	public void notifyReport4Resource(String resourceType,ResourceModel rm,OperationType ot);
	
	public void notifyReport4AddCoverage(String resType,List<CoverageModel> cmList);
}
