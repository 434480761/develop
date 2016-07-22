package nd.esp.service.lifecycle.services.vrlife.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategory4QuestionDBRepository;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.vrlife.VrLifeService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewTags;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("vrLifeServiceImpl4QuestionDb")
@Transactional(value="questionTransactionManager")
public class VrLifeServiceImpl4QuestionDb implements VrLifeService{
	private static final Logger LOG = LoggerFactory.getLogger(VrLifeServiceImpl4QuestionDb.class);
	
	@Autowired
    private NDResourceService ndResourceService;
	@Autowired
    private NotifyReportService nds;
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	@Autowired
	@Qualifier("lifecycleService4QtiV06")
	private LifecycleServiceV06 lifecycleService4Qti;
	@Autowired
    ResourceCategory4QuestionDBRepository resourceCategory4QuestionDBRepository;
	@Autowired
	private CategoryDataRepository categoryDataRepository;

	
	@SuppressWarnings("unchecked")
	@Override
	public StatusReviewViewModel4Out statusReview(StatusReviewViewModel4In inViewModel) {
		//校验资源是否存在
		Education oldBean = ndResourceService.checkResourceExist(inViewModel.getResType(), inViewModel.getIdentifier());
		
		@SuppressWarnings("rawtypes")
        ResourceRepository resourceRepository =  commonServiceHelper.getRepository(inViewModel.getResType());
		
		//获取包含维度信息的旧资源信息
		List<String> cgInlcude = new ArrayList<String>();
		cgInlcude.add(IncludesConstant.INCLUDE_CG);
		ResourceModel oldResourceModel = ndResourceService.getDetail(inViewModel.getResType(), inViewModel.getIdentifier(), cgInlcude);
		
		//资源状态修改
		oldBean.setStatus(inViewModel.getStatus());
		//资源更新时间修改
		oldBean.setLastUpdate(new Timestamp(System.currentTimeMillis()));
		//资源标签修改
		List<String> oldTags = oldBean.getTags();
		if(CollectionUtils.isNotEmpty(inViewModel.getTags())){
			for(StatusReviewTags tags : inViewModel.getTags()){
				if(tags.getOperation().equals("add")){
					for(String tag : tags.getTags()){
						if(!oldTags.contains(tag)){
							oldTags.add(tag);
						}
					}
				}else if(tags.getOperation().equals("delete")){
					for(String tag : tags.getTags()){
						if(oldTags.contains(tag)){
							oldTags.remove(tag);
						}
					}
				}
			}
		}
		oldBean.setTags(oldTags);
		
		try {
			oldBean = (Education) resourceRepository.update(oldBean);
		} catch (EspStoreException e) {
			LOG.error("vrlife-资源审核出错了", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
		}
		
		//如果是改成ONLINE,则处理publish_type
		if(inViewModel.getStatus().equals(LifecycleStatus.ONLINE.getCode()) && StringUtils.isNotEmpty(inViewModel.getPublishType())){
			if(oldResourceModel!=null && CollectionUtils.isNotEmpty(oldResourceModel.getCategoryList())){
				for(ResClassificationModel rcm : oldResourceModel.getCategoryList()){
					if(StringUtils.isNotEmpty(rcm.getTaxoncode()) && rcm.getTaxoncode().startsWith("PT")){
						CategoryData categoryData = new CategoryData();
						categoryData.setNdCode(inViewModel.getPublishType());
						try {
							categoryData = categoryDataRepository.getByExample(categoryData);
							if(categoryData != null){
								ResourceCategory resourceCategory = resourceCategory4QuestionDBRepository.get(rcm.getIdentifier());
								
								resourceCategory.setTaxoncode(categoryData.getNdCode());
								resourceCategory.setTaxonname(categoryData.getTitle());
								resourceCategory.setTaxoncodeid(categoryData.getIdentifier());
								resourceCategory4QuestionDBRepository.update(resourceCategory);
							}else{
								throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
										LifeCircleErrorMessageMapper.CheckNdCodeFail);
							}
						} catch (EspStoreException e) {
							throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
									LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
									e.getLocalizedMessage());
						}
						
						break;
					}
				}
			}
		}
		
		//创建资源生命周期  -- 记录审核人
        ResContributeModel contributeModel = new ResContributeModel();
        contributeModel.setLifecycleStatus(oldBean.getStatus());
        contributeModel.setMessage(
        		"VR人生资源审核:[" + inViewModel.getReviewPerson() + "] 修改了 [" 
        		+ inViewModel.getResType() + ":" + inViewModel.getIdentifier() + "]");
        contributeModel.setTitle("vrlife");
        contributeModel.setTargetType("User");
        contributeModel.setTargetId("vrlife");
        contributeModel.setTargetName(inViewModel.getReviewPerson());
        lifecycleService4Qti.addLifecycleStep(inViewModel.getResType(), inViewModel.getIdentifier(), contributeModel);
		
		//同步推送至报表系统
        ResourceModel resourceModel = ndResourceService.getDetail(inViewModel.getResType(), inViewModel.getIdentifier(), IncludesConstant.getIncludesList());
		nds.notifyReport4Resource(inViewModel.getResType(),resourceModel,OperationType.UPDATE);
		
		StatusReviewViewModel4Out outViewModel = new StatusReviewViewModel4Out();
		outViewModel.setIdentifier(oldBean.getIdentifier());
		outViewModel.setStatus(oldBean.getStatus());
		outViewModel.setTags(oldBean.getTags());
		
		return outViewModel;
	}
}
