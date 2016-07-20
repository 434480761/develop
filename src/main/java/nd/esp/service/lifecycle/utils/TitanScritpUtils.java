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

    public static Map<String, Object> buildScript(Education education,
                                                  List<ResCoverage> coverageList,
                                                  List<ResourceCategory> categoryList,
                                                  List<TechInfo> techInfoList ,
                                                  List<String> categoryPathList){
        if(education == null){
            return null;
        }

        StringBuffer script = new StringBuffer("");
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory_edu",education.getPrimaryCategory());
        param.putAll(buildEducationScript(script,education,categoryList,coverageList));

        if(CollectionUtils.isNotEmpty(coverageList)){
           param.putAll(buildCoverageScript(script, coverageList));
        }

        if(CollectionUtils.isNotEmpty(categoryPathList)){
            buildPathScript(script, categoryPathList);
        }

        if(CollectionUtils.isNotEmpty(categoryList)){
            param.putAll(buildCategoryScript(script, categoryList));
        }

        if(CollectionUtils.isNotEmpty(techInfoList)){
            param.putAll(buildTechInfoScript(script, techInfoList));
        }

        script.append("educationId=createEducation();"+
                "createCoverage(educationId);"+
                "createCategoriesPath(educationId);"+
                "createCategories(educationId);"+
                "createTechInfo(educationId);"
        );
        Map<String, Object> result = new HashMap<>();
        result.put("script",script);
        result.put("param",param);
        return result;

    }

    private static Map<String, Object> buildEducationScript(StringBuffer script, Education education ,List<ResourceCategory> categories, List<ResCoverage> coverages){
        Map<String, Object> graphParams = getParam4NotNull(education);
        String suffix = "_edu";
        Map<String, Object> result = new HashMap<>();

        StringBuilder educationScript = new StringBuilder(
                "public Long createEducation(){education=graph.addVertex(T.label, primaryCategory_edu");
        for (String key : graphParams.keySet()) {
            educationScript.append(",'").append(key).append("',").append(key).append(suffix);
            result.put(key+suffix, graphParams.get(key));
        }

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

        int index = 0;
        if(CollectionUtils.isNotEmpty(resCoverages)){
            for (String coverage : resCoverages) {
                String propertyName = "search_coverage" +index;
                educationScript.append(",'").append("search_coverage").append("',").append(propertyName);
                result.put(propertyName, coverage);
                index ++;
            }
            String searchCoverageString = StringUtils.join(resCoverages,",").toLowerCase();
            educationScript.append(",'").append("search_coverage_string").append("',").append("searchCoverageString");
            result.put("searchCoverageString",searchCoverageString);
        }

        if(CollectionUtils.isNotEmpty(categoryCodes)){
            index = 0;
            for (String code : categoryCodes) {
                String codeName = "search_code" +index;
                educationScript.append(",'").append("search_code").append("',").append(codeName);
                result.put(codeName, code);
                index ++;
            }
            String searchCodeString = StringUtils.join(categoryCodes, ",").toLowerCase();
            educationScript.append(",'").append("search_coverage").append("',").append("searchCodeString");
            result.put("searchCodeString",searchCodeString);
        }

        if(CollectionUtils.isNotEmpty(paths)){
            index = 0;
            for (String path : paths) {
                String pathName = "search_path" +index;
                educationScript.append(",'").append("search_path").append("',").append(pathName);
                result.put(pathName, path);
                index ++;
            }
            String searchPathString = StringUtils.join(paths, ",").toLowerCase();
            educationScript.append(",'").append("search_coverage").append("',").append("searchPathString");
            result.put("searchPathString",searchPathString);
        }
        educationScript.append(").id();");
        educationScript.append(" return education};");
        script.append(educationScript);
        return result;
    }


    private static Map<String , Object> buildTechInfoScript(StringBuffer script, List<TechInfo> techInfos){
        if(CollectionUtils.isEmpty(techInfos)){
            return new HashMap<>();
        }

        Map<String, Object> resultParam = new HashMap<>();
        int orderNumber = 0;
        StringBuilder techinfoScriptMethd = new StringBuilder("public void createTechInfo(Long education){");
        for(TechInfo techInfo : techInfos){
            String suffix = "_ti"+orderNumber;
            String techInfoNode = "techinfo"+suffix;
            Map<String, Object> techInfoParam = getParam4NotNull(techInfo);
            StringBuffer techInfoScript  = new StringBuffer(techInfoNode + " = graph.addVertex(T.label,'tech_info'");
            for (String key : techInfoParam.keySet()) {
                techInfoScript.append(",'").append(key).append("',").append(key).append(suffix);
                resultParam.put(key + suffix, techInfoParam.get(key));
            }
            techInfoScript.append(");");
            techInfoScript.append("g.V(education).next().addEdge('has_tech_info',"+techInfoNode+",'identifier',edgeIdentifier").append(suffix).append(");");
            resultParam.put("edgeIdentifier"+suffix, techInfo.getIdentifier());

            techinfoScriptMethd.append(techInfoScript);
            orderNumber ++;
        }

        techinfoScriptMethd.append("};");
        script.append(techinfoScriptMethd);

        return resultParam;
    }

    private static Map<String , Object> buildCategoryScript(StringBuffer script, List<ResourceCategory> categories){
        if(CollectionUtils.isEmpty(categories)){
            return new HashMap<>();
        }

        Map<String, Object> resultParam = new HashMap<>();
        int orderNumber = 0;

        StringBuilder categorieScriptMethod = new StringBuilder("public void createCategories(Long education){");
        for(ResourceCategory category : categories){
            StringBuffer categorieScript = null;
            String suffix = "_cg"+orderNumber;
            String categoryNode = "categoryNode"+suffix;
            String categoryNodeAll = "categoryNodeAll"+suffix;
            String taxoncodeName = "taxoncode"+suffix;
            String edgeIdentifierName = "edgeIdentifier" + suffix;
            Map<String, Object> techInfoParam = getParam4NotNull(category);
            categorieScript = new StringBuffer(categoryNodeAll+"=g.V().hasLabel('category_code').has('cg_taxoncode',"+taxoncodeName+");");
            StringBuilder addNodeScript = new StringBuilder(categoryNode+"=graph.addVertex(T.label,'category_code'");
            Map<String, Object> categoryNodeParam = getParam4NotNull(category);
            for (String key : categoryNodeParam.keySet()) {
                addNodeScript.append(", '").append(key).append("', ").append(key).append(suffix);
                resultParam.put(key + suffix, techInfoParam.get(key));
            }
            addNodeScript.append(");");

            String ifScript = "if(!"+categoryNodeAll+".iterator().hasNext()){"+addNodeScript.toString()+
                    "}else{"+categoryNode+"="+categoryNodeAll+".next()};";

            StringBuilder addEdgeScript = new StringBuilder("g.V(education).next().addEdge('has_category_code',"
                    +categoryNode+",'identifier',"+edgeIdentifierName+");");

            categorieScript.append(ifScript).append(addEdgeScript);

            resultParam.put(taxoncodeName, category.getTaxoncode());
            resultParam.put(edgeIdentifierName, category.getIdentifier());

            categorieScriptMethod.append(categorieScript);
            orderNumber ++ ;
        }

        categorieScriptMethod.append("};");
        script.append(categorieScriptMethod);

        return resultParam;
    }

    private static Map<String, Object> buildPathScript(StringBuffer script, List<String> categoryPaths){

        StringBuilder categroyPathMethod = new StringBuilder("public void createCategoriesPath(Long education){");
        Map<String, Object> resultParam = new HashMap<>();
        int orderNumber = 0;
        for (String path : categoryPaths){
            String suffix = "_cgp"+orderNumber;
            String categoryPathNode = "categoryNode"+suffix;
            String categoryPathNodeAll = "categoryNodeAll"+suffix;
            String taxonpathName = "taxonpath"+suffix;
            StringBuilder categroyPath = new StringBuilder(categoryPathNodeAll+
                    "=g.V().has('categories_path','cg_taxonpath',"+taxonpathName+");");

            String addNodeScript = categoryPathNode+"=graph.addVertex(T.label,'categories_path','cg_taxonpath',"+taxonpathName+");";
            String ifScript = "if(!"+categoryPathNodeAll+".iterator().hasNext()){"+addNodeScript+"" +
                    "}else{"+categoryPathNode+"="+categoryPathNodeAll+".next()};";

            StringBuilder addEdgeScript = new StringBuilder("g.V(education).next().addEdge('has_categories_path',"+categoryPathNode+");");
            categroyPath.append(ifScript).append(addEdgeScript);

            categroyPathMethod.append(categroyPath);

            resultParam.put(taxonpathName, path);
            orderNumber ++ ;
        }

        categroyPathMethod.append("};");
        script.append(categroyPathMethod);

        return resultParam;
    }

    private static Map<String, Object> buildCoverageScript(StringBuffer script, List<ResCoverage> coverages){
        if(CollectionUtils.isEmpty(coverages)){
            return new HashMap<>();
        }

        Map<String, Object> resultParam = new HashMap<>();
        int orderNumber = 0;
        StringBuilder coverageScriptMethod = new StringBuilder("public void createCoverage(Long education){");
        for(ResCoverage coverage : coverages){
            String suffix = "_cov"+orderNumber;
            String targetTypeName = "target_type" +suffix;
            String targetName = "target" +suffix;
            String strategyName = "strategy" +suffix;
            String coverageNodeNameAll = "coverageNodeAll"+suffix;
            String coverageNodeName = "coverageNode"+suffix;
            String edgeIdentifierName = "edgeIdentifier"+suffix;
            StringBuilder coverageScript = new StringBuilder(coverageNodeNameAll+"=g.V().hasLabel('coverage')" +
                    ".has('target_type',"+targetTypeName+").has('target',"+targetName+")" +
                    ".has('strategy',"+strategyName+");");
            StringBuilder addCoverageNodeScript = new StringBuilder(coverageNodeName+
                    "= graph.addVertex(T.label,'coverage','target_type',"+targetTypeName+
                    ",'strategy',"+strategyName+",'target',"+targetName+");");
            String ifScript = "if(!"+coverageNodeNameAll+".iterator().hasNext()){"+addCoverageNodeScript+"" +
                    "}else{"+coverageNodeName+"="+coverageNodeNameAll+".next()};";
            String addEdgeScript = "g.V(education).next()" +
                    ".addEdge('has_coverage',"+coverageNodeName+",'identifier',"+edgeIdentifierName+").id();";

            coverageScript.append(ifScript).append(addEdgeScript);

            coverageScriptMethod.append(coverageScript);
            resultParam.put(targetTypeName, coverage.getTargetType());
            resultParam.put(targetName, coverage.getTarget());
            resultParam.put(strategyName, coverage.getStrategy());
            resultParam.put(edgeIdentifierName, coverage.getIdentifier());
            orderNumber ++;
        }

        coverageScriptMethod.append("};");
        script.append(coverageScriptMethod);
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

    public static  void main(String[] args){
        StringBuffer scriptBuffer = new StringBuffer();
        String name = "name";
        Map<String, Object> createRelationParams = TitanScritpUtils
                .getParamAndChangeScript(scriptBuffer, null);
        System.out.println(scriptBuffer.toString());
    }

}
