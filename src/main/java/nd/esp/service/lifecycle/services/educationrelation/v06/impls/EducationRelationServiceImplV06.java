package nd.esp.service.lifecycle.services.educationrelation.v06.impls;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.models.chapter.v06.ChapterModel;
import nd.esp.service.lifecycle.models.v06.BatchAdjustRelationOrderModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationLifeCycleModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.models.v06.ResourceRelationResultModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.Lesson;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.LessonRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceForQuestionV06;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForPathViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("educationRelationServiceV06")
@Transactional
public class EducationRelationServiceImplV06 implements EducationRelationServiceV06 {
    private final static Logger LOG = LoggerFactory.getLogger(EducationRelationServiceImplV06.class);

    /**
     * 初始的sortNum值
     */
    private static final float SORT_NUM_INITIAL_VALUE = 5000f;

    /**
     * SDK注入
     */
    @Autowired
    private ResourceRelationRepository resourceRelationRepository;
    
    @Autowired
    private TeachingMaterialRepository teachingMaterialRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private ResourceCategoryRepository resourceCategoryRepository;
    
    @Autowired
    private CategoryDataRepository categoryDataRepository;

    @Autowired
    @Qualifier("chapterServiceV06")
    private ChapterService chapterServiceV06;
    
    @Autowired
    @Qualifier("educationRelationServiceForQuestionV06")
    private EducationRelationServiceForQuestionV06 educationRelationServiceForQuestion;
    
    @Autowired
    private EducationRelationDao educationRelationDao;
    
    @Autowired
    private ChapterDao chapterDao;
    
    @Autowired
    private NotifyInstructionalobjectivesService notifyService;
    
    @Autowired
    private NotifyReportService nrs;
    
    @Override
    public List<EducationRelationModel> createRelation(List<EducationRelationModel> educationRelationModels,
                                                       boolean isCreateWithResource) {
    	boolean notifyFlag = true;
        // 待添加的资源关系集合
        List<ResourceRelation> relations4Create = new ArrayList<ResourceRelation>();
        EspEntity resourceEntity = null;
        EspEntity targetEntity = null;
        ResourceRelation relation = new ResourceRelation();
        // 返回的结果集
        List<EducationRelationModel> resultList = new ArrayList<EducationRelationModel>();
        boolean haveExist = false;

        Map<String, List<ResourceRelationResultModel>> map4Total = new HashMap<String, List<ResourceRelationResultModel>>();
        Map<String, Integer> orderNumMap = new HashMap<String, Integer>();
        Map<String, Set<Integer>> isDuplications = new HashMap<String, Set<Integer>>(); 
        if (isCreateWithResource) {
            int notNullOrderNums = 0;
            for (EducationRelationModel erm : educationRelationModels) {
                if ((erm.getResType().equals(IndexSourceType.ChapterType.getName())
                        || erm.getResType().equals(IndexSourceType.LessonType.getName()) || erm.getResType()
                                                                                                         .equals(IndexSourceType.InstructionalObjectiveType.getName()))
                        && (erm.getResourceTargetType().equals(IndexSourceType.LessonType.getName()) || erm.getResourceTargetType()
                                                                                                                     .equals(IndexSourceType.InstructionalObjectiveType.getName()))) {
                    if (erm.getOrderNum() != null) {
                        notNullOrderNums++;
                        Set<Integer> orderNums = null;
                        if (isDuplications.get(erm.getSource()) == null) {
                            orderNums = new HashSet<Integer>();
                            orderNums.add(erm.getOrderNum());
                            isDuplications.put(erm.getSource(), orderNums);
                        } else {
                            isDuplications.get(erm.getSource()).add(erm.getOrderNum());
                        }
                        if (orderNumMap.get(erm.getSource()) == null
                                || (orderNumMap.get(erm.getSource()) != null && erm.getOrderNum() > orderNumMap.get(erm.getSource()))) {
                            orderNumMap.put(erm.getSource(), erm.getOrderNum());
                        }
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(isDuplications)){
                int sum = 0;
                for (Map.Entry<String, Set<Integer>> entry: isDuplications.entrySet()) {
                    sum += entry.getValue().size();
                }
                if (sum != notNullOrderNums) {
                    
                    LOG.error("orderNum不允许重复");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.OrderNumError.getCode(),
                                                  "orderNum不允许重复");
                }
            }
        }
        float newSortNum = SORT_NUM_INITIAL_VALUE;
        for (EducationRelationModel erm : educationRelationModels) {
            if(StringUtils.isEmpty(erm.getLabel())){
                erm.setLabel(null);
            }
            if("knowledgebases".equals(erm.getResourceTargetType())){
            	notifyFlag = false;
            }
            
           
            // 判断源资源是否存在，不存在抛出not found异常
            resourceEntity = resourceExist(erm.getResType(), erm.getSource(), ResourceType.RESOURCE_SOURCE);
            if (!isCreateWithResource) {// 与资源同时创建时不需要一下操作
        		targetEntity = resourceExist(erm.getResourceTargetType(), erm.getTarget(), ResourceType.RESOURCE_TARGET);
                EducationRelationModel model4Detail = relationExist(erm.getSource(),
                                                                    erm.getTarget(),
                                                                    erm.getRelationType(),
                                                                    erm.getLabel());
                if (null != model4Detail) {
                    // 获取源资源和目标资源的title值
                    model4Detail.setSourceTitle(resourceEntity.getTitle());
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

            if ((relation.getResType().equals(IndexSourceType.ChapterType.getName())
                    || relation.getResType().equals(IndexSourceType.LessonType.getName()) || relation.getResType()
                                                                                                     .equals(IndexSourceType.InstructionalObjectiveType.getName()))
                    && (relation.getResourceTargetType().equals(IndexSourceType.LessonType.getName()) || relation.getResourceTargetType()
                                                                                                                 .equals(IndexSourceType.InstructionalObjectiveType.getName()))) {
                if (erm.getOrderNum() != null) {
                    // 判断orderNum是否重复
                    boolean isRepeat = false;
                    for (ResourceRelationResultModel rr : list4Total) {
                        if ((rr.getResourceTargetType().equals(relation.getResourceTargetType())) && (rr.getOrderNum() != null && erm.getOrderNum() != null
                                && rr.getOrderNum().intValue() == erm.getOrderNum().intValue())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    
                    if (isRepeat) {

                        LOG.error("orderNum不允许重复");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      LifeCircleErrorMessageMapper.OrderNumError.getCode(),
                                                      "orderNum不允许重复");
                    } else {
                        relation.setOrderNum(erm.getOrderNum());
                    }
                } else {// 策略:orderNum取已存在的 MAX+1
                        // 取到orderNum最大值
                    int maxOrderNum = 0;
                    if (orderNumMap.get(erm.getSource()) != null) {
                        maxOrderNum = orderNumMap.get(erm.getSource());
                        if(CollectionUtils.isEmpty(list4Total)){
                            maxOrderNum += 1;
                        }
                    }
                    if (CollectionUtils.isNotEmpty(list4Total)) {
                        for (ResourceRelationResultModel rr : list4Total) {
                            if (rr.getOrderNum() != null) {
                                if ((rr.getResourceTargetType().equals(relation.getResourceTargetType()))
                                        && (rr.getOrderNum() > maxOrderNum)) {
                                    maxOrderNum = rr.getOrderNum();
                                }
                            }
                        }
                        maxOrderNum += 1;
                    }
                    relation.setOrderNum(maxOrderNum);
                }
                if (orderNumMap.get(erm.getSource()) == null
                        || relation.getOrderNum() > orderNumMap.get(erm.getSource())) {
                    orderNumMap.put(erm.getSource(), relation.getOrderNum());
                }
            }

            // 新增源资源与目标资源的创建时间
            relation.setResourceCreateTime(((Education) resourceEntity).getDbcreateTime());
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
        if(notifyFlag){
            //add by xiezy - 2016.04.15
            //异步通知智能出题
            if(!isCreateWithResource && !haveExist){
            	if(CollectionUtils.isEmpty(resourceRelations)){
            		for(ResourceRelation rr : resourceRelations){
                        notifyService.asynNotify4Relation(rr.getResType(), rr.getSourceUuid(), rr.getResourceTargetType(), rr.getTarget());
            		}
            	}
            }
            
            //通知报表系统 add by xuzy 20160511
            nrs.addResourceRelation(resourceRelations);
        }


        // 处理返回结果
        if (!isCreateWithResource) {
            if (CollectionUtils.isNotEmpty(resourceRelations)) {
                for (ResourceRelation resourceRelation : resourceRelations) {
                    EducationRelationModel model = new EducationRelationModel();
                    model.setIdentifier(resourceRelation.getIdentifier());
                    model.setSource(resourceRelation.getSourceUuid());
                    if (resourceEntity != null) {
                        model.setSourceTitle(resourceEntity.getTitle());
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
        EspEntity resourceEntity = resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);
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
        // 新增源资源与目标资源的创建时间
        relation.setResourceCreateTime(resourceRelation.getResourceCreateTime());
        relation.setTargetCreateTime(resourceRelation.getTargetCreateTime());
        
        // 当relation.getResourceTargetType()为课时或者教学目标时对orderNum进行非重复判断,其他不做处理
        if ((relation.getResType().equals(IndexSourceType.ChapterType.getName())
                || relation.getResType().equals(IndexSourceType.LessonType.getName()) || relation.getResType()
                                                                                                 .equals(IndexSourceType.InstructionalObjectiveType.getName()))
                && (relation.getResourceTargetType().equals(IndexSourceType.LessonType.getName()) || relation.getResourceTargetType()
                                                                                                             .equals(IndexSourceType.InstructionalObjectiveType.getName()))) {
            // 查出所有的order
            List<ResourceRelationResultModel> list4OrderNum = educationRelationDao.getResourceRelations(relation.getSourceUuid(),
                                                                                             relation.getResType(),
                                                                                             relation.getResourceTargetType());

            // 判断orderNum是否重复
            boolean isRepeat = false;
            for (ResourceRelationResultModel rr : list4OrderNum) {
                if (rr.getOrderNum() != null && educationRelationModel.getOrderNum() != null
                        && rr.getOrderNum().intValue() == educationRelationModel.getOrderNum().intValue()) {
                    isRepeat = true;
                    break;
                }
            }

            if (isRepeat) {

                LOG.error("orderNum不允许重复");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.OrderNumError.getCode(),
                                              "orderNum不允许重复");
            } else {
                relation.setOrderNum(educationRelationModel.getOrderNum());
            }
        }

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
        if (resourceEntity != null) {
            model.setSourceTitle(resourceEntity.getTitle());
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
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        List<ResourceRelation> notifyRelationList = new ArrayList<ResourceRelation>();
        notifyRelationList.add(rt);
        notifyService.asynNotify4RelationOnDelete(notifyRelationList);
        
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
        List<ResourceRelation> relations = null;

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
            if(CollectionUtils.isEmpty(relationsTotal)){
                return true;
            }

            //更新资源关系
            resourceRelationRepository.batchAdd(relationsTotal);
        } catch (EspStoreException e) {

            LOG.error("批量更新资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateBatchRelationFail.getCode(),
                                          e.getMessage());
        }
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4RelationOnDelete(relationsTotal);
        
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
        List<ResourceRelation> relations = null;

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
            if(CollectionUtils.isEmpty(relationsTotal)){
                return true;
            }

            // 更新资源关系
            resourceRelationRepository.batchAdd(relationsTotal);
        } catch (EspStoreException e) {

            LOG.error("批量更新资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateBatchRelationFail.getCode(),
                                          e.getMessage());
        }
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4RelationOnDelete(relationsTotal);
        
        //通知报表系统 add by xuzy 20160511
        nrs.deleteResourceRelation(relationsTotal);
        
        return true;
    }

    @Override
    public List<List<RelationForPathViewModel>> getRelationsByConditions(String resType,
                                                                         String sourceUuid,
                                                                         List<String> relationPath,
                                                                         boolean reverse,
                                                                         String categoryPattern) {
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);

        // 返回的结果集
        List<List<RelationForPathViewModel>> result = new ArrayList<List<RelationForPathViewModel>>();

        /*
         * 1.查找resType与课时的资源关系,找到课时的id集合
         */
        // 用于存放查找到的资源关系
        List<ResourceRelation> relations4Lessons = null;
        // 用于存放课时id
        List<String> lessonIds = new ArrayList<String>();
        // 入参
        ResourceRelation resourceRelation4Lessons = new ResourceRelation();
        if (reverse) {// 进行反向查询 Target->Source
            resourceRelation4Lessons.setTarget(sourceUuid);
            resourceRelation4Lessons.setResourceTargetType(resType);
            // resourceRelation4Lessons.setResType(relationPath.get(0));//relationPath.get(0) == lessons
            resourceRelation4Lessons.setResType("lessons");
        } else {// 不进行反向查询 Source->Target
            resourceRelation4Lessons.setSourceUuid(sourceUuid);
            resourceRelation4Lessons.setResType(resType);
            // resourceRelation4Lessons.setResourceTargetType(relationPath.get(0));
            resourceRelation4Lessons.setResourceTargetType("lessons");
        }

        try {
            // 调用SDK,查找resType与课时的资源关系
            relations4Lessons = resourceRelationRepository.getAllByExample(resourceRelation4Lessons);

            // 找出所有的课时id
            if (CollectionUtils.isNotEmpty(relations4Lessons)) {
                for (ResourceRelation rr : relations4Lessons) {
                    lessonIds.add(reverse ? rr.getSourceUuid() : rr.getTarget());
                }
            }
        } catch (EspStoreException e) {

            LOG.error("根据条件获取资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.GetRelationByTargetTypeFail.getCode(),
                                          e.getMessage());
        }

        // 如果resType没有对应的课时,返回空集合
        if (lessonIds.size() == 0) {
            return new ArrayList<List<RelationForPathViewModel>>();
        }

        /*
         * 2.查询课时与教材章节间的关系,获得教材章节的id
         */
        for (String lessonId : lessonIds) {
            // 用于存放查找到的资源关系
            List<ResourceRelation> relations4Materials = null;
            // 用于存放教材章节的id
            List<String> chapterIds = new ArrayList<String>();
            // 入参
            ResourceRelation resourceRelation4Materials = new ResourceRelation();
            if (reverse) {// 进行反向查询 Target->Source
                resourceRelation4Materials.setTarget(lessonId);
                resourceRelation4Materials.setResourceTargetType(relationPath.get(0));
                // resourceRelation4Materials.setResType(relationPath.get(1));//relationPath.get(1) == teachingmaterials
                resourceRelation4Materials.setResType("chapters");
            } else {// 不进行反向查询 Source->Target
                resourceRelation4Materials.setSourceUuid(lessonId);
                resourceRelation4Materials.setResType(relationPath.get(0));
                // resourceRelation4Materials.setResourceTargetType(relationPath.get(1));
                resourceRelation4Materials.setResourceTargetType("chapters");
            }
            try {
                // 调用SDK,查询课时与教材章节间的关系
                relations4Materials = resourceRelationRepository.getAllByExample(resourceRelation4Materials);

                // 找出所有的教材章节id
                if (CollectionUtils.isNotEmpty(relations4Materials)) {
                    for (ResourceRelation rr : relations4Materials) {
                        chapterIds.add(reverse ? rr.getSourceUuid() : rr.getTarget());
                    }
                }
            } catch (EspStoreException e) {

                LOG.error("根据条件获取资源关系失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.GetRelationByTargetTypeFail.getCode(),
                                              e.getMessage());
            }
            // 如果课时没有对应的教材章节,返回空集合
            if (chapterIds.size() == 0) {
                return new ArrayList<List<RelationForPathViewModel>>();
            }

            // 返回结果的元素
            List<RelationForPathViewModel> item = new ArrayList<RelationForPathViewModel>();

            /*
             * 3. 1)查找教材和章节父节点 2)处理返回结果
             */
            boolean isGetDetailSuccess = true;
            for (String chapterId : chapterIds) {
                try {
                    // 获取章节详细
                    Chapter chapter = chapterRepository.get(chapterId);
                    if (chapter == null || chapter.getEnable() == null || !chapter.getEnable()
                            || !ResourceNdCode.chapters.toString().equals(chapter.getPrimaryCategory())) {//章节不存在
                        isGetDetailSuccess = false;
                        continue;
                    }
                    
                    // 获取教材详细
                    String mid = chapter.getTeachingMaterial();
                    TeachingMaterial teachingMaterial = this.getDetail(mid);
                    if (teachingMaterial == null) {// 教材不存在
                        isGetDetailSuccess = false;
                        continue;
                    }
                    
                    ResourceCategory example = new ResourceCategory();
                    example.setResource(mid);
                    List<ResourceCategory> resourceCategories = resourceCategoryRepository.getAllByExample(example);
                    if(CollectionUtils.isEmpty(resourceCategories)){
                        isGetDetailSuccess = false;
                        continue;
                    }
                    List<String> categoryCodes = null;
                    for (ResourceCategory resourceCategory : resourceCategories) {
                        if(StringUtils.isNotEmpty(resourceCategory.getTaxonpath())){
                            String taxonpath = resourceCategory.getTaxonpath();
                            categoryCodes = Arrays.asList(taxonpath.split("/"));
                            break;
                        }
                    }
                    
                    if (CollectionUtils.isNotEmpty(categoryCodes)) {
                        for (String path : categoryCodes) {
                            if (StringUtils.isEmpty(path)) {
                                isGetDetailSuccess = false;
                                break;
                            }
                        }
                    } else {
                        isGetDetailSuccess = false;
                    }

                    if(!isGetDetailSuccess){
                        continue;
                    }
                    
                    // nd_code集合
                    List<String> ndCodes = new ArrayList<String>();
                    
                    if (categoryCodes.size() > 1) {
                        ndCodes.add(categoryCodes.get(1));
                    }
                    if (categoryCodes.size() > 2) {
                        ndCodes.add(categoryCodes.get(2));
                    }
                    if (categoryCodes.size() > 3) {
                        ndCodes.add(categoryCodes.get(3));
                    }
                    if (categoryCodes.size() > 4) {
                        ndCodes.add(categoryCodes.get(4));
                    }
                    if (categoryCodes.size() > 5) {
                        ndCodes.add(categoryCodes.get(5));
                    }
                    

                    // 层级计数
                    int levelCount = 1;

                    // 调用SDK,通过ndCode批量获取分类维度数据
                    List<CategoryData> categoryDatas = categoryDataRepository.getListWhereInCondition("ndCode", ndCodes);

                    if (CollectionUtils.isEmpty(categoryDatas) || categoryDatas.size() != ndCodes.size()) {
                        isGetDetailSuccess = false;
                        continue;
                    }

                    // 返回结果1,教材维度块整理
                    for (CategoryData categoryData : categoryDatas) {
                        RelationForPathViewModel viewModel = new RelationForPathViewModel();
                        viewModel.setIdentifier("");
                        viewModel.setNdCode(categoryData.getNdCode());
                        viewModel.setTitle(categoryData.getTitle());
                        viewModel.setLevel(levelCount);
                        levelCount++;
                        item.add(viewModel);
                    }

                    List<Chapter> chapterList = chapterDao.getParents(mid, chapter.getLeft(), chapter.getRight());

                    // 返回结果2,章节块整理
                    for (Chapter mcvm : chapterList) {
                        ChapterModel chapterModel = chapterServiceV06.getChapterDetail(mcvm.getIdentifier());
                        if (chapterModel == null) {// 章节不存在
                            isGetDetailSuccess = false;
                            break;
                        }
                        RelationForPathViewModel viewModel = new RelationForPathViewModel();
                        viewModel.setIdentifier(chapterModel.getIdentifier());
                        viewModel.setNdCode("");
                        viewModel.setTitle(chapterModel.getTitle());
                        viewModel.setLevel(levelCount);
                        levelCount++;
                        item.add(viewModel);
                    }

                    // 返回结果3,课时块整理
                    Lesson return4Lesson = lessonRepository.get(lessonId);
                    if (return4Lesson != null
                            && ResourceNdCode.lessons.toString().equals(return4Lesson.getPrimaryCategory())
                            && (return4Lesson.getEnable() == null || return4Lesson.getEnable())) {
                        RelationForPathViewModel viewModel = new RelationForPathViewModel();
                        viewModel.setIdentifier(return4Lesson.getIdentifier());
                        viewModel.setNdCode("");
                        viewModel.setTitle(return4Lesson.getTitle());
                        viewModel.setLevel(levelCount);
                        item.add(viewModel);
                    } else {
                        isGetDetailSuccess = false;
                    }

                } catch (EspStoreException e) {

                    LOG.error("获取资源之间的关系失败");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.GetRelationsFail.getCode(),
                                                  e.getMessage());
                }
            }

            if (isGetDetailSuccess) {// 整条线完整,才返回
                result.add(item);
            }

            isGetDetailSuccess = true;
        }

        return result;
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

    @Override
    public ListViewModel<RelationForQueryViewModel> queryListByResTypeByDB(String resType,String sourceUuid, String categories,
            String targetType,String relationType,String limit,boolean reverse,
            String coverage){
        return queryListByResTypeByDB(resType, sourceUuid, categories, targetType, null, null, relationType, limit, reverse, coverage);
    }
    
    @Override
    public ListViewModel<RelationForQueryViewModel> queryListByResTypeByDB(String resType,String sourceUuid, String categories,
            String targetType,String label, String tags, String relationType,String limit,boolean reverse,
            String coverage){
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);
        
        List<String> sourceUuids = new ArrayList<String>();
        sourceUuids.add(sourceUuid);
        
        // 返回的结果集
        ListViewModel<RelationForQueryViewModel> listViewModel = educationRelationDao.queryResByRelation(resType,
                                                                                                         sourceUuids,
                                                                                                         categories,
                                                                                                         targetType,
                                                                                                         label,
                                                                                                         tags,
                                                                                                         relationType,
                                                                                                         limit,
                                                                                                         reverse,
                                                                                                         coverage);
        
        return listViewModel;
    }
    
    @Override
    public ListViewModel<RelationForQueryViewModel> recursionQueryResourcesByDB(String resType,
                                                                                String sourceUuid,
                                                                                String categories,
                                                                                String targetType,
                                                                                String label,
                                                                                String tags,
                                                                                String relationType,
                                                                                String limit,
                                                                                String coverage) throws EspStoreException {
        // 判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, sourceUuid, ResourceType.RESOURCE_SOURCE);

        /*
         * 递归查询sourceUuid对应章节下的子章节,得到chapterIds(包含sourceUuid)
         */
        List<String> chapterIds = new ArrayList<String>();
        
        // 获取章节对象,目的是为了得到教材id
        Chapter chapter = chapterRepository.get(sourceUuid);
        
        // 递归查询所有子章节
        List<Chapter> children = chapterDao.getSubTreeByLeftAndRight(chapter.getTeachingMaterial(), chapter.getLeft(), chapter.getRight());
        
        // 得到所有需求的章节ids
        if (CollectionUtils.isEmpty(children)) {
            chapterIds.add(sourceUuid);
        } else {
            for (Chapter child : children) {
                if (child.getEnable()) {
                    chapterIds.add(child.getIdentifier());
                }
            }
        }

        // 返回的结果集
        // 此处reverse设置为false, 不需要反转
        ListViewModel<RelationForQueryViewModel> listViewModel = educationRelationDao.queryResByRelation(resType,
                                                                                                         chapterIds,
                                                                                                         categories,
                                                                                                         targetType,
                                                                                                         label,
                                                                                                         tags,
                                                                                                         relationType,
                                                                                                         limit,
                                                                                                         false,
                                                                                                         coverage);
        
        return listViewModel;
    }
    
    private TeachingMaterial getDetail(String id) {
        TeachingMaterial teachingMaterial;
        try {
            teachingMaterial = teachingMaterialRepository.get(id);
        } catch (EspStoreException e) {
            
            LOG.error("资源关系V0.6---获取教材详细出错", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.GetDetailTeachingMaterialFail.getCode(),
                                          e.getLocalizedMessage());
        }
        if (teachingMaterial == null
                || !ResourceNdCode.teachingmaterials.toString().equals(teachingMaterial.getPrimaryCategory())
                || (teachingMaterial.getEnable() != null && !teachingMaterial.getEnable())) {
            return null;
        }

        return teachingMaterial;
    }

    @Override
     public  ListViewModel<RelationForQueryViewModel> batchQueryResourcesByDB(String resType, Set<String> sids, String targetType, String relationType,
          String limit) {
        // 返回的结果集

        
        return this.batchQueryResourcesByDB(resType, sids, targetType, null, null, relationType, limit, false);
    }

    @Override
    public ListViewModel<RelationForQueryViewModel> batchQueryResourcesByDB(String resType,
                                                                            Set<String> sids,
                                                                            String targetType,
                                                                            String label,
                                                                            String tags,
                                                                            String relationType,
                                                                            String limit,
                                                                            boolean reverse) {
        // 返回的结果集
        ListViewModel<RelationForQueryViewModel> listViewModel = educationRelationDao.queryResByRelation(resType,
                                                                                                         new ArrayList<String>(sids),
                                                                                                         null,
                                                                                                         targetType,
                                                                                                         label,
                                                                                                         tags,
                                                                                                         relationType,
                                                                                                         limit,
                                                                                                         reverse,
                                                                                                         null);
        return listViewModel;

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

}
