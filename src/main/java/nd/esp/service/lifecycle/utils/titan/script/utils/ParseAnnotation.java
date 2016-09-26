package nd.esp.service.lifecycle.utils.titan.script.utils;

import nd.esp.service.lifecycle.repository.model.Asset;
import nd.esp.service.lifecycle.support.busi.titan.GremlinClientFactory;
import nd.esp.service.lifecycle.utils.BigDecimalUtils;
import nd.esp.service.lifecycle.utils.titan.script.annotation.*;
import nd.esp.service.lifecycle.utils.titan.script.model.EducationToTitanBeanUtils;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanResCoverageEdge;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanResCoverageVertex;
import nd.esp.service.lifecycle.utils.titan.script.model.education.TitanAsset;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModel;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelEdge;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptModelVertex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0;
import org.junit.Test;

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


    private Client client;

    public void init() {
        Cluster cluster = null;
        // TODO get from config
        String address = "172.24.133.94";
        int port = 8182;
        GryoMessageSerializerV1d0 serializerClass = null;
        Builder clusterBuilder = null;
        Map<String, Object> configMap = null;

        // This is required so that the result vertex can be serialized to
        // string
        serializerClass = new GryoMessageSerializerV1d0();
        configMap = new HashMap<String, Object>();
        configMap.put("serializeResultToString", "true");
        configMap.put("bufferSize", "819200");
        serializerClass.configure(configMap, null);

        // build cluster configuration
        clusterBuilder = Cluster.build(address);
        clusterBuilder.port(port);
        clusterBuilder.serializer(serializerClass);

        clusterBuilder.resultIterationBatchSize(20);
        clusterBuilder.maxContentLength(655360);

        // create a cluster instance
        cluster = clusterBuilder.create();
        client = cluster.connect();

        // client = Cluster.build("192.168.19.128").create().connect();
    }

    /**
     * 提供连接客户对象（单例）
     * @return
     */
    public Client getGremlinClient() {
        return client;
    }



    public static void main(String[] args) {
        TitanResCoverageVertex resCoverageVertex = new TitanResCoverageVertex();
        resCoverageVertex.setStrategy("User");
        resCoverageVertex.setTarget("123");
        resCoverageVertex.setTargetType("999");
        createScriptModel(resCoverageVertex);

        TitanResCoverageEdge edge = new TitanResCoverageEdge();
        edge.setResource(UUID.randomUUID().toString());
        edge.setTarget("7897978");
        edge.setTargetType("User");

        Asset asset = new Asset();
        asset.setIdentifier("0919127b-5536-454c-98f4-35c773d92061");
        asset.setPrimaryCategory("assets");
        asset.setTitle("liuran_789");
        asset.setLastUpdate(new Timestamp(System.currentTimeMillis()));


//        createScriptModel(edge);

        String s1 = "g.V().has('primary_category','assets').has('identifier','0919127b-5536-454c-98f4-35c773d92061')";
//
//        TitanModel titanAsset = EducationToTitanBeanUtils.toVertex(asset);
//
//        TitanScriptBuilder builder = new TitanScriptBuilder();
//        builder.addBeforeCheckExist(titanAsset);
//        builder.scriptEnd();
//        builder.getScript();
//
        ParseAnnotation parseAnnotation = new ParseAnnotation();
        parseAnnotation.init();
        Client client = parseAnnotation.getGremlinClient();
    }

    @Test
    public void testUpdateReationRedProperty(){
        TitanScriptBuilder scriptBuilder = new TitanScriptBuilder();
        scriptBuilder.updateRelationRedProperty("798798");
    }

    @Test
    public void test(){
        String s= script.replace("\n","").replace("\t","");
        System.out.println(s);
    }

    public static String script = "public Vertex updateEducation(String identifier){\n" +
            "\tVertex redv=null;\n" +
            "\tDefaultGraphTraversal dgt_v=g.V().has('identifier',identifier);\n" +
            "\tDefaultGraphTraversal dgt_e=g.E().hasLabel('has_coverage','has_category_code').has('identifier',identifier).outV();\n" +
            "\tif(dgt_v.hasNext()){\n" +
            "\t\tredv=dgt_v.next();\n" +
            "\t}else if(dgt_e.hasNext()){\n" +
            "\t\tredv=dgt_e.next();\n" +
            "\t};\n" +
            "\tif(redv==null||'tech_info'==redv.label()||'statistical'==redv.label()){return;};\n" +
            "\tIterator redvit=redv.properties('search_coverage','search_code','search_path','search_path_string','search_code_string','search_coverage_string');\n" +
            "\twhile(redvit.hasNext()){redvit.next().remove();};\n" +
            "\tString status=getStatus(redv);\n" +
            "\tIterator<Edge> it=g.V(redv).outE();\n" +
            "\tHashSet<String> coverageSet=new HashSet<String>();\n" +
            "\tHashSet<String> codeSet=new HashSet<String>();\n" +
            "\tHashSet<String> pathSet=new HashSet<String>();\n" +
            "\twhile(it.hasNext()){\n" +
            "\t\tEdge e=it.next();\n" +
            "\t\tif(e.label().equals('has_coverage')){\n" +
            "\t\t\tString value1=getCoverageProperty(e,'target_type')+\"/\"+getCoverageProperty(e,'target')+\"/\"+getCoverageProperty(e,'strategy')+\"/\"+status;\n" +
            "\t\t\tString value2=getCoverageProperty(e,'target_type')+\"/\"+getCoverageProperty(e,'target')+\"//\"+status;\n" +
            "\t\t\tString value3=getCoverageProperty(e,'target_type')+\"/\"+getCoverageProperty(e,'target')+\"/\"+getCoverageProperty(e,'strategy')+\"/\";\n" +
            "\t\t\tString value4=getCoverageProperty(e,'target_type')+\"/\"+getCoverageProperty(e,'target')+\"//\";\n" +
            "\t\t\tcoverageSet.add(value1);\n" +
            "\t\t\tcoverageSet.add(value2);\n" +
            "\t\t\tcoverageSet.add(value3);\n" +
            "\t\t\tcoverageSet.add(value4);\n" +
            "\t\t};\n" +
            "\t\tif(e.label().equals('has_category_code')){\n" +
            "\t\t\tString path=getEdgeProperty(e,'cg_taxonpath');\n" +
            "\t\t\tif(path!=null){\n" +
            "\t\t\t\tpathSet.add(path);\n" +
            "\t\t\t};\n" +
            "\t\t\tString code=getEdgeProperty(e,'cg_taxoncode');\n" +
            "\t\t\tif(code!=null){\n" +
            "\t\t\t\tcodeSet.add(code);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\tString string_coverage='';\n" +
            "\tString string_code='';\n" +
            "\tString string_path='';\n" +
            "\tint index=0;\n" +
            "\tfor(String str:coverageSet){\n" +
            "\t\tredv.property('search_coverage',str);\n" +
            "\t\tif(index==0){\n" +
            "\t\t\tstring_coverage=string_coverage+str;\n" +
            "\t\t}else{\n" +
            "\t\t\tstring_coverage=string_coverage+','+str;\n" +
            "\t\t};\n" +
            "\t\tindex++;\n" +
            "\t};\n" +
            "\tredv.property('search_coverage_string',string_coverage.toLowerCase());\n" +
            "\tindex=0;\n" +
            "\tfor(String str:codeSet){\n" +
            "\t\tredv.property('search_code',str);\n" +
            "\t\tif(index==0){\n" +
            "\t\t\tstring_code=string_code+str;\n" +
            "\t\t}else{\n" +
            "\t\t\tstring_code=string_code+\",\"+str;\n" +
            "\t\t};\n" +
            "\t\tindex++;\n" +
            "\t};\n" +
            "\tredv.property('search_code_string',string_code.toLowerCase());\n" +
            "\tindex=0;\n" +
            "\tfor(String str:pathSet){\n" +
            "\t\tredv.property('search_path',str);\n" +
            "\t\tstring_path=string_path+str+',';\n" +
            "\t\tif(index==0){\n" +
            "\t\t\tstring_code=string_code+str;\n" +
            "\t\t}else{\n" +
            "\t\t\tstring_code=string_code+','+str;\n" +
            "\t\t};\n" +
            "\t\tindex++;\n" +
            "\t};\n" +
            "\tredv.property('search_path_string',string_path.toLowerCase());\n" +
            "\tredv;\n" +
            "};\n" +
            "public String getCoverageProperty(Edge e,String name){\n" +
            "\tString pro=getEdgeProperty(e,name);\n" +
            "\tif(pro==null){\n" +
            "\t\treturn \"\";\n" +
            "\t}else{\n" +
            "\t\treturn pro;\n" +
            "\t}\n" +
            "};\n" +
            "public String getEdgeProperty(Edge e,String name){\n" +
            "\tif(e.properties(name).hasNext()){\n" +
            "\t\treturn e.property(name).value();\n" +
            "\t}else{\n" +
            "\t\treturn null;\n" +
            "\t}\n" +
            "};\n" +
            "public String getStatus(Vertex v){\n" +
            "\tif(v.properties('lc_status').hasNext()){\n" +
            "\t\treturn v.property('lc_status').value();\n" +
            "\t}else{\n" +
            "\t\treturn '';\n" +
            "\t}\n" +
            "};";
}
