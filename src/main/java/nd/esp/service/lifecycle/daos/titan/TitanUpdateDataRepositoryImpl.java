package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/8/9.
 */
@Repository
public class TitanUpdateDataRepositoryImpl implements TitanUpdateDataRepository {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanCategoryRepositoryImpl.class);

    @Autowired
    private TitanCategoryRepository titanCategoryRepository;
    @Autowired
    private TitanCoverageRepository titanCoverageRepository;
    @Autowired
    private TitanRelationRepository titanRelationRepository;
    @Autowired
    private TitanTechInfoRepository titanTechInfoRepository;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Override
    public boolean updateOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {
        Map<String, ResCoverage> coverageMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resCoverageList)) {
            for (ResCoverage coverage : resCoverageList) {
                String key = coverage.getTarget() + coverage.getStrategy() + coverage.getTargetType();
                if (coverageMap.get(key) == null) {
                    coverageMap.put(key, coverage);
                }
            }
        }

        Set<String> categoryPathSet = new HashSet<>();
        Map<String, ResourceCategory> categoryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resourceCategoryList)) {
            for (ResourceCategory resourceCategory : resourceCategoryList) {
                if (StringUtils.isNotEmpty(resourceCategory.getTaxonpath())) {
                    categoryPathSet.add(resourceCategory.getTaxonpath());
                }
                if (categoryMap.get(resourceCategory.getTaxoncode()) == null) {
                    categoryMap.put(resourceCategory.getTaxoncode(), resourceCategory);
                }

            }
        }

        Map<String, TechInfo> techInfoMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(techInfos)) {
            for (TechInfo techInfo : techInfos) {
                if (techInfoMap.get(techInfo.getTitle()) == null) {
                    techInfoMap.put(techInfo.getTitle(), techInfo);
                }
            }
        }

        List<ResCoverage> coverageList = new ArrayList<>();
        coverageList.addAll(coverageMap.values());
        List<ResourceCategory> categoryList = new ArrayList<>();
        categoryList.addAll(categoryMap.values());
        List<TechInfo> techInfoList = new ArrayList<>();
        techInfoList.addAll(techInfoMap.values());

        if(!checkEducationExist(education)){
            titanSync(TitanSyncType.UPDATE_DATA_RESOURCE_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
        }else {
            batchUpdateCategory(categoryList);
            batchUpdateCoverage(coverageList);
            batchUpdateTechInfo(techInfoList);
        }
        return false;
    }

    private boolean checkEducationExist(Education education){
        Long id = null;
        try {
            id = titanCommonRepository.getVertexIdByLabelAndId(education.getPrimaryCategory(), education.getIdentifier());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(id == null){
            return false;
        }

        return true;
    }

    private String batchUpdateTechInfo(List<TechInfo> techInfoList) {
        for (TechInfo techInfo : techInfoList) {
            TechInfo result = updateTechInfo(techInfo);
            if(result == null){
                titanSync(TitanSyncType.UPDATE_DATA_OTHER, techInfo.getResType(), techInfo.getResource());
            }
        }
        return null;
    }

    private TechInfo updateTechInfo(TechInfo techInfo){
        String checkTechInfoExist = "g.E().hasLabel('has_tech_info').has('identifier',edgeIdentifier).id()";
        Map<String, Object> checkTechInfoParam = new HashMap<>();
        checkTechInfoParam.put("edgeIdentifier", techInfo.getIdentifier());
        String oldTechInfoId = null;
        try {
            oldTechInfoId = titanCommonRepository.executeScriptUniqueString(checkTechInfoExist, checkTechInfoParam);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}", e.getMessage(), techInfo.getResource());
            //TODO titan sync
            return null;
        }

        StringBuffer scriptBuffer = null;
        Map<String, Object> graphParams = null;

        boolean isAdd = false;
        String techInfoEdgeId = null;
        if (StringUtils.isEmpty(oldTechInfoId)) {
            isAdd = true;
            scriptBuffer = new StringBuffer("techinfo = graph.addVertex(T.label, type");
            graphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer, techInfo);
            scriptBuffer.append(");g.V().has(primaryCategory,'identifier',sourceIdentifier).next().addEdge('has_tech_info',techinfo ,'identifier',edgeIdentifier");

            graphParams.putAll(TitanScritpUtils.getParamAndChangeScript(scriptBuffer, techInfo));

            scriptBuffer.append(").id();");
            graphParams.put("type", "tech_info");
            graphParams.put("primaryCategory", techInfo.getResType());
            graphParams.put("sourceIdentifier", techInfo.getResource());
            graphParams.put("edgeIdentifier", techInfo.getIdentifier());
            try {
                techInfoEdgeId = titanCommonRepository.executeScriptUniqueString(scriptBuffer.toString(), graphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}", e.getMessage(), techInfo.getResource());
                //TODO titan sync
                return null;
            }
        } else {
            scriptBuffer = new StringBuffer("g.V().has('identifier',identifier)");
            graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer, techInfo);
            scriptBuffer.append(";");
            graphParams.put("identifier", techInfo.getIdentifier());

            StringBuffer updateEdge = new StringBuffer("g.E().has('identifier',identifier)");
            Map<String, Object> updateEdgeParam = TitanScritpUtils.getParamAndChangeScript4Update(updateEdge, techInfo);

            try {
                titanCommonRepository.executeScript(scriptBuffer.toString(), graphParams);
                titanCommonRepository.executeScript(updateEdge.toString(), updateEdgeParam);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}", e.getMessage(), techInfo.getResource());
                //TODO titan sync
                return null;
            }
        }

        if (isAdd && StringUtils.isEmpty(techInfoEdgeId)) {
            return null;
        }

        return  techInfo;
    }

    private String batchUpdateCoverage(List<ResCoverage> coverageList) {
        for (ResCoverage resCoverage : coverageList) {
            ResCoverage result = updateCoverage(resCoverage);
            if(result == null){
                titanSync(TitanSyncType.UPDATE_DATA_OTHER, resCoverage.getResType(), resCoverage.getResource());
            }

        }
        return null;
    }

    private ResCoverage updateCoverage(ResCoverage resCoverage){
        try {
            titanCommonRepository.deleteEdgeById(resCoverage.getIdentifier());
        } catch (Exception e) {
            return null;
        }

        Long coverageNodeId = getCoverageIdFormTitan(resCoverage);
        String coveragePathId;
        if (coverageNodeId == null) {
            // coverage node not exist
            // create coverage node and create has_coveage edge
            StringBuffer scriptBuffer = new StringBuffer(
                    "coverageNodeId = graph.addVertex(T.label,'coverage','target_type',target_type,'strategy',strategy,'target',target).id();");
            scriptBuffer.append("g.V().has(primaryCategory,'identifier',identifier).next()" +
                    ".addEdge('has_coverage',g.V(coverageNodeId).next(),'identifier',edgeIdentifier");
            Map<String, Object> innerGraphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer, resCoverage);
            scriptBuffer.append(").id()");
            innerGraphParams.put(ES_Field.target_type.toString(),
                    resCoverage.getTargetType());
            innerGraphParams.put(ES_Field.target.toString(),
                    resCoverage.getTarget());
            innerGraphParams.put(ES_Field.strategy.toString(),
                    resCoverage.getStrategy());
            innerGraphParams.put("primaryCategory", resCoverage.getResType());
            innerGraphParams.put("identifier", resCoverage.getResource());
            innerGraphParams.put("edgeIdentifier", resCoverage.getIdentifier());

            try {
                coveragePathId = titanCommonRepository.executeScriptUniqueString(scriptBuffer.toString(), innerGraphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{} identifier:{}", e.getMessage(), resCoverage.getResource());
                //TODO titan sync
                return null;
            }
        } else {
            StringBuffer script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).next()" +
                    ".addEdge('has_coverage',g.V(coverageNodeId).next(),'identifier',edgeIdentifier");
            Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript(script, resCoverage);
            script.append(").id()");
            graphParams.put("primaryCategory", resCoverage.getResType());
            graphParams.put("identifier", resCoverage.getResource());
            graphParams.put("coverageNodeId", coverageNodeId);
            graphParams.put("edgeIdentifier", resCoverage.getIdentifier());

            try {
                coveragePathId = titanCommonRepository.executeScriptUniqueString(script.toString(), graphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{} identifier:{}", e.getMessage(), resCoverage.getResource());
                //TODO titan sync
                return null;
            }
        }

        if (coveragePathId == null) {
            return null;
        }

        return resCoverage;
    }

    private Long getCoverageIdFormTitan(ResCoverage resCoverage) {
        String scriptString = "g.V().hasLabel('coverage').has('target_type',target_type).has('target',target).has('strategy',strategy).id()";

        Map<String, Object> graphParams = new HashMap<String, Object>();
        graphParams.put(ES_Field.target_type.toString(),
                resCoverage.getTargetType());
        graphParams.put(ES_Field.target.toString(), resCoverage.getTarget());
        graphParams
                .put(ES_Field.strategy.toString(), resCoverage.getStrategy());

        try {
            return titanCommonRepository.executeScriptUniqueLong(scriptString,
                    graphParams);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.error("titan_repository error:{}", e.getMessage());
        }
        return null;
    }

    private String batchUpdateCategory(List<ResourceCategory> categoryList) {
        for (ResourceCategory resourceCategory : categoryList) {

            ResourceCategory result = updateCategory(resourceCategory);
            if(result == null){
                titanSync(TitanSyncType.UPDATE_DATA_OTHER, resourceCategory.getPrimaryCategory(), resourceCategory.getResource());
            }
        }
        return null;
    }


    private ResourceCategory updateCategory(ResourceCategory resourceCategory){
        StringBuffer script;
        Map<String, Object> graphParams;
        try {
            titanCommonRepository.deleteEdgeById(resourceCategory.getIdentifier());
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}", e.getMessage(), resourceCategory.getResource());
            return null;
        }

        //检查code在数据库中是否已经存在
        Long categoryCodeNodeId = getCategoryCodeId(resourceCategory);
        String edgeId;
        if (categoryCodeNodeId != null) {
            script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).next()" +
                    ".addEdge('has_category_code',g.V(categoryCodeNodeId).next()");
            graphParams = TitanScritpUtils.getParamAndChangeScript(script, resourceCategory);
            script.append(",'identifier',edgeIdentifier");
            //增加对taxonpath的null判断
            if (resourceCategory.getTaxonpath() != null) {
                script.append(",'cg_taxonpath',cgTaxonpath");
                graphParams.put("cgTaxonpath", resourceCategory.getTaxonpath());
            }
            script.append(").id()");
            graphParams.put("primaryCategory",
                    resourceCategory.getPrimaryCategory());
            graphParams.put("identifier", resourceCategory.getResource());
            graphParams.put("categoryCodeNodeId", categoryCodeNodeId);
            graphParams.put("edgeIdentifier", resourceCategory.getIdentifier());

            try {
                edgeId = titanCommonRepository.executeScriptUniqueString(script.toString(), graphParams);
            } catch (Exception e) {
                return null;
            }
        } else {
            script = new StringBuffer(
                    "category_code = graph.addVertex(T.label,'category_code'");
            graphParams = TitanScritpUtils.getParamAndChangeScript(script, resourceCategory);
            script.append(");");
            script.append("g.V().hasLabel(primaryCategory).has('identifier',identifier).next()" +
                    ".addEdge('has_category_code',category_code");
            Map<String, Object> paramEdgeMap = TitanScritpUtils.getParamAndChangeScript(script, resourceCategory);
            script.append(",'identifier',edgeIdentifier");
            //增加对taxonpath的null判断
            if (resourceCategory.getTaxonpath() != null) {
                script.append(",'cg_taxonpath',cgTaxonpath");
                graphParams.put("cgTaxonpath", resourceCategory.getTaxonpath());
            }
            script.append(").id()");

            graphParams.put("primaryCategory",
                    resourceCategory.getPrimaryCategory());
            graphParams.putAll(paramEdgeMap);

            graphParams.put("identifier", resourceCategory.getResource());
            graphParams.put("edgeIdentifier", resourceCategory.getIdentifier());

            try {
                edgeId = titanCommonRepository.executeScriptUniqueString(script.toString(), graphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}", e.getMessage(), resourceCategory.getResource());
                //TODO titan sync
                return null;
            }
        }

        return resourceCategory;
    }

    private Long getCategoryCodeId(ResourceCategory resCoverage) {
        String scriptString = "g.V().hasLabel('category_code').has('cg_taxoncode',taxoncode).id()";
        Map<String, Object> graphParams = new HashMap<String, Object>();
        graphParams.put("taxoncode", resCoverage.getTaxoncode());
        Long taxoncodeId = null;
        try {
            taxoncodeId = titanCommonRepository.executeScriptUniqueLong(scriptString, graphParams);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}", e.getMessage());
            //FIXME 这个地方的代码应该做
        }
        return taxoncodeId;
    }

    private void titanSync(TitanSyncType syncType, String parmaryCategory, String identifier){
        titanRepositoryUtils.titanSync4MysqlAdd(syncType, parmaryCategory, identifier,999);
    }
}
