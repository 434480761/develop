package nd.esp.service.lifecycle.services.knowledges.v06.impls;

import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.v06.ChapterKnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeRelationsModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.common.KnowledgeRelationType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.KnowledgeRelation;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.services.knowledges.v06.KnowledgeService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeService;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 知识点业务service层
 * 
 * @author caocr
 */
@Service("knowledgeServiceV06")
@Transactional
public class KnowledgesServiceImpl implements KnowledgeService {
    private static final Logger LOG = LoggerFactory.getLogger(KnowledgesServiceImpl.class);
    private static final int CREATE_TYPE = 0;// 新增知识点操作
    @Autowired
    private TreeService treeService;
    @Autowired
    private ChapterRepository knowledgeRepository;

    @Autowired
    private KnowledgeRelationRepository knowledgeRelationRepository;

    @Autowired
    private ResourceRelationRepository resourceRelationRepository;

    @Autowired
    private ChapterDao chapterDao;
    
    @Autowired
    private NDResourceService ndResourceService;

    @Autowired
    private JdbcTemplate jt;
    @Autowired
    private CommonServiceHelper commonServiceHelper;

    @Override
    public KnowledgeModel createKnowledge(KnowledgeModel model) {
        // 检查title是否重复
        if (isExistKnowledgeTitle(model.getTitle())) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.checkResKnowledgeTitleUniqueFail);
        }
        // knowledge_relations属性处理
        dealKnowledgeRelations(model, CREATE_TYPE);
        
        return (KnowledgeModel) ndResourceService.create(ResourceNdCode.knowledges.toString(), model);
    }
    
    @Override
    public KnowledgeModel updateKnowledge(KnowledgeModel model) {
        Education oldBean;
        try {
            oldBean = (Education) commonServiceHelper.getRepository(ResourceNdCode.knowledges.toString()).get(model.getIdentifier());
        } catch (EspStoreException e) {
            LOG.error("调用存储SDK出错了", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        String newTitle = model.getTitle();
        if (oldBean != null &&
                !org.apache.commons.lang3.StringUtils.equals(newTitle, oldBean.getTitle())) {
            // 检查title是否重复
            if (isExistKnowledgeTitle(newTitle)) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.checkResKnowledgeTitleUniqueFail);
            }
        }
        return (KnowledgeModel) ndResourceService.update(ResourceNdCode.knowledges.toString(), model);
    }

    @Override
    public boolean isExistKnowledgeTitle(String title) {
        String sql = "SELECT identifier FROM `ndresource` WHERE enable = 1 AND primary_category=? AND title=?";
        List<String> idList = jt.queryForList(sql, new Object[]{IndexSourceType.KnowledgeType.getName(), title}, String.class);
        return idList.size() > 0;
    }
    
    @Override
    public void isHaveChildrens(String parent){
        //1.判断知识点是否存在
        Chapter knowledge = null;
        try {
            knowledge = knowledgeRepository.get(parent);
        } catch (EspStoreException e) {
            
            LOG.error("知识点V06刪除知识点---获取知识点详情失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }
        if(knowledge == null || !knowledge.getEnable()){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.KnowledgeNotFound);
        }
        
        Chapter knowledge2 = new Chapter();
        knowledge2.setParent(parent);
        knowledge2.setEnable(true);
        knowledge2.setPrimaryCategory("knowledges");
        List<Chapter> knowledges = null;
        try {
             knowledges = knowledgeRepository.getAllByExample(knowledge2);
            
            if(CollectionUtils.isNotEmpty(knowledges)){
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.KnowledgeHaveChildrens);
            }
        } catch (EspStoreException e) {
            
            LOG.error("知识点V0.6---批量获取知识点详细出错", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        
    }
    
    @Override
    public void moveKnowledge(String kid, KnowledgeModel knowledgeModel) {
        //1.判断知识点是否存在
        Chapter knowledge = chapterDao.getChapterFromSdk(ResourceNdCode.knowledges.toString(),null,kid); 
        if (knowledge == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.KnowledgeNotFound);
        }
        
        TreeDirection treeDirection = TreeDirection.fromString(knowledgeModel.getExtProperties().getDirection());

        
        
        TreeModel current = new TreeModel(knowledge);
        
        //3.补全chapterModel所需的字段
        knowledgeModel.setIdentifier(kid);
        
        treeService.moveSubTree(ndResourceService.getTreeTargetAndParent(knowledgeModel,knowledge), current, treeDirection);        
//        if(LOG.isInfoEnabled()){
//            LOG.info("知识点V0.6---移动知识点成功，subjectCode:{},kid:{}", getSubjectNdCode(list), kid);
//        }
    }

//    /**
//     * @author linsm
//     * @param model
//     * @param knowledge
//     * @since 
//     */
//    private TreeTrargetAndParentModel getTreeTargetAndParent(KnowledgeModel knowledgeModel, Chapter knowledge) {
//        TreeTrargetAndParentModel model = new TreeTrargetAndParentModel();
//        //target and parent
//      String mid = knowledge.getTeachingMaterial();
//      
//      if(StringUtils.isNotEmpty(knowledgeModel.getExtProperties().getTarget())){
//          Chapter targetChapter = chapterDao.getChapterFromSdkWithCheck(ResourceNdCode.knowledges.toString(),mid,knowledgeModel.getExtProperties().getTarget());
//          model.setTarget(new TreeModel(targetChapter));
//      }else if(StringUtils.isNotEmpty(knowledgeModel.getExtProperties().getParent())){
//          if(!knowledgeModel.getExtProperties().getParent().equals("ROOT")){
//              Chapter parentKnowledge = chapterDao.getChapterFromSdkWithCheck(ResourceNdCode.knowledges.toString(),mid,knowledgeModel.getExtProperties().getParent());
//              model.setParent(new TreeModel(parentKnowledge));
//          }else{
//            //root 默认为mid
//              
//              int nodeNum = (int)chapterDao.countQueryChapterList(mid, null);
//              //虚根
//              TreeModel parent = new TreeModel();
//              parent.setLeft(0);
//              parent.setRight(nodeNum*2+1);
//              parent.setRoot(mid);
//              parent.setParent(null);
//              parent.setIdentifier(mid);
//              model.setParent(parent);
//          }
//      }else{
//          throw new WafException(LifeCircleErrorMessageMapper.KnowledgeCheckParamFail.getCode(),
//                               LifeCircleErrorMessageMapper.KnowledgeCheckParamFail.getMessage());
//      }
//        return model;
//        
//    }

    /**
     * knowledge_relations逻辑处理
     * 
     * @param model 知识点
     * @param type 业务类型
     * @since
     */
    private boolean dealKnowledgeRelations(KnowledgeModel model, int type) {
        List<KnowledgeRelationsModel> knowledgeRelations = model.getKnowledgeRelations();
        if (knowledgeRelations != null && !knowledgeRelations.isEmpty()) {
            List<KnowledgeRelation> list = new ArrayList<KnowledgeRelation>();
            for (KnowledgeRelationsModel knowledgeRelation : knowledgeRelations) {
                KnowledgeRelation relation = new KnowledgeRelation();
                relation.setIdentifier(UUID.randomUUID().toString());
                relation.setSource(knowledgeRelation.getSource());
                relation.setTarget(knowledgeRelation.getTarget());
                relation.setRelationType(knowledgeRelation.getRelationType());
                relation.setContextType(knowledgeRelation.getContextType());
                relation.setContextObject(knowledgeRelation.getContextObject());

                try {
                    Chapter knowledge = knowledgeRepository.get(knowledgeRelation.getSource());
                    boolean modelFlag = false;
                    if (knowledge != null && knowledge.getEnable()) {
                        knowledge = knowledgeRepository.get(knowledgeRelation.getTarget());
                        if (knowledge != null && knowledge.getEnable()) {
                            modelFlag = true;
                        } else {
                            LOG.error("知识点V06---知识点未找到" + knowledgeRelation.getTarget());
                        }
                    } else {
                        LOG.error("知识点V06---知识点未找到" + knowledgeRelation.getSource());
                    }
                    if (!modelFlag) {
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      LifeCircleErrorMessageMapper.KnowledgeNotFound);
                    } else {
                        list.add(relation);
                    }
                } catch (EspStoreException e) {

                    LOG.error("知识点V06---添加知识点关系检查关系中的资源错误", e);

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  e.getLocalizedMessage());
                }
            }
            if (!list.isEmpty()) {
                try {
                    knowledgeRelationRepository.batchAdd(list);
                } catch (EspStoreException e) {

                    LOG.error("知识点V06---知识点调用批量添加关系出错", e);

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  e.getLocalizedMessage());
                }
            }
        }

        return true;

    }

    @Override
    public KnowledgeRelationsModel addKnowledgeRelation(KnowledgeRelationsModel knowledgeRelation) {
        try {
            Chapter knowledge = knowledgeRepository.get(knowledgeRelation.getSource());
            boolean modelFlag = false;
            if (knowledge != null && knowledge.getEnable()) {
                knowledge = knowledgeRepository.get(knowledgeRelation.getTarget());
                if (knowledge != null && knowledge.getEnable()) {
                    modelFlag = true;
                } else {
                    LOG.error("知识点V06---知识点未找到" + knowledgeRelation.getTarget());
                }
            } else {
                LOG.error("知识点V06---知识点未找到" + knowledgeRelation.getSource());
            }
            if (!modelFlag) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.KnowledgeNotFound);
            }
        } catch (EspStoreException e) {

            LOG.error("知识点V06---添加知识点关系检查关系中的资源错误", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }

        KnowledgeRelation relation = BeanMapperUtils.beanMapper(knowledgeRelation, KnowledgeRelation.class);

        try {
            relation = knowledgeRelationRepository.add(relation);
        } catch (EspStoreException e) {
            LOG.error("知识点V06---添加知识点关系错误", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }

        return BeanMapperUtils.beanMapper(relation, KnowledgeRelationsModel.class);
    }

    @Override
    public List<KnowledgeRelationsModel> getKnowledgeRelations(String contexttype,
                                                               String relationtype,
                                                               String contextobjectid,
                                                               String knowledgeId) {
        List<KnowledgeRelationsModel> list = new ArrayList<KnowledgeRelationsModel>();

        try {
            KnowledgeRelationType temp = null;
            if (StringUtils.hasText(relationtype)) {
                for (KnowledgeRelationType type : KnowledgeRelationType.values()) {
                    if (Integer.valueOf(type.getValue()).equals(Integer.valueOf(relationtype))) {
                        temp = type;
                        break;
                    }
                }
            }

            KnowledgeRelation entity = new KnowledgeRelation();
            entity.setContextType(contexttype);
            entity.setContextObject(contextobjectid);
            entity.setSource(knowledgeId);
            if (temp != null) {
                entity.setRelationType(String.valueOf(temp.getValue()));
            }
            List<KnowledgeRelation> knowledgeRelations = knowledgeRelationRepository.getAllByExample(entity);
            List<String> knowledgeIds = null;
            if (CollectionUtils.isNotEmpty(knowledgeRelations)) {
                knowledgeIds = new ArrayList<String>();
                for (KnowledgeRelation ch : knowledgeRelations) {
                    knowledgeIds.add(ch.getSource());
                    knowledgeIds.add(ch.getTarget());
                }
                Map<String, List<ResClassificationModel>> categoriesMap = new HashMap<String, List<ResClassificationModel>>();
                Map<String, Chapter> knowledgeMap = new HashMap<String, Chapter>();
                if (CollectionUtils.isNotEmpty(knowledgeIds)) {
                    List<Chapter> knowledges = knowledgeRepository.getAll(knowledgeIds);
                    List<ResourceCategory> resourceCategories = chapterDao.batchGetCategories(knowledgeIds);
                    List<ResClassificationModel> resClassificationModels = null;
                    // 是否可以用map???存在相同的知识点关联怎么办？
                    if (CollectionUtils.isNotEmpty(knowledges)) {
                        for (Chapter knowledge : knowledges) {
                            knowledgeMap.put(knowledge.getIdentifier(), knowledge);
                            if (CollectionUtils.isNotEmpty(resourceCategories)) {
                                resClassificationModels = new ArrayList<ResClassificationModel>();
                                for (ResourceCategory resourceCategory : resourceCategories) {
                                    if ((knowledge.getIdentifier()).equals(resourceCategory.getResource())) {
                                        ResClassificationModel model = BeanMapperUtils.beanMapper(resourceCategory,
                                                                                                  ResClassificationModel.class);
                                        resClassificationModels.add(model);
                                    }
                                }
                                categoriesMap.put(knowledge.getIdentifier(), resClassificationModels);
                            }
                        }
                    }
                }

                for (KnowledgeRelation knowledgeRelation : knowledgeRelations) {
                    KnowledgeRelationsModel relationsModel = new KnowledgeRelationsModel();
                    relationsModel.setIdentifier(knowledgeRelation.getIdentifier());
                    relationsModel.setContextObject(knowledgeRelation.getContextObject());
                    relationsModel.setContextType(knowledgeRelation.getContextType());
                    relationsModel.setRelationType(knowledgeRelation.getRelationType());
                    // 查看知识点关联，如果source或者target找不到怎么办,暂时先不处理
                    if (knowledgeMap.get(knowledgeRelation.getSource()) != null
                            && knowledgeMap.get(knowledgeRelation.getSource()).getEnable()) {
                        KnowledgeModel source = CommonHelper.convertModelOut(knowledgeMap.get(knowledgeRelation.getSource()),
                                                                             KnowledgeModel.class);
                        source.setCategoryList(categoriesMap.get(knowledgeRelation.getSource()));
                        relationsModel.setSourceKnowledgeModel(source);
                    } else {
                        relationsModel.setSourceKnowledgeModel(null);
                    }

                    if (knowledgeMap.get(knowledgeRelation.getTarget()) != null
                            && knowledgeMap.get(knowledgeRelation.getTarget()).getEnable()) {
                        KnowledgeModel target = CommonHelper.convertModelOut(knowledgeMap.get(knowledgeRelation.getTarget()),
                                                                             KnowledgeModel.class);
                        target.setCategoryList(categoriesMap.get(knowledgeRelation.getTarget()));
                        relationsModel.setTargetKnowledgeModel(target);
                    } else {
                        relationsModel.setTargetKnowledgeModel(null);
                    }

                    // 不需要显示
                    relationsModel.setSource(null);
                    relationsModel.setTarget(null);
                    list.add(relationsModel);
                }
            }

        } catch (EspStoreException e) {

            LOG.error("知识点关系V06---查询知识关系列表或者知识点列表失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());

        }

        return list;
    }

    @Override
    public void deleteKnowledgeRelation(String id) {
        try {
            knowledgeRelationRepository.del(id);
        } catch (EspStoreException e) {

            LOG.error("删除知识点关联失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }

    }

    @Override
    public List<ChapterKnowledgeModel> addBatchChapterKnowledges(List<ChapterKnowledgeModel> models) {
        List<ResourceRelation> chapterKnowledges = new ArrayList<ResourceRelation>();
        ResourceRelation chapterKnowledge = null;
        for (ChapterKnowledgeModel model : models) {
            chapterKnowledge = new ResourceRelation();
//            chapterKnowledge.setEnable(true);
            chapterKnowledge.setResType(IndexSourceType.ChapterType.getName());
            chapterKnowledge.setSourceUuid(model.getOutline());
            chapterKnowledge.setRelationType("ASSOCIATE");
            chapterKnowledge.setResourceTargetType(IndexSourceType.KnowledgeType.getName());
            chapterKnowledge.setTarget(model.getKnowledge());
            ResourceRelation rt;
            try {
                rt = resourceRelationRepository.getByExample(chapterKnowledge);
            } catch (EspStoreException e) {
                LOG.error("知识点标签V06---批量添加知识点标签:获取章节知识点关系失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getLocalizedMessage());
            }
            if(null != rt && rt.getEnable()) {
//                chapterKnowledge.setIdentifier(rt.getIdentifier());
                Set<String> tags = new HashSet<String>();
                if(rt.getTags()!=null) {
                    tags.addAll(rt.getTags());
                }
                if(model.getTags()!=null) {
                    tags.addAll(model.getTags());
                }
//                chapterKnowledge.setTags(new LinkedList<String>(tags));
                rt.setTags(new LinkedList<String>(tags));
            } else {
                LOG.error("知识点标签V06---批量添加知识点标签:章节知识点关系未找到");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.ResourceRelationNotExist.getCode(),
                                              LifeCircleErrorMessageMapper.ResourceRelationNotExist.getMessage()
                                                      + ", 章节id:" + model.getOutline() + ", 知识点id:"
                                                      + model.getKnowledge());
//                chapterKnowledge.setIdentifier(UUID.randomUUID().toString());
//                chapterKnowledge.setTags(model.getTags());
            }
//            chapterKnowledges.add(chapterKnowledge);
            chapterKnowledges.add(rt);
        }
        try {
            chapterKnowledges = resourceRelationRepository.batchAdd(chapterKnowledges);
        } catch (EspStoreException e) {

            LOG.error("知识点标签V06---批量添加知识点标签失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }
        List<ChapterKnowledgeModel> resultModels = new ArrayList<ChapterKnowledgeModel>();
        ChapterKnowledgeModel resultModel = null;
        for (ResourceRelation ck : chapterKnowledges) {
            resultModel = new ChapterKnowledgeModel();
            resultModel.setIdentifier(ck.getIdentifier());
            resultModel.setOutline(ck.getSourceUuid());
            resultModel.setKnowledge(ck.getTarget());
            resultModel.setTags(ck.getTags());
            resultModels.add(resultModel);
        }
        return resultModels;
    }

    @Override
    public void deleteKnowledgeChapterKnowledge(String id, String tag, String outline) {
        ResourceRelation example = null;
        example = new ResourceRelation();
//        example.setEnable(true);
        example.setResType(IndexSourceType.ChapterType.getName());
        example.setSourceUuid(outline);
        example.setRelationType("ASSOCIATE");
        example.setResourceTargetType(IndexSourceType.KnowledgeType.getName());
        example.setTarget(id);
                
        try {
            ResourceRelation chapterKnowledge = resourceRelationRepository.getByExample(example);
            if(chapterKnowledge == null || !chapterKnowledge.getEnable()) {
                LOG.error("删除知识点标签失败:章节知识点关系未找到");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceRelationNotExist);
            }
            List<String> tags = chapterKnowledge.getTags();
            if (CollectionUtils.isNotEmpty(tags) && tags.contains(tag)) {
                tags.remove(tag);
                chapterKnowledge.setTags(tags);
                resourceRelationRepository.update(chapterKnowledge);
            } else {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.TagNotExist);
            }
        } catch (EspStoreException e) {

            LOG.error("删除知识点标签失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }
    }

}
