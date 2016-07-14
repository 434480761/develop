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

    public static  void main(String[] args){
        StringBuffer scriptBuffer = new StringBuffer();
        String name = "name";
        Map<String, Object> createRelationParams = TitanScritpUtils
                .getParamAndChangeScript(scriptBuffer, null);
        System.out.println(scriptBuffer.toString());
    }

}
