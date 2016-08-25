package nd.esp.service.lifecycle.utils.titan.script.utils;

import nd.esp.service.lifecycle.utils.titan.script.annotation.*;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanEducationQuestions;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanResCoverageEdge;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanResCoverageVertex;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModel;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelEdge;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelVertex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2016/8/24.
 */
public class ParseAnnotation {
    public static TitanScriptModel createScriptModel(TitanModel titanModel) {
        TitanScriptModel titanScriptModel = null;
        //处理节点
        if (titanModel.getClass().getAnnotation(TitanVertex.class) != null) {
            TitanScriptModelVertex vertex = new TitanScriptModelVertex();
            TitanVertex annotation = titanModel.getClass().getAnnotation(TitanVertex.class);
            vertex.setLabel(annotation.label());
            Map<Field, List<Annotation>> annotationMap = getAllFieldAnnotationMap(titanModel);

            Map<String, Object> fieldMap = getTitanFieldNameAndValue(titanModel, annotationMap);
            Map<String, Object> compositeKeyMap = getTitanCompositeKeyNameAndValue(titanModel,annotationMap);

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
                try {
                    if (titanField.name() == null || "".equals(titanField.name())) {
                        fieldMap.put(field.getName(), field.get(model));
                    }else{
                        fieldMap.put(titanField.name(),field.get(model));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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


    public static void main(String[] args) {
        TitanEducationQuestions qustions = new TitanEducationQuestions();
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

    }
}
