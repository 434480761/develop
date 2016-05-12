package nd.esp.service.lifecycle.services.educationrelation.v06.impls;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.models.v06.BatchAdjustRelationOrderModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationLifeCycleModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.models.v06.ResourceRelationResultModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceForQuestionV06;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("educationRelationServiceForQuestionV06")
@Transactional(value="questionTransactionManager")
public class EducationRelationServiceImplForQuestionV06 implements EducationRelationServiceForQuestionV06 {
    private final static Logger LOG = LoggerFactory.getLogger(EducationRelationServiceImplForQuestionV06.class);

    /**
     * 初始的sortNum值
     */
    private static final float SORT_NUM_INITIAL_VALUE = 5000f;

    /**
     * SDK注入
     */
    @Autowired
    private ResourceRelation4QuestionDBRepository resourceRelationRepository;
    
    @Autowired
    private EducationRelationDao educationRelationDao;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private NotifyReportService nrs;
    
    @Override
    public List<EducationRelationModel> createRelation(List<EducationRelationModel> educationRelationModels,
                                                       boolean isCreateWithResource) {
        // 待添加的资源关系集合
        List<ResourceRelation> relations4Create = new ArrayList<ResourceRelation>();
        ResourceRelation relation = new ResourceRelation();
        EspEntity question = null;
        EspEntity targetEntity = null;
        // 返回的结果集
        List<EducationRelationModel> resultList = new ArrayList<EducationRelationModel>();
        boolean haveExist = false;

        Map<String, List<ResourceRelationResultModel>> map4Total = new HashMap<String, List<ResourceRelationResultModel>>();
        float newSortNum = SORT_NUM_INITIAL_VALUE;
        for (EducationRelationModel erm : educationRelationModels) {
            if(StringUtils.isEmpty(erm.getLabel())){
                erm.setLabel(null);
            }
            // 判断源资源是否存在，不存在抛出not found异常
            question = resourceExist(erm.getResType(), erm.getSource(), ResourceType.RESOURCE_SOURCE);
            if (!isCreateWithResource) {// 与资源同时创建时不需要一下操作
                targetEntity = resourceExist(erm.getResourceTargetType(),
                                                       erm.getTarget(),
                                                       ResourceType.RESOURCE_TARGET);

                EducationRelationModel model4Detail = relationExist(erm.getSource(),
                                                                    erm.getTarget(),
                                                                    erm.getRelationType(),
                                                                    erm.getLabel());
                if (null != model4Detail) {
                    // 获取源资源和目标资源的title值
                    model4Detail.setSourceTitle(question.getTitle());
                    model4Detail.setTargetTitle(targetEntity.getTitle());

                    resultList.add(model4Detail);
                    haveExist = true;
                    break;
                }

            }

            // 生成SDK的入参对象
            relation.setTarget(erm.getTarget());
            relation.setLabel(erm.getLabel());
            relation.setTags(erm.getTags());
            relation.setResType(erm.getResType());
            relation.setResourceTargetType(erm.getResourceTargetType());
            // orderNum仅用于检索显示,默认为0
            relation.setOrderNum(0);
            // 关系类型，默认值是ASSOCIATE
            if (StringUtils.isEmpty(erm.getRelationType())) {
                relation.setRelationType("ASSOCIATE");
            } else {
                relation.setRelationType(erm.getRelationType());
            }
            relation.setSourceUuid(erm.getSource());

            if(erm.getIdentifier() != null){
            	relation.setIdentifier(erm.getIdentifier());
            }else{
            	relation.setIdentifier(UUID.randomUUID().toString());
            }
            
            if (erm.getLifeCycle() == null) {
                relation.setCreator(null);
                relation.setStatus("AUDIT_WAITING");
            } else {
                relation.setCreator(erm.getLifeCycle().getCreator());
                relation.setStatus(erm.getLifeCycle().getStatus());
            }
            relation.setEnable(true);
            // 默认值
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            relation.setCreateTime(timestamp);
            relation.setLastUpdate(timestamp);

            // 处理sortNum,用于排序
            List<ResourceRelationResultModel> list4Total = null;
            if (relation.getResType().equals(IndexSourceType.ChapterType.getName())
                    && relation.getResourceTargetType().equals(IndexSourceType.QuestionType.getName())) {// 这个判断后期可能需要去掉
                relation.setSortNum(SORT_NUM_INITIAL_VALUE);
            } else {
                // 调用SDK,目的是获取资源关系的总数
                list4Total = map4Total.get(relation.getSourceUuid());
                if (CollectionUtils.isEmpty(list4Total)) {
                    newSortNum = SORT_NUM_INITIAL_VALUE;
                    list4Total = educationRelationDao.getResourceRelationsWithOrder(relation.getSourceUuid(),
                                                                                    relation.getResType());
                    if (CollectionUtils.isNotEmpty(list4Total)) {// 说明源资源与目标资源之间没有任何关系
                        // 说明已经存在关系,将新增加的资源关系放到最后一个
                        newSortNum = list4Total.get(0).getSortNum() == null ? SORT_NUM_INITIAL_VALUE + 10
                                                                           : list4Total.get(0).getSortNum() + 10;
                        map4Total.put(relation.getSourceUuid(), list4Total);
                    } else {
                        List<ResourceRelationResultModel> list4TotalTmp = new ArrayList<ResourceRelationResultModel>();
                        ResourceRelationResultModel model = new ResourceRelationResultModel();
                        model.setOrderNum(relation.getOrderNum());
                        model.setSortNum(relation.getSortNum());
                        model.setResourceTargetType(relation.getResourceTargetType());
                        list4TotalTmp.add(model);
                        map4Total.put(relation.getSourceUuid(), list4TotalTmp);
                    }
                } else {
                    newSortNum += 10;
                }

                relation.setSortNum(newSortNum);
            }

            //新增源资源与目标资源的创建时间
            relation.setResourceCreateTime(((Education)question).getDbcreateTime());
            if(isCreateWithResource){
            	relation.setTargetCreateTime(erm.getTargetCT());
            }else{
            	relation.setTargetCreateTime(((Education)targetEntity).getDbcreateTime());
            }
            
            relations4Create.add(relation);
        }

        List<ResourceRelation> resourceRelations = null;
        try {
            // 调用SDK
            if (CollectionUtils.isNotEmpty(relations4Create)) {
                resourceRelations = resourceRelationRepository.batchAdd(relations4Create);
            }
        } catch (EspStoreException e) {

            LOG.error("添加资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CreateEducationRelationFail.getCode(),
                                          e.getMessage());
        }

        if (!haveExist && CollectionUtils.isEmpty(resourceRelations)) {

            LOG.error("添加资源关系失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CreateEducationRelationFail);
        }
        //通知报表系统 add by xuzy 20160511
        nrs.addResourceRelation(resourceRelations);

        // 处理返回结果
        if (!isCreateWithResource) {
            if (CollectionUtils.isNotEmpty(resourceRelations)) {
                for (ResourceRelation resourceRelation : resourceRelations) {
                    EducationRelationModel model = new EducationRelationModel();
                    model.setIdentifier(resourceRelation.getIdentifier());
                    model.setSource(resourceRelation.getSourceUuid());
                    if (question != null) {
                        model.setSourceTitle(question.getTitle());
                    }
                    model.setTarget(resourceRelation.getTarget());
                    if (targetEntity != null) {
                        model.setTargetTitle(targetEntity.getTitle());
                    }
                    model.setRelationType(resourceRelation.getRelationType());
                    model.setLabel(resourceRelation.getLabel());
                    model.setTags(resourceRelation.getTags());
                    model.setOrderNum(resourceRelation.getOrderNum());
                    model.setResourceTargetType(resourceRelation.getResourceTargetType());
                    model.setResType(resourceRelation.getResType());
                    EducationRelationLifeCycleModel lifeCycleModel = new EducationRelationLifeCycleModel();
                    lifeCycleModel.setCreateTime(resourceRelation.getCreateTime());
                    lifeCycleModel.setLastUpdate(resourceRelation.getLastUpdate());
                    lifeCycleModel.setEnable(resourceRelation.getEnable());
                    lifeCycleModel.setCreator(resourceRelation.getCreator());
                    lifeCycleModel.setStatus(resourceRelation.getStatus());
                    model.setLifeCycle(lifeCycleModel);
                    
                    resultList.add(model);
                }
            }
        }

        return resultList;
    }

    @Override
    public EducationRelationModel updateRelation(String resType,
                                                 String sourceUuid,
                                                 String rid,
                                                 EducationRelationModel educationRelationModel) {
        // 获取修改前的数据
        ResourceRelation resourceRelation = getRelationByRid(rid);
        if (resourceRelation == null) {
            LOG.error("资源关系不存在");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ResourceRelationNotExist);
        }
        
        // 判断源资源是否存在，不存在抛出not found异常
        EspEntity question = resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);
        EspEntity targetEntity = resourceExist(resourceRelation.getResourceTargetType(),
                                               resourceRelation.getTarget(),
                                               ResourceType.RESOURCE_TARGET);

        // 生成SDK的入参对象
        ResourceRelation relation = new ResourceRelation();

        // 可变属性
        // 未传，则保留原值
        if (educationRelationModel.getLifeCycle() == null) {
            relation.setStatus(resourceRelation.getStatus());
        } else {
            relation.setStatus(educationRelationModel.getLifeCycle().getStatus());
        }
        // 未传，则保留原值
        if (CollectionUtils.isEmpty(educationRelationModel.getTags())) {
            relation.setTags(resourceRelation.getTags());
        } else {
            relation.setTags(educationRelationModel.getTags());
        }
        relation.setOrderNum(educationRelationModel.getOrderNum());
        // 未传，则保留原值
        if (StringUtils.isEmpty(educationRelationModel.getRelationType())) {
            relation.setRelationType(resourceRelation.getRelationType());
        } else {
            relation.setRelationType(educationRelationModel.getRelationType());
        }
        // 未传，则保留原值
        if (StringUtils.isEmpty(educationRelationModel.getLabel())) {
            relation.setLabel(resourceRelation.getLabel());
        } else {
            relation.setLabel(educationRelationModel.getLabel());
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        relation.setLastUpdate(timestamp);

        // 不可变属性
        relation.setIdentifier(rid);
        relation.setResourceTargetType(resourceRelation.getResourceTargetType());
        relation.setResType(resourceRelation.getResType());
        relation.setSourceUuid(sourceUuid);
        relation.setCreator(resourceRelation.getCreator());
        relation.setTarget(resourceRelation.getTarget());
        relation.setSortNum(resourceRelation.getSortNum());
        relation.setCreateTime(resourceRelation.getCreateTime());
        relation.setEnable(resourceRelation.getEnable());
        //新增源资源与目标资源的创建时间
        relation.setResourceCreateTime(resourceRelation.getResourceCreateTime());
        relation.setTargetCreateTime(resourceRelation.getTargetCreateTime());

        ResourceRelation result = null;
        try {
            // 调用SDK
            result = resourceRelationRepository.update(relation);
        } catch (EspStoreException e) {

            LOG.error("更新资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateEducationRelationFail.getCode(),
                                          e.getMessage());
        }

        // 修改资源关系失败
        if (result == null) {

            LOG.error("更新资源关系失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateEducationRelationFail);
        }
        //通知报表系统 add by xuzy 20160511
        nrs.updateResourceRelation(relation);

        // 处理返回结果
        EducationRelationModel model = new EducationRelationModel();
        model.setIdentifier(result.getIdentifier());
        model.setSource(result.getSourceUuid());
        if (question != null) {
            model.setSourceTitle(question.getTitle());
        }
        model.setTarget(result.getTarget());
        if (targetEntity != null) {
            model.setTargetTitle(targetEntity.getTitle());
        }
        model.setRelationType(result.getRelationType());
        model.setLabel(result.getLabel());
        model.setTags(result.getTags());
        model.setOrderNum(result.getOrderNum());
        model.setResourceTargetType(result.getResourceTargetType());
        model.setResType(result.getResType());
        EducationRelationLifeCycleModel lifeCycleModel = new EducationRelationLifeCycleModel();
        lifeCycleModel.setCreateTime(result.getCreateTime());
        lifeCycleModel.setLastUpdate(result.getLastUpdate());
        lifeCycleModel.setEnable(result.getEnable());
        lifeCycleModel.setCreator(result.getCreator());
        lifeCycleModel.setStatus(result.getStatus());
        model.setLifeCycle(lifeCycleModel);

        return model;
    }
    
    @Override
    public boolean deleteRelation(String rid, String sourceUuid, String resType) {
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);

        ResourceRelation rt = getRelationByRid(rid);
        
        if (rt == null) {
            LOG.error("资源关系不存在");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ResourceRelationNotExist);
        }
        
        rt.setEnable(false);
        rt.setLastUpdate(new Timestamp(System.currentTimeMillis()));

        try {
            resourceRelationRepository.update(rt);
        } catch (EspStoreException e) {

            LOG.error("更新资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateLessonFail);
        }
        List<ResourceRelation> notifyRelationList = new ArrayList<ResourceRelation>();
        notifyRelationList.add(rt);
        //通知报表系统 add by xuzy 20160511
        nrs.deleteResourceRelation(notifyRelationList);

        return true;
    }

    @Override
    public boolean deleteRelationByTarget(String resType,
                                          String sourceUuid,
                                          List<String> target,
                                          String relationType,
                                          boolean reverse) {
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);
        // 用于存放需要删除的资源关系id
//        List<String> deleteIds = new ArrayList<String>();

        // 根据条件查询全部的资源关系,再进行批量删除
        ResourceRelation resourceRelation = new ResourceRelation();
        if (reverse) {
            resourceRelation.setTarget(sourceUuid);
            resourceRelation.setResourceTargetType(resType);
        } else {
            resourceRelation.setSourceUuid(sourceUuid);
            resourceRelation.setResType(resType);
        }
        // 如果relationType不为null和空,则赋值
        if (!StringUtils.isEmpty(relationType)) {
            resourceRelation.setRelationType(relationType);
        }

        // 用于存放查找到的资源关系
        List<ResourceRelation> relationsTotal = new ArrayList<ResourceRelation>();
        List<ResourceRelation> relations = new ArrayList<ResourceRelation>();

        if (!CollectionUtils.isEmpty(target)) {// 当target有值时,需循环调用sdk找出全部的资源关系id
            for (String tagetId : target) {
                if (reverse) {
                    resourceRelation.setSourceUuid(tagetId);
                } else {
                    resourceRelation.setTarget(tagetId);
                }

                try {
                    // 调用SDK,根据条件查询资源关系
                    relations = resourceRelationRepository.getAllByExample(resourceRelation);

                    if (!CollectionUtils.isEmpty(relations)) {
                        // 将找到的资源关系id,放入deleteIds中
                        for (ResourceRelation rr : relations) {
                            // deleteIds.add(rr.getIdentifier());
                            rr.setEnable(false);
                            rr.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                        }
                        relationsTotal.addAll(relations);
                    }
                } catch (EspStoreException e) {

                    LOG.error("target,根据条件获取资源关系失败", e);

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.GetRelationByTargetFail.getCode(),
                                                  e.getMessage());
                }
            }
        } else {// 当target为null或empty时,只需调用一次
            try {
                // 调用SDK,根据条件查询资源关系
                relations = resourceRelationRepository.getAllByExample(resourceRelation);

                if (!CollectionUtils.isEmpty(relations)) {
                    // 将找到的资源关系id,放入deleteIds中
                    for (ResourceRelation rr : relations) {
                        // deleteIds.add(rr.getIdentifier());
                        rr.setEnable(false);
                        rr.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                    }
                    relationsTotal.addAll(relations);
                }
            } catch (EspStoreException e) {

                LOG.error("target,根据条件获取资源关系失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.GetRelationByTargetFail.getCode(),
                                              e.getMessage());
            }
        }

        try {
//            if (deleteIds.isEmpty()) {
//                return true;
//            }
            if(CollectionUtils.isEmpty(relationsTotal)){
                return true;
            }

            // 调用SDK,批量删除资源关系
//            resourceRelationRepository.batchDel(deleteIds);
            //更新资源关系
            resourceRelationRepository.batchAdd(relationsTotal);
        } catch (EspStoreException e) {

            LOG.error("批量更新资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateBatchRelationFail.getCode(),
                                          e.getMessage());
        }
        //通知报表系统 add by xuzy 20160511
        nrs.deleteResourceRelation(relationsTotal);
        return true;
    }

    @Override
    public boolean deleteRelationByTargetType(String resType,
                                              String sourceUuid,
                                              List<String> targetType,
                                              String relationType,
                                              boolean reverse) {
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);
        // 用于存放需要删除的资源关系id
//        List<String> deleteIds = new ArrayList<String>();

        // 根据条件查询全部的资源关系,再进行批量删除
        ResourceRelation resourceRelation = new ResourceRelation();
        if (reverse) {
            resourceRelation.setTarget(sourceUuid);
            resourceRelation.setResourceTargetType(resType);
        } else {
            resourceRelation.setSourceUuid(sourceUuid);
            resourceRelation.setResType(resType);
        }
        // 如果relationType不为null和空,则赋值
        if (!StringUtils.isEmpty(relationType)) {
            resourceRelation.setRelationType(relationType);
        }

        // 用于存放查找到的资源关系
        List<ResourceRelation> relationsTotal = new ArrayList<ResourceRelation>();
        List<ResourceRelation> relations = new ArrayList<ResourceRelation>();

        if (!CollectionUtils.isEmpty(targetType)) {// 当targetType有值时,需循环调用sdk找出全部的资源关系id
            for (String type : targetType) {
                if (reverse) {
                    resourceRelation.setResType(type);
                } else {
                    resourceRelation.setResourceTargetType(type);
                }

                try {
                    // 调用SDK,根据条件查询资源关系
                    relations = resourceRelationRepository.getAllByExample(resourceRelation);

                    if(!CollectionUtils.isEmpty(relations)){
                        
                        // 将找到的资源关系id,放入deleteIds中
                        for (ResourceRelation rr : relations) {
    //                        deleteIds.add(rr.getIdentifier());
                            rr.setEnable(false);
                            rr.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                        }
                        relationsTotal.addAll(relations);
                    }
                } catch (EspStoreException e) {

                    LOG.error("根据条件获取资源关系失败", e);

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.GetRelationByTargetTypeFail.getCode(),
                                                  e.getMessage());
                }
            }
        } else {// 当targetType为null或empty时,只需调用一次
            try {
                // 调用SDK,根据条件查询资源关系
                relations = resourceRelationRepository.getAllByExample(resourceRelation);
                if (!CollectionUtils.isEmpty(relations)) {
                    // 将找到的资源关系id,放入deleteIds中
                    for (ResourceRelation rr : relations) {
                        // deleteIds.add(rr.getIdentifier());
                        rr.setEnable(false);
                        rr.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                    }
                    relationsTotal.addAll(relations);
                }
            } catch (EspStoreException e) {

                LOG.error("根据条件获取资源关系失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.GetRelationByTargetTypeFail.getCode(),
                                              e.getMessage());
            }
        }

        try {
//            if (deleteIds.isEmpty()) {
//                return true;
//            }
            if(CollectionUtils.isEmpty(relationsTotal)){
                return true;
            }

            // 调用SDK,批量删除资源关系
//            resourceRelationRepository.batchDel(deleteIds);
            // 更新资源关系
            resourceRelationRepository.batchAdd(relationsTotal);
        } catch (EspStoreException e) {

            LOG.error("批量更新资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateBatchRelationFail.getCode(),
                                          e.getMessage());
        }
        //通知报表系统 add by xuzy 20160511
        nrs.deleteResourceRelation(relationsTotal);
        return true;
    }

    @Override
    public void batchAdjustRelationOrder(String resType,
                                         String sourceUuid,
                                         List<BatchAdjustRelationOrderModel> batchAdjustRelationOrderModels) {
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);
        if (!CollectionUtils.isEmpty(batchAdjustRelationOrderModels)) {
            // 调整前先校验数据,有问题返回异常,不允许调整
            for (BatchAdjustRelationOrderModel bar : batchAdjustRelationOrderModels) {
                // 判断 需要移动的目标对象 是否存在
                if (this.getRelationByRid(bar.getTarget()) == null) {// 不存在

                    LOG.error("需要移动的目标对象--" + bar.getTarget() + "--不存在");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.TargetRelationNotExist);
                }
                // 判断 移动目的地的靶心对象 是否存在
                if (this.getRelationByRid(bar.getDestination()) == null) {// 不存在

                    LOG.error("需要移动的目标对象--" + bar.getTarget() + "--移动目的地的靶心对象--" + bar.getDestination() + "--不存在");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.DestinationRelationNotExist);
                }
                if ("first".equals(bar.getAt()) || "last".equals(bar.getAt())) {// 移动到第一个位置或者移动到最后
                    // 相邻对象adjoin应该为"none"
                    if (!"none".equals(bar.getAdjoin())) {
                        String message = "first".equals(bar.getAt()) ? "移动到第一个位置" : "移动到最后一个";

                        LOG.error("需要移动的目标对象--" + bar.getTarget() + "--" + message + "时相邻对象的值应该为none");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      LifeCircleErrorMessageMapper.AdjoinValueError);
                    }
                } else if ("middle".equals(bar.getAt())) {// 将目标增加到destination和adjoin中间
                    // 判断 相邻对象 是否存在
                    if (!"none".equals(bar.getAdjoin()) && this.getRelationByRid(bar.getAdjoin()) == null) {// 不存在

                        LOG.error("需要移动的目标对象--" + bar.getTarget() + "--相邻对象--" + bar.getAdjoin() + "--不存在");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      LifeCircleErrorMessageMapper.AdjoinRelationNotExist);
                    }
                } else {

                    LOG.error("需要移动的目标对象--" + bar.getTarget() + "--at只允许为first,last,middle三个值");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.AtValueError);
                }
            }

            // 需要移动的目标对象
            ResourceRelation targetRelation = null;
            // 移动目的地靶心对象
            ResourceRelation destinationRelation = null;
            // 相邻对象
            ResourceRelation adjoinRelation = null;

            // 数据正确,进行顺序调整
            for (BatchAdjustRelationOrderModel bar4Adjust : batchAdjustRelationOrderModels) {
                if ("first".equals(bar4Adjust.getAt())) {// 移动到第一个位置
                    targetRelation = this.getRelationByRid(bar4Adjust.getTarget());
                    destinationRelation = this.getRelationByRid(bar4Adjust.getDestination());

                    // 修改targetRelation的sortNum,在destinationRelation的sortNum上-10
                    targetRelation.setSortNum(destinationRelation.getSortNum() - 10);

                } else if ("last".equals(bar4Adjust.getAt())) {// 移动到最后
                    targetRelation = this.getRelationByRid(bar4Adjust.getTarget());
                    destinationRelation = this.getRelationByRid(bar4Adjust.getDestination());

                    // 修改targetRelation的sortNum,在destinationRelation的sortNum上+10
                    targetRelation.setSortNum(destinationRelation.getSortNum() + 10);
                } else {// 将目标增加到destination和adjoin中间
                    targetRelation = this.getRelationByRid(bar4Adjust.getTarget());
                    destinationRelation = this.getRelationByRid(bar4Adjust.getDestination());
                    adjoinRelation = this.getRelationByRid(bar4Adjust.getAdjoin());

                    // 修改targetRelation的sortNum,
                    // 为(destinationRelation的sortNum + adjoinRelation的sortNum) / 2
                    targetRelation.setSortNum((destinationRelation.getSortNum() + adjoinRelation.getSortNum()) / 2);
                }

                try {
                    resourceRelationRepository.update(targetRelation);
                } catch (EspStoreException e) {

                    LOG.error("批量调整顺序时更新失败", e);

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.AdjustOrderFail.getCode(),
                                                  e.getMessage());
                }
            }
        }
    }

    private ResourceRelation getRelationByRid(String rid) {
        // 判断rid对应的资源关系是否存在,不存在抛出异常
        ResourceRelation resourceRelation = null;
        try {
            resourceRelation = resourceRelationRepository.get(rid);
        } catch (EspStoreException e) {

            LOG.error("获取资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.GetRelationDetailFail.getCode(),
                                          e.getMessage());
        }

        if (resourceRelation == null || !resourceRelation.getEnable()) {
            return null;
        }

        return resourceRelation;
    }
    
    /**
     * 判断源资源是否存在
     * 
     * @param resType 资源种类
     * @param resId 源资源id
     * @param type  源资源类型
     * @since
     */
    private EspEntity resourceExist(String resType, String resId, String type) {
        EspEntity flag = null;
        try {
            /*
             * 调用各个资源的获取详细方法,用于判断对应资源是否存在, 若不存在,则抛出异常
             */
            EspRepository<?> resourceRepository = ServicesManager.get(resType);
            flag = resourceRepository.get(resId);
        } catch (EspStoreException e) {
            if (ResourceType.RESOURCE_SOURCE.equals(type)) {

                LOG.error("源资源:" + resType + "--" + resId + "未找到", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            if (ResourceType.RESOURCE_TARGET.equals(type)) {

                LOG.error("目标资源:" + resType + "--" + resId + "未找到", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }

        // 资源不存在,抛出异常
        if (flag == null
                || ((flag instanceof Education) && ((Education) flag).getEnable() != null && !((Education) flag).getEnable())
                || !((Education) flag).getPrimaryCategory().equals(resType)) {
            if (ResourceType.RESOURCE_SOURCE.equals(type)) {

                LOG.error("源资源:" + resType + "--" + resId + "未找到");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.SourceResourceNotFond.getCode(),
                                              "源资源:" + resType + "--" + resId + "未找到");
            }
            if (ResourceType.RESOURCE_TARGET.equals(type)) {

                LOG.error("目标资源:" + resType + "--" + resId + "未找到");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.TargetResourceNotFond.getCode(),
                                              "目标资源:" + resType + "--" + resId + "未找到");
            }
        }
        
        return flag;
    }
    
    public EducationRelationModel relationExist(String sourceId, String targetId, String relationType, String label) {
        ResourceRelation example = new ResourceRelation();
        example.setSourceUuid(sourceId);
        example.setTarget(targetId);
        if (StringUtils.isEmpty(relationType)) {
            example.setRelationType("ASSOCIATE");
        } else {
            example.setRelationType(relationType);
        }
        example.setLabel(label);
        example.setEnable(true);

        List<ResourceRelation> resourceRelations = null;
        try {
            resourceRelations = resourceRelationRepository.getAllByExample(example);
        } catch (EspStoreException e) {

            LOG.error("获取资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.GetRelationDetailFail.getCode(),
                                          e.getMessage());
        }
        
        if(CollectionUtils.isEmpty(resourceRelations)){
            return null;
        }

        ResourceRelation result = null;
        
        for (ResourceRelation resourceRelation : resourceRelations) {
            if ((StringUtils.isEmpty(label) && StringUtils.isEmpty(resourceRelation.getLabel()))
                    || (StringUtils.isNotEmpty(label) && label.equals(resourceRelation.getLabel()))) {
                result = resourceRelation;
                break;
            }
        }
        
        if (result == null) {
            return null;
        }

        // 处理返回结果
        EducationRelationModel model = new EducationRelationModel();
        model.setIdentifier(result.getIdentifier());
        model.setSource(result.getSourceUuid());
        model.setTarget(result.getTarget());
        model.setRelationType(result.getRelationType());
        model.setLabel(result.getLabel());
        model.setTags(result.getTags());
        model.setOrderNum(result.getOrderNum());
        model.setResourceTargetType(result.getResourceTargetType());
        model.setResType(result.getResType());
        EducationRelationLifeCycleModel lifeCycleModel = new EducationRelationLifeCycleModel();
        lifeCycleModel.setCreateTime(result.getCreateTime());
        lifeCycleModel.setLastUpdate(result.getLastUpdate());
        lifeCycleModel.setEnable(result.getEnable());
        lifeCycleModel.setCreator(result.getCreator());
        lifeCycleModel.setStatus(result.getStatus());
        model.setLifeCycle(lifeCycleModel);

        return model;
    }

}
