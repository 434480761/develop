package nd.esp.service.lifecycle.services.coveragesharing.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.models.coveragesharing.v06.CoverageSharingModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.CoverageSharing;
import nd.esp.service.lifecycle.repository.sdk.CoverageSharingRepository;
import nd.esp.service.lifecycle.services.coveragesharing.v06.CoverageSharingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
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
import com.nd.gaea.rest.security.authens.UserInfo;
/**
 * 库分享 Service层
 * @author xiezy
 * @date 2016年8月24日
 */
@Service
public class CoverageSharingServiceImpl implements CoverageSharingService{
	private static final Logger LOG = LoggerFactory.getLogger(CoverageSharingServiceImpl.class);
	
	@Autowired
	private CoverageSharingRepository coverageSharingRepository;
	
	@Override
	public CoverageSharingModel createCoverageSharing(CoverageSharingModel csm, UserInfo userInfo) {
		
		CoverageSharing cs = BeanMapperUtils.beanMapper(csm, CoverageSharing.class);
		if(userInfo != null){
			cs.setCreator(userInfo.getUserId());
		}
		cs.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		CoverageSharing result = null;
		try {
			result = coverageSharingRepository.add(cs);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		
		if(result == null){
			
			LOG.error("创建库分享失败");
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateCoverageSharingFail);
		}
		
		return BeanMapperUtils.beanMapper(result, CoverageSharingModel.class);
	}
	
	@Override
	public boolean deleteCoverageSharing(String id) {
		
		try {
			CoverageSharing cs = coverageSharingRepository.get(id);
			if(cs == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	                    LifeCircleErrorMessageMapper.CoverageSharingNotFound);
			}
			
			coverageSharingRepository.del(id);
		} catch (EspStoreException e) {
			
			LOG.error("删除库分享失败");
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		
		return true;
	}
	
	@Override
	public ListViewModel<CoverageSharingModel> getCoverageSharingList(
			String source, String target, String limit) {
		
		ListViewModel<CoverageSharingModel> result = new ListViewModel<CoverageSharingModel>();
		
		//构造查询条件
		AdaptQueryRequest<CoverageSharing> adaptQueryRequest = new AdaptQueryRequest<CoverageSharing>();
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);
		adaptQueryRequest.setLimit(limitResult[1]);
		adaptQueryRequest.setOffset(limitResult[0]);
		if(StringUtils.hasText(source)){
			adaptQueryRequest.and("sourceCoverage", source);
		}
		if(StringUtils.hasText(target)){
			adaptQueryRequest.and("targetCoverage", target);
		}
		
		try {
			QueryResponse<CoverageSharing> queryResponse = coverageSharingRepository.searchByExample(adaptQueryRequest);
			long total = 0L;
	        List<CoverageSharingModel> items = new ArrayList<CoverageSharingModel>();
	        if (queryResponse != null && queryResponse.getHits() != null) {

	            items = ObjectUtils.fromJson(ObjectUtils.toJson(queryResponse.getHits().getDocs()),
	                                         new TypeToken<List<CoverageSharingModel>>() {});
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
	public boolean judgeSharingExistOrNot(String source, String target) {
		
		CoverageSharing cs = new CoverageSharing();
		cs.setSourceCoverage(source);
		cs.setTargetCoverage(target);
		
		CoverageSharing result = null;
		try {
			result = coverageSharingRepository.getByExample(cs);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		
		if(result != null){
			return true;
		}
		
		return false;
	}

	@Override
	public List<CoverageSharingModel> getCoverageSharingByTarget(String target) {
		
		List<CoverageSharingModel> list = new ArrayList<CoverageSharingModel>();
		
		CoverageSharing cs = new CoverageSharing();
		cs.setTargetCoverage(target);
		try {
			List<CoverageSharing> csList = coverageSharingRepository.getAllByExample(cs);
			if(CollectionUtils.isNotEmpty(csList)){
				for(CoverageSharing coverageSharing : csList){
					CoverageSharingModel csm = BeanMapperUtils.beanMapper(coverageSharing, CoverageSharingModel.class);
					list.add(csm);
				}
			}
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
		return list;
	}
}
