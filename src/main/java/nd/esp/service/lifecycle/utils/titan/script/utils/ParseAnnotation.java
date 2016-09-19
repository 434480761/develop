package nd.esp.service.lifecycle.utils.titan.script.utils;

import nd.esp.service.lifecycle.utils.BigDecimalUtils;
import nd.esp.service.lifecycle.utils.titan.script.annotation.*;
import nd.esp.service.lifecycle.utils.titan.script.model.education.TitanQuestion;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanResCoverageEdge;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanResCoverageVertex;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModel;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelEdge;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelVertex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/8/24.
 */
public class ParseAnnotation {
    public static TitanScriptModel createScriptModel(TitanModel titanModel) {
        if (titanModel == null){
            return null;
        }
        TitanScriptModel titanScriptModel = null;
        //处理节点
        if (titanModel.getClass().getAnnotation(TitanVertex.class) != null) {
            TitanScriptModelVertex vertex = new TitanScriptModelVertex();
            TitanVertex annotation = titanModel.getClass().getAnnotation(TitanVertex.class);
            vertex.setLabel(annotation.label());
            Map<Field, List<Annotation>> annotationMap = getAllFieldAnnotationMap(titanModel);

            Map<String, Object> fieldMap = getTitanFieldNameAndValue(titanModel, annotationMap);
            Map<String, Object> compositeKeyMap = getTitanCompositeKeyNameAndValue(titanModel,annotationMap);

            vertex.setType(TitanScriptModel.Type.V);
            vertex.setCompositeKeyMap(compositeKeyMap);
            vertex.setFieldMap(fieldMap);

            titanScriptModel = vertex;

        }
        //处理边
        else if (titanModel.getClass().getAnnotation(TitanEdge.class) != null) {
            TitanScriptModelEdge edge = new TitanScriptModelEdge();
            TitanEdge annotation = titanModel.getClass().getAnnotation(TitanEdge.class);
            edge.setLabel(annotation.label());

            Map<Field, List<Annotation>> annotationMap = getAllFieldAnnotationMap(titanModel);

            Map<String, Object> fieldMap = getTitanFieldNameAndValue(titanModel, annotationMap);
            Map<String, Object> compositeKeyMap = getTitanCompositeKeyNameAndValue(titanModel,annotationMap);
            Map<String, Object> resourceMap = getTitanEdgeResourceNameAndValue(titanModel,annotationMap);
            Map<String, Object> targetMap = getTitanEdgeTargetNameAndValue(titanModel, annotationMap);

            edge.setType(TitanScriptModel.Type.E);
            edge.setCompositeKeyMap(compositeKeyMap);
            edge.setFieldMap(fieldMap);
            edge.setResourceKeyMap(resourceMap);
            edge.setTargetKeyMap(targetMap);

            titanScriptModel = edge;
        }

        return titanScriptModel;
    }

    private static Map<Field, List<Annotation>> getAllFieldAnnotationMap(TitanModel titanModel) {
        Map<Field, List<Annotation>> map = new HashMap<>();

        List<Field> fields = new ArrayList<>();
        getAllDeclareField(titanModel.getClass(),fields);

        for (Field field : fields) {
            map.put(field, Arrays.asList(field.getAnnotations()));
        }

        return map;
    }

    static private void getAllDeclareField(Class<?> className,
                                           List<Field> fields) {
        if (className == null) {
            return;
        }
        fields.addAll(Arrays.asList(className.getDeclaredFields()));
        getAllDeclareField(className.getSuperclass(), fields);
    }

    /**
     * 获取所有的属性名和属性值
     * */
    private static Map<String, Object> getTitanFieldNameAndValue(TitanModel model, Map<Field, List<Annotation>> fieldListMap) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : fieldListMap.keySet()) {
            List<Annotation> annotations = fieldListMap.get(field);
            fieldMap.putAll(getOneTitanFieldNameAndValue(field,model,annotations));
        }

        return fieldMap;
    }

    /**
     * 获取所有有@TitanCompositeKey注解标记的属性和属性值
     * */
    private static Map<String, Object> getTitanCompositeKeyNameAndValue(TitanModel model, Map<Field, List<Annotation>> fieldListMap) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : fieldListMap.keySet()) {
            List<Annotation> annotations = fieldListMap.get(field);
            for (Annotation comAnnotation : annotations) {
                if (comAnnotation instanceof TitanCompositeKey) {
                    fieldMap.putAll(getOneTitanFieldNameAndValue(field, model, annotations));
                    break;
                }
            }
        }
        return fieldMap;
    }

    private static Map<String, Object>  getTitanEdgeResourceNameAndValue(TitanModel model, Map<Field, List<Annotation>> fieldListMap){
        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : fieldListMap.keySet()) {
            List<Annotation> annotations = fieldListMap.get(field);
            for (Annotation comAnnotation : annotations) {
                if (comAnnotation instanceof TitanEdgeResourceKey) {
                    fieldMap.putAll(getOneTitanRsourceNameAndValue(field, model, annotations));
                    break;
                }
            }
        }
        return fieldMap;
    }

    private static Map<String, Object>  getTitanEdgeTargetNameAndValue(TitanModel model, Map<Field, List<Annotation>> fieldListMap){
        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : fieldListMap.keySet()) {
            List<Annotation> annotations = fieldListMap.get(field);
            for (Annotation comAnnotation : annotations) {
                if (comAnnotation instanceof TitanEdgeTargetKey) {
                    fieldMap.putAll(getOneTitanTargetAndValue(field, model, annotations));
                    break;
                }
            }
        }
        return fieldMap;
    }


    /**
     * 获取所有通过Field和@TitanField注解获取属性名和属性值
     * */
    private static Map<String, Object> getOneTitanFieldNameAndValue(Field field, TitanModel model, List<Annotation> annotations){
        Map<String, Object> fieldMap = new HashMap<>();
        for (Annotation annotation : annotations) {
            if (annotation instanceof TitanField) {
                field.setAccessible(true);
                TitanField titanField = (TitanField) annotation;
                Object value = titanFieldFilter(field, model);

                if (titanField.name() == null || "".equals(titanField.name())) {
                    fieldMap.put(field.getName(), value);
                }else{
                    fieldMap.put(titanField.name(),value);
                }
                break;
            }
        }

        return fieldMap;
    }

    private static Map<String, Object> getOneTitanRsourceNameAndValue(Field field, TitanModel model, List<Annotation> annotations){
        Map<String, Object> fieldMap = new HashMap<>();
        for (Annotation annotation : annotations) {
            if (annotation instanceof TitanEdgeResourceKey) {
                field.setAccessible(true);
                TitanEdgeResourceKey titanResource = (TitanEdgeResourceKey) annotation;
                try {
                    if (titanResource.source() == null || "".equals(titanResource.source())) {
                        fieldMap.put(field.getName(), field.get(model));
                    }else{
                        fieldMap.put(titanResource.source(),field.get(model));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        return fieldMap;
    }

    private static Map<String, Object> getOneTitanTargetAndValue(Field field, TitanModel model, List<Annotation> annotations){
        Map<String, Object> fieldMap = new HashMap<>();
        for (Annotation annotation : annotations) {
            if (annotation instanceof TitanEdgeTargetKey) {
                field.setAccessible(true);
                TitanEdgeTargetKey titanField = (TitanEdgeTargetKey) annotation;
                try {
                    if (titanField.target() == null || "".equals(titanField.target())) {
                        fieldMap.put(field.getName(), field.get(model));
                    }else{
                        fieldMap.put(titanField.target(),field.get(model));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        return fieldMap;
    }

    /**
     * 对titan字段类型进行转换，已经长度过长的进行过滤
     * */
    private static Object titanFieldFilter(Field field, TitanModel model){
        Object value = null;
        try {
            value = field.get(model);
        } catch (IllegalAccessException e) {
            return null;
        }
        //BigDecimal进行转换
        if (value instanceof BigDecimal) {
            value = BigDecimalUtils.toString(value);
        }
        //把时间类型转换成Long类型
        if(value instanceof Date){
            value = ((Date)value).getTime();
        }

        if(value instanceof String){
            String str = (String) value;
            if(str.length() > 10000){
                return null;
            }
        }

        return value;
    }


    public static void main(String[] args) {
        TitanQuestion qustions = new TitanQuestion();
        qustions.setDbpreview("123");
        qustions.setIdentifier(UUID.randomUUID().toString());
        createScriptModel(qustions);

        TitanResCoverageVertex resCoverageVertex = new TitanResCoverageVertex();
        resCoverageVertex.setStrategy("User");
        resCoverageVertex.setTarget("123");
        resCoverageVertex.setTargetType("999");
        createScriptModel(resCoverageVertex);

        TitanResCoverageEdge edge = new TitanResCoverageEdge();
        edge.setIdentifier(UUID.randomUUID().toString());
        edge.setResource(UUID.randomUUID().toString());
        edge.setResType("assets");
        edge.setStrategy("1234");
        edge.setTarget("7897978");
        edge.setTargetType("User");

        createScriptModel(edge);


        TitanScriptBuilder builder = new TitanScriptBuilder();
        builder.update(edge);

    }
}
