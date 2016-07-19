package nd.esp.service.lifecycle.utils;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.*;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by liuran on 2016/5/26.
 */
public class TitanScritpUtils {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanScritpUtils.class);
    public static Long getOneVertexOrEdegeIdByResultSet(ResultSet resultSet) {
        Iterator<Result> it = resultSet.iterator();
        try {
            if (it.hasNext()) {
                return it.next().getLong();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    //获取所有不为null的属性
    private static Map<String, Object> getParam4NotNull(Object model) {
        Map<String, Object> graphParams = new HashMap<String, Object>();
        if(model == null){
            return graphParams;
        }
        Properties db_titan_field = getDbTitanFields(model);
        List<Field> fields = new ArrayList<Field>();
        getAllDeclareField(model.getClass(), fields);
        for (Field field : fields) {
            // not need to save in titan
            if (db_titan_field == null || !db_titan_field.containsKey(field.getName()))
                continue;
            Object value = null;
            try {
                field.setAccessible(true);
                value = field.get(model);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (value == null)
                continue;

            //TODO 字符串长度过长
//            if(value instanceof String){
//                String str = (String) value;
//                if(str.length() > 1000){
//                    for(Field f : fields){
//                        f.setAccessible(true);
//                        if("identifier".equals(f.getName())){
//                            try {
//                                LOG.info("over length identifier :{} ;class:{}",f.get(model),model.getClass().getName());
//                            } catch (IllegalAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    continue;
//                }
//            }

            //BigDecimal进行转换
            if (value instanceof BigDecimal) {
                value = BigDecimalUtils.toString(value);
            }
            //把时间类型转换成Long类型
            if(value instanceof Date){
                value = ((Date)value).getTime();
            }

            graphParams.put(db_titan_field.getProperty(field.getName()), value);
        }
        return graphParams;
    }

    //获取所有为null的属性
    private static List<String> getParam4Null(Object model) {
        List<String> paramList = new ArrayList<>();
        Properties db_titan_field = getDbTitanFields(model);
        List<Field> fields = new ArrayList<Field>();
        getAllDeclareField(model.getClass(), fields);
        for (Field field : fields) {
            // not need to save in titan
            if (db_titan_field == null || !db_titan_field.containsKey(field.getName()))
                continue;
            Object value = null;
            try {
                field.setAccessible(true);
                value = field.get(model);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (value == null) {
                paramList.add(db_titan_field.getProperty(field.getName()));
            }
        }

        return paramList;
    }


    public static Map<String, Object> getParamAndChangeScript(
            StringBuffer scriptBuffer, Object model) {
        Map<String, Object> graphParams = getParam4NotNull(model);

        for (String key : graphParams.keySet()) {
            scriptBuffer.append(", '").append(key).append("', ").append(key);
        }
        return graphParams;
    }

    public static Map<String, Object> getParamAndChangeScript4Update(
            StringBuffer scriptBuffer, Object model) {
        Map<String, Object> graphParams = getParam4NotNull(model);
        List<String> nullGraphParams = getParam4Null(model);
        for (String key : graphParams.keySet()) {
            scriptBuffer.append(".property('").append(key).append("', ").append(key).append(")");
        }

        StringBuffer removeScript = null;
        if (nullGraphParams != null && nullGraphParams.size()>0) {
            removeScript = new StringBuffer();
            for (int i = 0; i < nullGraphParams.size(); i++) {
                if (i < nullGraphParams.size() - 1){
                    removeScript.append("'").append(nullGraphParams.get(i)).append("',");
                }else{
                    removeScript.append("'").append(nullGraphParams.get(i)).append("'");
                }
            }
        }

        if(removeScript != null){
            scriptBuffer.append(".properties(").append(removeScript).append(").drop()");
        }

        return graphParams;
    }

    public static void getSetScriptAndParam(StringBuffer scriptBuffer,Map<String, Object> params,
                                            String fieldName, Set<String> values){
        if(values == null){
            return;
        }
        int index = 0;
        for (String value : values){
            String paramKey = fieldName+index;
            index ++ ;
            scriptBuffer.append(".property(set,'").append(fieldName).append("',").append(paramKey).append(")");
            params.put(paramKey , value);
        }
    }

    static private void getAllDeclareField(Class<?> className,
                                           List<Field> fields) {
        if (className == null) {
            return;
        }
        fields.addAll(Arrays.asList(className.getDeclaredFields()));
        getAllDeclareField(className.getSuperclass(), fields);
    }

    private static Properties getDbTitanFields(Object model) {
        if(model instanceof Question){
            return LifeCircleApplicationInitializer.db_titan_field_ndresource_ext_questions;
        }else if(model instanceof Ebook ||
                model instanceof TeachingMaterial ||
                model instanceof  GuidanceBooks){
            return LifeCircleApplicationInitializer.db_titan_field_ndresource_ext_common;
        }else if (model instanceof Education) {
            return LifeCircleApplicationInitializer.db_titan_field_ndresource;
        }
        if (model instanceof ResourceRelation) {
            return LifeCircleApplicationInitializer.db_titan_field_relation;
        }
        if (model instanceof TechInfo) {
            return LifeCircleApplicationInitializer.db_titan_field_techinfo;
        }
        if(model instanceof ResourceCategory){
            return LifeCircleApplicationInitializer.db_titan_field_category;
        }
        if(model instanceof KnowledgeRelation){
            return LifeCircleApplicationInitializer.db_titan_field_knowledgerelation;
        }

        return new Properties();
    }

    public static Map<String, Object> buildScript(Education education, List<ResCoverage> coverageList, List<ResourceCategory> categoryList, List<TechInfo> techInfoList){
        if(education == null){
            return null;
        }

        StringBuffer script = new StringBuffer("education = graph.addVertex(T.label, primaryCategory_edu");
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory_edu",education.getPrimaryCategory());
        param.putAll(buildEducationScript(script,education));

        if(CollectionUtils.isNotEmpty(coverageList)){
           param.putAll(buildCoverageScript(script, coverageList));
        }

        if(CollectionUtils.isNotEmpty(categoryList)){
            param.putAll(buildCategoryScript(script, categoryList));
        }

//        if(CollectionUtils.isNotEmpty(techInfoList)){
//            buildTechInfoScript(script, techInfoList);
//        }
//
//        if(CollectionUtils.isNotEmpty(categoryList) ||CollectionUtils.isNotEmpty(coverageList)){
//            buildSearchPropertyScript(script,categoryList,coverageList,education);
//        }
        Map<String, Object> result = new HashMap<>();
        result.put("script",script);
        result.put("param",param);
        return result;

    }

    private static Map<String, Object> buildEducationScript(StringBuffer script, Education education){
        Map<String, Object> graphParams = getParam4NotNull(education);
        String suffix = "_edu";
        Map<String, Object> result = new HashMap<>();

        for (String key : graphParams.keySet()) {
            script.append(", '").append(key).append("', ").append(key).append(suffix);
            result.put(key+suffix, graphParams.get(key));
        }
        script.append(").id();");
        return result;
    }


    private static Map<String , Object> buildTechInfoScript(StringBuffer script, List<TechInfo> techInfos){
        if(CollectionUtils.isEmpty(techInfos)){
            return new HashMap<>();
        }

        Map<String, Object> resultParam = new HashMap<>();
        int orderNumber = 0;
        StringBuffer techInfoScript = null;
        for(TechInfo techInfo : techInfos){
            String suffix = "_ti"+orderNumber;
            Map<String, Object> techInfoParam = getParam4NotNull(techInfo);
            techInfoScript  = new StringBuffer("techinfo").append(suffix).append(" = graph.addVertex(T.label, techInfoLabel");
            for (String key : techInfoParam.keySet()) {
                techInfoScript.append(", '").append(key).append("', ").append(key).append(suffix);
                resultParam.put(key + suffix, techInfoParam.get(key));
            }
            techInfoScript.append(").id();");
            techInfoScript.append("g.V(education).next().addEdge('has_tech_info',techinfo ,'identifier',edgeIdentifier").append(suffix).append(")");
            resultParam.put("edgeIdentifier"+suffix, techInfo.getIdentifier());

            script.append(techInfoScript);
            orderNumber ++;
        }
        return resultParam;
    }

    private static Map<String , Object> buildCategoryScript(StringBuffer script, List<ResourceCategory> categories){
        if(CollectionUtils.isEmpty(categories)){
            return new HashMap<>();
        }

        Map<String, Object> resultParam = new HashMap<>();
        Set<String> categoryPathSet = new HashSet<>();
        int orderNumber = 0;
        StringBuffer categorieScript = null;

        for(ResourceCategory category : categories){
            String suffix = "_cg"+orderNumber;
            String categoryNode = "categoryNode"+suffix;
            String taxoncodeName = "taxoncode"+suffix;
            String edgeIdentifierName = "edgeIdentifier" + suffix;
            Map<String, Object> techInfoParam = getParam4NotNull(category);
            categorieScript = new StringBuffer(categoryNode+"=g.V().hasLabel('category_code').has('cg_taxoncode',"+taxoncodeName+");");
            StringBuilder addNodeScript = new StringBuilder(categoryNode+"=graph.addVertex(T.label,'category_code'");
            Map<String, Object> categoryNodeParam = getParam4NotNull(category);
            for (String key : categoryNodeParam.keySet()) {
                addNodeScript.append(", '").append(key).append("', ").append(key).append(suffix);
                resultParam.put(key + suffix, techInfoParam.get(key));
            }
            addNodeScript.append(");");

            String ifScript = "if(!"+categoryNode+".iterator().hasNext()){"+addNodeScript.toString()+"};";

            StringBuilder addEdgeScript = new StringBuilder("g.V(education).next().addEdge('has_category_code',"+categoryNode+".next(),'identifier',"+edgeIdentifierName+");");

            categorieScript.append(ifScript).append(addEdgeScript);

            resultParam.put(taxoncodeName, category.getTaxonname());
            resultParam.put(edgeIdentifierName, category.getIdentifier());

            script.append(categorieScript);

            if(StringUtils.isNotEmpty(category.getTaxonpath())){
                categoryPathSet.add(category.getTaxonpath());
            }
            orderNumber ++ ;
        }

//        orderNumber = 0;
//        for (String path : categoryPathSet){
//            String suffix = "_cgp"+orderNumber;
//            String categoryPathNode = "categoryNode"+suffix;
//            String taxonpathName = "taxonpath"+suffix;
//            StringBuffer categroyPath = new StringBuffer(categoryPathNode+"=g.V().has('categories_path','cg_taxonpath',"+taxonpathName+");");
//
//            String addNodeScript = categoryPathNode+"=graph.addVertex(T.label,'categories_path','cg_taxonpath',"+taxonpathName+");";
//            String ifScript = "if(!"+categoryPathNode+".iterator().hasNext()){"+addNodeScript+"};";
//
//            StringBuilder addEdgeScript = new StringBuilder("g.V(education).next().addEdge('has_categories_path',"+categoryPathNode+".next());");
//            categroyPath.append(ifScript).append(addEdgeScript);
//
//            script.append(categroyPath);
//
//            resultParam.put(taxonpathName, path);
//            orderNumber ++ ;
//        }


        return resultParam;
    }

    private static Map<String, Object> buildCoverageScript(StringBuffer script, List<ResCoverage> coverages){
        if(CollectionUtils.isEmpty(coverages)){
            return new HashMap<>();
        }

        Map<String, Object> resultParam = new HashMap<>();
        int orderNumber = 0;
        StringBuffer coverageScript = null;
        for(ResCoverage coverage : coverages){
            String suffix = "_cov"+orderNumber;
            String targetTypeName = "target_type" +suffix;
            String targetName = "target" +suffix;
            String strategyName = "strategy" +suffix;
            String coverageNodeName = "coverageNode"+suffix;
            String edgeIdentifierName = "edgeIdentifier"+suffix;
            coverageScript = new StringBuffer(coverageNodeName+"=g.V().hasLabel('coverage').has('target_type',"+targetTypeName+").has('target',"+targetName+").has('strategy',"+strategyName+");");
            StringBuilder addCoverageNodeScript = new StringBuilder(coverageNodeName+ "= graph.addVertex(T.label,'coverage','target_type',"+targetTypeName+",'strategy',"+strategyName+",'target',"+targetName+");");
            String ifScript = "if(!"+coverageNodeName+".iterator().hasNext()){"+addCoverageNodeScript+"};";
            String addEdgeScript = "g.V().has('identifier',identifier_edu).next().addEdge('has_coverage',"+coverageNodeName+".next(),'identifier',"+edgeIdentifierName+").id();";
//            addEdgeScript = "";
            coverageScript.append(ifScript).append(addEdgeScript);

            script.append(coverageScript);
            resultParam.put(targetTypeName, coverage.getTargetType());
            resultParam.put(targetName, coverage.getTarget());
            resultParam.put(strategyName, coverage.getStrategy());
            resultParam.put(edgeIdentifierName, coverage.getIdentifier());
            orderNumber ++;
        }
        return resultParam;
    }

    private static Map<String, Object> buildRelationScript(StringBuffer script, List<ResourceRelation> resourceRelations){
        int orderNumber = 0;
        Map<String, Object> result = new HashMap<>();
        for(ResourceRelation resourceRelation : resourceRelations){
            String suffix = "_rr"+orderNumber;
            String sourcePrimaryCategoryName = "source_primaryCategory"+suffix;
            String sourceIdentifierName = "source_identifier"+suffix;
            String targetPrimaryCategoryName = "target_primaryCategory"+suffix;
            String targetIdentifierName = "target_identifier"+suffix;
            String edgeIdentifierName = "edgeIdentifier"+suffix;

            StringBuffer scriptBuffer = new StringBuffer(
                    "g.V().hasLabel("+sourcePrimaryCategoryName+").has('identifier',"+sourceIdentifierName+").next()" +
                            ".addEdge('has_relation'," +
                            "g.V().hasLabel("+targetPrimaryCategoryName+").has('identifier',"+targetIdentifierName+").next(),'identifier',"+edgeIdentifierName);

            Map<String, Object> createRelationParams = getParam4NotNull(resourceRelation);
            for (String key : createRelationParams.keySet()) {
                scriptBuffer.append(", '").append(key).append("', ").append(key).append(suffix);
                result.put(key + suffix, createRelationParams.get(key));
            }

            scriptBuffer.append(").id()");

            createRelationParams.put("sourcePrimaryCategoryName", resourceRelation.getResType());
            createRelationParams.put("sourceIdentifierName", resourceRelation.getSourceUuid());
            createRelationParams.put("targetPrimaryCategoryName", resourceRelation.getResourceTargetType());
            createRelationParams.put("targetIdentifierName", resourceRelation.getTarget());
            createRelationParams.put("edgeIdentifierName", resourceRelation.getIdentifier());

            script.append(scriptBuffer);
        }

        return result;
    }

    private static Map<String, Object> buildSearchPropertyScript(StringBuffer script,List<ResourceCategory> categories, List<ResCoverage> coverages ,Education education){
        Set<String> resCoverages = new HashSet<>() ;
        Set<String> categoryCodes = new HashSet<>();
        Set<String> paths = new HashSet<>();
        if(CollectionUtils.isNotEmpty(coverages)){
            for(ResCoverage resCoverage : coverages){
                String setValue4 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy()+"/"+education.getStatus();
                String setValue3 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//"+education.getStatus();
                String setValue2 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy()+"/";
                String setValue1 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//";
                resCoverages.add(setValue1);
                resCoverages.add(setValue2);
                resCoverages.add(setValue3);
                resCoverages.add(setValue4);
            }
        }

        if(CollectionUtils.isNotEmpty(categories)){
            for(ResourceCategory category : categories){
                if(StringUtils.isNotEmpty(category.getTaxonpath())){
                    paths.add(category.getTaxonpath());
                }
                if(StringUtils.isNotEmpty(category.getTaxoncode())){
                    categoryCodes.add(category.getTaxoncode());
                }

            }
        }

        StringBuffer searchPropertyScript = new StringBuffer("g.V(education)");
        Map<String, Object> param = new HashMap<>();

        addSetProperty("search_coverage",resCoverages,searchPropertyScript,param);
        addSetProperty("search_code",categoryCodes,searchPropertyScript,param);
        addSetProperty("search_path",paths,searchPropertyScript,param);

        script.append(searchPropertyScript);
        return param;
    }

    private static void addSetProperty(String fieldName,
                                Set<String> values ,StringBuffer script ,Map<String, Object> param) {
        if(values == null || values.size() == 0){
            return;
        }
        int index = 0;
        for (String value : values){
            String paramKey = fieldName+index;
            index ++ ;
            script.append(".property(set,'").append(fieldName).append("',").append(paramKey).append(")");
            param.put(paramKey , value);
        }
    }

    public static  void main(String[] args){
        StringBuffer scriptBuffer = new StringBuffer();
        String name = "name";
        Map<String, Object> createRelationParams = TitanScritpUtils
                .getParamAndChangeScript(scriptBuffer, null);
        System.out.println(scriptBuffer.toString());
    }

}
