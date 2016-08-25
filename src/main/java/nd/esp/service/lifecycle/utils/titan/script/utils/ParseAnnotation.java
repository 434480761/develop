package nd.esp.service.lifecycle.utils.titan.script.utils;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanCompositeKey;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanEdge;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanEducationQuestions;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModel;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelEdge;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelVertex;
import nd.esp.service.lifecycle.utils.xstream.MapConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2016/8/24.
 */
public class ParseAnnotation {
    public static TitanScriptModel createScriptModel(TitanModel titanModel) {
        TitanScriptModel titanScriptModel;
        //处理节点
        if (titanModel.getClass().getAnnotation(TitanVertex.class) != null) {
            TitanScriptModelVertex vertex = new TitanScriptModelVertex();
            TitanVertex annotation = titanModel.getClass().getAnnotation(TitanVertex.class);
            vertex.setLabel(annotation.label());
            Map<String, Object> test = getTitanFieldNameAndValue(titanModel, getAllFieldAnnotationMap(titanModel));
        }
        //处理边
        else if (titanModel.getClass().getAnnotation(TitanEdge.class) != null) {
            TitanScriptModelEdge edge = new TitanScriptModelEdge();
            TitanEdge annotation = titanModel.getClass().getAnnotation(TitanEdge.class);
            edge.setLabel(annotation.label());


        }

        return null;
    }

    public static Map<Field, List<Annotation>> getAllFieldAnnotationMap(TitanModel titanModel) {
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

    public static Map<String, Object> getTitanFieldNameAndValue(TitanModel model, Map<Field, List<Annotation>> fieldListMap) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : fieldListMap.keySet()) {
            List<Annotation> annotations = fieldListMap.get(field);
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
        }

        return fieldMap;
    }

    public static Map<String, Object> getTitanCompositeKeyAndValue(TitanModel model, Map<Field, List<Annotation>> fieldListMap) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : fieldListMap.keySet()) {
            List<Annotation> annotations = fieldListMap.get(field);
            for (Annotation comAnnotation : annotations) {
                if (comAnnotation instanceof TitanCompositeKey) {
                    field.setAccessible(true);
                    TitanCompositeKey titanField = (TitanCompositeKey) comAnnotation;
                    for (Annotation fieldAnnontation : annotations){
                        if (fieldAnnontation instanceof TitanField) {
                            field.setAccessible(true);
                            TitanField fieldAnnon = (TitanField) fieldAnnontation;
                            try {
                                if (fieldAnnon.name() == null || "".equals(fieldAnnon.name())) {
                                    fieldMap.put(field.getName(), field.get(model));
                                }else{
                                    fieldMap.put(fieldAnnon.name(),field.get(model));
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }

        return fieldMap;
    }

    public static void main(String[] args) {
        TitanEducationQuestions qustions = new TitanEducationQuestions();
        qustions.setDbpreview("123");
        createScriptModel(qustions);
    }
}
