package nd.esp.service.lifecycle.services.task.v06.impls;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;

import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.models.TechnologyRequirementModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.services.task.v06.PackCallbackService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.support.busi.ResourceTypesUtil;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.UrlParamParseUtil;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

@Service
public class PackCallbackServiceImpl implements PackCallbackService {
    private static final Logger LOG = LoggerFactory.getLogger(PackCallbackServiceImpl.class);
    
    @Autowired
    private TechInfoRepository techInfoRepository;
    
    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    private CommonServiceHelper commonServiceHelper;
    
//    @Autowired
//    private TaskService taskService;
    
    @Override
    public void packCallback(Map<String, String> callbackParams, TaskStatusInfo taskInfo) throws IOException{ 
        String id = callbackParams.get("identifier");
        String target = callbackParams.get("target");
        String status = callbackParams.get("status");
        String pack_info = callbackParams.get("pack_info");
        String err_msg = callbackParams.get("err_msg");
        Boolean webp_first = Boolean.valueOf(callbackParams.get("webp_first"));
        String res_type = callbackParams.get("res_type");
        LOG.info("打包回调，资源"+res_type+":"+id+", target="+target);
        commonServiceHelper.assertDownloadable(res_type);
        

        // 根据uuid获取资源详细信息
        ResourceModel resource = null;
        try {
            resource = ndResourceService.getDetail(res_type,id,IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI));
        } catch (LifeCircleException e) { //资源不存在
            LOG.error("打包回调，资源"+res_type+":"+id+"不存在");
            taskInfo.setStatus("resourse_deleted");
            return;
        }
        if (resource == null) {
            LOG.error("打包回调，资源"+res_type+":"+id+"不存在");
            taskInfo.setStatus("resourse_deleted");
            return;
        }
        
        
        Map<String, Map<String, String>> responses = null;
        Map<String,String> storeinfo_temp = new HashMap<String, String>();
        
        if("1".equals(status)) {
            responses = ObjectUtils.fromJson(pack_info, new TypeToken<Map<String, Map<String, String>>>(){});
            if(null != responses) {
                List<ResTechInfoModel> techInfos = resource.getTechInfoList();
                for(String infoKey:responses.keySet()) {
                    storeinfo_temp = responses.get(infoKey);
                    String techInfoTitle = PackageUtil.getStoreInfoKey(target, false);
                    if(webp_first) {
                        techInfoTitle += "_webp";
                    }
                    boolean isTechInfoExist = false;
                    ResTechInfoModel newTechInfo = null;
                    if(null != techInfos) {
                        for(ResTechInfoModel techInfo:techInfos) {
                            if(techInfo.getTitle().equals(techInfoTitle)) {
                                newTechInfo = techInfo;
                                isTechInfoExist = true;
                            }
                        }
                    }
                    if(!isTechInfoExist) {
                        newTechInfo = new ResTechInfoModel();
                        newTechInfo.setTitle(techInfoTitle);
                        newTechInfo.setIdentifier(UUID.randomUUID().toString());
                        newTechInfo.setResource(resource);
                        newTechInfo.setRequirements(new ArrayList<TechnologyRequirementModel>());
                    }
                    newTechInfo.setLocation("${ref-path}"+storeinfo_temp.get("location"));
                    BigDecimal bigDecimal=new BigDecimal(String.valueOf(storeinfo_temp.get("size")));
                    long size=bigDecimal.longValue();
                    newTechInfo.setSize(size);
                    newTechInfo.setFormat(storeinfo_temp.get("format"));
                    newTechInfo.setMd5(storeinfo_temp.get("md5"));
                    try {
                        TechInfo ti = BeanMapperUtils.beanMapper(newTechInfo, TechInfo.class);
                        ti.setResource(id);
                        ti.setResType(res_type);
                        ti.setRequirements(ObjectUtils.toJson(newTechInfo.getRequirements()));
                        techInfoRepository.add(ti);
                    } catch (Exception e1) {
                        LOG.error("更新tech_info数据失败:"+e1.getMessage());
                    }
                }
                
                taskInfo.setStatus(PackageUtil.PackStatus.READY.getStatus());
                taskInfo.setStoreInfo(ObjectUtils.toJson(storeinfo_temp));
            }
        }else {
            err_msg = URLDecoder.decode(err_msg, "utf-8");
            LOG.error("打包失败:"+err_msg);
            
            taskInfo.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
            taskInfo.setErrMsg(err_msg);
        }
    }
     

}
