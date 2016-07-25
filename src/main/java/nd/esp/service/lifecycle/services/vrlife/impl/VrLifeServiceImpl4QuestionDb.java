package nd.esp.service.lifecycle.services.vrlife.impl;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.vrlife.VrLifeService;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewTags;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("vrLifeServiceImpl4QuestionDb")
@Transactional(value="questionTransactionManager")
public class VrLifeServiceImpl4QuestionDb implements VrLifeService{
//	private static final Logger LOG = LoggerFactory.getLogger(VrLifeServiceImpl4QuestionDb.class);
	
	@Autowired
    private NDResourceService ndResourceService;
	@Autowired
	@Qualifier("lifecycleService4QtiV06")
	private LifecycleServiceV06 lifecycleService4Qti;
	
	@Override
	public StatusReviewViewModel4Out statusReview(StatusReviewViewModel4In inViewModel) {
		//获取包含维度信息的旧资源信息
		List<String> includes = new ArrayList<String>();
		includes.add(IncludesConstant.INCLUDE_CG);
		includes.add(IncludesConstant.INCLUDE_LC);
		ResourceModel oldResourceModel = ndResourceService.getDetail(inViewModel.getResType(), inViewModel.getIdentifier(), includes);
		
		//新的资源model,用于局部更新
		ResourceModel newResourceModel = new ResourceModel();
		newResourceModel.setIdentifier(oldResourceModel.getIdentifier());
		//资源状态修改
		ResLifeCycleModel lc4Status = new ResLifeCycleModel();
		lc4Status.setStatus(inViewModel.getStatus());
		newResourceModel.setLifeCycle(lc4Status);
		//资源标签修改
		List<String> dealTags = oldResourceModel.getTags();
		if(CollectionUtils.isNotEmpty(inViewModel.getTags())){
			for(StatusReviewTags tags : inViewModel.getTags()){
				if(tags.getOperation().equals("add")){
					for(String tag : tags.getTags()){
						if(!dealTags.contains(tag)){
							dealTags.add(tag);
						}
					}
				}else if(tags.getOperation().equals("delete")){
					for(String tag : tags.getTags()){
						if(dealTags.contains(tag)){
							dealTags.remove(tag);
						}
					}
				}
			}
		}
		newResourceModel.setTags(dealTags);
		
		//如果是改成ONLINE,则处理publish_type
		if(inViewModel.getStatus().equals(LifecycleStatus.ONLINE.getCode()) && StringUtils.isNotEmpty(inViewModel.getPublishType())){
			ResClassificationModel newPtCategory = new ResClassificationModel();
			
			if(oldResourceModel!=null && CollectionUtils.isNotEmpty(oldResourceModel.getCategoryList())){
				for(ResClassificationModel rcm : oldResourceModel.getCategoryList()){
					if(StringUtils.isNotEmpty(rcm.getTaxoncode()) && rcm.getTaxoncode().startsWith("PT")){//有PT的情况,目前认为一个资源只有一个PT维度
						newPtCategory.setIdentifier(rcm.getIdentifier());
						newPtCategory.setTaxoncode(inViewModel.getPublishType());
						newPtCategory.setOperation("update");
						
						break;
					}else{//没有PT的情况
						newPtCategory.setTaxoncode(inViewModel.getPublishType());
						newPtCategory.setOperation("add");
					}
				}
			}else{//原资源没有categories的情况
				newPtCategory.setTaxoncode(inViewModel.getPublishType());
				newPtCategory.setOperation("add");
			}
			
			List<ResClassificationModel> categoryList = new ArrayList<ResClassificationModel>();
			categoryList.add(newPtCategory);
			newResourceModel.setCategoryList(categoryList);
		}
		
		//调用局部更新的service方法
		newResourceModel = ndResourceService.patch(inViewModel.getResType(), newResourceModel, DbName.QUESTION);
		
		//创建资源生命周期  -- 记录审核人
        ResContributeModel contributeModel = new ResContributeModel();
        contributeModel.setLifecycleStatus(inViewModel.getStatus());
        contributeModel.setMessage(
        		"VR人生资源审核:[" + inViewModel.getReviewPerson() + "] 修改了 [" 
        		+ inViewModel.getResType() + ":" + inViewModel.getIdentifier() + "]");
        contributeModel.setTitle("vrlife");
        contributeModel.setTargetType("User");
        contributeModel.setTargetId("vrlife");
        contributeModel.setTargetName(inViewModel.getReviewPerson());
        lifecycleService4Qti.addLifecycleStep(inViewModel.getResType(), inViewModel.getIdentifier(), contributeModel);
		
        //转成输出model
		StatusReviewViewModel4Out outViewModel = new StatusReviewViewModel4Out();
		outViewModel.setIdentifier(newResourceModel.getIdentifier());
		outViewModel.setStatus(inViewModel.getStatus());
		outViewModel.setTags(newResourceModel.getTags());
		
		return outViewModel;
	}
}