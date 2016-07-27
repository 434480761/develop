package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanImportRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/7/26.
 */
@Repository
public class TitanImportRepositoryImpl implements TitanImportRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanImportRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;
    @Autowired
    private TitanSyncRepository titanSyncRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;

    @Override
    /**
     * @return false 导入数据失败 ；true 导入成功
     * */
    public boolean importOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {
        Map<String,ResCoverage> coverageMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resCoverageList)){
            for(ResCoverage coverage : resCoverageList){
                String key = coverage.getTarget()+coverage.getStrategy()+coverage.getTargetType();
                if(coverageMap.get(key)==null){
                    coverageMap.put(key, coverage);
                }
            }
        }

        Set<String> categoryPathSet = new HashSet<>();
        Map<String, ResourceCategory> categoryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resourceCategoryList)){
            for (ResourceCategory resourceCategory : resourceCategoryList){
                if(StringUtils.isNotEmpty(resourceCategory.getTaxonpath())){
                    categoryPathSet.add(resourceCategory.getTaxonpath());
                }
                if(categoryMap.get(resourceCategory.getTaxoncode())==null){
                    categoryMap.put(resourceCategory.getTaxoncode(), resourceCategory);
                }

            }
        }

        Map<String, TechInfo> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(techInfos)){
            for (TechInfo techInfo : techInfos){
                if(techInfoMap.get(techInfo.getTitle()) == null){
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
        List<String> categoryPathList = new ArrayList<>();
        categoryPathList.addAll(categoryPathSet);

        Map<String, Object> result = TitanScritpUtils.buildScript(education,coverageList,categoryList,techInfoList,categoryPathList);
        //校验addVertex中的参数个数过多，个数超过250返回为null
        if(CollectionUtils.isEmpty(result)){
            //TODO 这种错误情况保存到数据库中，返回true
            saveErrorSource(education);
        } else {
            Long educationId = null;
            try {
                String script = result.get("script").toString();
                Map<String, Object> param = (Map<String, Object>) result.get("param");
                educationId = titanCommonRepository.executeScriptUniqueLong(script, param);
            } catch (Exception e) {
                LOG.error("titanImportErrorData:{}" ,education.getIdentifier());
                e.printStackTrace();
                return  false;
            }
            if(educationId == null){
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean checkResourceExistInTitan(Education education) {
        String script = "g.V().has(primaryCategory,'identifier',identifier).count();";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory",education.getPrimaryCategory());
        param.put("identifier",education.getIdentifier());

        Long count = null;
        try {
            count = titanCommonRepository.executeScriptUniqueLong(script, param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(count == null || count == 0){
            LOG.info("资源在titan中不存在 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(), education.getIdentifier());
        } else if(count > 1){
            LOG.info("资源在titan中有重复 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(), education.getIdentifier());
        } else {
            return true;
        }

        return false;
    }

    @Override
    public boolean checkResourceAllInTitan(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        String baseScript = "g.V().has(primaryCategory,'identifier',identifier)";
        Map<String,ResCoverage> coverageMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resCoverageList)){
            for(ResCoverage coverage : resCoverageList){
                String key = coverage.getTarget()+coverage.getStrategy()+coverage.getTargetType();
                if(coverageMap.get(key)==null){
                    coverageMap.put(key, coverage);
                }
            }
        }

        Set<String> categoryPathSet = new HashSet<>();
        Map<String, ResourceCategory> categoryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resourceCategoryList)){
            for (ResourceCategory resourceCategory : resourceCategoryList){
                if(StringUtils.isNotEmpty(resourceCategory.getTaxonpath())){
                    categoryPathSet.add(resourceCategory.getTaxonpath());
                }
                if(categoryMap.get(resourceCategory.getTaxoncode())==null){
                    categoryMap.put(resourceCategory.getTaxoncode(), resourceCategory);
                }

            }
        }

        Map<String, TechInfo> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(techInfos)){
            for (TechInfo techInfo : techInfos){
                if(techInfoMap.get(techInfo.getTitle()) == null){
                    techInfoMap.put(techInfo.getTitle(), techInfo);
                }
            }
        }

        List<String> innerScriptList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", education.getPrimaryCategory());
        paramMap.put("identifier",education.getIdentifier());
        int index = 0;
        for(ResCoverage coverage : coverageMap.values()){
            String targetType = "target_type"+index;
            String strategy = "strategy" + index;
            String target = "target" +index;
            String script =  "outE().hasLabel('has_coverage').inV().has('target_type',"+targetType+")" +
                    ".has('strategy',"+strategy+").has('target',"+target+")";
            paramMap.put(targetType, coverage.getTargetType());
            paramMap.put(strategy, coverage.getStrategy());
            paramMap.put(target, coverage.getTarget());
            innerScriptList.add(script);
            index ++;
        }

        index = 0;
        for (TechInfo techInfo : techInfoMap.values()){
            String techInfoTitle = "techInfoTitle"+index;
            String script = "outE().hasLabel('has_tech_info').inV().has('ti_title',"+techInfoTitle+")";
            paramMap.put(techInfoTitle,techInfo.getTitle());
            innerScriptList.add(script);
            index ++;
        }

        index = 0;
        for (ResourceCategory resourceCategory : categoryMap.values()){
            String taxoncode = "taxoncode" + index;
            String script = "outE().hasLabel('has_category_code').inV().has('cg_taxoncode',"+taxoncode+")";

            paramMap.put(taxoncode, resourceCategory.getTaxoncode());
            innerScriptList.add(script);
            index ++;
        }

        index = 0;
        for(String path : categoryPathSet){
            String categoryPath = "path" + index;
            String script = "outE().hasLabel('has_categories_path').inV().has('cg_taxonpath',"+categoryPath+")";
            paramMap.put(categoryPath, path);
            innerScriptList.add(script);
            index ++ ;
        }
        StringBuffer checkAllScript = new StringBuffer(baseScript);
        if(CollectionUtils.isNotEmpty(innerScriptList)){
            checkAllScript.append(".and(");
            for (int i=0 ; i < innerScriptList.size() ;i++){
                if(i == 0){
                    checkAllScript.append(innerScriptList.get(i));
                } else {
                    checkAllScript.append(",").append(innerScriptList.get(i));
                }
            }
            checkAllScript.append(").id()");
        } else {
            checkAllScript.append(".id()");
        }
        Long id = null;
        try {
            id = titanCommonRepository.executeScriptUniqueLong(checkAllScript.toString(), paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(id == null){
            LOG.info("数据校验：数据有误 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(),education.getIdentifier());
            return false;
        }

        String getEdgeNumber = baseScript + ".outE().or(hasLabel('has_coverage'),hasLabel('has_tech_info')" +
                ",hasLabel('has_category_code'),hasLabel('has_categories_path')).count();";
        Map<String, Object> getEdgeNumberParam = new HashMap<>();
        getEdgeNumberParam.put("primaryCategory", education.getPrimaryCategory());
        getEdgeNumberParam.put("identifier",education.getIdentifier());
        Long edgeCount = null;
        try {
            edgeCount = titanCommonRepository.executeScriptUniqueLong(getEdgeNumber,getEdgeNumberParam);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Integer edgeCountMysql = coverageMap.size() + techInfoMap.size() + categoryMap.size() + categoryPathSet.size();
        if (edgeCount == null || edgeCount.intValue() != edgeCountMysql ){
            LOG.info("数据校验：数据有误 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(),education.getIdentifier());
            return false;
        }

        return true;
    }


    private void saveErrorSource(Education education){
        TitanSync titanSync = new TitanSync();
        titanSync.setIdentifier(UUID.randomUUID().toString());
        titanSync.setLevel(0);
        titanSync.setResource(education.getIdentifier());
        titanSync.setExecuteTimes(999);
        titanSync.setCreateTime(System.currentTimeMillis());
        titanSync.setTitle("");
        titanSync.setDescription("");
        titanSync.setPrimaryCategory(education.getPrimaryCategory());
        titanSync.setType(TitanSyncType.IMPORT_DATA_ERROR.toString());
        try {
            titanSyncRepository.add(titanSync);
        } catch (EspStoreException e) {
            e.printStackTrace();
        }
    }
}
