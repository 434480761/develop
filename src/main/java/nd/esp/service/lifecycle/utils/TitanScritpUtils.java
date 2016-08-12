package nd.esp.service.lifecycle.utils;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.utils.titan.script.ScriptAbstract;
import nd.esp.service.lifecycle.utils.titan.script.ScriptEducation;
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
    public enum KeyWords {
        script, params
    }

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
    public static Map<String, Object> getParam4NotNull(Object model) {
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
            if(value instanceof String){
                String str = (String) value;
                if(str.length() > 10000){
                    for(Field f : fields){
                        f.setAccessible(true);
                        if("identifier".equals(f.getName())){
                            try {
                                LOG.info("field_length_too_long :{} ;class:{}",f.get(model),model.getClass().getName());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    continue;
                }
            }

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

    public static Map<String, Object> getParamAndChangeScript4Repair(StringBuffer scriptBuffer, Object model){
        Map<String, Object> graphParams = getParam4NotNull(model);
        for (String key : graphParams.keySet()) {
            scriptBuffer.append(".property('").append(key).append("', ").append(key).append(")");
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
        if(model instanceof ResCoverage){
            return LifeCircleApplicationInitializer.db_titan_field_coverage;
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
        Map<String,Object> educationParam = buildEducationScript(script,education,categoryList,coverageList);
        //对addVertex中的参数个数做限制，最多不能超过250个
        if(educationParam.size() > 125){
            return null;
        }
        param.putAll(educationParam);
        Map<String, Object> coverageParamMap = null;
        if(CollectionUtils.isNotEmpty(coverageList)){
            coverageParamMap = buildCoverageScript(script, coverageList);
            param.putAll(coverageParamMap);
        }

        Map<String, Object> pathParamMap = null;
        if(CollectionUtils.isNotEmpty(categoryPathList)){
            pathParamMap= buildPathScript(script, categoryPathList);
            param.putAll(pathParamMap);
        }

        Map<String, Object> categoryParamMap = null;
        if(CollectionUtils.isNotEmpty(categoryList)){
            categoryParamMap=buildCategoryScript(script, categoryList);
            param.putAll(categoryParamMap);
        }

        Map<String, Object> techInfoParamMap = null;
        if(CollectionUtils.isNotEmpty(techInfoList)){
            techInfoParamMap = buildTechInfoScript(script, techInfoList);
            param.putAll(techInfoParamMap);
        }

        Map<String, Object> checkParam = buildCheckExistScript(script,education.getPrimaryCategory(),education.getIdentifier());
        param.putAll(checkParam);

        //调用脚本方法
        script.append("if(!checkExist()){");
        script.append("educationId=createEducation();");
        if(CollectionUtils.isNotEmpty(coverageParamMap)){
            script.append("createCoverage(educationId);");
        }

        if(CollectionUtils.isNotEmpty(pathParamMap)){
            script.append("createCategoriesPath(educationId);");
        }

        if(CollectionUtils.isNotEmpty(categoryParamMap)){
            script.append("createCategories(educationId);");
        }

        if(CollectionUtils.isNotEmpty(techInfoParamMap)){
            script.append("createTechInfo(educationId);");
        }
        script.append("};");
        script.append("g.V().hasLabel(primaryCategory_ck).has('identifier',identifier_ck).id()");
        Map<String, Object> result = new HashMap<>();
        result.put("script",script);
        result.put("param",param);
        return result;

    }

    /**
     * 生成检查资源是否存在的脚本
     * */
    private static Map<String, Object> buildCheckExistScript(StringBuffer script, String primaryCategory, String identifier){
        Map<String, Object> resultParam = new HashMap<>();
        resultParam.put("identifier_ck",identifier);
        resultParam.put("primaryCategory_ck", primaryCategory);

        String checkScrip = "public boolean checkExist(){" +
                "if(g.V().hasLabel(primaryCategory_ck).has('identifier',identifier_ck).iterator().hasNext()){return true}" +
                "else{return false}};";
        script.append(checkScrip);
        return resultParam;
    }

    /**
     * 创建导入Education的脚本，包括Education中的冗余字段
     * */
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


        setProperty(educationScript, result, resCoverages, TitanKeyWords.search_coverage, TitanKeyWords.search_coverage_string);
        setProperty(educationScript, result ,categoryCodes, TitanKeyWords.search_code , TitanKeyWords.search_code_string);
        setProperty(educationScript, result ,paths, TitanKeyWords.search_path, TitanKeyWords.search_path_string);
        educationScript.append(").id();");
        educationScript.append(" return education};");
        script.append(educationScript);
        return result;
    }

    private static void setProperty(StringBuilder script, Map<String, Object> param, Set<String> values, TitanKeyWords fieldSet, TitanKeyWords fieldString){
        if(CollectionUtils.isEmpty(values)){
            return;
        }
        int index = 0;
        for (String path : values) {
            String pathName = fieldSet.toString() +index;
            script.append(",'").append(fieldSet.toString()).append("',").append(pathName);
            param.put(pathName, path);
            index ++;
        }
        String searchPathString = StringUtils.join(values, ",").toLowerCase();
        script.append(",'").append(fieldString.toString()).append("',").append(fieldString.toString());
        param.put(fieldString.toString(),searchPathString);
    }

    /**
     * 创建导入TechInfo脚本
     * */
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

    /**
     * 创建导入维度数据的脚本
     * */
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

    /**
     * 创建导入Path的脚本
     * */
    private static Map<String, Object> buildPathScript(StringBuffer script, List<String> categoryPaths){
        if(CollectionUtils.isEmpty(categoryPaths)){
            return new HashMap<>();
        }
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

    /**
     * 创建导入覆盖范围的脚本
     * */
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

    public static Map<String, Object> buildRelationScript(StringBuffer script, List<ResourceRelation> resourceRelations){
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

            scriptBuffer.append(");");

            createRelationParams.put(sourcePrimaryCategoryName, resourceRelation.getResType());
            createRelationParams.put(sourceIdentifierName, resourceRelation.getSourceUuid());
            createRelationParams.put(targetPrimaryCategoryName, resourceRelation.getResourceTargetType());
            createRelationParams.put(targetIdentifierName, resourceRelation.getTarget());
            createRelationParams.put(edgeIdentifierName, resourceRelation.getIdentifier());

            script.append(scriptBuffer);
        }

        return result;
    }

    /**
     * 生成获取详情的脚本
     * */
    public static Map<KeyWords, Object> buildGetDetailScript(String primaryCategory,
                                                             List<String> identifierList,
                                                             List<String> includeList,
                                                             boolean isAll){
        StringBuilder scriptBuilder = new StringBuilder(
                "g.V().has('identifier',");
        StringBuffer withInScript = new StringBuffer("within(");

        Map<String, Object> params = new HashMap<>();
        int index = 0;
        for (int i=0; i <identifierList.size() ;i ++){
            String indentifierName = "identifier"+index;
            if(i == 0){
                withInScript.append(indentifierName);
            } else {
                withInScript.append(",").append(indentifierName);
            }
            params.put(indentifierName, identifierList.get(i));

            index ++;
        }
        withInScript.append(")");
        scriptBuilder.append(withInScript.toString()).append(").has('primary_category',primary_category)");

        if(!isAll){
            scriptBuilder.append(".has('lc_enable',true)");
        }
        params.put("primary_category", primaryCategory);

        scriptBuilder.append(TitanUtils.generateScriptForInclude(includeList,primaryCategory));
        scriptBuilder.append(".valueMap();");

        Map<KeyWords, Object> result = new HashMap<>();
        result.put(KeyWords.script, scriptBuilder.toString());
        result.put(KeyWords.params, params);

        return  result;
    }

    public static  void main(String[] args){
        StringBuffer scriptBuffer = new StringBuffer();
        String name = "name";
        Map<String, Object> createRelationParams = TitanScritpUtils
                .getParamAndChangeScript(scriptBuffer, null);
        System.out.println(scriptBuffer.toString());
    }

}
