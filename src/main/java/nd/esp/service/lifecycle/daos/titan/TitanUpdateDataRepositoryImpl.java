package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
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
            .getLogger(TitanUpdateDataRepositoryImpl.class);

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    /**
     * 更新资源的相关属性
     * */
    @Override
    public boolean updateOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {
        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList).get(education.getIdentifier());
        List<ResourceCategory> categoryList = TitanResourceUtils.distinctCategory(resourceCategoryList).get(education.getIdentifier());
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos).get(education.getIdentifier());

        //更新资源并检查资源在titan中是否存在，不存在不进行后续的操作
        if(!updateEducation(education)){
            titanSync(TitanSyncType.UPDATE_DATA_RESOURCE,education.getPrimaryCategory(),education.getIdentifier());
        }else {
            batchUpdateCategory(categoryList);
            batchUpdateCoverage(coverageList);
            batchUpdateTechInfo(techInfoList);
        }
        return false;
    }

    /**
     * 修复关系
     * */
    public void batchUpdateRelation(List<ResourceRelation> relationList){
        for (ResourceRelation resourceRelation : relationList){
            boolean success = repairEdge(resourceRelation,TitanKeyWords.has_relation.toString());
            if (!success){
                titanSync(TitanSyncType.UPDATE_DATA_RELATION, "RELATION", resourceRelation.getIdentifier());
            }
        }
    }

    /**
     * 修复资源自定字段
     * */
    private boolean updateEducation(Education education){
        Education repairEducation = new Education();
        repairEducation.setmIdentifier(education.getmIdentifier());
        repairEducation.setNdresCode(education.getNdresCode());
        repairEducation.setHasRight(education.getHasRight());
        repairEducation.setRightStartDate(education.getRightStartDate());
        repairEducation.setRightEndDate(education.getRightEndDate());
        repairEducation.setContext(education.getContext());
        repairEducation.setProviderMode(education.getProviderMode());
        //Education中enable有默认值需要重设置
        repairEducation.setEnable(null);
        return repairNode(repairEducation,education.getPrimaryCategory(),education.getIdentifier());
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

    /**
     * 修复TechInfo
     * */
    private TechInfo updateTechInfo(TechInfo techInfo){
//        StringBuffer scriptBuffer;
//        Map<String, Object> graphParams;
//        scriptBuffer = new StringBuffer("g.V().has('identifier',identifier)");
//        graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer, techInfo);
//        scriptBuffer.append(";");
//        graphParams.put("identifier", techInfo.getIdentifier());

//        try {
//            titanCommonRepository.executeScript(scriptBuffer.toString(), graphParams);
//        } catch (Exception e) {
//            //TODO titan sync
//            return null;
//        }

        boolean success =  repairEdge(techInfo, TitanKeyWords.has_tech_info.toString());
        if(!success){
            titanSync(TitanSyncType.UPDATE_DATA_TECH, techInfo.getResType(), techInfo.getResource());

        }

        return  techInfo;
    }

    private String batchUpdateCoverage(List<ResCoverage> coverageList) {
        for (ResCoverage resCoverage : coverageList) {
            boolean success =  repairEdge(resCoverage, TitanKeyWords.has_coverage.toString());
            if(!success){
                titanSync(TitanSyncType.UPDATE_DATA_COVERAGE, resCoverage.getResType(), resCoverage.getResource());
            }

        }
        return null;
    }



    private String batchUpdateCategory(List<ResourceCategory> categoryList) {
        for (ResourceCategory resourceCategory : categoryList) {

            boolean success = repairEdge(resourceCategory, TitanKeyWords.has_category_code.toString());
            if(!success){
                titanSync(TitanSyncType.UPDATE_DATA_CATEGORY, resourceCategory.getPrimaryCategory(), resourceCategory.getResource());
            }
        }
        return null;
    }

    /**
     * 修复边,全量更新属性
     * */
    private boolean repairEdge(EspEntity entity, String label){
        StringBuffer script = new StringBuffer("g.E().has(edgeLabel,'identifier',identifier)");
        Map<String, Object> graphParams;
        graphParams = TitanScritpUtils.getParamAndChangeScript4Repair(script, entity);
        graphParams.put("edgeLabel",label);
        graphParams.put("identifier",entity.getIdentifier());
        script.append(".id()");
        String id = null;
        try {
           id= titanCommonRepository.executeScriptUniqueString(script.toString(), graphParams);
        } catch (Exception e) {
           LOG.error(e.getLocalizedMessage());
            return false;
        }

        if(id == null){
            return false;
        }

        return true;
    }

    /**
     * 修复一个节点，通过增量的方式修复
     * */
    private boolean repairNode(Education entity, String label ,String identifier){
        StringBuffer script = new StringBuffer("g.V().has(nodeLabel,'identifier',identifier)");
        Map<String, Object> graphParams;
        //获取Education中不为null的属性
        graphParams = TitanScritpUtils.getParamAndChangeScript4Repair(script, entity);
        graphParams.put("nodeLabel",label);
        graphParams.put("identifier",identifier);
        script.append(".id()");
        String id = null;
        try {
            id= titanCommonRepository.executeScriptUniqueString(script.toString(), graphParams);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            return false;
        }

        if(id == null){
            return false;
        }

        return true;
    }

    /**
     * 保存异常数据到titan sync表中
     * */
    private void titanSync(TitanSyncType syncType, String parmaryCategory, String identifier){
        titanRepositoryUtils.titanSync4MysqlAdd(syncType, parmaryCategory, identifier,999);
    }
    
}
