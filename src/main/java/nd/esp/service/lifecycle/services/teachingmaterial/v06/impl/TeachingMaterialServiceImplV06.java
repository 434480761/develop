package nd.esp.service.lifecycle.services.teachingmaterial.v06.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.daos.teachingmaterial.v06.TeachingMaterialDao;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.TeachingMaterialServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 业务实现类
 * @author xuzy
 */
@Service(value="teachingMaterialServiceV06")
@Transactional
public class TeachingMaterialServiceImplV06 implements
		TeachingMaterialServiceV06 {
	private static final int CREATE_TYPE = 0;//新增教材操作
	private static final int UPDATE_TYPE = 1;//修改教材操作
	@Autowired
	private NDResourceService ndResourceService;
	@Autowired
	private TeachingMaterialDao teachingMaterialDao;
	@Autowired
	private TeachingMaterialRepository teachingMaterialRepository;
	
	@Override
	public TeachingMaterialModel createTeachingMaterial(String resType,
			TeachingMaterialModel tmm) {
		//1、校验是否已存在相同的教材
	    checkTeachingMaterial(resType,tmm,CREATE_TYPE);
		
		
		//2、调用通用创建接口
		tmm.setTechInfoList(null);
		return (TeachingMaterialModel)ndResourceService.create(resType, tmm);
	}

	@Override
	public TeachingMaterialModel updateTeachingMaterial(String resType,
			TeachingMaterialModel tmm) {
		//1、校验资源是否存在
	    checkTeachingMaterial(resType,tmm,UPDATE_TYPE);
		
		
		//2、调用通用创建接口
		tmm.setTechInfoList(null);
		return (TeachingMaterialModel)ndResourceService.update(resType, tmm);
	}

	/**
	 * 判断是否需要校验相同的教材
	 * 
	 * @author:xuzy
	 * @date:2016年1月25日
	 * @param resType
	 * @param tmm
	 * @return
	 */
	private boolean isNeedValidSameTm(String resType,TeachingMaterialModel tmm){
		if(ResourceNdCode.teachingmaterials.toString().equals(resType) && tmm.getLifeCycle() != null && tmm.getLifeCycle().getStatus().equals("ONLINE")){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 初始化
	 * 
	 * @author:xuzy
	 * @date:2015年7月30日
	 * @param tmm
	 * @param type
	 * @return
	 */
	private TeachingMaterial checkTeachingMaterial(String resType,TeachingMaterialModel tmm,int type){
		//判断教材是否重复
		Set<String> paths = new HashSet<String>();
		TeachingMaterial oldData = null;
		List<ResClassificationModel> categoryList = tmm.getCategoryList();
		if(CollectionUtils.isNotEmpty(categoryList)){
			for (ResClassificationModel resClassificationModel : categoryList) {
				if(StringUtils.isNotEmpty(resClassificationModel.getTaxonpath())){
					paths.add(resClassificationModel.getTaxonpath());
				}
			}
		}
		if(CollectionUtils.isEmpty(paths)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckTaxonpathFail);
		}else{
			if(isNeedValidSameTm(resType,tmm)){
				for (String p : paths) {
					List<Map<String,Object>> tmList = teachingMaterialDao.queryListByCategories(p, tmm.getIdentifier());
					if(CollectionUtils.isNotEmpty(tmList)){
						for (Map<String,Object> m : tmList) {
							if(m != null && ResourceNdCode.teachingmaterials.toString().equals((String)m.get("primaryCategory"))){
								Boolean enable = (Boolean)m.get("enable");
								if(enable != null && !enable){
									throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.TeachingMaterialDisable);
								}
								throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.SameTeachingMaterialFail);
							}
						}
					}
				}
			}
		}

//		if(type == UPDATE_TYPE){
//			//判断教材是否存在
//			try {
//				oldData = teachingMaterialRepository.get(tmm.getIdentifier());
//			} catch (EspStoreException e) {
//				LOG.error("教材V0.6操作中查询详细失败",e);
//				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
//			}
//			if(oldData == null){
//				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChangeObjectNotExist);
//			}
//			//教材是否被删除过
//			if(!oldData.getEnable()){
//				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChangeObjectNotExist);
//			}
//		}
		return oldData;
	}

	@Override
	public TeachingMaterial getById(String id) throws EspStoreException {
		return teachingMaterialRepository.get(id);
	}

}
