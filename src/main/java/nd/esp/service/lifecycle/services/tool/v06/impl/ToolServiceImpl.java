package nd.esp.service.lifecycle.services.tool.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.services.tool.v06.ToolServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategory4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;

@Service(value="ToolServiceImpl")
public class ToolServiceImpl implements ToolServiceV06{
	private final static Logger LOG= LoggerFactory.getLogger(ToolServiceImpl.class);
	
	@Autowired
	private ResourceCategoryRepository resourceCategoryRepository;
	
	@Autowired
	private ResourceCategory4QuestionDBRepository resourceCategory4QuestionDBRepository;
	
	@Autowired
	private CategoryDataRepository categoryDataRepository;
	
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	
	@Override
	public List<String> getTmCategories(String tmId) {
		List<String> returnList = new ArrayList<String>();
		ResourceCategory rc = new ResourceCategory();
		rc.setResource(tmId);
		try {
			List<ResourceCategory> rcList = resourceCategoryRepository.getAllByExample(rc);
			if(CollectionUtils.isNotEmpty(rcList)){
				Set<String> set = new HashSet<String>();
				for (ResourceCategory resourceCategory : rcList) {
					String path = resourceCategory.getTaxonpath();
					if(StringUtils.isNotEmpty(path)){
						set.add(path);
					}
				}
				if(CollectionUtils.isNotEmpty(set)){
					returnList.addAll(set);
				}
			}
		} catch (EspStoreException e) {
			LOG.warn("根据教材id查询维度路径path失败",e);
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		return returnList;
	}

	@Override
	public void copyTmCategories2Res(List<String> tmCategories, List<Map<String,String>> resList) {
		List<ResourceCategory> rcList = new ArrayList<ResourceCategory>();
		List<ResourceCategory> rcList4Question = new ArrayList<ResourceCategory>();
		if(CollectionUtils.isNotEmpty(tmCategories)){
			for (String path : tmCategories) {
				for (Map<String, String> map : resList) {
					String resId = map.get("resId");
					String resType = map.get("resType");
					
					//1、根据资源id与taxonpath查询维度数据是否存在
					ResourceCategory entity = new ResourceCategory();
					entity.setResource(resId);
					entity.setTaxonpath(path);
					ResourceCategory rc = null;
					try {
						if(commonServiceHelper.isQuestionDb(resType)){
							rc = resourceCategory4QuestionDBRepository.getByExample(entity);
						}else{
							rc = resourceCategoryRepository.getByExample(entity);
						}
						
					} catch (EspStoreException e) {
						LOG.warn("工具API接口将教材的维度路径copy至目标资源出错",e);
						throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
					}
					if(rc == null){
						//说明未找到
				        EspRepository repository = ServicesManager.get(resType);
				        Education  edu = null;
				        try {
							edu = (Education)repository.get(resId);
						} catch (EspStoreException e) {
							LOG.warn("工具API接口将教材的维度路径copy至目标资源出错",e);
							throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
						}
				        
				        
				        if(edu != null){
				        	edu.setLastUpdate(new Timestamp(System.currentTimeMillis()));
				        	try {
								repository.update(edu);
							} catch (EspStoreException e1) {
								LOG.warn("工具API接口将教材的维度路径copy至目标资源出错",e1);
								throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e1.getLocalizedMessage());
							}
				        	
				        	
					        //增加维度数据
					        String[] taxonCodes = path.split("/");
					        Set<String> ndCodeList = new HashSet<String>();
					        for (String taxonCode : taxonCodes) {
								if(StringUtils.isNotEmpty(taxonCode) && !taxonCode.equals("K12")){
									ndCodeList.add(taxonCode);
								}
							}
					        
					        if(CollectionUtils.isNotEmpty(ndCodeList)){
					        	List<CategoryData> beanListResult;
								try {
									beanListResult = categoryDataRepository.getListWhereInCondition("ndCode",new ArrayList<String>(ndCodeList));
								} catch (EspStoreException e) {
									LOG.warn("工具API接口将教材的维度路径copy至目标资源出错",e);
									throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
								}
								//取维度
								if(CollectionUtils.isNotEmpty(beanListResult)){
									Map<String,String> categoryMap = commonServiceHelper.getCategoryByData(beanListResult);
						        	for (CategoryData cd : beanListResult) {
										ResourceCategory tmp = new ResourceCategory();
										tmp.setIdentifier(UUID.randomUUID().toString());
										tmp.setTaxoncode(cd.getNdCode());
										tmp.setTaxonname(cd.getTitle());
										tmp.setTaxonpath(path);
										tmp.setResource(resId);
										tmp.setShortName(cd.getShortName());
										tmp.setTaxoncodeid(cd.getIdentifier());
										tmp.setCategoryName(cd.getTitle());
										tmp.setCategoryCode(cd.getNdCode().substring(0, 2));
										tmp.setCategoryName(categoryMap.get(tmp.getCategoryCode()));
										tmp.setPrimaryCategory(resType);
										
										if(CommonServiceHelper.isQuestionDb(resType)){
											rcList4Question.add(tmp);
										}else{
											rcList.add(tmp);
										}
									}
								}
					        }
						}

					}
				}
			}
		}
		
		//批量保存数据
		if(CollectionUtils.isNotEmpty(rcList)){
			commonServiceHelper.batchAddResourceCategory(rcList);
		}
		
		if(CollectionUtils.isNotEmpty(rcList4Question)){
			commonServiceHelper.batchAddResourceCategory4Question(rcList4Question);
		}
	}

}
