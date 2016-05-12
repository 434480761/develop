package nd.esp.service.lifecycle.services.notify.impls;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResCoverageModel;
import nd.esp.service.lifecycle.educommon.models.ResRelationModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.report.ReportCategory;
import nd.esp.service.lifecycle.repository.model.report.ReportCategoryData;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceCategory;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.report.ReportCategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.report.ReportResourceRelationRepository;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value="reportTransactionManager")
public class NotifyReportServiceImpl implements NotifyReportService {
	private static final String INSERT = "insert";
	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	@Autowired
	private ReportResourceCategoryRepository rrcr;
	
	@Autowired
	private ReportResourceRelationRepository rrrr;
	
	@Autowired
	private ReportCategoryRepository rcr;
	
	@Autowired
	private ReportCategoryDataRepository rcdr;
	
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
		if(CollectionUtils.isNotEmpty(rm.getCategoryList())){
			List<ReportResourceCategory> rrcList = new ArrayList<ReportResourceCategory>();
			for (ResClassificationModel rcm : rm.getCategoryList()) {
				ReportResourceCategory rrc = new ReportResourceCategory();
				rrc.setCreateTime(new Timestamp(rm.getLifeCycle().getCreateTime().getTime()));
				rrc.setCategoryName(rcm.getCategoryName());
				rrc.setIdentifier(UUID.randomUUID().toString());
				rrc.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
				rrc.setOperationFlag(INSERT);
				rrc.setResource(rm.getIdentifier());
				rrc.setResource(rcm.getResourceId());
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
		deleteResourceCategory(rm.getIdentifier());
		addResourceCategory(rm);
	}

	@Override
	public void deleteResourceCategory(String resource) {
		long lastUpdate = System.currentTimeMillis();
		String sql = "update resource_categories set last_update = "+lastUpdate +",operation_flag = '"+DELETE+"' where resource = '"+resource+"'";
		reportJdbcTemplate.execute(sql);
	}

	@Override
	public void addCategory(CategoryModel cm) {
		long time = System.currentTimeMillis();
		ReportCategory rc = new ReportCategory();
		rc.setCreateTime(new Timestamp(time));
		rc.setTitle(cm.getTitle());
		rc.setLastUpdate(new BigDecimal(time));
		rc.setOperationFlag(INSERT);
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
			rc.setOperationFlag(UPDATE);
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
		ReportCategory rc = null;
		try {
			rc = rcr.get(identifier);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
		if(rc != null){
			rc.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			rc.setOperationFlag(DELETE);
			rc.setIdentifier(identifier);
			try {
				rcr.update(rc);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
	                    e.getMessage());
			}
		}
	}

	@Override
	public void addCategoryData(CategoryDataModel cdm) {
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
		rcd.setOperationFlag(INSERT);
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
			rcd.setOperationFlag(UPDATE);
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
		ReportCategoryData rcd = null;
		try {
			rcd = rcdr.get(identifier);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
		if(rcd != null){
			rcd.setIdentifier(identifier);
			rcd.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			rcd.setOperationFlag(DELETE);
			try {
				rcdr.update(rcd);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
	                    e.getMessage());
			}
		}
		
	}

	@Override
	public void addResourceRelation(List<ResourceRelation> relationList) {
		if(CollectionUtils.isNotEmpty(relationList)){
			long time = System.currentTimeMillis();
			List<ReportResourceRelation> rrrList = new ArrayList<ReportResourceRelation>();
			for (ResourceRelation resourceRelation : relationList) {
				ReportResourceRelation rrr = new ReportResourceRelation();
				rrr.setCreateTime(new Timestamp(time));
				rrr.setIdentifier(resourceRelation.getIdentifier());
				rrr.setLastUpdate(new Timestamp(time));
				rrr.setOperationFlag(INSERT);
				rrr.setRelationType(resourceRelation.getRelationType());
				rrr.setResourceCreateTime(resourceRelation.getResourceCreateTime());
				rrr.setResourceTargetType(resourceRelation.getResourceTargetType());
				rrr.setResType(resourceRelation.getResType());
				rrr.setSourceUuid(resourceRelation.getSourceUuid());
				rrr.setTarget(resourceRelation.getTarget());
				rrr.setTargetCreateTime(resourceRelation.getTargetCreateTime());
				rrrList.add(rrr);
			}
			if(rrrList.size() > 0){
				try {
					rrrr.batchAdd(rrrList);
				} catch (EspStoreException e) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
		                    e.getMessage());
				}
			}
		}
		
	}

	@Override
	public void updateResourceRelation(ResourceRelation relation) {
		ReportResourceRelation rrr = null;
		try {
			rrr = rrrr.get(relation.getIdentifier());
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getMessage());
		}
		if(rrr == null){
			List<ResourceRelation> rrList = new ArrayList<ResourceRelation>();
			rrList.add(relation);
			addResourceRelation(rrList);
		}else{
			rrr.setIdentifier(relation.getIdentifier());
			rrr.setLastUpdate(new Timestamp(System.currentTimeMillis()));
			rrr.setOperationFlag(UPDATE);
			rrr.setRelationType(relation.getRelationType());
			rrr.setResourceCreateTime(relation.getResourceCreateTime());
			rrr.setResourceTargetType(relation.getResourceTargetType());
			rrr.setResType(relation.getResType());
			rrr.setSourceUuid(relation.getSourceUuid());
			rrr.setTarget(relation.getTarget());
			rrr.setTargetCreateTime(relation.getTargetCreateTime());
			try {
				rrrr.update(rrr);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
	                    e.getMessage());
			}
		}
	}

	@Override
	public void deleteResourceRelation(List<ResourceRelation> relationList) {
		if(CollectionUtils.isNotEmpty(relationList)){
			long time = System.currentTimeMillis();
			List<ReportResourceRelation> rrrList = new ArrayList<ReportResourceRelation>();
			for (ResourceRelation resourceRelation : relationList) {
				ReportResourceRelation rrr = new ReportResourceRelation();
				rrr.setIdentifier(resourceRelation.getIdentifier());
				rrr.setLastUpdate(new Timestamp(time));
				rrr.setOperationFlag(DELETE);
				rrr.setResType(resourceRelation.getResType());
				rrr.setResourceTargetType(resourceRelation.getResourceTargetType());
				rrrList.add(rrr);
			}
			if(rrrList.size() > 0){
				try {
					rrrr.batchAdd(rrrList);
				} catch (EspStoreException e) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
		                    e.getMessage());
				}
			}
		}
	}
	
	@Override
	public void deleteResourceRelationBySourceId(String resType,String sourceId){
		long lastUpdate = System.currentTimeMillis();
		Timestamp ts = new Timestamp(lastUpdate);
		String sql = "update resource_relations set last_update = '"
				+ DateUtils.format(ts, "yyyy-MM-dd HH:mm:ss") + "',operation_flag = '" + DELETE
				+ "' where (res_type='" + resType + "' and source_uuid = '"
				+ sourceId + "') or (resource_target_type='" + resType
				+ "' and target = '" + sourceId + "')";
		reportJdbcTemplate.execute(sql);
	}
	
	
	public void notifyReport4AddCoverage(String resType,List<CoverageModel> cmList){
		long time = System.currentTimeMillis();
		if(CollectionUtils.isNotEmpty(cmList)){
			List<ReportResourceCategory> rcList = new ArrayList<ReportResourceCategory>();
			List<ReportResourceRelation> rrList = new ArrayList<ReportResourceRelation>();
			for (CoverageModel coverageModel : cmList) {
				if("Org".equals(coverageModel.getTargetType()) && "nd".equals(coverageModel.getTarget())){
					String resource = coverageModel.getResource();
					String sql = "select identifier,resource,taxOnCode,category_name from resource_categories where primary_category = '"+resType+"' and resource = '"+resource+"'";
					String relationSql = "select identifier,relation_type,res_type,resource_target_type,source_uuid,target,create_time from resource_relations where (res_type='"
							+ resType
							+ "' and source_uuid='"
							+ resource
							+ "') or (resource_target_type='"
							+ resType
							+ "' and target='" + resource + "')";
					
					List<Map<String,Object>> resultList = null;
					List<Map<String,Object>> relationsList = null;
					if(CommonServiceHelper.isQuestionDb(resType)){
						resultList = questionJdbcTemplate.queryForList(sql);
						relationsList = questionJdbcTemplate.queryForList(relationSql);
					}else{
						resultList = defaultJdbcTemplate.queryForList(sql);
						relationsList = defaultJdbcTemplate.queryForList(relationSql);
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
							rrc.setOperationFlag(INSERT);
							rrc.setResource(res);
							rrc.setTaxOnCode(taxOnCode);
							rcList.add(rrc);
						}
					}
					if(CollectionUtils.isNotEmpty(relationsList)){
						for (Map<String, Object> map : relationsList) {
							String identifier = (String)map.get("identifier");
							String relationType = (String)map.get("relation_type");
							String rt = (String)map.get("res_type");
							String rtt = (String)map.get("resource_target_type");
							String su = (String)map.get("source_uuid");
							String t = (String)map.get("target");
							Timestamp ct = (Timestamp)map.get("create_time");
							
							ReportResourceRelation rrr = new ReportResourceRelation();
							rrr.setIdentifier(identifier);
							rrr.setCreateTime(ct);
							rrr.setLastUpdate(new Timestamp(time));
							rrr.setOperationFlag(INSERT);
							rrr.setRelationType(relationType);
							rrr.setResType(rt);
							rrr.setSourceUuid(su);
							rrr.setTarget(t);
							rrr.setResourceTargetType(rtt);
							rrList.add(rrr);
						}
					}
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
			
			if(CollectionUtils.isNotEmpty(rrList)){
				try {
					rrrr.batchAdd(rrList);
				} catch (EspStoreException e) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
		                    e.getMessage());
				}
			}
		}
	}
	
	@Override
	public void notifyReport4Resource(String resourceType,ResourceModel rm,OperationType ot){
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
				addResourceCategory(rm);
				List<ResRelationModel> relationList = rm.getRelations();
				if(CollectionUtils.isNotEmpty(relationList)){
					List<ResourceRelation> rList = new ArrayList<ResourceRelation>();
					for (ResRelationModel rrm : relationList) {
						ResourceRelation rr = new ResourceRelation();
						rr.setIdentifier(rrm.getIdentifier());
						rr.setSourceUuid(rrm.getSource());
						rr.setResType(rrm.getSourceType());
						rr.setTarget(rrm.getTarget());
						rr.setResourceTargetType(rrm.getTargetType());
						rr.setRelationType(rrm.getRelationType());
						rList.add(rr);
					}
					addResourceRelation(rList);
				}
				
			}else if(ot == OperationType.UPDATE){
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
	
}
