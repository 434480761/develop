package nd.esp.service.lifecycle.services.notify.impls;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResCoverageModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.report.ReportCategory;
import nd.esp.service.lifecycle.repository.model.report.ReportCategoryData;
import nd.esp.service.lifecycle.repository.model.report.ReportNdResource;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceCategory;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceUsing;
import nd.esp.service.lifecycle.repository.sdk.report.ReportCategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportNdresourceRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportResourceUsingRepository;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value="reportTransactionManager")
public class NotifyReportServiceImpl implements NotifyReportService {
	@Autowired
	private ReportResourceCategoryRepository rrcr;
	
	@Autowired
	private ReportCategoryRepository rcr;
	
	@Autowired
	private ReportCategoryDataRepository rcdr;
	
	@Autowired
	private ReportResourceUsingRepository rrur;
	
	@Autowired
	private ReportNdresourceRepository rnrr;
	
	@Autowired
	@Qualifier(value="reportJdbcTemplate")
	private JdbcTemplate reportJdbcTemplate;
	
	@Autowired
	@Qualifier(value="questionJdbcTemplate")
	private JdbcTemplate questionJdbcTemplate;
	
	@Autowired
	@Qualifier(value="defaultJdbcTemplate")
	private JdbcTemplate defaultJdbcTemplate;

	@Override
	public void addResourceCategory(ResourceModel rm) {
		if(!StaticDatas.SYNC_REPORT_DATA){
			return;
		}
		if(CollectionUtils.isNotEmpty(rm.getCategoryList())){
			List<ReportResourceCategory> rrcList = new ArrayList<ReportResourceCategory>();
			long time = System.currentTimeMillis();
			for (ResClassificationModel rcm : rm.getCategoryList()) {
				ReportResourceCategory rrc = new ReportResourceCategory();
				rrc.setCreateTime(new Timestamp(rm.getLifeCycle().getCreateTime().getTime()));
				rrc.setCategoryName(rcm.getCategoryName());
				rrc.setIdentifier(UUID.randomUUID().toString());
				rrc.setLastUpdate(new BigDecimal(time));
				if(rcm.getResourceId() != null){
					rrc.setResource(rcm.getResourceId());
				}else{
					rrc.setResource(rm.getIdentifier());
				}
				rrc.setTaxOnCode(rcm.getTaxoncode());
				rrcList.add(rrc);
			}
			if(rrcList.size() > 0){
				try {
					rrcr.batchAdd(rrcList);
				} catch (EspStoreException e) {
		            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                            e.getMessage());
				}
			}
		}
		
	}

	@Override
	public void updateResourceCategory(ResourceModel rm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		deleteResourceCategory(rm.getIdentifier());
		addResourceCategory(rm);
	}

	@Override
	public void deleteResourceCategory(String resource) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		String sql = "delete resource_categories where resource = '"+resource+"'";
		reportJdbcTemplate.execute(sql);
	}

	@Override
	public void addCategory(CategoryModel cm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		long time = System.currentTimeMillis();
		ReportCategory rc = new ReportCategory();
		rc.setCreateTime(new Timestamp(time));
		rc.setTitle(cm.getTitle());
		rc.setLastUpdate(new BigDecimal(time));
		rc.setShortName(cm.getShortName());
		rc.setIdentifier(cm.getIdentifier());
		try {
			rcr.add(rc);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}

	@Override
	public void updateCategory(CategoryModel cm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		ReportCategory rc = null;
		try {
			rc = rcr.get(cm.getIdentifier());
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
		if(rc == null){
			addCategory(cm);
		}else{
			rc.setTitle(cm.getTitle());
			rc.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			rc.setShortName(cm.getShortName());
			rc.setIdentifier(cm.getIdentifier());
			try {
				rcr.update(rc);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
	                    e.getMessage());
			}
		}
	}

	@Override
	public void deleteCategory(String identifier) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		try {
			rcr.del(identifier);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}

	@Override
	public void addCategoryData(CategoryDataModel cdm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		long time = System.currentTimeMillis();
		ReportCategoryData rcd = new ReportCategoryData();
		if(cdm.getCategory() != null){
			rcd.setCategory(cdm.getCategory().getIdentifier());
		}
		
		rcd.setCreateTime(new Timestamp(time));
		rcd.setDescription(cdm.getDescription());
		rcd.setIdentifier(cdm.getIdentifier());
		rcd.setTitle(cdm.getTitle());
		rcd.setLastUpdate(new BigDecimal(time));
		rcd.setNdCode(cdm.getNdCode());
		if(cdm.getParent() != null){
			rcd.setParent(cdm.getParent().getIdentifier());
		}
		try {
			rcdr.add(rcd);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}

	@Override
	public void updateCategoryData(CategoryDataModel cdm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		ReportCategoryData rcd = null;
		try {
			rcd = rcdr.get(cdm.getIdentifier());
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
		if(rcd == null){
			addCategoryData(cdm);
		}else{
			rcd.setDescription(cdm.getDescription());
			rcd.setIdentifier(cdm.getIdentifier());
			rcd.setTitle(cdm.getTitle());
			rcd.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			rcd.setNdCode(cdm.getNdCode());
			if(cdm.getCategory() != null){
				rcd.setCategory(cdm.getCategory().getIdentifier());
			}
			if(cdm.getParent() != null){
				rcd.setParent(cdm.getParent().getIdentifier());
			}
			try {
				rcdr.add(rcd);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
	                    e.getMessage());
			}
		}
		
	}

	@Override
	public void deleteCategoryData(String identifier) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		try {
			rcdr.del(identifier);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}
	
	public void notifyReport4AddCoverage(String resType,List<CoverageModel> cmList){
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		long time = System.currentTimeMillis();
		if(CollectionUtils.isNotEmpty(cmList)){
			List<ReportResourceCategory> rcList = new ArrayList<ReportResourceCategory>();
			for (CoverageModel coverageModel : cmList) {
				if("Org".equals(coverageModel.getTargetType()) && "nd".equals(coverageModel.getTarget())){
					String resource = coverageModel.getResource();
					String sql = "select identifier,resource,taxOnCode,category_name from resource_categories where primary_category = '"+resType+"' and resource = '"+resource+"'";
					
					List<Map<String,Object>> resultList = null;
					if(CommonServiceHelper.isQuestionDb(resType)){
						resultList = questionJdbcTemplate.queryForList(sql);
					}else{
						resultList = defaultJdbcTemplate.queryForList(sql);
					}
					if(CollectionUtils.isNotEmpty(resultList)){
						for (Map<String, Object> map : resultList) {
							String identifier = (String)map.get("identifier");
							String res = (String)map.get("resource");
							String taxOnCode = (String)map.get("taxOnCode");
							String categoryName = (String)map.get("category_name");
							ReportResourceCategory rrc = new ReportResourceCategory();
							rrc.setCategoryName(categoryName);
							rrc.setCreateTime(new Timestamp(time));
							rrc.setIdentifier(identifier);
							rrc.setLastUpdate(new BigDecimal(time));
							rrc.setResource(res);
							rrc.setTaxOnCode(taxOnCode);
							rcList.add(rrc);
						}
					}
					updateResourceLastTime(resType,resource);
				}
			}
			
			if(CollectionUtils.isNotEmpty(rcList)){
				try {
					rrcr.batchAdd(rcList);
				} catch (EspStoreException e) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
		                    e.getMessage());
				}
			}
			
		}
	}
	
	@Override
	public void notifyReport4Resource(String resourceType,ResourceModel rm,OperationType ot){
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		boolean isNd = false;
		if(ot == OperationType.CREATE){
			List<ResCoverageModel> covList = rm.getCoverages();
			if(CollectionUtils.isNotEmpty(covList)){
				for (ResCoverageModel resCoverageModel : covList) {
					if("Org".equals(resCoverageModel.getTargetType()) && "nd".equals(resCoverageModel.getTarget())){
						isNd = true;
						break;
					}
				}
			}
		}else if(ot == OperationType.UPDATE){
			isNd = checkCoverageIsNd(resourceType,rm.getIdentifier());
		}
		
		if(isNd){
			if(ot == OperationType.CREATE){
				addResource(resourceType, rm);
				addResourceCategory(rm);
			}else if(ot == OperationType.UPDATE){
				updateResource(resourceType, rm);
				updateResourceCategory(rm);
			}
		}
	}
	
	/**
	 * 判断是否为ND库资源
	 * @param resourceType
	 * @param identifier
	 * @return
	 */
	public boolean checkCoverageIsNd(String resourceType,String identifier){
		boolean isNd = false;
		String sql = "SELECT count(1) from ndresource nd,res_coverages rc where nd.enable=1 and nd.primary_category='"
				+ resourceType
				+ "' and nd.identifier = '"
				+ identifier
				+ "' and rc.res_type='"
				+ resourceType
				+ "' and rc.resource = nd.identifier and rc.target_type = 'Org' and rc.target = 'nd'";
		if(CommonServiceHelper.isQuestionDb(resourceType)){
			Integer num = questionJdbcTemplate.queryForObject(sql, Integer.class);
			if(num != null && num.intValue() > 0){
				isNd = true;
			}
		}else{
			Integer num = defaultJdbcTemplate.queryForObject(sql, Integer.class);
			if(num != null && num.intValue() > 0){
				isNd = true;
			}
		}
		return isNd;
	}

	@Override
	public void addResource(String resType, ResourceModel rm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		ReportNdResource rnr = convertResource(resType,rm);
		try {
			rnrr.add(rnr);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}

	@Override
	public void updateResource(String resType, ResourceModel rm) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		ReportNdResource rnr = convertResource(resType,rm);
		try {
			rnrr.update(rnr);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}

	@Override
	public void deleteResource(String resType, String uuid) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		String sql = "udpate ndresource set enable = 0,last_update="+System.currentTimeMillis()+" where primary_category='"+resType+"' and identifier = '"+uuid+"'";
		reportJdbcTemplate.execute(sql);
	}

	@Override
	public void addResourceUsing(ReportResourceUsing rru) {
		if(!StaticDatas.SYNC_REPORT_DATA){return;}
		try {
			rrur.add(rru);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
	}
	
	private ReportNdResource convertResource(String resType, ResourceModel rm){
		ReportNdResource rnd = new ReportNdResource();
		rnd.setIdentifier(rm.getIdentifier());
		rnd.setPrimaryCategory(resType);
		rnd.setDescription(rm.getDescription());
		rnd.setTitle(rm.getTitle());
		if(rm.getLifeCycle() != null){
			rnd.setEnable(rm.getLifeCycle().isEnable());
			rnd.setStatus(rm.getLifeCycle().getStatus());
			if(rm.getLifeCycle().getCreateTime() != null){
				rnd.setCreateTime(new Timestamp(rm.getLifeCycle().getCreateTime().getTime()));
				rnd.setLastUpdate(new BigDecimal(rm.getLifeCycle().getLastUpdate().getTime()));
			}
		}
		return rnd;
	}
	
	private void updateResourceLastTime(String resType,String uuid){
		String sql = "update ndresource set last_update="+System.currentTimeMillis()+" where primary_category='"+resType+"' and identifier = '"+uuid+"'";
		reportJdbcTemplate.execute(sql);
	}

}
