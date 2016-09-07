package nd.esp.service.lifecycle.daos.educationrelation.v06.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.v06.ResourceRelationResultModel;
import nd.esp.service.lifecycle.models.v06.ResultModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.EduRedisTemplate;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.google.gson.reflect.TypeToken;

/**
 * 资源关系查询目标资源
 * 
 * @author caocr
 *
 */
@Repository
public class EducationRelationDaoImpl implements EducationRelationDao {
    private final static Logger LOG = LoggerFactory.getLogger(EducationRelationDaoImpl.class);
    
    @Autowired
    private ResourceRelationRepository repository;

    @Autowired
    private ResourceRelation4QuestionDBRepository resourceRelation4QuestionDBRepository;
    
    @Autowired
    private NDResourceDao nDResourceDao;
    
    @PersistenceContext(unitName="entityManagerFactory")
    EntityManager entityManager;
    
    @PersistenceContext(unitName="questionEntityManagerFactory")
    EntityManager entityManager2;
    
    @Autowired
    private EduRedisTemplate<RelationForQueryViewModel> ert;
    
    // redis结果集后缀
    private static String REDIS_RESULT_SUFFIX = "set";
    
    @Override
    public ListViewModel<RelationForQueryViewModel> queryResByRelation(String resType,
                                                                       List<String>  sourceUuids,
                                                                       String categories,
                                                                       String targetType,
                                                                       String label, 
                                                                       String tags, 
                                                                       String relationType,
                                                                       String limit,
                                                                       boolean reverse,
                                                                       String coverage,
                                                                       boolean isPortal) {
        // 获取资源对应的tableName
        String tableName = LifeCircleApplicationInitializer.tablenames_properties.getProperty(targetType);
        
        if (StringUtils.isEmpty(tableName)) {

            LOG.error("源资源类型或者目标资源类型错误");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckTargetTypeError);

        }
        
        // 返回的结果集
        ListViewModel<RelationForQueryViewModel> listViewModel = new ListViewModel<RelationForQueryViewModel>();
        if (CollectionUtils.isEmpty(sourceUuids)) {
            listViewModel.setLimit(limit);
            listViewModel.setTotal(0L);
            listViewModel.setItems(new ArrayList<RelationForQueryViewModel>());
            return listViewModel;
        }
        
        List<RelationForQueryViewModel> resultViewModels = null;
        long resultCount = 0;
        
        if (CommonServiceHelper.isQuestionDb(resType)
                || CommonServiceHelper.isQuestionDb(targetType)) {
            List<RelationForQueryViewModel> results = new ArrayList<RelationForQueryViewModel>();
            resultCount = queryResDataByRelationForQuestion(resType,
                                                            sourceUuids,
                                                            categories,
                                                            targetType,
                                                            label,
                                                            tags,
                                                            relationType,
                                                            limit,
                                                            reverse,
                                                            coverage,
                                                            isPortal,
                                                            results);
            resultViewModels = results;

        } else {
            resultViewModels = queryResDataByRelation(resType,
                                                      sourceUuids,
                                                      categories,
                                                      targetType,
                                                      label,
                                                      tags,
                                                      relationType,
                                                      limit,
                                                      reverse,
                                                      coverage,
                                                      isPortal);
        }
        
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        if (!CommonServiceHelper.isQuestionDb(resType)
                && !CommonServiceHelper.isQuestionDb(targetType)) {
            resultCount = queryResCountByRelation(resType,
                                                  sourceUuids,
                                                  categories,
                                                  targetType,
                                                  label,
                                                  tags,
                                                  relationType,
                                                  reverse,
                                                  coverage,
                                                  result[0].intValue() == 0 ? true : false,
                                                  isPortal);
        }

        listViewModel.setLimit(limit);
        listViewModel.setItems(resultViewModels);
        listViewModel.setTotal(resultCount);
        if (result[0].intValue() == 0 && resultViewModels.size() < result[1]) {
            listViewModel.setTotal(Long.valueOf(resultViewModels.size()));
        }
        
        return listViewModel;
    }
    
    /**
     * 判断是否需要preview
     * <p>Create Time: 2015年10月21日   </p>
     * <p>Create author: caocr   </p>
     * @param resType
     * @return
     */
    private boolean isNeedPreview(final String resType) {
        if (resType.equals(IndexSourceType.InstructionalObjectiveType.getName())
                || resType.equals(IndexSourceType.LessonType.getName())
                || resType.equals(IndexSourceType.KnowledgeType.getName())
                || resType.equals(IndexSourceType.ChapterType.getName())) {
            return false;
        }

        return true;
    }
    
    /**
     * 查询数据
     * 
     * @param resType
     * @param sourceUuid
     * @param categories
     * @param targetType
     * @param relationType
     * @param limit
     * @param reverse
     * @param coverage
     * @param chapterIds
     * @return
     * @since
     */
    private List<RelationForQueryViewModel> queryResDataByRelation(String resType,
                                                                   List<String> sourceUuids,
                                                                   String categories,
                                                                   String targetType,
                                                                   String label,
                                                                   String tags,
                                                                   String relationType,
                                                                   String limit,
                                                                   boolean reverse,
                                                                   String coverage,
                                                                   boolean isPortal) {
        final boolean isNeedPreview = this.isNeedPreview(targetType);
        String commonSelect = "b.identifier AS identifier, b.title AS title, b.description AS description, b.tags AS tags, b.keywords AS keywords, rr1.identifier AS relation_id, rr1.relation_type AS relation_type, rr1.label AS label, rr1.order_num AS order_num, rr1.enable AS enable, rr1.tags AS relation_tags,rr1.resource_target_type AS target_type, rr1.creator AS creator,rr1.estatus AS status, rr1.create_time AS create_time, rr1.last_update AS last_update";
        StringBuffer querySqlTmp =  new StringBuffer(commonSelect);
        if(reverse) {
            querySqlTmp.append(", rr1.target AS source_uuid");
        } else {
            querySqlTmp.append(", rr1.source_uuid AS source_uuid");
        }
        
        if (isNeedPreview) {
            querySqlTmp.append(", b.preview AS preview");
        }
        
        StringBuffer querySql = commonSql(querySqlTmp, targetType, categories, coverage, label, tags, relationType, reverse, isPortal);
        
        querySql.append(") AND rr1.enable = 1 AND rr1.resource_target_type = :targetType AND b.primary_category = :primaryTargetType");
        if (reverse) {
            querySql.append(" AND b.identifier = rr1.source_uuid AND rr1.target IN (:sourceUuids)");
        } else {
            querySql.append(" AND b.identifier = rr1.target AND rr1.source_uuid IN (:sourceUuids)");
        }
        
        if(StringUtils.isNotEmpty(relationType)){
            querySql.append(" AND rr1.relation_type = :relationType");
        }
        
        if(StringUtils.isNotEmpty(label)){
            querySql.append(" AND rr1.label = :label");
        }
        
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                querySql.append(" AND rr1.tags LIKE ").append(":tag").append(i);
                i++;
            }
        }
        
        String sortResourceType = "";
        String sortTargetType = "";
        if (reverse) {
            sortResourceType = targetType;
            sortTargetType = resType;
        } else {
            sortResourceType = resType;
            sortTargetType = targetType;
        }
        
        // 当目标资源为课时和教学目标的时候，要根据order_num排序
        if ((sortResourceType.equals(IndexSourceType.ChapterType.getName())
                || sortResourceType.equals(IndexSourceType.LessonType.getName()) || sortResourceType.equals(IndexSourceType.InstructionalObjectiveType.getName()))
                && (sortTargetType.equals(IndexSourceType.LessonType.getName()) || sortTargetType.equals(IndexSourceType.InstructionalObjectiveType.getName()))) {
            querySql.append(" ORDER BY rr1.order_num ASC");
        } else {
            if (reverse) {
                querySql.append(" ORDER BY rr1.sort_num ASC, rr1.resource_create_time DESC");
            } else {
                querySql.append(" ORDER BY rr1.sort_num ASC, rr1.target_create_time DESC");
            }
        }
        
        // sql的LIMIT
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        querySql.append(" LIMIT ").append(result[0]).append(",").append(result[1]);
        
        //数据查询
        Session session = (Session) entityManager.getDelegate();;
        
        Query queryRes = session.createSQLQuery(querySql.toString()).setResultTransformer(Transformers.aliasToBean(ResultModel.class));
        
        // 参数注入
        sqlParamDeal(queryRes, sourceUuids, resType, targetType, label, tags, relationType, categories, coverage, reverse);
        
        List<ResultModel> resultList = queryRes.list();
        
        List<RelationForQueryViewModel> viewModels = new ArrayList<RelationForQueryViewModel>();

        if (CollectionUtils.isNotEmpty(resultList)) {
            setData(resultList, viewModels, isNeedPreview);
        }

        return viewModels;
    }
    
    /**
     * 查询数据
     * 
     * @param resType
     * @param sourceUuids
     * @param categories
     * @param targetType
     * @param label
     * @param tags
     * @param relationType
     * @param limit
     * @param reverse
     * @param coverage
     * @return
     * @since
     */
    private long queryResDataByRelationForQuestion(String resType,
                                                   List<String> sourceUuids,
                                                   String categories,
                                                   String targetType,
                                                   String label,
                                                   String tags,
                                                   String relationType,
                                                   String limit,
                                                   boolean reverse,
                                                   String coverage,
                                                   boolean isPortal,
                                                   List<RelationForQueryViewModel> viewModels) {
        final boolean isNeedPreview = this.isNeedPreview(targetType);
        
        List<RelationForQueryViewModel> results = new ArrayList<RelationForQueryViewModel>();
        
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        int offset = result[0];
        int num = result[1];
        
        Set<String> resourceUuids = new HashSet<String>();
        
        // 查关系的sql
        String relationSql = relationCommonSqlForQuestion(resType, targetType, label, tags, relationType, reverse);
        
        // 查关系参数注入
        Map<String, Object> relationParams = relationSqlParamDealForQuestion(sourceUuids,
                                                                           resType,
                                                                           targetType,
                                                                           label,
                                                                           tags,
                                                                           relationType,
                                                                           reverse);
        
        // 查数据的sql
        String resourceSql = resourceCommonSqlForQuestion(targetType, categories, coverage, isPortal);

        // 查数据参数注入
        Map<String, Object> resourceParams = resourceSqlParamDealForQuestion(new ArrayList<String>(resourceUuids),
                                                                             targetType,
                                                                             categories,
                                                                             coverage);
        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(relationParams);
        params.putAll(resourceParams);
        
        int hashCode = CommonHelper.getHashCodeKey(relationSql + resourceSql, params);
        String key = hashCode + REDIS_RESULT_SUFFIX;
        
        if (offset != 0) {
            List<RelationForQueryViewModel> cacheResults = getResult(key, limit);
            if(CollectionUtils.isNotEmpty(cacheResults)){
                viewModels.addAll(cacheResults);
                return  ert.zSetCount(key);
            }
        }
        
        //先查关系
        List<ResultModel> relationResultList = queryRelationForQuestion(resType,
                                                                        sourceUuids,
                                                                        targetType,
                                                                        reverse,
                                                                        relationSql,
                                                                        relationParams);
        if (CollectionUtils.isNotEmpty(relationResultList)) {
            for (ResultModel relationResultModel : relationResultList) {
                resourceUuids.add(relationResultModel.getIdentifier());
            }
        } else {
            return 0;
        }
        
        //再查数据
        List<ResultModel> resourceResultList = queryResDataForQuestion(resourceUuids,
                                                                       targetType,
                                                                       resourceSql,
                                                                       resourceParams);
        
        if (CollectionUtils.isNotEmpty(resourceResultList)) {
            setDataForQuestion(relationResultList, resourceResultList, results, isNeedPreview);
        } else {
            return 0;
        }

        saveResult(key, results);
        
        if (offset >= results.size()) {
            return results.size();
        }

        int toIndex = offset + num;
        if (toIndex > results.size()) {
            toIndex = results.size();
        }

        if (CollectionUtils.isNotEmpty(results.subList(offset, toIndex))) {
            viewModels.addAll(results.subList(offset, toIndex));
        }
        
        return results.size();
    }
    
    /**
     * 对于习题先查出所有关系，再查资源
     * 
     * @param resType
     * @param sourceUuids
     * @param targetType
     * @param reverse
     * @return
     * @since
     */
    private List<ResultModel> queryRelationForQuestion(String resType,
                                                       List<String> sourceUuids,
                                                       String targetType,
                                                       boolean reverse,
                                                       String querySql,
                                                       Map<String,Object> params) {
        String queryResType = null;
        if (reverse) {
            queryResType = targetType;
        } else {
            queryResType = resType;
        }
        
        //数据查询
        Session session = null;
        if (CommonServiceHelper.isQuestionDb(queryResType)) {
            session = (Session) entityManager2.getDelegate();
        } else {
            session = (Session) entityManager.getDelegate();
        }
        
        Query queryRes = session.createSQLQuery(querySql).setResultTransformer(Transformers.aliasToBean(ResultModel.class));
        
        // 参数设置
        for (String paramKey : params.keySet()) {
            if (paramKey.equals("sourceUuids")) {
                queryRes.setParameterList("sourceUuids", sourceUuids);
            } else {
                queryRes.setParameter(paramKey, params.get(paramKey));
            }
        }
        
        return queryRes.list();
    }
    
    /**
     * 查数据（习题）
     * 
     * @param sourceUuids
     * @param targetType
     * @param sql
     * @return
     * @since
     */
    private List<ResultModel> queryResDataForQuestion(Set<String> sourceUuids,
                                                      String targetType,
                                                      String querySql,
                                                      Map<String,Object> params) {
        // sql的LIMIT
//        querySql.append(" LIMIT ").append(start).append(",").append(offset);

        // 数据查询
        Session session = null;
        if (CommonServiceHelper.isQuestionDb(targetType)) {
            session = (Session) entityManager2.getDelegate();
        } else {
            session = (Session) entityManager.getDelegate();
        }

        Query queryRes = session.createSQLQuery(querySql.toString())
                                .setResultTransformer(Transformers.aliasToBean(ResultModel.class));

        // 参数设置
        for (String paramKey : params.keySet()) {
            if(paramKey.equals("sourceUuids")){
                queryRes.setParameterList("sourceUuids", sourceUuids);
            } else {
                queryRes.setParameter(paramKey, params.get(paramKey));
            }
        }
        queryRes.setParameter("sourceUuidStr", sourceUuids);

        return queryRes.list();
    }
    
    /**
     * 查询数据记录数
     * 
     * @param resType
     * @param sourceUuids
     * @param categories
     * @param targetType
     * @param relationType
     * @param reverse
     * @param coverage
     * @return
     * @since
     */
    private long queryResCountByRelation(String resType,
                                         List<String> sourceUuids,
                                         String categories,
                                         String targetType,
                                         String label,
                                         String tags,
                                         String relationType,
                                         boolean reverse,
                                         String coverage,
                                         boolean isFirstPage,
                                         boolean isPortal) {
        String commonSelect = "COUNT(rr1.identifier) AS total";
        StringBuffer countSqlTmp =  new StringBuffer(commonSelect);
        StringBuffer countSql = commonSql(countSqlTmp, targetType, categories, coverage, label, tags, relationType, reverse, isPortal);
        
        countSql.append(") AND rr1.enable = 1 AND rr1.resource_target_type = :targetType AND b.primary_category = :primaryTargetType");
        if (reverse) {
            countSql.append(" AND b.identifier = rr1.source_uuid AND rr1.target IN (:sourceUuids)");
        } else {
            countSql.append(" AND b.identifier = rr1.target AND rr1.source_uuid IN (:sourceUuids)");
        }
        
        if(StringUtils.isNotEmpty(relationType)){
            countSql.append(" AND rr1.relation_type = :relationType");
        }
        
        if(StringUtils.isNotEmpty(label)){
            countSql.append(" AND rr1.label = :label");
        }
        
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                countSql.append(" AND rr1.tags LIKE ").append(":tag").append(i);
                i++;
            }
        }

        Map<String, Object> sqlParam = sqlParamDealForCount(sourceUuids, resType, targetType, label, tags, relationType, categories, coverage, reverse);
        DbName dbName = CommonServiceHelper.isQuestionDb(resType) ? DbName.QUESTION : DbName.DEFAULT;
        return nDResourceDao.getResourceQueryCount(countSql.toString(), sqlParam, isFirstPage, dbName);
    }
    
    /**
     *普通sql拼接 
     * 
     * @param stringBuffer
     * @param targetType
     * @param categories
     * @param coverage
     * @param relationType
     * @param reverse
     * @return
     * @since
     */
    private StringBuffer commonSql(StringBuffer stringBuffer,
                                   String targetType,
                                   String categories,
                                   String coverage,
                                   String label,
                                   String tags,
                                   String relationType,
                                   boolean reverse,
                                   boolean isPortal) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(stringBuffer).append(" FROM ndresource b INNER JOIN resource_relations rr1 ON b.identifier IN (SELECT a.identifier FROM ndresource a INNER JOIN resource_relations rr ON (a.primary_category = :primaryTargetType AND rr.res_type = :resType AND rr.resource_target_type = :targetType");
        
        if (reverse) {
            buffer.append(" AND a.identifier = rr.source_uuid AND rr.target IN (:sourceUuids)");
        } else {
            buffer.append(" AND a.identifier = rr.target AND rr.source_uuid IN (:sourceUuids)");
        }
        
        if (StringUtils.isNotEmpty(label)) {
            buffer.append(" AND rr.label = :label");
        }
        
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                buffer.append(" AND rr.tags LIKE ").append(":tag").append(i);
                i++;
            }
        }
        
        if (StringUtils.isNotEmpty(relationType)) {
            buffer.append(" AND rr.relation_type = :relationType");
        }

        buffer.append(" AND a.enable = 1 AND rr.enable = 1)");
        
        Set<String> set = null;
        if(StringUtils.isNotEmpty(categories)){
            set = new HashSet<String>();
            set.addAll(Arrays.asList(categories.split(",")));
            
            // 查询数据维度数据sql拼接
            categorySql(buffer, set);
        }
        
        if (StringUtils.isNotEmpty(coverage)) {
            StringBuffer coverageSql = new StringBuffer(" INNER JOIN res_coverages rcv ON a.identifier = rcv.resource AND rcv.res_type='");
            coverageSql.append(targetType).append("' AND (").append(coverageParam4Sql(coverage)).append(")");
            buffer.append(coverageSql);
        }
        
        if(!isPortal){
        	StringBuffer portalSql = new StringBuffer(" LEFT JOIN res_coverages portal ON a.identifier = portal.resource AND portal.res_type='");
        	portalSql.append(targetType).append("' WHERE (").append(portalParam4Sql()).append(")");
            buffer.append(portalSql);
        }
        
        return buffer;
    }
    
    /**
     *普通sql拼接 (习题)
     * 
     * @param relationType
     * @param reverse
     * @return
     * @since
     */
    private String relationCommonSqlForQuestion(String resType,
                                                      String targetType,
                                                      String label,
                                                      String tags,
                                                      String relationType,
                                                      boolean reverse) {
        String commonSelect = "SELECT rr.identifier AS relation_id, rr.relation_type AS relation_type, rr.label AS label, rr.order_num AS order_num, rr.enable AS enable, rr.tags AS relation_tags,rr.resource_target_type AS target_type, rr.creator AS creator, rr.estatus AS status, rr.create_time AS create_time, rr.last_update AS last_update";
        StringBuffer querySqlTmp = new StringBuffer(commonSelect);
        if (reverse) {
            querySqlTmp.append(", rr.source_uuid AS identifier, rr.target AS source_uuid");
        } else {
            querySqlTmp.append(", rr.target AS identifier, rr.source_uuid AS source_uuid");
        }
        querySqlTmp.append(" FROM resource_relations rr WHERE rr.res_type = :resType AND rr.resource_target_type = :targetType");

        if (reverse) {
            querySqlTmp.append(" AND rr.target IN (:sourceUuids)");
        } else {
            querySqlTmp.append(" AND rr.source_uuid IN (:sourceUuids)");
        }

        if (StringUtils.isNotEmpty(label)) {
            querySqlTmp.append(" AND rr.label = :label");
        }

        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                querySqlTmp.append(" AND rr.tags LIKE ").append(":tag").append(i);
                i++;
            }
        }

        if (StringUtils.isNotEmpty(relationType)) {
            querySqlTmp.append(" AND rr.relation_type = :relationType");
        }

        querySqlTmp.append(" AND rr.enable = 1");
        
        String sortResourceType = "";
        String sortTargetType = "";
        if (reverse) {
            sortResourceType = targetType;
            sortTargetType = resType;
        } else {
            sortResourceType = resType;
            sortTargetType = targetType;
        }
        
        // 当目标资源为课时和教学目标的时候，要根据order_num排序
        if ((sortResourceType.equals(IndexSourceType.ChapterType.getName())
                || sortResourceType.equals(IndexSourceType.LessonType.getName()) || sortResourceType.equals(IndexSourceType.InstructionalObjectiveType.getName()))
                && (sortTargetType.equals(IndexSourceType.LessonType.getName()) || sortTargetType.equals(IndexSourceType.InstructionalObjectiveType.getName()))) {
            querySqlTmp.append(" ORDER BY rr.order_num ASC");
        } else {
            if (reverse) {
                querySqlTmp.append(" ORDER BY rr.sort_num ASC, rr.resource_create_time DESC");
            } else {
                querySqlTmp.append(" ORDER BY rr.sort_num ASC, rr.target_create_time DESC");
            }
        }

        return querySqlTmp.toString();
    }
    
    /**
     * 普通sql拼接 (习题)
     * 
     * @param stringBuffer
     * @param targetType
     * @param categories
     * @param coverage
     * @return
     * @since
     */
    private String resourceCommonSqlForQuestion(String targetType,
                                              String categories,
                                              String coverage,
                                              boolean isPortal) {
        final boolean isNeedPreview = this.isNeedPreview(targetType);

        String commonSelect = "SELECT a.identifier AS identifier, a.title AS title, a.description AS description, a.tags AS tags, a.keywords AS keywords ";

        StringBuffer querySqlTmp = new StringBuffer(commonSelect);
        if (isNeedPreview) {
            querySqlTmp.append(", a.preview AS preview");
        }
        querySqlTmp.append(" FROM ndresource a ");
        
        Set<String> set = null;
        
        if(StringUtils.isEmpty(coverage) && StringUtils.isEmpty(categories) && isPortal){
            querySqlTmp.append("WHERE ");
        } else if(StringUtils.isNotEmpty(coverage) || StringUtils.isNotEmpty(categories) || !isPortal){
            if(StringUtils.isNotEmpty(categories)){
                set = new HashSet<String>();
                set.addAll(Arrays.asList(categories.split(",")));
                
                // 查询数据维度数据sql拼接
                categorySql(querySqlTmp, set);
            }
            
            if (StringUtils.isNotEmpty(coverage)) {
                StringBuffer coverageSql = new StringBuffer(" INNER JOIN res_coverages rcv ON a.identifier = rcv.resource AND rcv.res_type='");
                coverageSql.append(targetType).append("' AND (").append(coverageParam4Sql(coverage)).append(")");
                querySqlTmp.append(coverageSql);
            }
            
            if (!isPortal){
            	StringBuffer portalSql = new StringBuffer(" LEFT JOIN res_coverages portal ON a.identifier = portal.resource AND portal.res_type='");
            	portalSql.append(targetType).append("' WHERE (").append(portalParam4Sql()).append(")");
            	querySqlTmp.append(portalSql);
            }
            
            querySqlTmp.append(" AND ");
        }
        
        querySqlTmp.append("a.primary_category = :targetType AND a.identifier IN (:sourceUuids) AND a.enable = 1 ORDER BY INSTR (:sourceUuidStr, a.identifier)");
        
        return querySqlTmp.toString();
    }
    
    /**
     * 拼接维度数据sql
     * 
     * @param sql
     * @param categories
     * @since
     */
    private void categorySql(StringBuffer stringBuffer, Set<String> set) {
        boolean havePath = false;
        boolean haveCode = false;

        for (String str4Join : set) {// 目的是减少Join表
            if (StringUtils.isNotEmpty(str4Join)) {
                if (str4Join.contains("/")) {
                    havePath = true;
                } else {
                    haveCode = true;
                }
            }
        }

        if (havePath) {
            stringBuffer.append(" INNER JOIN resource_categories rc ON a.identifier = rc.resource");
        }

        if (haveCode) {
            stringBuffer.append(" INNER JOIN resource_categories rcc ON a.identifier = rcc.resource");
        }

        stringBuffer.append(categoryParam4Sql(set));
    }
    
    /**
     * 维度数据sql参数拼接  
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: caocr   </p>
     * @param categories
     * @return
     */
    private StringBuffer categoryParam4Sql(Set<String> categories){
        StringBuffer result = new StringBuffer();
        int i =1;
        
        List<StringBuffer> pathList = new ArrayList<StringBuffer>();
        List<StringBuffer> codeList = new ArrayList<StringBuffer>();
        for(String category : categories){
            if(category.contains("/")){//说明是path
                StringBuffer path = new StringBuffer();
                if(category.contains("*")){
                    path.append("rc.taxOnPath LIKE :cgpathlike").append(i);
                }else{
                    path.append("rc.taxOnPath = :cgpath").append(i);
                }
                
                pathList.add(path);
            }else{//说明是code
                StringBuffer code = new StringBuffer();
                if(category.contains("*")){//使用通配符
                    code.append("rcc.taxOnCode LIKE :cgcodelike").append(i);
                }else{
                    code.append("rcc.taxOnCode= :cgcode").append(i);
                }

                codeList.add(code);
            }
            
            i++;
        }
        
        if (CollectionUtils.isNotEmpty(pathList) && CollectionUtils.isNotEmpty(codeList)) {// path,code同存
            result.append(" AND (")
                  .append(StringUtils.join(pathList, " OR "))
                  .append(") AND (")
                  .append(StringUtils.join(codeList, " OR "))
                  .append(")");
        } else if (CollectionUtils.isEmpty(pathList) && CollectionUtils.isNotEmpty(codeList)) {// 只有code
            result.append(" AND (").append(StringUtils.join(codeList, " OR ")).append(")");
        } else if (CollectionUtils.isNotEmpty(pathList) && CollectionUtils.isEmpty(codeList)) {// 只有path
            result.append(" AND (").append(StringUtils.join(pathList, " OR ")).append(")");
        }
        
        return result;
    }
    
    /**
     * 资源覆盖范围sql参数拼接    
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: caocr   </p>
     * @param coverages
     * @return
     */
    private String coverageParam4Sql(String coverage) {
        String result = "";
        List<String> coverageElemnt = Arrays.asList(coverage.split("/"));
        String targetTypeSql = coverageElemnt.get(0).equals("*") ? "" : "rcv.target_type=:cvty";
        String targetSql = coverageElemnt.get(1).equals("*") ? "" : "rcv.target=:cvt";
        String strategySql = coverageElemnt.get(2).equals("*") ? "" : "rcv.strategy=:cvs";

        List<String> condition = new ArrayList<String>();
        if (StringUtils.isNotEmpty(targetTypeSql)) {
            condition.add(targetTypeSql);
        }
        if (StringUtils.isNotEmpty(targetSql)) {
            condition.add(targetSql);
        }
        if (StringUtils.isNotEmpty(strategySql)) {
            condition.add(strategySql);
        }

        result = StringUtils.join(condition, " AND ");

        return result;
    }
    
    /**
     * 当isPortal=false时需要对资源的覆盖范围做特殊处理
     * @author xiezy
     * @date 2016年8月31日
     * @return
     */
    private String portalParam4Sql(){
    	String result = "portal.target IS NULL OR ";
    	
    	result += "portal.target != '" + CoverageConstant.ESP_PORTAL_ZH_COVERAGE_TARGET + "'";
    	
    	return result;
    }
    
    /**
     * sql参数处理
     * 
     * @param query
     * @param sourceUuids
     * @param resType
     * @param targetType
     * @param relationType
     * @param categories
     * @param coverage
     * @param reverse
     * @since
     */
    private void sqlParamDeal(Query query,
                              List<String> sourceUuids,
                              String resType,
                              String targetType,
                              String label,
                              String tags,
                              String relationType,
                              String categories,
                              String coverage,
                              boolean reverse) {
        query.setParameter("primaryTargetType", targetType);
        query.setParameterList("sourceUuids", sourceUuids);
        if (reverse) {
            query.setParameter("resType", targetType);
            query.setParameter("targetType", resType);
        } else {
            query.setParameter("resType", resType);
            query.setParameter("targetType", targetType);
        }

        if (StringUtils.isNotEmpty(label)) {
            query.setParameter("label", label);
        }
        
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                query.setParameter(new StringBuffer("tag").append(i).toString(), new StringBuffer("%\"").append(tag)
                                                                                                        .append("\"%")
                                                                                                        .toString());
                i++;
            }
        }
        
        if (StringUtils.isNotEmpty(relationType)) {
            query.setParameter("relationType", relationType);
        }
        
        Set<String> set = null;
        if(StringUtils.isNotEmpty(categories)){
            set = new HashSet<String>();
            set.addAll(Arrays.asList(categories.split(",")));
        }
        // categories的参数处理,命名规则:taxOnPath=:cgpath+i,rcc.taxOnCode=:cgcode+i(i从1开始到categories.size()为止)
        catSqlParamDeal(query, set);

        // 设置coverage参数
        covSqlParamDeal(query, coverage);
    }
    
    /**
     * sql参数处理
     * 
     * @param query
     * @param sourceUuids
     * @param resType
     * @param targetType
     * @param relationType
     * @param reverse
     * @since
     */
    private Map<String, Object> relationSqlParamDealForQuestion(List<String> sourceUuids,
                                         String resType,
                                         String targetType,
                                         String label,
                                         String tags,
                                         String relationType,
                                         boolean reverse) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceUuids", sourceUuids);
        if (reverse) {
            params.put("resType", targetType);
            params.put("targetType", resType);
        } else {
            params.put("resType", resType);
            params.put("targetType", targetType);
        }

        if (StringUtils.isNotEmpty(label)) {
            params.put("label", label);
        }

        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                params.put(new StringBuffer("tag").append(i).toString(), new StringBuffer("%\"").append(tag)
                                                                                                        .append("\"%")
                                                                                                        .toString());
                i++;
            }
        }

        if (StringUtils.isNotEmpty(relationType)) {
            params.put("relationType", relationType);
        }
        
        return params;
    }
    
    /**
     * sql参数处理(习题)
     * 
     * @param targetType
     * @param categories
     * @param coverage
     * @since
     */
    private Map<String, Object> resourceSqlParamDealForQuestion(List<String> resourceUuids,
                                                                String targetType,
                                                                String categories,
                                                                String coverage) {
        Map<String, Object> sqlParam = new HashMap<String, Object>();
        sqlParam.put("sourceUuids", resourceUuids);
        sqlParam.put("targetType", targetType);
        
        Set<String> set = null;
        if(StringUtils.isNotEmpty(categories)){
            set = new HashSet<String>();
            set.addAll(Arrays.asList(categories.split(",")));
        }
        
        // categories的参数处理,命名规则:taxOnPath=:cgpath+i,rcc.taxOnCode=:cgcode+i(i从1开始到categories.size()为止)
        Map<String, Object> catSqlParam = catSqlParamDealForCount(set);
        if (CollectionUtils.isNotEmpty(catSqlParam)) {
            sqlParam.putAll(catSqlParam);
        }

        // 设置coverage参数
        Map<String, Object> covSqlParam = covSqlParamDealForCount(coverage);
        if (CollectionUtils.isNotEmpty(covSqlParam)) {
            sqlParam.putAll(covSqlParam);
        }
        
        return sqlParam;
    }
    
    /**
     * 参数注入
     * 
     * @param sourceUuids
     * @param resType
     * @param targetType
     * @param label
     * @param tags
     * @param relationType
     * @param categories
     * @param coverage
     * @param reverse
     * @return
     * @since
     */
    private Map<String, Object> sqlParamDealForCount(List<String> sourceUuids,
                                      String resType,
                                      String targetType,
                                      String label,
                                      String tags,
                                      String relationType,
                                      String categories,
                                      String coverage,
                                      boolean reverse) {
        Map<String, Object> sqlParam = new HashMap<String, Object>();
        sqlParam.put("primaryTargetType", targetType);
        sqlParam.put("sourceUuids", sourceUuids);
        if (reverse) {
            sqlParam.put("resType", targetType);
            sqlParam.put("targetType", resType);
        } else {
            sqlParam.put("resType", resType);
            sqlParam.put("targetType", targetType);
        }

        if (StringUtils.isNotEmpty(label)) {
            sqlParam.put("label", label);
        }
        
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<String>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            int i = 1;
            for (String tag : tagSet) {
                sqlParam.put(new StringBuffer("tag").append(i).toString(), new StringBuffer("%\"").append(tag)
                                                                                                        .append("\"%")
                                                                                                        .toString());
                i++;
            }
        }
        
        if (StringUtils.isNotEmpty(relationType)) {
            sqlParam.put("relationType", relationType);
        }
        
        Set<String> set = null;
        if(StringUtils.isNotEmpty(categories)){
            set = new HashSet<String>();
            set.addAll(Arrays.asList(categories.split(",")));
        }
        // categories的参数处理,命名规则:taxOnPath=:cgpath+i,rcc.taxOnCode=:cgcode+i(i从1开始到categories.size()为止)
        Map<String, Object> catSqlParam = catSqlParamDealForCount(set);
        if(CollectionUtils.isNotEmpty(catSqlParam)){
            sqlParam.putAll(catSqlParam);
        }

        // 设置coverage参数
        Map<String, Object> covSqlParam = covSqlParamDealForCount(coverage);
        if(CollectionUtils.isNotEmpty(covSqlParam)){
            sqlParam.putAll(covSqlParam);
        }
        
        return sqlParam;
    }
    
    /**
     * 分类维度参数拼接
     * 
     * @param set
     * @param query
     * @since
     */
    private void catSqlParamDeal(Query query, Set<String> set) {
        if (CollectionUtils.isNotEmpty(set)) {
            int i = 1;
            for (String category : set) {
                if (category.contains("/")) {
                    if (category.contains("*")) {
                        category = category.replace('*', '%');
                        query.setParameter(new StringBuffer("cgpathlike").append(i).toString(), category);
                    } else {
                        query.setParameter(new StringBuffer("cgpath").append(i).toString(), category);
                    }
                } else {
                    if (category.contains("*")) {
                        category = category.replace('*', '%');
                        query.setParameter(new StringBuffer("cgcodelike").append(i).toString(), category);
                    } else {
                        query.setParameter(new StringBuffer("cgcode").append(i).toString(), category);
                    }
                }

                i++;
            }
        }
    }
    
    /**
     * 分类维度参数拼接
     * 
     * @param set
     * @param query
     * @since
     */
    private Map<String, Object> catSqlParamDealForCount(Set<String> set) {
        Map<String, Object> catSqlParam = new HashMap<String, Object>();
        if (CollectionUtils.isNotEmpty(set)) {
            int i = 1;
            for (String category : set) {
                if (category.contains("/")) {
                    if (category.contains("*")) {
                        category = category.replace('*', '%');
                        catSqlParam.put(new StringBuffer("cgpathlike").append(i).toString(), category);
                    } else {
                        catSqlParam.put(new StringBuffer("cgpath").append(i).toString(), category);
                    }
                } else {
                    if (category.contains("*")) {
                        category = category.replace('*', '%');
                        catSqlParam.put(new StringBuffer("cgcodelike").append(i).toString(), category);
                    } else {
                        catSqlParam.put(new StringBuffer("cgcode").append(i).toString(), category);
                    }
                }

                i++;
            }
        }
        
        return catSqlParam;
    }
    
    /**
     * 覆盖范围参数拼接
     * 
     * @param coverage
     * @param query
     * @since
     */
    private void covSqlParamDeal(Query query, String coverage) {
        if (StringUtils.isNotEmpty(coverage)) {
            List<String> coverageElemnt = Arrays.asList(coverage.split("/"));
            boolean haveTargetType = coverageElemnt.get(0).equals("*") ? false : true;
            boolean haveTarget = coverageElemnt.get(1).equals("*") ? false : true;
            boolean haveStrategy = coverageElemnt.get(2).equals("*") ? false : true;

            if (haveTargetType) {
                query.setParameter("cvty", coverageElemnt.get(0));
            }

            if (haveTarget) {
                query.setParameter("cvt", coverageElemnt.get(1));
            }

            if (haveStrategy) {
                query.setParameter("cvs", coverageElemnt.get(2));
            }
        }
    }
    
    /**
     * 覆盖范围参数拼接
     * 
     * @param coverage
     * @param query
     * @since
     */
    private Map<String, Object> covSqlParamDealForCount(String coverage) {
        Map<String, Object> covSqlParam = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(coverage)) {
            List<String> coverageElemnt = Arrays.asList(coverage.split("/"));
            boolean haveTargetType = coverageElemnt.get(0).equals("*") ? false : true;
            boolean haveTarget = coverageElemnt.get(1).equals("*") ? false : true;
            boolean haveStrategy = coverageElemnt.get(2).equals("*") ? false : true;

            if (haveTargetType) {
                covSqlParam.put("cvty", coverageElemnt.get(0));
            }

            if (haveTarget) {
                covSqlParam.put("cvt", coverageElemnt.get(1));
            }

            if (haveStrategy) {
                covSqlParam.put("cvs", coverageElemnt.get(2));
            }
        }
        
        return covSqlParam;
    }
    
    /**
     * 将返回的ResultModel转换为RelationForQueryViewModel
     * 
     * @param resultList
     * @param viewModels
     * @param isNeedPreview
     * @since
     */
    private void setData(List<ResultModel> resultList,
                         List<RelationForQueryViewModel> viewModels,
                         boolean isNeedPreview) {
        RelationForQueryViewModel viewModel = null;
        for(ResultModel resultModel : resultList) {
            viewModel = BeanMapperUtils.beanMapper(resultModel, RelationForQueryViewModel.class);
            viewModel.setTags(ObjectUtils.fromJson(resultModel.getTags(), new TypeToken<List<String>>() {
            }));
            viewModel.setKeywords(ObjectUtils.fromJson(resultModel.getKeywords(), new TypeToken<List<String>>() {
            }));
            viewModel.setRelationId(resultModel.getRelation_id());
            viewModel.setRelationType(resultModel.getRelation_type());
            if(resultModel.getOrder_num() != null){
                viewModel.setOrderNum(resultModel.getOrder_num());
            } else {
                viewModel.setOrderNum(0);
            }
            viewModel.setRelationTags(ObjectUtils.fromJson(resultModel.getRelation_tags(), new TypeToken<List<String>>() {
            }));
            viewModel.setSid(resultModel.getSource_uuid());
            if (isNeedPreview) {
                viewModel.setPreview(ObjectUtils.fromJson(resultModel.getPreview(), Map.class));
            } else {
                viewModel.setPreview(null);
            }
            viewModel.setTargetType(resultModel.getTarget_type());
            viewModel.setCreateTime(resultModel.getCreate_time());
            viewModel.setLastUpdate(resultModel.getLast_update());
            viewModels.add(viewModel);
        }
    }
    
    /**
     * 通过Redis获取记录数items
     * <p>Create Time: 2016年1月12日   </p>
     * <p>Create author: xuzy   </p>
     * @param key
     * @param limit
     * @return
     */
    private List<RelationForQueryViewModel> getResult(String key, String limit) {
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        long offSet = (long)result[0];
        long num = (long)result[1];
        
        //判断key是否存在
        boolean flag = ert.existKey(key);
        
        if(!flag){
            return null;
        }
        
        //取出KEY对应缓存总数
        long count = ert.zSetCount(key);
        
        if(offSet >= count){
            return new ArrayList<RelationForQueryViewModel>();
        }
        
        long toIndex = offSet + num;
        if(toIndex > count){
            toIndex = count;
        }
        
        //根据分页参数取出缓存数据
        List<RelationForQueryViewModel> fmList = ert.zRangeByScore(key, offSet, toIndex - 1, RelationForQueryViewModel.class);
        return fmList;
    }
    
    /**
     * 
     * 
     * @param relationResultList
     * @param resourceResultList
     * @param viewModels
     * @param isNeedPreview
     * @since
     */
    private void setDataForQuestion(List<ResultModel> relationResultList,
                                    List<ResultModel> resourceResultList,
                                    List<RelationForQueryViewModel> viewModels,
                                    boolean isNeedPreview) {
        RelationForQueryViewModel viewModel = null;
        
        Map<String, ResultModel> resourceResultMap = new HashMap<String, ResultModel>();
        for(ResultModel resourceResultModel : resourceResultList){
            resourceResultMap.put(resourceResultModel.getIdentifier(), resourceResultModel);
        }
        
        for (ResultModel relationResultModel : relationResultList) {
            if (resourceResultMap.get(relationResultModel.getIdentifier()) != null) {
                viewModel = new RelationForQueryViewModel();
                viewModel.setRelationId(relationResultModel.getRelation_id());
                viewModel.setRelationType(relationResultModel.getRelation_type());
                viewModel.setLabel(relationResultModel.getLabel());
                if (relationResultModel.getOrder_num() != null) {
                    viewModel.setOrderNum(relationResultModel.getOrder_num());
                } else {
                    viewModel.setOrderNum(0);
                }
                viewModel.setEnable(true);
                viewModel.setRelationTags(ObjectUtils.fromJson(relationResultModel.getRelation_tags(),
                                                               new TypeToken<List<String>>() {
                                                               }));
                viewModel.setIdentifier(relationResultModel.getIdentifier());
                viewModel.setTitle(resourceResultMap.get(relationResultModel.getIdentifier()).getTitle());
                viewModel.setDescription(resourceResultMap.get(relationResultModel.getIdentifier()).getDescription());
                if (isNeedPreview) {
                    viewModel.setPreview(ObjectUtils.fromJson(resourceResultMap.get(relationResultModel.getIdentifier()).getPreview(), Map.class));
                } else {
                    viewModel.setPreview(null);
                }
                viewModel.setTags(ObjectUtils.fromJson(resourceResultMap.get(relationResultModel.getIdentifier())
                                                                        .getTags(), new TypeToken<List<String>>() {
                }));
                viewModel.setKeywords(ObjectUtils.fromJson(resourceResultMap.get(relationResultModel.getIdentifier())
                                                                            .getKeywords(),
                                                           new TypeToken<List<String>>() {
                                                           }));
                viewModel.setSid(relationResultModel.getSource_uuid());
                viewModel.setTargetType(relationResultModel.getTarget_type());
                viewModel.setCreateTime(relationResultModel.getCreate_time());
                viewModel.setLastUpdate(relationResultModel.getLast_update());
                viewModels.add(viewModel);
            }
        }
    }

    @Override
    public List<ResourceRelationResultModel> getResourceRelationsWithOrder(String source, String resType) {
        Session session = null;
        if (CommonServiceHelper.isQuestionDb(resType)) {
            session = (Session) entityManager2.getDelegate();
        } else {
            session = (Session) entityManager.getDelegate();
        }
        Query query = session.getNamedQuery("getResourceRelationsWithOrder")
                             .setResultTransformer(Transformers.aliasToBean(ResourceRelationResultModel.class));
        
        query.setParameter("sourceUuid", source);
        query.setParameter("resType", resType);
        return query.list();
    }

    @Override
    public List<ResourceRelationResultModel> getResourceRelations(String source,
                                                                  String resType,
                                                                  String resourceTargetType) {
        Session session = null;
        if (CommonServiceHelper.isQuestionDb(resType)) {
            session = (Session) entityManager2.getDelegate();
        } else {
            session = (Session) entityManager.getDelegate();
        }
        Query query = session.getNamedQuery("getResourceRelations")
                             .setResultTransformer(Transformers.aliasToBean(ResourceRelationResultModel.class));
        ;
        query.setParameter("sourceUuid", source);
        query.setParameter("resType", resType);
        query.setParameter("targetType", resourceTargetType);
        return query.list();
    }
    
    /**
     * 缓存items到Redis    
     * <p>Create Time: 2016年1月14日   </p>
     * <p>Create author: xuzy   </p>
     * @param key
     * @param items
     */
    private void saveResult(String key, List<RelationForQueryViewModel> items) {
        // 保存到redis
        ert.zSet(key, items);

        // 设置过期时间
        ert.expire(key, 1l, TimeUnit.DAYS);
    }

    @Override
    public List<ResourceRelation> batchGetRelationByResourceSourceOrTarget(String primaryCategory, Set<String> uuidsSet){
        //FIXME question db
        javax.persistence.Query query = repository
                .getEntityManager()
                .createNamedQuery("batchGetRelationByResourceSourceOrTarget");
        query.setParameter("resType", primaryCategory);
        query.setParameter("rids", uuidsSet);

        javax.persistence.Query queryQuestions = resourceRelation4QuestionDBRepository
                .getEntityManager()
                .createNamedQuery("batchGetRelationByResourceSourceOrTarget");
        queryQuestions.setParameter("resType", primaryCategory);
        queryQuestions.setParameter("rids", uuidsSet);

        @SuppressWarnings("unchecked")
        List<ResourceRelation> result = query.getResultList();
        @SuppressWarnings("unchecked")
        List<ResourceRelation> resultQuestions = queryQuestions.getResultList();

        result.addAll(resultQuestions);

        return result;
    }
}
