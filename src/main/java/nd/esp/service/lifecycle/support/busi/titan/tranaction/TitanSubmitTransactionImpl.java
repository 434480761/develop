package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import nd.esp.service.lifecycle.utils.titan.script.model.EducationToTitanBeanUtils;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
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
        Map<String, String> educationIds = new HashMap<>();
        long time1 = System.currentTimeMillis();

        for (TitanRepositoryOperation operation : repositoryOperations) {
            TitanOperationType type = operation.getOperationType();
            switch (type) {
                case add: case update:
                    if (operation.getEntity() instanceof Education){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                    }
                    if (operation.getEntity() instanceof ResCoverage) {
                        educationIds.put(((ResCoverage) operation.getEntity()).getResource(),
                                ((ResCoverage) operation.getEntity()).getResType());
                        //删除边
                        builder.delete(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if(operation.getEntity() instanceof ResourceCategory){
                        educationIds.put(((ResourceCategory) operation.getEntity()).getResource(),
                                ((ResourceCategory) operation.getEntity()).getPrimaryCategory());
                        //删除边
                        builder.delete(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if(operation.getEntity() instanceof  TechInfo){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    }else if (operation.getEntity() instanceof ResourceStatistical){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if (operation.getEntity() instanceof ResourceRelation){
                        builder.delete(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    }
                    break;
                case delete:
                    titanRepository.delete(operation.getEntity().getIdentifier());
                    break;
                default:
                    LOG.info("没有对应的处理方法");
            }
        }

        System.out.println("time_for:"+(System.currentTimeMillis() - time1));
        builder.scriptEnd();

        Map<String, Object> param = builder.getParam();
        StringBuilder script = builder.getScript();
        if (param != null && param.size() > 0) {
            String id = null;
            try {
                long time = System.currentTimeMillis();
                //创建
                id = titanCommonRepository.executeScriptUniqueString(script.toString(), param);

                System.out.println("执行脚本:"+(System.currentTimeMillis() - time));
                time = System.currentTimeMillis();

                //测试用临时使用的更新办法
                for (String identifier : educationIds.keySet()){
                    updateEducation(educationIds.get(identifier), identifier);
                }
                System.out.println("更新冗余字段:"+(System.currentTimeMillis() - time));

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

    private Map<String, Object>  getUpdateEducation(List<TitanRepositoryOperation> operationList , String identifier){
        List<ResourceCategory> categoryList = new ArrayList<>();
        List<ResCoverage> coverageList = new ArrayList<>();
        String status = null;
        String primaryCategory = null;
        for (TitanRepositoryOperation operation : operationList){
            EspEntity entity = operation.getEntity();
            if (entity instanceof Education && identifier.equals(entity.getIdentifier())){
                status = ((Education) entity).getStatus();
                primaryCategory = ((Education) entity).getPrimaryCategory();
            }

            if (entity instanceof ResourceCategory && identifier.equals(((ResourceCategory) entity).getResource())){
                categoryList.add((ResourceCategory) entity);
            }

            if (entity instanceof ResCoverage && identifier.equals(((ResCoverage) entity).getResource())){
                coverageList.add((ResCoverage) entity);
            }

        }

        return updateEducation(primaryCategory,identifier,categoryList,coverageList,status);
    }

    private boolean updateEducation(String primaryCategory, String identifier){
        String script = "other=g.V().has(primaryCategory,'identifier',identifier)" +
                ".as('x').union(outE('has_coverage','has_category_code')).valueMap(true);" +
                "status=g.V().has(primaryCategory,'identifier',identifier).valueMap('lc_status');" +
                "List<Object> otherList=other.toList();List<Object> enableList=status.toList();otherList << enableList[0];";

        Map<String, Object> param = new HashMap<>();
        param.put("identifier",identifier);
        param.put("primaryCategory",primaryCategory);
        List<Map<String, String>>  resultMap = new LinkedList<>();
        try {
            ResultSet resultSet = titanCommonRepository.executeScriptResultSet(script, param);
            List<String> reslutList = new ArrayList<>();
            Iterator<Result> iterator = resultSet.iterator();
            while (iterator.hasNext()) {
                reslutList.add(iterator.next().getString());
            }

            resultMap = TitanResultParse.changeStrToKeyValue(reslutList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String status = "";
        List<ResourceCategory> resourceCategoryList = new ArrayList<>();
        List<ResCoverage> resCoverageList = new ArrayList<>();
        for (Map<String, String> map : resultMap){
            if (map.containsKey("lc_status")){
                status = map.get("lc_status");
            }

            if (TitanKeyWords.has_category_code.toString().equals(map.get("label"))){
                ResourceCategory resourceCategory = new ResourceCategory();
                resourceCategory.setTaxoncode(map.get("cg_taxoncode"));
                resourceCategory.setTaxonpath(map.get("cg_taxonpath"));
                resourceCategoryList.add(resourceCategory);
            }

            if (TitanKeyWords.has_coverage.toString().equals(map.get("label"))){
                ResCoverage resCoverage = new ResCoverage();
                resCoverage.setTargetType(map.get("target_type"));
                resCoverage.setStrategy(map.get("strategy"));
                resCoverage.setTarget(map.get("target"));
                resCoverageList.add(resCoverage);
            }

        }

        Map<String,Object> result = updateEducation(primaryCategory,identifier,resourceCategoryList,resCoverageList,status);
        StringBuffer scriptResult = (StringBuffer) result.get("script");
        Map<String, Object> paramResult = (Map<String, Object>) result.get("param");

        String dropScript = "g.V().has(primaryCategory,'identifier',identifier)." +
                "properties('search_coverage','search_code','search_path','search_path_string','search_code_string','search_coverage_string').drop()";
        Map<String, Object> dropParam = new HashMap<>();
        dropParam.put("primaryCategory", primaryCategory);
        dropParam.put("identifier", identifier);

        try {
            titanCommonRepository.executeScript(dropScript, dropParam);
            titanCommonRepository.executeScript(scriptResult.toString(), paramResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Map<String,Object> updateEducation(String primaryCategory, String identifier ,List<ResourceCategory> categoryList,
                                    List<ResCoverage> coverageList, String status){
        Set<String> resCoverages = new HashSet<>();
        for (ResCoverage resCoverage : coverageList) {
            resCoverages.addAll(TitanScritpUtils.getAllResourceCoverage(resCoverage, status));
        }

        Set<String> paths = new HashSet<>(TitanResourceUtils.distinctCategoryPath(categoryList));
        Set<String> categoryCodes = new HashSet<>(TitanResourceUtils.distinctCategoryCode(categoryList));

        StringBuffer script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).property('primary_category',primaryCategory)");
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", primaryCategory);
        param.put("identifier", identifier);

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

        Map<String, Object> result = new HashMap<>();
        result.put("script",script);
        result.put("param", param);
        return result;
    }

}
