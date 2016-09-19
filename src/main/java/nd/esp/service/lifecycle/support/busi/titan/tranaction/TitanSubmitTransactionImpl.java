package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import nd.esp.service.lifecycle.utils.titan.script.model.EducationToTitanBeanUtils;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanSubmitTransactionImpl implements TitanSubmitTransaction {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanSubmitTransactionImpl.class);
    @Autowired
    private TitanRepository titanRepository;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private CoverageDao coverageDao;

    @Autowired
    private NDResourceDao ndResourceDao;

    @Override
    public boolean submit(TitanTransaction transaction) {
        //TODO 可以做事务的重试
        LinkedList<TitanRepositoryOperation> repositoryOperations = transaction.getAllStep();
        boolean success = true;
        long t1 =  System.currentTimeMillis();
        submit(repositoryOperations);
        long t2 =  System.currentTimeMillis();
        System.out.println(t2 - t1);

        //TODO 每个事务中需要获取资源的类型和ID，方案一：在事务名中存放类型和ID；方案二：在需要的时候再进行解析
        if (!success){
//            titanRepositoryUtils.titanSync4MysqlAdd();
        }

        return true;
    }

    private boolean submit(LinkedList<TitanRepositoryOperation> repositoryOperations){
        TitanScriptBuilder  builder = new TitanScriptBuilder();
        List<String> deleteEdge = new ArrayList<>();
        List<String> deleteVertex = new ArrayList<>();
        List<TitanScriptBuilder> tsbList = new ArrayList<>();
        Map<String, String> educationIds = new HashMap<>();
        List<TitanScriptBuilder> tsbEducations = new ArrayList<>();


        for (TitanRepositoryOperation operation : repositoryOperations) {
            TitanOperationType type = operation.getOperationType();
            switch (type) {
                case add: case update:
                    if (operation.getEntity() instanceof ResCoverage) {
                        educationIds.put(((ResCoverage) operation.getEntity()).getResource(),
                                ((ResCoverage) operation.getEntity()).getResType());
                        deleteEdge.add(operation.getEntity().getIdentifier());

                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if(operation.getEntity() instanceof ResourceCategory){
                        educationIds.put(((ResourceCategory) operation.getEntity()).getResource(),
                                ((ResourceCategory) operation.getEntity()).getPrimaryCategory());
                        deleteEdge.add(operation.getEntity().getIdentifier());

                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if(operation.getEntity() instanceof  TechInfo){
                        deleteEdge.add(operation.getEntity().getIdentifier());
                        deleteVertex.add(operation.getEntity().getIdentifier());

                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    }else if (operation.getEntity() instanceof ResourceStatistical){
                        deleteEdge.add(operation.getEntity().getIdentifier());
                        deleteVertex.add(operation.getEntity().getIdentifier());

                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if (operation.getEntity() instanceof ResourceRelation){
                        deleteEdge.add(operation.getEntity().getIdentifier());
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if (operation.getEntity() instanceof Education){
                        educationIds.put(operation.getEntity().getIdentifier(), ((Education) operation.getEntity()).getPrimaryCategory());
                        //删除冗余字段和null的属性
                        TitanScriptBuilder tsb = new TitanScriptBuilder();
                        tsb.deleteNullProperty(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        tsbList.add(tsb);
                        TitanScriptBuilder educationBuilder = new TitanScriptBuilder();
                        educationBuilder.addOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        tsbEducations.add(educationBuilder);
                    }
                    break;
                case delete:
                    titanRepository.delete(operation.getEntity().getIdentifier());
                    break;
                default:
                    LOG.info("没有对应的处理方法");
            }
        }

        Map<String, Object> param = builder.getParam();
        StringBuilder script = builder.getScript();
        if (param != null && param.size() > 0) {
            String id = null;
            try {
                //删除资源的null属性和冗余字段数据
                for (TitanScriptBuilder tsb : tsbList){
                    titanCommonRepository.executeScript(tsb.getScript().toString(), tsb.getParam());
                }

                for (TitanScriptBuilder tsb : tsbEducations){
                    titanCommonRepository.executeScript(tsb.getScript().toString(),tsb.getParam());
                }

                Thread.sleep(100);

                //删除边和节点
                if (deleteEdge.size() != 0)
                    batchDelete(deleteEdge,"edge");
                if (deleteVertex.size() != 0){
                    batchDelete(deleteVertex,"vertex");
                }



                //创建
                id = titanCommonRepository.executeScriptUniqueString(script.toString(), param);

                //测试用临时使用的更新办法
                for (String identifier : educationIds.keySet()){
                    updateEducation(educationIds.get(identifier), identifier);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }


    private boolean batchDelete(List<String> identifierList, String type){
        String titanType;
        if ("edge".equals(type)){
            titanType = "E";
        } else {
            titanType = "V";
        }
        StringBuilder scriptBuilder = new StringBuilder(
                "g."+titanType+"().has('identifier',");
        StringBuilder withInScript = new StringBuilder("within(");

        Map<String, Object> params = new HashMap<>();
        for (int i=0; i <identifierList.size() ;i ++){
            String indentifierName = "identifier"+i;
            if(i == 0){
                withInScript.append(indentifierName);
            } else {
                withInScript.append(",").append(indentifierName);
            }
            params.put(indentifierName, identifierList.get(i));
        }
        withInScript.append(")");
        scriptBuilder.append(withInScript).append(")").append(".drop()");

        try {
            titanCommonRepository.executeScript(scriptBuilder.toString(), params);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean updateEducation(String primaryCategory, String identifier){
        Education education = getEducation(primaryCategory, identifier);
        Set<String> uuids = new HashSet<>();
        uuids.add(education.getIdentifier());
        //后去coverage、category
        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceRepositoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);

        Map<String, List<ResCoverage>> coverageMap = TitanResourceUtils.groupCoverage(resCoverageList);
        Map<String, List<ResourceCategory>> categoryMap = TitanResourceUtils.groupCategory(resourceRepositoryList);

        Set<String> resCoverages = new HashSet<>();
        List<ResCoverage> tempCoverageList = coverageMap.get(education.getIdentifier());
        List<ResourceCategory> tempCategoryList = categoryMap.get(education.getIdentifier());
        if (CollectionUtils.isNotEmpty(tempCoverageList)) {
            for (ResCoverage resCoverage : tempCoverageList) {
                resCoverages.addAll(TitanScritpUtils.getAllResourceCoverage(resCoverage, education.getStatus()));
            }
        }

        Set<String> paths = new HashSet<>(TitanResourceUtils.distinctCategoryPath(tempCategoryList));
        Set<String> categoryCodes = new HashSet<>(TitanResourceUtils.distinctCategoryCode(tempCategoryList));

        StringBuffer script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).property('primary_category',primaryCategory)");
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", education.getPrimaryCategory());
        param.put("identifier", education.getIdentifier());

        TitanScritpUtils.getSetScriptAndParam(script,param, TitanKeyWords.search_code.toString(),categoryCodes);
        TitanScritpUtils.getSetScriptAndParam(script,param, TitanKeyWords.search_coverage.toString(),resCoverages);
        TitanScritpUtils.getSetScriptAndParam(script,param, TitanKeyWords.search_path.toString(),paths);

        if (CollectionUtils.isNotEmpty(paths)) {
            String searchPathString = StringUtils.join(paths, ",").toLowerCase();
            script.append(".property('search_path_string',searchPathString)");
            param.put("searchPathString", searchPathString);

        }

        if (CollectionUtils.isNotEmpty(categoryCodes)) {
            String searchCodeString = StringUtils.join(categoryCodes, ",").toLowerCase();
            script.append(".property('search_code_string',searchCodeString)");
            param.put("searchCodeString", searchCodeString);
        }

        if (CollectionUtils.isNotEmpty(resCoverages)) {
            String searchCoverageString = StringUtils.join(resCoverages, ",").toLowerCase();
            script.append(".property('search_coverage_string',searchCoverageString)");
            param.put("searchCoverageString", searchCoverageString);
        }

        String dropScript = "g.V().has(primaryCategory,'identifier',identifier)." +
                "properties('search_coverage','search_code','search_path','search_path_string','search_code_string','search_coverage_string').drop()";
        Map<String, Object> dropParam = new HashMap<>();
        dropParam.put("primaryCategory", primaryCategory);
        dropParam.put("identifier", education.getIdentifier());


        try {
            titanCommonRepository.executeScript(dropScript, dropParam);
            titanCommonRepository.executeScript(script.toString(), param);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}", e.getMessage());
            return false;
        }
        return true;
    }

    private Education getEducation(String primaryCategory, String identifier) {
        String pc = primaryCategory;
        if ("guidancebooks".equals(primaryCategory)) {
            pc = "teachingmaterials";
        }

        EspRepository<?> espRepository = ServicesManager.get(pc);
        Education education = null;
        try {
            education = (Education) espRepository.get(identifier);
        } catch (EspStoreException e) {
            e.printStackTrace();
        }

        return education;
    }
}
