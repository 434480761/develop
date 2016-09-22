package nd.esp.service.lifecycle.services.task.v06.impls;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.MDC;
import org.hibernate.mapping.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;
import nd.esp.service.lifecycle.daos.ResLifecycle.v06.ResLifecycleDao;
import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.models.TechnologyRequirementModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.TransCodeCallBackParam;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.task.v06.TranscodeCallbackService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.enums.RequirementType;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

@Service
public class TranscodeCallbackServiceImpl implements TranscodeCallbackService {
    private static final Logger LOG = LoggerFactory.getLogger(TranscodeCallbackServiceImpl.class);
    
    private static final String TRANSCODE_CUT_PREFIX="transcodeCut_";
    private static final String TRANSCODE_FRAME1_PREFIX="frame1";
    private static final String TRANSCODE_COVER_KEY="cover";
    
    private static final String TECH_INFO_SOURCE_KEY="source";
    private static final String TECH_INFO_HREF_KEY="href";
    private static final String [] TECH_INFO_HREF_KEYS_ARR={"href","href-360p","href-480p","href-720p","href-360p-ogv","href-480p-ogv","href-720p-ogv","href-ogv"};
    private static final List<String> TECH_INFO_HREF_KEYS = Arrays.asList(TECH_INFO_HREF_KEYS_ARR);
    private static final String VIDEO_FORMAT_TARGET="mp4";
    private static final String VIDEO_THEORA_FORMAT="ogv";
    private static final String AUDIO_FORMAT_TARGET="mp3";
    private static final String AUDIO_THEORA_FORMAT="ogg";
    
    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    @Qualifier("lifecycleServiceV06")
    private LifecycleServiceV06 lifecycleService;
    
    @Autowired
    private TechInfoRepository techInfoRepository;
    
    @Autowired
    private ResLifecycleDao resLifecycleDao;

    @Override
    public void transcodeCallback(TransCodeCallBackParam argument, TaskStatusInfo taskInfo) throws IOException {
        String resType = taskInfo.getResType();
        String id = taskInfo.getUuid();
        
        ResourceModel resource = null;
        try {
            resource = ndResourceService.getDetail(resType, id, 
                    IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI+","+IncludesConstant.INCLUDE_LC));
        } catch (LifeCircleException e) { // 资源不存在时会抛出异常

            LOG.error("转码完成的资源已删除", e);
            
        }
        if (resource == null) {
            
            LOG.error("转码完成的资源已删除");

            taskInfo.setStatus("resourse_deleted");
            return ;
        }
        try {
            int status = UpdateResouceStatus(argument, taskInfo, resType, id, resource);

            if (1 == status) {
                if(TransCodeUtil.SUBTYPE_VIDEO.equals(argument.getTranscodeType())
                        || TransCodeUtil.SUBTYPE_AUDIO.equals(argument.getTranscodeType())) {
                    updateVideoTechInfos(resource, argument, resType);
                    updateVideoPreview(resource, argument, resType);
                } else {
                    updateNormalTechInfos(resource, argument, resType);
                    try {
                        updateNormalPreview(resource, argument, resType);
                    } catch (Exception e) {
                        LOG.info("updateNormalPreview：",e);
                        LOG.error("updateNormalPreview：",e);
                    }
                    
                }
                
                taskInfo.setStatus(PackageUtil.PackStatus.READY.getStatus());
                taskInfo.setErrMsg(argument.getErrMsg());
            } else {
                taskInfo.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
                taskInfo.setErrMsg(argument.getErrMsg());
            }
        } catch (Exception e) {
            LOG.error("转码任务回调失败", e);
        }
    }

    @Override
    public void imageTranscodeCallback(TransCodeCallBackParam argument, TaskStatusInfo taskInfo) throws IOException {
        String resType = taskInfo.getResType();
        String id = taskInfo.getUuid();

        ResourceModel resource = null;
        try {
            resource = ndResourceService.getDetail(resType, id,
                    IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI+","+IncludesConstant.INCLUDE_LC));
        } catch (LifeCircleException e) { // 资源不存在时会抛出异常

            LOG.error("转码完成的资源已删除", e);

        }
        if (resource == null) {

            LOG.error("转码完成的资源已删除");

            taskInfo.setStatus("resourse_deleted");
            return ;
        }
        try {
            int status = UpdateResouceStatus(argument, taskInfo, resType, id, resource);

            if (1 == status) {
                updateImageTechInfos(resource, argument, resType);

                taskInfo.setStatus(PackageUtil.PackStatus.READY.getStatus());
                taskInfo.setErrMsg(argument.getErrMsg());
            } else {
                taskInfo.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
                taskInfo.setErrMsg(argument.getErrMsg());
            }

        } catch (Exception e) {
            LOG.error("转码任务回调失败", e);
        }
    }

    @Override
    public void documentTranscodeCallback(TransCodeCallBackParam argument, TaskStatusInfo taskInfo) throws IOException {
        String resType = taskInfo.getResType();
        String id = taskInfo.getUuid();

        ResourceModel resource = null;
        try {
            resource = ndResourceService.getDetail(resType, id,
                    IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI+","+IncludesConstant.INCLUDE_LC));
        } catch (LifeCircleException e) { // 资源不存在时会抛出异常

            LOG.error("转码完成的资源已删除", e);

        }
        if (resource == null) {

            LOG.error("转码完成的资源已删除");

            taskInfo.setStatus("resourse_deleted");
            return ;
        }
        try {
            int status = UpdateResouceStatus(argument, taskInfo, resType, id, resource);

            if (1 == status) {
                updateDocumentTechInfos(resource, argument, resType);
                try {
                    updateNormalPreview(resource, argument, resType);
                } catch (Exception e) {
                    LOG.info("updateNormalPreview：",e);
                    LOG.error("updateNormalPreview：",e);
                }

                taskInfo.setStatus(PackageUtil.PackStatus.READY.getStatus());
                taskInfo.setErrMsg(argument.getErrMsg());
            } else {
                taskInfo.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
                taskInfo.setErrMsg(argument.getErrMsg());
            }
        } catch (Exception e) {
            LOG.error("转码任务回调失败", e);
        }
    }

    private int UpdateResouceStatus(TransCodeCallBackParam argument, TaskStatusInfo taskInfo, String resType, String id, ResourceModel resource) {
        int status = argument.getStatus();
        String updateStatus = status==1 ? TransCodeUtil.getTransEdStatus(true) : TransCodeUtil.getTransErrStatus(true);
        if(!updateStatus.equals(resource.getLifeCycle().getStatus())) {
            lifecycleService.addLifecycleStep(resType, id, status==1,
                    status==1?"转码成功："+argument.getErrMsg():"转码失败："+argument.getErrMsg());
            //恢复原状态
            if(StringUtils.isNotEmpty(taskInfo.getDescription())) {
                updateStatus = taskInfo.getDescription();
                ResContributeModel contributeModel = new ResContributeModel();
                contributeModel.setTargetId("777");
                contributeModel.setTargetName("LCMS");
                contributeModel.setTargetType("USER");
                contributeModel.setMessage("恢复资源原状态："+taskInfo.getDescription());
                contributeModel.setLifecycleStatus(taskInfo.getDescription());
                contributeModel.setProcess(100.0f);
                lifecycleService.addLifecycleStep(resType, id, contributeModel, false);
                MDC.put("resource", id);
                MDC.put("res_type", resType);
                MDC.put("operation_type", "转码完成");
                MDC.put("remark", "历史转码完成："+taskInfo.getErrMsg());
            }
            MDC.clear();
            resLifecycleDao.updateLifecycleStatus(resType, id, updateStatus);
        }

        return status;
    }

    /**
     * 添加requirement到 techInfo中
     * 
     * @author linsm
     * @param techInfo
     * @param metadata json格式数据(Map<String, Object>)
     * @return techInfo
     * @since
     * update by lsm (把重新生成改成-》增量)
     */
    private ResTechInfoModel addRequirement(ResTechInfoModel techInfo, String metadata) {
        // requirement
        if (StringUtils.isNotEmpty(metadata)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataMap = ObjectUtils.fromJson(metadata, Map.class);
            // List<TechnologyRequirementModel> requirementModels = new ArrayList<TechnologyRequirementModel>();
            List<TechnologyRequirementModel> requirementModels = techInfo.getRequirements();
            if(requirementModels == null){
                requirementModels = new ArrayList<TechnologyRequirementModel>();
                techInfo.setRequirements(requirementModels);
            } else {
                for (Iterator<TechnologyRequirementModel> iterator = requirementModels.iterator();iterator.hasNext();) {
                    if(metadataMap.keySet().contains(iterator.next().getName())) {
                        iterator.remove();
                    }
                }
            }
            for (Entry<String, Object> techData : metadataMap.entrySet()) {
                TechnologyRequirementModel technologyRequirementModel = new TechnologyRequirementModel();
                technologyRequirementModel.setIdentifier(UUID.randomUUID().toString());
                technologyRequirementModel.setType(RequirementType.QUOTA.toString());
                technologyRequirementModel.setName(techData.getKey());
                if(technologyRequirementModel.getName().equals("Video") || technologyRequirementModel.getName().equals("Audio")) {
                    technologyRequirementModel.setValue(ObjectUtils.toJson(techData.getValue()));
                } else {
                    technologyRequirementModel.setValue(String.valueOf(techData.getValue()));
                }
                requirementModels.add(technologyRequirementModel);
            }
            // techInfo.setRequirements(requirementModels);
        }

        return techInfo;
    }
    
    private void updateNormalTechInfos(ResourceModel resource, TransCodeCallBackParam argument, String resType) {
        List<ResTechInfoModel> techInfos = resource.getTechInfoList();
        if(techInfos == null){
            techInfos = new ArrayList<ResTechInfoModel>();
            resource.setTechInfoList(techInfos);
        }
        
        Map<String, String> metadataMap = argument.getMetadata();
        //(source) techInfo update requirement
        ResTechInfoModel newTechInfo = null;
        ResTechInfoModel sourceTechInfo = null;

        for(ResTechInfoModel resTechInfoModel:techInfos){
            if(resTechInfoModel!= null && TECH_INFO_SOURCE_KEY.equals(resTechInfoModel.getTitle())){
                sourceTechInfo = resTechInfoModel;
            }
            
            if(resTechInfoModel!= null && TECH_INFO_HREF_KEY.equals(resTechInfoModel.getTitle())){
                newTechInfo = resTechInfoModel;
            }
        }
        
        //techInfo add
        if(newTechInfo == null) {
            newTechInfo = new ResTechInfoModel();
            newTechInfo.setTitle(TECH_INFO_HREF_KEY);
            newTechInfo.setIdentifier(UUID.randomUUID().toString());
            newTechInfo.setRequirements(new ArrayList<TechnologyRequirementModel>());
        }
        newTechInfo.setLocation(argument.getHref());
        if(sourceTechInfo!=null) {
            newTechInfo.setFormat(sourceTechInfo.getFormat());
        }
        newTechInfo.setRequirements(new ArrayList<TechnologyRequirementModel>());
        try {
            List<TechInfo> tiList = new ArrayList<TechInfo>();
            TechInfo ti = BeanMapperUtils.beanMapper(newTechInfo, TechInfo.class);
            ti.setResource(resource.getIdentifier());
            ti.setResType(resType);
            ti.setRequirements(ObjectUtils.toJson(newTechInfo.getRequirements()));
            tiList.add(ti);
            if(sourceTechInfo!=null) {
                TechInfo srcTi = BeanMapperUtils.beanMapper(sourceTechInfo, TechInfo.class);
                srcTi.setResource(resource.getIdentifier());
                srcTi.setResType(resType);
                srcTi.setRequirements(ObjectUtils.toJson(sourceTechInfo.getRequirements()));
                tiList.add(srcTi);
            }
            techInfoRepository.batchAdd(tiList);
        } catch (Exception e1) {
            LOG.error("更新tech_info数据失败:"+e1.getMessage());
        }
    }
    
    private void updateNormalPreview(ResourceModel resource, TransCodeCallBackParam argument, String resType) {
        //课件ppt转码、课件模板，预览图
        LOG.info(resType+"转码，更新preview.");
        if(IndexSourceType.SourceCourseWareType.getName().equals(resType)
                || IndexSourceType.AssetType.getName().equals(resType) 
                || IndexSourceType.LessonPlansType.getName().equals(resType)
                || IndexSourceType.LearningPlansType.getName().equals(resType)) {
            Map<String,Object> previewMap = ndResourceService.getResPreviewByHref(resType, argument.getHref());
            LOG.info("previewMap: "+ObjectUtils.toJson(previewMap));
            if(CollectionUtils.isNotEmpty(previewMap)) {
                
                List<String> urlList = (List<String>) previewMap.get("previewUrls");
                if(urlList.size()>100) {
                    urlList = urlList.subList(0, 100);
                }
                LOG.info("previewMap1:" );
                for(String url:urlList) {
                    String key = url.substring(url.lastIndexOf('/')+1);
                    if(key.contains(".")) {
                        key = key.substring(0, key.lastIndexOf('.'));
                        if(IndexSourceType.LessonPlansType.getName().equals(resType)
                                || IndexSourceType.LearningPlansType.getName().equals(resType)) {
                            key = "Slide" + key;
                        }
                    }
                    LOG.info("previewMap2:" );
                    resource.getPreview().put(key, url);
                }
                LOG.info("UpdatePreview6: "+ObjectUtils.toJson(resource.getPreview()));
                resLifecycleDao.updatePreview(resType, resource.getIdentifier(), resource.getPreview());
            }
        }
    }

    private void updateImageTechInfos(ResourceModel resource, TransCodeCallBackParam argument, String resType) {
        List<ResTechInfoModel> techInfos = resource.getTechInfoList();
        if(techInfos == null){
            techInfos = new ArrayList<ResTechInfoModel>();
            resource.setTechInfoList(techInfos);
        }

        Map<String, String> metadataMap = argument.getMetadata();
        Map<String,ResTechInfoModel> newTechInfos = new HashMap<String,ResTechInfoModel>();
        if(metadataMap != null && StringUtils.isNotEmpty(metadataMap.get(TECH_INFO_SOURCE_KEY))){
            for(ResTechInfoModel resTechInfoModel:techInfos){
                if(resTechInfoModel!= null && TECH_INFO_HREF_KEYS.contains(resTechInfoModel.getTitle())){
                    //newTechInfo = resTechInfoModel;
                    newTechInfos.put(resTechInfoModel.getTitle(), resTechInfoModel);
                }
            }
        }

        //techInfo add
        Map<String,String> locations = argument.getLocations();
        for(String key : locations.keySet()) {
//            String hrefKey = argument.getHref().equals(locations.get(key)) ? TECH_INFO_HREF_KEY : TECH_INFO_HREF_KEY+"-"+key;

            ResTechInfoModel newTechInfo = newTechInfos.get(key);
            if(newTechInfo == null) {
                newTechInfo = new ResTechInfoModel();
                newTechInfos.put(key, newTechInfo);
                newTechInfo.setTitle(key);
                newTechInfo.setIdentifier(UUID.randomUUID().toString());
                newTechInfo.setRequirements(new ArrayList<TechnologyRequirementModel>());
            }
            newTechInfo.setLocation(locations.get(key));
            String targetMetadata = null;
            targetMetadata = metadataMap.get(key);
            Map<String,Object> targetMetadataMap = ObjectUtils.fromJson(targetMetadata, Map.class);
            long size=0;
            if(targetMetadataMap!=null && targetMetadataMap.get("FileSize")!=null) {
                BigDecimal bigDecimal=new BigDecimal(String.valueOf(targetMetadataMap.get("FileSize")));
                size = bigDecimal.longValue();
            }
            if(targetMetadataMap!=null && targetMetadataMap.get("md5")!=null) {
                newTechInfo.setMd5((String)targetMetadataMap.get("md5"));
            }
            newTechInfo.setSize(size);
            newTechInfo.setFormat("image/jpg");

            if(metadataMap != null){
                if(StringUtils.isNotEmpty(metadataMap.get(key))) {
                    addRequirement(newTechInfo,metadataMap.get(key));
                }
            }
        }
        try {
            List<TechInfo> tiList = new ArrayList<TechInfo>();
            for(String key : newTechInfos.keySet()) {
                TechInfo ti = BeanMapperUtils.beanMapper(newTechInfos.get(key), TechInfo.class);
                ti.setResource(resource.getIdentifier());
                ti.setResType(resType);
                ti.setRequirements(ObjectUtils.toJson(newTechInfos.get(key).getRequirements()));
                tiList.add(ti);
            }
            techInfoRepository.batchAdd(tiList);
        } catch (Exception e1) {
            LOG.error("更新tech_info数据失败:"+e1.getMessage());
        }
    }
    
    private void updateVideoTechInfos(ResourceModel resource, TransCodeCallBackParam argument, String resType) {
        List<ResTechInfoModel> techInfos = resource.getTechInfoList();
        if(techInfos == null){
            techInfos = new ArrayList<ResTechInfoModel>();
            resource.setTechInfoList(techInfos);
        }
        
        Map<String, String> metadataMap = argument.getMetadata();
        //(source) techInfo update requirement
        //ResTechInfoModel newTechInfo = null;
        ResTechInfoModel sourceTechInfo = null;
        Map<String,ResTechInfoModel> newTechInfos = new HashMap<String,ResTechInfoModel>();
        if(metadataMap != null && StringUtils.isNotEmpty(metadataMap.get(TECH_INFO_SOURCE_KEY))){
            for(ResTechInfoModel resTechInfoModel:techInfos){
                if(resTechInfoModel!= null && TECH_INFO_SOURCE_KEY.equals(resTechInfoModel.getTitle())){
                    //update requirement
                    addRequirement(resTechInfoModel, metadataMap.get(TECH_INFO_SOURCE_KEY));
                    sourceTechInfo = resTechInfoModel;
                }
                
                if(resTechInfoModel!= null && TECH_INFO_HREF_KEYS.contains(resTechInfoModel.getTitle())){
                    //newTechInfo = resTechInfoModel;
                    newTechInfos.put(resTechInfoModel.getTitle(), resTechInfoModel);
                }
            }
        }
        
        //techInfo add
        Map<String,String> locations = argument.getLocations();
        for(String key : locations.keySet()) {
//            String hrefKey = argument.getHref().equals(locations.get(key)) ? TECH_INFO_HREF_KEY : TECH_INFO_HREF_KEY+"-"+key;
            
            ResTechInfoModel newTechInfo = newTechInfos.get(key);
            if(newTechInfo == null) {
                newTechInfo = new ResTechInfoModel();
                newTechInfos.put(key, newTechInfo);
                newTechInfo.setTitle(key);
                newTechInfo.setIdentifier(UUID.randomUUID().toString());
                newTechInfo.setRequirements(new ArrayList<TechnologyRequirementModel>());
            }
            newTechInfo.setLocation(locations.get(key));
            String targetMetadata = null;
            targetMetadata = metadataMap.get(key);
            Map<String,Object> targetMetadataMap = ObjectUtils.fromJson(targetMetadata, Map.class);
            long size=0;
            if(targetMetadataMap!=null && targetMetadataMap.get("FileSize")!=null) {
                BigDecimal bigDecimal=new BigDecimal(String.valueOf(targetMetadataMap.get("FileSize")));
                size = bigDecimal.longValue();
            }
            newTechInfo.setSize(size);
            String transcodeTargetFmt = "";
            if(TransCodeUtil.SUBTYPE_VIDEO.equals(argument.getTranscodeType())) {
                
                if(!key.contains(VIDEO_THEORA_FORMAT)) {
                    transcodeTargetFmt = VIDEO_FORMAT_TARGET;
                } else {
                    transcodeTargetFmt = VIDEO_THEORA_FORMAT;
                }
            	newTechInfo.setFormat("video/"+transcodeTargetFmt);
            } else {
                if(!key.contains(AUDIO_THEORA_FORMAT)) {
                    transcodeTargetFmt = AUDIO_FORMAT_TARGET;
                } else {
                    transcodeTargetFmt = AUDIO_THEORA_FORMAT;
                }
            	newTechInfo.setFormat("audio/"+transcodeTargetFmt);
            }
            if(metadataMap != null){
                if(StringUtils.isNotEmpty(metadataMap.get(key))) {
                    addRequirement(newTechInfo,metadataMap.get(key));
                }
            }
        }
        try {
            List<TechInfo> tiList = new ArrayList<TechInfo>();
            for(String key : newTechInfos.keySet()) {
                TechInfo ti = BeanMapperUtils.beanMapper(newTechInfos.get(key), TechInfo.class);
                ti.setResource(resource.getIdentifier());
                ti.setResType(resType);
                ti.setRequirements(ObjectUtils.toJson(newTechInfos.get(key).getRequirements()));
                tiList.add(ti);
            }
            if(sourceTechInfo!=null) {
                TechInfo srcTi = BeanMapperUtils.beanMapper(sourceTechInfo, TechInfo.class);
                srcTi.setResource(resource.getIdentifier());
                srcTi.setResType(resType);
                srcTi.setRequirements(ObjectUtils.toJson(sourceTechInfo.getRequirements()));
                tiList.add(srcTi);
            }
            techInfoRepository.batchAdd(tiList);
        } catch (Exception e1) {
            LOG.error("更新tech_info数据失败:"+e1.getMessage());
        }
    }
    
    private void updateVideoPreview(ResourceModel resource, TransCodeCallBackParam argument, String resType) {
        //视频转码
        //preview;add transcodeCut and cover
        if(CollectionUtils.isNotEmpty(argument.getPreviews())||StringUtils.isNotEmpty(argument.getCover())){
            
            if(CollectionUtils.isEmpty(resource.getPreview())){
                resource.setPreview(new HashMap<String, String>());
            }
            // transcodeCut
            List<String> previewList = argument.getPreviews();
            if (CollectionUtils.isNotEmpty(argument.getPreviews())) {
                for (int i = 0; i < previewList.size(); i++) {
                    if(previewList.get(i).contains("frame1.jpg")) {
                        resource.getPreview().put(TRANSCODE_FRAME1_PREFIX,
                                "${ref-path}" + previewList.get(i)); // FIXME key 待定？,手动添加前缀
                    } else {
                        resource.getPreview().put(TRANSCODE_CUT_PREFIX + String.valueOf(i + 1),
                                              "${ref-path}" + previewList.get(i)); // FIXME key 待定？,手动添加前缀
                    }
                }
            }
    
            // cover
            if (StringUtils.isNotEmpty(argument.getCover())) {
                
                LOG.info("cover location: "+argument.getCover());
                
                resource.getPreview().put(TRANSCODE_COVER_KEY,
                                          "${ref-path}" + argument.getCover());
            }
            
            resLifecycleDao.updatePreview(resType, resource.getIdentifier(), resource.getPreview());
        }
    }
    
//    private void addLifecycleStep(String resType, ResourceModel resource, int status, String message) {
//        ResContributeModel contributeModel = new ResContributeModel();
//        if(1 == status) {
//            contributeModel.setLifecycleStatus(TransCodeUtil.getTransEdStatus(true));
//            contributeModel.setMessage("转码成功");
//            contributeModel.setProcess(100.0f);
//        } else {
//            contributeModel.setLifecycleStatus(TransCodeUtil.getTransErrStatus(true));
//            contributeModel.setMessage("转码失败："+message);
//            contributeModel.setProcess(0.0f);
//        }
//
//        if(!contributeModel.getLifecycleStatus().equals(resource.getLifeCycle().getStatus())) {
//            contributeModel.setTargetId("777");
//            contributeModel.setTargetName("LCMS");
//            contributeModel.setTargetType("USER");
//            lifecycleService.addLifecycleStep(resType, resource.getIdentifier(), contributeModel, false);
//        }
//
//    }


    private void updateDocumentTechInfos(ResourceModel resource, TransCodeCallBackParam argument, String resType) {
        List<ResTechInfoModel> techInfos = resource.getTechInfoList();
        if(techInfos == null){
            techInfos = new ArrayList<ResTechInfoModel>();
            resource.setTechInfoList(techInfos);
        }

        Map<String, String> metadataMap = argument.getMetadata();
        ResTechInfoModel sourceTechInfo = null;
        Map<String,ResTechInfoModel> newTechInfos = new HashMap<String,ResTechInfoModel>();
        if(metadataMap != null && StringUtils.isNotEmpty(metadataMap.get(TECH_INFO_SOURCE_KEY))){
            for(ResTechInfoModel resTechInfoModel:techInfos){
                if(resTechInfoModel!= null && TECH_INFO_SOURCE_KEY.equals(resTechInfoModel.getTitle())){
                    //update requirement
                    addRequirement(resTechInfoModel, metadataMap.get(TECH_INFO_SOURCE_KEY));
                    sourceTechInfo = resTechInfoModel;
                }

                if(resTechInfoModel!= null && TECH_INFO_HREF_KEYS.contains(resTechInfoModel.getTitle())){
                    //newTechInfo = resTechInfoModel;
                    newTechInfos.put(resTechInfoModel.getTitle(), resTechInfoModel);
                }
            }
        }

        //techInfo add
        Map<String,String> locations = argument.getLocations();
        for(String key : locations.keySet()) {

            ResTechInfoModel newTechInfo = newTechInfos.get(key);
            if(newTechInfo == null) {
                newTechInfo = new ResTechInfoModel();
                newTechInfos.put(key, newTechInfo);
                newTechInfo.setTitle(key);
                newTechInfo.setIdentifier(UUID.randomUUID().toString());
                newTechInfo.setRequirements(new ArrayList<TechnologyRequirementModel>());
            }
            newTechInfo.setLocation(locations.get(key));
            String targetMetadata = null;
            targetMetadata = metadataMap.get(key);
            Map<String,Object> targetMetadataMap = ObjectUtils.fromJson(targetMetadata, Map.class);
            long size=0;
            if(targetMetadataMap!=null && targetMetadataMap.get("FileSize")!=null) {
                BigDecimal bigDecimal=new BigDecimal(String.valueOf(targetMetadataMap.get("FileSize")));
                size = bigDecimal.longValue();
            }
            newTechInfo.setSize(size);
            newTechInfo.setFormat(key);
            if(metadataMap != null){
                if(StringUtils.isNotEmpty(metadataMap.get(key))) {
                    addRequirement(newTechInfo,metadataMap.get(key));
                }
            }
        }
        try {
            List<TechInfo> tiList = new ArrayList<TechInfo>();
            for(String key : newTechInfos.keySet()) {
                TechInfo ti = BeanMapperUtils.beanMapper(newTechInfos.get(key), TechInfo.class);
                ti.setResource(resource.getIdentifier());
                ti.setResType(resType);
                ti.setRequirements(ObjectUtils.toJson(newTechInfos.get(key).getRequirements()));
                tiList.add(ti);
            }
            if(sourceTechInfo!=null) {
                TechInfo srcTi = BeanMapperUtils.beanMapper(sourceTechInfo, TechInfo.class);
                srcTi.setResource(resource.getIdentifier());
                srcTi.setResType(resType);
                srcTi.setRequirements(ObjectUtils.toJson(sourceTechInfo.getRequirements()));
                tiList.add(srcTi);
            }
            techInfoRepository.batchAdd(tiList);
        } catch (Exception e1) {
            LOG.error("更新tech_info数据失败:"+e1.getMessage());
        }
    }

}
