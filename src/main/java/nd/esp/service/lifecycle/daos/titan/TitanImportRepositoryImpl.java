package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.titanV07.NDResourceTitanService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
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
    private TitanRelationRepository titanRelationRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Autowired
    private NDResourceTitanService ndResourceTitanService;

    @Autowired
    private NDResourceService ndResourceService;

    @Autowired
    private TitanStatisticalRepository titanStatisticalRepository;

    @Override
    /**
     * @return false 导入数据失败 ；true 导入成功
     * */
    public boolean importOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {

        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList).get(education.getIdentifier());
        List<ResourceCategory> categoryList = TitanResourceUtils.distinctCategory(resourceCategoryList).get(education.getIdentifier());
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos).get(education.getIdentifier());
        List<String> categoryPathList = TitanResourceUtils.distinctCategoryPath(resourceCategoryList);

        //生成导入资源的脚本
        Map<String, Object> result = TitanScritpUtils.buildScript(education,coverageList,categoryList,techInfoList,categoryPathList);
        //校验addVertex中的参数个数过多，个数超过250返回为null
        if(CollectionUtils.isEmpty(result)){
            //TODO 这种错误情况保存到数据库中，返回true
            titanSync(education);
        } else {
            Long educationId = null;
            try {
                String script = result.get("script").toString();
                Map<String, Object> param = (Map<String, Object>) result.get("param");
                educationId = titanCommonRepository.executeScriptUniqueLong(script, param);
            } catch (Exception e) {
                LOG.error("titanImportErrorData:{}" ,education.getIdentifier());
                return  false;
            }
            if(educationId == null){
            	titanSync(education);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean importStatistical(List<ResourceStatistical> statisticalList) {
//        Map<TitanScritpUtils.KeyWords, Object> result = TitanScritpUtils.buildStatisticalScript(statisticalList);
//        String script = result.get(TitanScritpUtils.KeyWords.script).toString();
//        Map<String, Object> param = (Map<String, Object>) result.get(TitanScritpUtils.KeyWords.params);
//        try {
//            titanCommonRepository.executeScript(script, param);
//        } catch (Exception e) {
//            LOG.info(e.getLocalizedMessage());
//        }
        titanStatisticalRepository.batchAdd4Import(statisticalList);
        return false;
    }

    /**
     * 导入关系，使用创建关系脚本
     * */
    @Override
    public boolean batchImportRelation(List<ResourceRelation> resourceRelation) {
        titanRelationRepository.batchAdd4Import(resourceRelation);
        return false;
    }

    /**
     * 检查资源在titan中是否存在
     * */
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
           LOG.info(e.getLocalizedMessage());
        }

        if(count == null || count == 0){

            titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
        } else if(count > 1){
            titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
        } else {
            return true;
        }

        return false;
    }

    /**
     * 检查资源的coverage、techInfo、category是否存在
     * 1、校验资源是否存在  2、检验关系的条数是否合理  3、校验mysql对应的关系是否存在
     * */
    @Override
    //FIXME 现在边上都有ID，后面通过ID对边进行检测
    public boolean checkResourceAllInTitan(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        String baseScript = "g.V().has(primaryCategory,'identifier',identifier)";

        List<String> categoryPathSet = TitanResourceUtils.distinctCategoryPath(resourceCategoryList);
        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList).get(education.getIdentifier());
        List<ResourceCategory> categoryList = TitanResourceUtils.distinctCategory(resourceCategoryList).get(education.getIdentifier());
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos).get(education.getIdentifier());

        //产生资源校验的脚本
        List<String> innerScriptList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", education.getPrimaryCategory());
        paramMap.put("identifier",education.getIdentifier());
        for(int index = 0; index < coverageList.size() ;index ++){
            ResCoverage coverage = coverageList.get(index);
            String targetType = "target_type"+index;
            String strategy = "strategy" + index;
            String target = "target" +index;
            String script =  "outE().hasLabel('has_coverage').inV().has('target_type',"+targetType+")" +
                    ".has('strategy',"+strategy+").has('target',"+target+")";
            paramMap.put(targetType, coverage.getTargetType());
            paramMap.put(strategy, coverage.getStrategy());
            paramMap.put(target, coverage.getTarget());
            innerScriptList.add(script);
        }

        for (int index =0 ;index < techInfoList.size() ;index++){
            TechInfo techInfo = techInfoList.get(index);
            String techInfoTitle = "techInfoTitle"+index;
            String script = "outE().hasLabel('has_tech_info').inV().has('ti_title',"+techInfoTitle+")";
            paramMap.put(techInfoTitle,techInfo.getTitle());
            innerScriptList.add(script);
        }

        for (int index =0 ;index <categoryList.size() ;index++){
            ResourceCategory resourceCategory = categoryList.get(index);
            String taxoncode = "taxoncode" + index;
            String script = "outE().hasLabel('has_category_code').inV().has('cg_taxoncode',"+taxoncode+")";

            paramMap.put(taxoncode, resourceCategory.getTaxoncode());
            innerScriptList.add(script);
        }

        for(int index =0 ;index <categoryPathSet.size() ;index ++){
            String path = categoryPathSet.get(index);
            String categoryPath = "path" + index;
            String script = "outE().hasLabel('has_categories_path').inV().has('cg_taxonpath',"+categoryPath+")";
            paramMap.put(categoryPath, path);
            innerScriptList.add(script);
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
            LOG.error(e.getLocalizedMessage());
        }

        //校验资源是否存在
        if(id == null){
            LOG.info("数据校验：数据不存在 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(),education.getIdentifier());
            return false;
        }

        //保证titan里面不会出现多余的数据
        String getEdgeNumber = baseScript + ".outE().or(hasLabel('has_coverage'),hasLabel('has_tech_info')" +
                ",hasLabel('has_category_code'),hasLabel('has_categories_path')).count();";
        Map<String, Object> getEdgeNumberParam = new HashMap<>();
        getEdgeNumberParam.put("primaryCategory", education.getPrimaryCategory());
        getEdgeNumberParam.put("identifier",education.getIdentifier());
        Long edgeCount = null;
        try {
            edgeCount = titanCommonRepository.executeScriptUniqueLong(getEdgeNumber,getEdgeNumberParam);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }

        //category在保存的时候没有做去重处理，在导入数据的时候有做去重处理
        if(CollectionUtils.isEmpty(resourceCategoryList)){
            resourceCategoryList = new ArrayList<>();
        }
        Integer edgeCountMysql = coverageList.size() + techInfoList.size() + resourceCategoryList.size() + categoryPathSet.size();
        if (edgeCount == null || edgeCount.intValue() > edgeCountMysql ){
            LOG.info("数据校验：数据有误 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(),education.getIdentifier());
            return false;
        }

        return true;
    }

    @Override
    public boolean checkResourceAllInTitanDetail(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        List<String> includes = new ArrayList<>();
        includes.add(IncludesConstant.INCLUDE_CG);
        includes.add(IncludesConstant.INCLUDE_EDU);
        includes.add(IncludesConstant.INCLUDE_TI);
        includes.add(IncludesConstant.INCLUDE_CR);
        includes.add(IncludesConstant.INCLUDE_LC);

        //通过获取详情检查资源的每个字段
        ResourceModel titan = ndResourceService.getDetail(education.getPrimaryCategory(), education.getIdentifier(), includes, true);
        ResourceModel mysql = ndResourceTitanService.getDetail(education.getPrimaryCategory(), education.getIdentifier(), includes, true);
        boolean success = equalModel(titan, mysql);


        //校验mysql关系的条数等于titan边的条数据
        String baseScript = "g.V().has(primaryCategory,'identifier',identifier).outE().or(hasLabel('has_coverage'),hasLabel('has_tech_info')" +
                ",hasLabel('has_category_code'),hasLabel('has_categories_path')).count();";

        //校验coverage数据正确性


        //校验资源关系数据正确性


        //校验章节知识点关系正确性

        return false;
    }


    private void titanSync(Education education){
        titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.IMPORT_DATA_ERROR,
                education.getPrimaryCategory(), education.getIdentifier(),999);
    }

    /**
     * 保存异常数据到titan sync表中
     * */
    private void titanSync(TitanSyncType syncType, String parmaryCategory, String identifier){
        titanRepositoryUtils.titanSync4MysqlAdd(syncType, parmaryCategory, identifier,999);
    }

    private boolean equalModel(ResourceModel titan, ResourceModel mysql){
        return true;
    }

    public static void main(String[] args){
        System.out.println(Float.MAX_VALUE);
    }
}
