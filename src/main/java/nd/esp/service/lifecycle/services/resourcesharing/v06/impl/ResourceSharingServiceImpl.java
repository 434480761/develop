package nd.esp.service.lifecycle.services.resourcesharing.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nd.esp.service.lifecycle.models.resourcesharing.v06.ResourceSharingModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.ResourceSharing;
import nd.esp.service.lifecycle.repository.sdk.ResourceSharingRepository;
import nd.esp.service.lifecycle.services.resourcesharing.v06.ResourceSharingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.nd.gaea.rest.security.authens.UserInfo;

/**
 * 资源分享 Service层
 * @author xiezy
 * @date 2016年8月29日
 */
@Service
public class ResourceSharingServiceImpl implements ResourceSharingService {
	private static final Logger LOG = LoggerFactory.getLogger(ResourceSharingServiceImpl.class);
	
	@Autowired
	private ResourceSharingRepository resourceSharingRepository;
	
	@Override
	public ResourceSharingModel createResourceSharing(ResourceSharingModel rsm, UserInfo userInfo) {
		
		ResourceSharing resourceSharing = BeanMapperUtils.beanMapper(rsm, ResourceSharing.class);
		if(userInfo != null){
			resourceSharing.setSharerId(userInfo.getUserId());
			resourceSharing.setSharerName(userInfo.getUserName());
		}
		
		//生成密码
		String password = randomPassword();
		//MD5加密
		resourceSharing.setProtectPasswd(CommonHelper.encryptToMD5(password));
		
		resourceSharing.setSharingTime(new Timestamp(System.currentTimeMillis()));
		
		ResourceSharing result = null;
		try {
			result = resourceSharingRepository.add(resourceSharing);
		} catch (EspStoreException e) {
			
			LOG.error("创建资源分享失败",e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		
		if(result == null){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateResourceSharingFail);
		}
		
		ResourceSharingModel model = BeanMapperUtils.beanMapper(result, ResourceSharingModel.class);
		model.setProtectPasswd(password);//明文返回
		return model;
	}
	
	/**
	 * 随机生成4位密码
	 * @author xiezy
	 * @date 2016年8月29日
	 * @return
	 */
	private String randomPassword(){
		String val = "";

		Random random = new Random();
		for (int i = 0; i < 4; i++) {
			String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字

			if ("char".equalsIgnoreCase(charOrNum)) // 字符串
			{
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
				val += (char) (choice + random.nextInt(26));
			} else if ("num".equalsIgnoreCase(charOrNum)) // 数字
			{
				val += String.valueOf(random.nextInt(10));
			}
		}
		
		return val;
	}

	@Override
	public boolean deleteAllResourceSharingBySomeone(
			String resType, String resourceId, UserInfo userInfo) {
		
		try {
			ResourceSharing example = new ResourceSharing();
			example.setResourceType(resType);
			example.setResource(resourceId);
			if(userInfo != null){
				example.setSharerId(userInfo.getUserId());
			}
			
			List<ResourceSharing> list = resourceSharingRepository.getAllByExample(example);
			if(CollectionUtils.isNotEmpty(list)){
				List<String> deleteIds = new ArrayList<String>();
				for(ResourceSharing rs : list){
					deleteIds.add(rs.getIdentifier());
				}
				
				if(CollectionUtils.isNotEmpty(deleteIds)){
					resourceSharingRepository.batchDel(deleteIds);
				}
			}
		} catch (EspStoreException e) {
			
			LOG.error("删除资源分享失败",e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		
		return true;
	}

	@Override
	public boolean deleteResourceSharingBySharingId(String resType,
			String resourceId, String sharingId, UserInfo userInfo) {
		
		try {
			ResourceSharing example = new ResourceSharing();
			example.setIdentifier(sharingId);
			example.setResourceType(resType);
			example.setResource(resourceId);
			if(userInfo != null){
				example.setSharerId(userInfo.getUserId());
			}
			
			ResourceSharing temp = resourceSharingRepository.getByExample(example);
			if(temp != null){
				resourceSharingRepository.del(temp.getIdentifier());
			}else{
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	                    LifeCircleErrorMessageMapper.DeleteResourceSharingFail);
			}
		} catch (EspStoreException e) {
			
			LOG.error("删除资源分享失败",e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		
		return true;
	}

	@Override
	public ListViewModel<ResourceSharingModel> getResourceSharingList(
			String resType, String resourceId, String limit, UserInfo userInfo) {
		
		ListViewModel<ResourceSharingModel> result = new ListViewModel<ResourceSharingModel>();
		
		// 构造查询条件
		AdaptQueryRequest<ResourceSharing> adaptQueryRequest = new AdaptQueryRequest<ResourceSharing>();
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);
		adaptQueryRequest.setLimit(limitResult[1]);
		adaptQueryRequest.setOffset(limitResult[0]);
		adaptQueryRequest.and("resourceType", resType);
		adaptQueryRequest.and("resource", resourceId);
		if (userInfo != null) {
			adaptQueryRequest.and("sharerId", userInfo.getUserId());
		}
		
		try {
			QueryResponse<ResourceSharing> queryResponse = resourceSharingRepository.searchByExample(adaptQueryRequest);
			long total = 0L;
	        List<ResourceSharingModel> items = new ArrayList<ResourceSharingModel>();
	        if (queryResponse != null && queryResponse.getHits() != null) {

	            items = ObjectUtils.fromJson(ObjectUtils.toJson(queryResponse.getHits().getDocs()),
	                                         new TypeToken<List<ResourceSharingModel>>() {});
	            total = queryResponse.getHits().getTotal();
	        }
	        result.setTotal(total);
	        result.setItems(items);
	        result.setLimit(limit);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		return result;
	}

	@Override
	public ResourceSharingModel verifyPasswd(String sharingId, String passwd) {
		
		ResourceSharing example = new ResourceSharing();
		example.setIdentifier(sharingId);
		example.setProtectPasswd(CommonHelper.encryptToMD5(passwd));
		
		ResourceSharing result = null;
		try {
			result = resourceSharingRepository.getByExample(example);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		if(result == null){
			return null;
		}
		
		return BeanMapperUtils.beanMapper(result, ResourceSharingModel.class);
	}
}
