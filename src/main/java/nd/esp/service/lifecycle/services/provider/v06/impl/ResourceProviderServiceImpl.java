package nd.esp.service.lifecycle.services.provider.v06.impl;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.models.provider.v06.ResourceProviderModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.ResourceProvider;
import nd.esp.service.lifecycle.repository.sdk.ResourceProviderRepository;
import nd.esp.service.lifecycle.services.provider.v06.ResourceProviderService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;

/**
 * 资源提供商 Service层
 * @author xiezy
 * @date 2016年8月15日
 */
@Service
public class ResourceProviderServiceImpl implements ResourceProviderService{
	private static final Logger LOG = LoggerFactory.getLogger(ResourceProviderServiceImpl.class);
	
	@Autowired
	private ResourceProviderRepository resourceProviderRepository;

	@Override
	public ResourceProviderModel createResourceProvider(ResourceProviderModel rpm) {
		//逻辑校验,title不允许重复
		ResourceProvider rp4Title = new ResourceProvider();
		rp4Title.setTitle(rpm.getTitle());
		try {
			rp4Title = resourceProviderRepository.getByExample(rp4Title);
		} catch (EspStoreException e) {
			LOG.error("校验title是否重复时查询出错");
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                     LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		if(rp4Title != null){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    	  LifeCircleErrorMessageMapper.CheckDuplicateProviderTitleFail);
		}
		
		ResourceProvider rp = BeanMapperUtils.beanMapper(rpm, ResourceProvider.class);
		try {
			rp = resourceProviderRepository.add(rp);
		} catch (EspStoreException e) {
			LOG.error("创建提供商出错");
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                     LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		if(rp == null){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
              	  LifeCircleErrorMessageMapper.CreateProviderFail);
		}
		
		return BeanMapperUtils.beanMapper(rp, ResourceProviderModel.class);
	}

	@Override
	public ResourceProviderModel updateResourceProvider(ResourceProviderModel rpm) {
		ResourceProvider rp4Detail = null;
		try {
			//校验
			rp4Detail = resourceProviderRepository.get(rpm.getIdentifier());
			
			if(rp4Detail == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		              	  LifeCircleErrorMessageMapper.ResourceProviderNotFound);
			}
			
			//判断title是否重复,如果是修改成原本的title是允许的
			ResourceProvider rp4Title = new ResourceProvider();
			rp4Title.setTitle(rpm.getTitle());
			rp4Title = resourceProviderRepository.getByExample(rp4Title);
			
			if(rp4Title != null && !rp4Title.getIdentifier().equals(rp4Detail.getIdentifier())){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                  	  LifeCircleErrorMessageMapper.CheckDuplicateProviderTitleFail);
			}
			
			rp4Detail.setTitle(rpm.getTitle());
			rp4Detail.setDescription(rpm.getDescription());
			
			rp4Detail = resourceProviderRepository.update(rp4Detail);
			
			if(rp4Detail == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		              	  LifeCircleErrorMessageMapper.UpdateProviderFail);
			}
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		return BeanMapperUtils.beanMapper(rp4Detail, ResourceProviderModel.class);
	}

	@Override
	public boolean deleteResourceProvider(String id) {
		try {
			ResourceProvider rp4Detail = resourceProviderRepository.get(id);
			
			if(rp4Detail == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		              	  LifeCircleErrorMessageMapper.ResourceProviderNotFound);
			}
			
			resourceProviderRepository.del(id);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		return true;
	}

	@Override
	public ListViewModel<ResourceProviderModel> getResourceProviderList(String words, String limit) {
		ListViewModel<ResourceProviderModel> result = new ListViewModel<ResourceProviderModel>();
		
		AdaptQueryRequest<ResourceProvider> adaptQueryRequest = new AdaptQueryRequest<ResourceProvider>();
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);
		adaptQueryRequest.setLimit(limitResult[1]);
		adaptQueryRequest.setOffset(limitResult[0]);
		if(StringUtils.hasText(words)){
			adaptQueryRequest.and("title", words);
		}
		
		try {
			QueryResponse<ResourceProvider> queryResponse = resourceProviderRepository.searchByExampleSupportLike(adaptQueryRequest);
			long total = 0L;
	        List<ResourceProviderModel> items = new ArrayList<ResourceProviderModel>();
	        if (queryResponse != null && queryResponse.getHits() != null) {

	            items = ObjectUtils.fromJson(ObjectUtils.toJson(queryResponse.getHits().getDocs()),
	                                         new TypeToken<List<ResourceProviderModel>>() {});
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
}
