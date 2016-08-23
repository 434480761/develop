package nd.esp.service.lifecycle.services.copyright.v06.impl;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.models.copyright.v06.CopyrightOwnerModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.CopyrightOwner;
import nd.esp.service.lifecycle.repository.sdk.CopyrightOwnerRepository;
import nd.esp.service.lifecycle.services.copyright.v06.CopyrightOwnerService;
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
 * 资源版权方 Service层
 * 
 * @author xiezy
 * @date 2016年8月16日
 */
@Service
public class CopyrightOwnerServiceImpl implements CopyrightOwnerService {
	private static final Logger LOG = LoggerFactory
			.getLogger(CopyrightOwnerServiceImpl.class);

	@Autowired
	private CopyrightOwnerRepository copyrightOwnerRepository;

	@Override
	public CopyrightOwnerModel createCopyrightOwner(CopyrightOwnerModel com) {
		// 逻辑校验,title不允许重复
		CopyrightOwner cm4Title = new CopyrightOwner();
		cm4Title.setTitle(com.getTitle());
		try {
			cm4Title = copyrightOwnerRepository.getByExample(cm4Title);
		} catch (EspStoreException e) {
			LOG.error("校验title是否重复时查询出错");
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getMessage());
		}

		if (cm4Title != null) {
			throw new LifeCircleException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CheckDuplicateCopyrightOwnerTitleFail);
		}

		CopyrightOwner co = BeanMapperUtils.beanMapper(com, CopyrightOwner.class);
		try {
			co = copyrightOwnerRepository.add(co);
		} catch (EspStoreException e) {
			LOG.error("创建版权方出错");
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getMessage());
		}

		if (co == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CreateCopyrightOwnerFail);
		}

		return BeanMapperUtils.beanMapper(co, CopyrightOwnerModel.class);
	}

	@Override
	public CopyrightOwnerModel updateCopyrightOwner(CopyrightOwnerModel com) {
		CopyrightOwner co4Detail = null;
		try {
			//校验
			co4Detail = copyrightOwnerRepository.get(com.getIdentifier());
			
			if(co4Detail == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		              	  LifeCircleErrorMessageMapper.CopyrightOwnerNotFound);
			}
			
			//判断title是否重复,如果是修改成原本的title是允许的
			CopyrightOwner co4Title = new CopyrightOwner();
			co4Title.setTitle(com.getTitle());
			co4Title = copyrightOwnerRepository.getByExample(co4Title);
			
			if(co4Title != null && !co4Title.getIdentifier().equals(co4Detail.getIdentifier())){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                  	  LifeCircleErrorMessageMapper.CheckDuplicateCopyrightOwnerTitleFail);
			}
			
			co4Detail.setTitle(com.getTitle());
			co4Detail.setDescription(com.getDescription());
			
			co4Detail = copyrightOwnerRepository.update(co4Detail);
			
			if(co4Detail == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		              	  LifeCircleErrorMessageMapper.UpdateCopyrightOwnerFail);
			}
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		return BeanMapperUtils.beanMapper(co4Detail, CopyrightOwnerModel.class);
	}

	@Override
	public boolean deleteCopyrightOwner(String id) {
		try {
			CopyrightOwner co4Detail = copyrightOwnerRepository.get(id);
			
			if(co4Detail == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		              	  LifeCircleErrorMessageMapper.CopyrightOwnerNotFound);
			}
			
			copyrightOwnerRepository.del(id);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		
		return true;
	}

	@Override
	public ListViewModel<CopyrightOwnerModel> getCopyrightOwnerList(String words, String limit) {
		ListViewModel<CopyrightOwnerModel> result = new ListViewModel<CopyrightOwnerModel>();
		
		AdaptQueryRequest<CopyrightOwner> adaptQueryRequest = new AdaptQueryRequest<CopyrightOwner>();
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);
		adaptQueryRequest.setLimit(limitResult[1]);
		adaptQueryRequest.setOffset(limitResult[0]);
		if(StringUtils.hasText(words)){
			adaptQueryRequest.and("title", words);
		}
		
		try {
			QueryResponse<CopyrightOwner> queryResponse = copyrightOwnerRepository.searchByExampleSupportLike(adaptQueryRequest);
			long total = 0L;
	        List<CopyrightOwnerModel> items = new ArrayList<CopyrightOwnerModel>();
	        if (queryResponse != null && queryResponse.getHits() != null) {

	            items = ObjectUtils.fromJson(ObjectUtils.toJson(queryResponse.getHits().getDocs()),
	                                         new TypeToken<List<CopyrightOwnerModel>>() {});
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
