package nd.esp.service.lifecycle.services.thirdpartybuss.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.models.ApiModel;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModle;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.OffsetPageRequest;
import nd.esp.service.lifecycle.repository.model.ThirdPartyBsys;
import nd.esp.service.lifecycle.repository.sdk.ThirdPartyBsysRepository;
import nd.esp.service.lifecycle.services.staticdatas.StaticDataService;
import nd.esp.service.lifecycle.services.thirdpartybuss.v06.ThirdPartyBussService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.staticdata.UpdateStaticDataTask;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Service
public class ThirdPartyBussServiceImpl implements ThirdPartyBussService {
    private static final Logger LOG = LoggerFactory.getLogger(ThirdPartyBussServiceImpl.class);
    
    @Autowired
    private ThirdPartyBsysRepository bsysRepository;
    
    @Autowired 
    private RequestMappingHandlerMapping handlerMapping;
    
    @Autowired
    private StaticDataService staticDataService;

    @Override
    public ThirdPartyBsysModle registerService(ThirdPartyBsysModle bsysModle, boolean isAuto) {
      //生成SDK的入参对象,并进行model转换
        ThirdPartyBsys service = new ThirdPartyBsys();
        service = BeanMapperUtils.beanMapper(bsysModle, ThirdPartyBsys.class);
        service.setBsysivcconfig(ObjectUtils.toJson(bsysModle.getBsysivcconfig()));
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        service.setCreateTime(ts);
        service.setUpdateTime(ts);
        service.setIdentifier(UUID.randomUUID().toString());
        service.setBsyskey(service.getIdentifier());
        if(isAuto){
        	service.setTitle("E-Learning auto registry");
        }
        
        ThirdPartyBsys rtService = null;
        try {
            //调用SDK,添加
            rtService = bsysRepository.add(service);
        } catch (EspStoreException e) {
            LOG.error("注册第三方业务系统失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateBsysFail);
        }
        
        //如果返回null,则抛出异常
        if (null == rtService) {
            LOG.error("注册第三方业务系统失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateBsysFail);
        }
        
        staticDataService.updateLastTime(UpdateStaticDataTask.IVC_TASK_ID);
        staticDataService.updateIvcMapNow();
        staticDataService.updateIvcUserMapNow();
        
        //处理返回结果
        ThirdPartyBsysModle rtBsysModle = BeanMapperUtils.beanMapper(rtService, ThirdPartyBsysModle.class);
        rtBsysModle.setBsysivcconfig(ObjectUtils.fromJson(rtService.getBsysivcconfig(), Map.class));
        
        return rtBsysModle;
    }

    @Override
    public ListViewModel<ThirdPartyBsysModle> queryServiceInfo(String bsysname, String admin, String limit) {
        ThirdPartyBsys example = new ThirdPartyBsys();
        example.setBsysname(bsysname);
        example.setBsysadmin(admin);
        
        ListViewModel<ThirdPartyBsysModle> list = new ListViewModel<ThirdPartyBsysModle>();
        //分页参数
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        list.setLimit(limit);
        Pageable pageable = new OffsetPageRequest(result[0], result[1]);
        try {
            Page<ThirdPartyBsys> seviceResult = bsysRepository.getPageByExample(example, pageable);
            if(seviceResult != null) {
                list.setTotal(seviceResult.getTotalElements());
                List<ThirdPartyBsysModle> items = new ArrayList<ThirdPartyBsysModle>();
                for(ThirdPartyBsys service:seviceResult.getContent()) {
                    ThirdPartyBsysModle serviceModel = BeanMapperUtils.beanMapper(service, ThirdPartyBsysModle.class);
                    serviceModel.setBsysivcconfig(ObjectUtils.fromJson(service.getBsysivcconfig(), Map.class));
                    items.add(serviceModel);
                }
                list.setItems(items);
            }
        } catch (EspStoreException e) {
            LOG.error("获取第三方业务系统信息失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetBsysFail);
        }

        return list;
    }

    @Override
    public boolean deleteService(String uuid) {
        try {
            ThirdPartyBsys contribute = bsysRepository.get(uuid);
            if(contribute == null) {
                LOG.error("指定的第三方业务系统不存在");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.BsysNotFound);
            }
            bsysRepository.del(uuid);
        } catch (EspStoreException e) {
            LOG.error("删除第三方业务系统失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.DeleteBsysFail);
        }
        
        staticDataService.updateLastTime(UpdateStaticDataTask.IVC_TASK_ID);
        staticDataService.updateIvcMapNow();
        staticDataService.updateIvcUserMapNow();
        
        return true;
    }

    @Override
    public ThirdPartyBsysModle modifyService(String uuid, ThirdPartyBsysModle bsysModel) {
        ThirdPartyBsys originBsys = null;
        try {
            originBsys = bsysRepository.get(uuid);
        } catch (EspStoreException e1) {
            LOG.error("获取第三方业务系统信息失败", e1);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetBsysFail);
        }
        if(originBsys == null) {
            LOG.error("指定的第三方业务系统不存在");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.BsysNotFound);
        }
        
        //生成SDK的入参对象,并进行model转换
        ThirdPartyBsys busssys = new ThirdPartyBsys();
        busssys = BeanMapperUtils.beanMapper(bsysModel, ThirdPartyBsys.class);
        busssys.setBsysivcconfig(ObjectUtils.toJson(bsysModel.getBsysivcconfig()));
        busssys.setBsyskey(originBsys.getBsyskey());
        busssys.setCreateTime(originBsys.getCreateTime());
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        busssys.setUpdateTime(ts);
        
        ThirdPartyBsys rtBsys = null;
        try {
            //调用SDK,更新
            rtBsys = bsysRepository.update(busssys);
        } catch (EspStoreException e) {
            LOG.error("修改第三方业务系统失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateBsysFail);
        }
        
        //如果返回null,则抛出异常
        if (null == rtBsys) {
            LOG.error("修改第三方业务系统失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateBsysFail);
        }
        
        staticDataService.updateLastTime(UpdateStaticDataTask.IVC_TASK_ID);
        staticDataService.updateIvcMapNow();
        staticDataService.updateIvcUserMapNow();
        
        //处理返回结果
        ThirdPartyBsysModle serviceModel = BeanMapperUtils.beanMapper(rtBsys, ThirdPartyBsysModle.class);
        serviceModel.setBsysivcconfig(ObjectUtils.fromJson(rtBsys.getBsysivcconfig(), Map.class));
        
        return serviceModel;
    }

    @Override
    public ListViewModel<ApiModel> queryApiList() {
    	ListViewModel<ApiModel> apiList = new ListViewModel<ApiModel>();
    	List<ApiModel> list = new ArrayList<ApiModel>();
    	
        Map<RequestMappingInfo, HandlerMethod> map = this.handlerMapping.getHandlerMethods();
        Iterator<Map.Entry<RequestMappingInfo, HandlerMethod>> iterator = map.entrySet().iterator();
        long count = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            RequestMappingInfo info = (RequestMappingInfo) entry.getKey();
            String api = info.getPatternsCondition().toString().replace("[", "").replace("]", "");
            String params = info.getParamsCondition().toString().replace("[", "").replace("]", "").replace(" ", "");
            if(StringUtils.hasText(params)) {
            	api += "?"+params;
            }
            String method = info.getMethodsCondition().toString();
            if(api.contains("/"+Constant.LIFE_CYCLE_API_VERSION+"/")) {
            	ApiModel apiModel = new ApiModel();
            	apiModel.setUrl(api);
            	apiModel.setMothod(method);
            	list.add(apiModel);
            	++count;
            }
        }
        apiList.setItems(list);
        apiList.setTotal(count);
        apiList.setLimit("(0,"+count+")");
        
        return apiList;
    }
}
