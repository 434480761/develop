package nd.esp.service.lifecycle.controllers.knowledgebase.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;
import nd.esp.service.lifecycle.services.knowledgebase.v06.KnowledgeBaseService;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.knowledgebase.v06.KnowledgeBaseViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库V0.6API
 * 
 * @author xuzy
 */
@RestController
@RequestMapping("/v0.6/knowledgeBase")
public class KnowledgeBaseControllerV06 {
	
	@Autowired
	private KnowledgeBaseService kbs;
	/**
	 * 创建知识库
	 * @param kbvm
	 * @return
	 */
	@RequestMapping(method = { RequestMethod.POST }, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public KnowledgeBaseViewModel createKnowledgeBase(@RequestBody KnowledgeBaseViewModel kbvm){
		kbvm.setIdentifier(UUID.randomUUID().toString());
		KnowledgeBaseModel kbm = BeanMapperUtils.beanMapper(kbvm, KnowledgeBaseModel.class);
		kbm = kbs.createKnowledgeBase(kbm);
		return BeanMapperUtils.beanMapper(kbvm, KnowledgeBaseViewModel.class);
	}
	
	/**
	 * 批量创建知识库
	 * @param kbvm
	 * @return
	 */
	@RequestMapping(value="/batchAdd", method = { RequestMethod.POST }, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<KnowledgeBaseViewModel> batchCreateKnowledgeBase(@RequestBody KnowledgeBaseViewModel kbvm){
		KnowledgeBaseModel kbm = BeanMapperUtils.beanMapper(kbvm, KnowledgeBaseModel.class);
		List<KnowledgeBaseModel> list = kbs.batchCreateKnowledgeBase(kbm);
		List<KnowledgeBaseViewModel> returnList = new ArrayList<KnowledgeBaseViewModel>();
		if(CollectionUtils.isNotEmpty(list)){
			for (KnowledgeBaseModel k : list) {
				returnList.add(BeanMapperUtils.beanMapper(k, KnowledgeBaseViewModel.class));
			}
		}
		return returnList;
	}
	
	/**
	 * 根据知识类型，知识子结构，知识点名称查询知识库列表
	 */
	@RequestMapping(value = "/query",method = { RequestMethod.GET }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<KnowledgeBaseViewModel> queryKnowledgeBaseByKpid(@RequestParam(required=true) String kcid,@RequestParam(required=false) String title){
		List<KnowledgeBaseViewModel> returnList = new ArrayList<KnowledgeBaseViewModel>();
		List<KnowledgeBaseModel> kbvmList = null;
		if(StringUtils.isNotEmpty(title)){
			kbvmList = kbs.queryKnowledgeBaseListByCond(kcid, kcid, title);
		}else{
			kbvmList = kbs.queryKnowledgeBaseListByKpid(kcid);
		}
		
		if(CollectionUtils.isNotEmpty(kbvmList)){
			for (KnowledgeBaseModel knowledgeBaseModel : kbvmList) {
				KnowledgeBaseViewModel kbvm = new KnowledgeBaseViewModel();
				kbvm = BeanMapperUtils.beanMapper(knowledgeBaseModel, KnowledgeBaseViewModel.class);
				returnList.add(kbvm);
			}
		}
		return returnList;
	}
	
	/**
	 * 根据知识库id、教学目标套件查询教学目标
	 */
	@RequestMapping(value = "/queryInstructionObjective",method = { RequestMethod.GET }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<Map<String,Object>> queryInstructionalObjectiveByCond(@RequestParam(required=false) String kbid,@RequestParam(required=false) String ocid){
		return kbs.queryInstructionalObjectiveByCond(kbid, ocid);
	}
	
}
