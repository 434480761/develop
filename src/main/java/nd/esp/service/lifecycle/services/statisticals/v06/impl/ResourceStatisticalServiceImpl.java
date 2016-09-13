package nd.esp.service.lifecycle.services.statisticals.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.daos.statisticals.ResourceStatisticalsDao;
import nd.esp.service.lifecycle.educommon.support.StatisticsPlatform;
import nd.esp.service.lifecycle.models.statisticals.v06.ResourceStatisticalModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.repository.sdk.ResourceStatisticalRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.services.coursewareobjects.v06.impls.CourseWareObjectServiceImplV06;
import nd.esp.service.lifecycle.services.statisticals.v06.ResourceStatisticalService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("StatisticalServiceImpl")
@Transactional(value="transactionManager")
@Primary
public class ResourceStatisticalServiceImpl implements ResourceStatisticalService {
    private final Logger LOG = LoggerFactory.getLogger(CourseWareObjectServiceImplV06.class);

    @Autowired
    private ResourceStatisticalRepository statisticalRepository;

    @Autowired
    @Qualifier(value = "ResourceStatisticalsDaoImpl")
    private ResourceStatisticalsDao resourceStatisticalsDao;
    
    /**
     * 增加资源评价统计指标数据
     * @param svms 评价数据
     * @param resType 资源类型
     * @param id 资源ID
     * */
    public List<ResourceStatisticalModel> addStatistical(List<ResourceStatisticalModel> sms, String resType, String id) {
    	checkResourceExist(resType, id);
    	
    	// 构造Entity数据并保存
        List<ResourceStatistical> statisticalsList = new ArrayList<ResourceStatistical>();

        Timestamp time = new Timestamp(System.currentTimeMillis());

        for (ResourceStatisticalModel sm : sms) {
            ResourceStatistical statistical = ObjectUtils.fromJson(ObjectUtils.toJson(sm), ResourceStatistical.class);

            ResourceStatistical dbStatistical = null;
            try {
                ResourceStatistical example = new ResourceStatistical();
                example.setKeyTitle(statistical.getKeyTitle());
                example.setResource(id);
                example.setResType(resType);

                dbStatistical = statisticalRepository.getByExample(example);
            } catch (EspStoreException e) {
                LOG.error("资源统计指标操作--检查资源失败");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
            }

            if (dbStatistical == null) {
                statistical.setIdentifier(UUID.randomUUID().toString());
            }
            else {
                statistical.setIdentifier(dbStatistical.getIdentifier());
            }

            statistical.setResType(resType);
            statistical.setUpdateTime(time);
            statistical.setResource(id);

            statisticalsList.add(statistical);
        }

        try {
            statisticalsList = statisticalRepository.batchAdd(statisticalsList);
        } catch (EspStoreException e) {
            LOG.error("资源统计指标操作--保存资源失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }
        
        LOG.info("----统计成功:" + resType + "---" + id);
        for (ResourceStatistical statistical : statisticalsList) {
        	LOG.info("统计详情" + statistical.getKeyTitle() + "---" + statistical.getKeyValue());
        }

        // 模型转出
        List<ResourceStatisticalModel> returnList = new ArrayList<ResourceStatisticalModel>();
        for (ResourceStatistical statistical : statisticalsList) {
            ResourceStatisticalModel sm = BeanMapperUtils.beanMapper(statistical, ResourceStatisticalModel.class);
            returnList.add(sm);
        }
        
        //统计
        countValue(id, resType);

        return returnList;
    }
    
    @Override
	public List<ResourceStatisticalModel> addStatisticalByCumulative(List<ResourceStatisticalModel> sms, 
			String resType, String id) {
    	
    	checkResourceExist(resType, id);
    	
    	// 构造Entity数据并保存
        List<ResourceStatistical> statisticalsList = new ArrayList<ResourceStatistical>();

        Timestamp time = new Timestamp(System.currentTimeMillis());
        
        for (ResourceStatisticalModel sm : sms) {
        	ResourceStatistical statistical = ObjectUtils.fromJson(ObjectUtils.toJson(sm), ResourceStatistical.class);
        	
        	ResourceStatistical temp = resourceStatisticalsDao.getResourceStatistical(resType, id, sm.getKeyTitle());
        	if(temp == null){
        		statistical.setIdentifier(UUID.randomUUID().toString());
        		if(sm.getKeyValue() < 0){
        			statistical.setKeyValue(0D);
        		}
        	}else{
        		statistical.setIdentifier(temp.getIdentifier());
        		
        		double sum = sm.getKeyValue() + temp.getKeyValue();
        		if(sum < 0){
        			statistical.setKeyValue(0D);
        		}else{
        			statistical.setKeyValue(sum);
        		}
        	}
        	
        	statistical.setResType(resType);
            statistical.setUpdateTime(time);
            statistical.setResource(id);

            statisticalsList.add(statistical);
        }
    	
        try {
            statisticalsList = statisticalRepository.batchAdd(statisticalsList);
        } catch (EspStoreException e) {
            LOG.error("资源统计指标操作--保存资源失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }
        
        LOG.info("----统计累加成功:" + resType + "---" + id);
        for (ResourceStatistical statistical : statisticalsList) {
        	LOG.info("统计详情" + statistical.getKeyTitle() + "---" + statistical.getKeyValue());
        }

        // 模型转出
        List<ResourceStatisticalModel> returnList = new ArrayList<ResourceStatisticalModel>();
        for (ResourceStatistical statistical : statisticalsList) {
            ResourceStatisticalModel sm = BeanMapperUtils.beanMapper(statistical, ResourceStatisticalModel.class);
            returnList.add(sm);
        }
        
        //统计
        countValue(id, resType);

        return returnList;
	}
    
    private void checkResourceExist(String resType, String id){
    	/**
         * 检查资源resType和UUID
         * */
        EspRepository<?> espRepository = ServicesManager.get(resType);

        try {
            EspEntity entity = espRepository.get(id);

            if (entity == null
                    || ((entity instanceof Education) && ((Education) entity).getEnable() != null && !((Education) entity)
                            .getEnable())) {
                LOG.warn("资源统计指标操作--根据relation的sourceType和source获取数据失败");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceStatisticalCheckReourceFail);
            }

        } catch (EspStoreException e1) {
            LOG.error("资源统计指标操作--添加统计指标失败", e1);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e1.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, List<ResourceStatisticalModel>> getList(List<String> key, List<String> rid) {

        Map<String, List<ResourceStatisticalModel>> smMap = new HashMap<String, List<ResourceStatisticalModel>>();

        if (CollectionUtils.isNotEmpty(key) && CollectionUtils.isNotEmpty(rid)) {

            // 去除重复的key和rid
            Set<String> ridSet = new HashSet<String>(rid);
            Set<String> keySet = new HashSet<String>(key);

            for (String uuid : ridSet) {
                List<ResourceStatisticalModel> smList = new ArrayList<ResourceStatisticalModel>();
                for (String title : keySet) {
                    ResourceStatistical example = new ResourceStatistical();
                    example.setResource(uuid);
                    example.setKeyTitle(title);

                    ResourceStatistical dbStatistical = null;
                    try {
                        dbStatistical = statisticalRepository.getByExample(example);
                    } catch (EspStoreException e) {
                        LOG.error("资源统计指标操作--检查资源失败");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
                    }

                    ResourceStatisticalModel sm = null;

                    if (dbStatistical != null) {
                        sm = BeanMapperUtils.beanMapper(dbStatistical, ResourceStatisticalModel.class);
                        smList.add(sm);
                    }
                }

                smMap.put(uuid, smList);
            }

        }

        return smMap;
    }

    private void countValue(String resourceId, String resType) {

        String keyTitle = "valuesum";
        Double valuesum = new Double(0);

        List<ResourceStatistical> list = resourceStatisticalsDao.getAllRsByReousrceId(resourceId);

        if (!CollectionUtils.isEmpty(list)) {
            for (ResourceStatistical rs : list) {
                if (rs != null && !keyTitle.equals(rs.getKeyTitle())) {
                    valuesum = valuesum + rs.getKeyValue();
                }
            }
        }

        ResourceStatistical example = new ResourceStatistical();
        example.setKeyTitle(keyTitle);
        example.setResource(resourceId);
        ResourceStatistical dbStatistical = null;

        try {
            dbStatistical = statisticalRepository.getByExample(example);
        } catch (EspStoreException e) {
            LOG.error("资源统计指标操作--检查资源失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }
        try {
            if (dbStatistical != null) {
                dbStatistical.setKeyValue(valuesum);

                statisticalRepository.update(dbStatistical);
            }
            else {
                dbStatistical = new ResourceStatistical();
                dbStatistical.setIdentifier(UUID.randomUUID().toString());
                dbStatistical.setResource(resourceId);
                dbStatistical.setKeyTitle(keyTitle);
                dbStatistical.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                dbStatistical.setDataFrom(StatisticsPlatform.TOTAL.getName());
                dbStatistical.setResType(resType);
                dbStatistical.setKeyValue(valuesum);

                statisticalRepository.add(dbStatistical);
            }
        } catch (EspStoreException e) {
            LOG.error("资源统计指标操作--保存资源失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }
    }

	@Override
	public void addDownloadStatistical(String bsyskey, String resType, String id) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		List<ResourceStatistical> rsList = resourceStatisticalsDao.getAllRsByReousrceId(id);
		boolean flag = false;
		//1、判断业务方是否为101ppt
		if(Constant.BSYSKEY_101PPT.equals(bsyskey) || Constant.BSYSKEY_101PPT_HISENSE.equals(bsyskey)){
			flag = true;
		}
		List<ResourceStatistical> datas = new ArrayList<ResourceStatistical>();
		boolean totalFlag = false;
		boolean pptFlag = false;
		if(CollectionUtils.isNotEmpty(rsList)){
			for (ResourceStatistical rs : rsList) {
				if(StatisticsPlatform.TOTAL.getName().equals(rs.getDataFrom())){
					totalFlag = true;
					rs.setKeyValue(rs.getKeyValue()+1);
					datas.add(rs);
				}else if(flag && StatisticsPlatform.NDPPT.getName().equals(rs.getDataFrom())){
					pptFlag = true;
					rs.setKeyValue(rs.getKeyValue()+1);
					datas.add(rs);
				}
			}
		}
		if(!totalFlag || CollectionUtils.isEmpty(rsList)){
			ResourceStatistical dbStatistical = new ResourceStatistical();
            dbStatistical.setIdentifier(UUID.randomUUID().toString());
            dbStatistical.setResource(id);
            dbStatistical.setKeyTitle("downloads");
            dbStatistical.setUpdateTime(ts);
            dbStatistical.setDataFrom(StatisticsPlatform.TOTAL.getName());
            dbStatistical.setResType(resType);
            dbStatistical.setKeyValue(1.0);
            datas.add(dbStatistical);
		}
		if(flag && (!pptFlag || CollectionUtils.isEmpty(rsList))){
			ResourceStatistical tmp = new ResourceStatistical();
			tmp.setIdentifier(UUID.randomUUID().toString());
			tmp.setResource(id);
			tmp.setKeyTitle("downloads");
			tmp.setUpdateTime(ts);
			tmp.setDataFrom(StatisticsPlatform.NDPPT.getName());
			tmp.setResType(resType);
			tmp.setKeyValue(1.0);
            datas.add(tmp);
		}
		
		if(CollectionUtils.isNotEmpty(datas)){
			try {
				statisticalRepository.batchAdd(datas);
			} catch (EspStoreException e) {
				 LOG.error("资源统计指标操作--批量保存资源失败");
		         throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void resourceTop(String resType, String uuid, boolean effect) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
        /**
         * 检查资源resType和UUID
         * */
        EspRepository<?> espRepository = ServicesManager.get(resType);
        try {
            EspEntity entity = espRepository.get(uuid);
            if (entity == null
                    || ((entity instanceof Education) && ((Education) entity).getEnable() != null && !((Education) entity)
                            .getEnable())) {
            	LOG.warn("置顶接口资源未找到！资源类型：{}，资源id：{}",resType,uuid);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                        LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                                + " resourceType:" + resType + " uuid:" + uuid);
            }
        } catch (EspStoreException e1) {
        	LOG.error("资源置顶接口异常！资源类型：{}，资源id：{}",resType,uuid);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e1.getMessage());
        }
        
    	//取出资源的置顶数值
        ResourceStatistical example = new ResourceStatistical();
        example.setKeyTitle("top");
        example.setResource(uuid);
        ResourceStatistical dbStatistical = null;
        try {
            dbStatistical = statisticalRepository.getByExample(example);
        } catch (EspStoreException e) {
            LOG.error("资源统计指标操作--检查资源失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }

        if(effect){
        	//置顶
        	//获取当前最大值
        	double max = resourceStatisticalsDao.getMaxTopValue(resType);
            if(dbStatistical != null){
            	if(dbStatistical.getKeyValue().doubleValue() < max){
            		dbStatistical.setKeyValue(max+0.01);
            		dbStatistical.setUpdateTime(ts);
            		try {
						statisticalRepository.add(dbStatistical);
					} catch (EspStoreException e) {
			            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
			                    e.getMessage());
					}
            	}
            }else{
            	ResourceStatistical rs = new ResourceStatistical();
            	rs.setIdentifier(UUID.randomUUID().toString());
            	rs.setDataFrom(StatisticsPlatform.TOTAL.getName());
            	rs.setKeyTitle("top");
            	rs.setKeyValue(max+0.01);
            	rs.setResource(uuid);
            	rs.setResType(resType);
            	rs.setUpdateTime(ts);
            	try {
					statisticalRepository.add(rs);
				} catch (EspStoreException e) {
		            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
		                    e.getMessage());
				}
            }
        }else{
        	//取消置顶
        	if(dbStatistical != null){
        		try {
					statisticalRepository.del(dbStatistical.getIdentifier());
				} catch (EspStoreException e) {
					 throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
			                    e.getMessage());
				}
        	}else{
        		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	                    "LC/RESOURCE_TOP_ERROR","资源未置顶过");
        	}
        }
	}
}
