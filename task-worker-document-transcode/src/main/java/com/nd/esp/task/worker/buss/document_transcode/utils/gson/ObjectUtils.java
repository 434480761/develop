package com.nd.esp.task.worker.buss.document_transcode.utils.gson;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nd.esp.task.worker.buss.document_transcode.utils.CollectionUtils;
import com.nd.esp.task.worker.buss.document_transcode.utils.StringUtils;
import com.nd.esp.task.worker.buss.document_transcode.utils.collection.MapExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rits.cloning.Cloner;

/**
 * 对象操作的工具类
 * <p/>
 * 包括对象的序列化，JSON转换，XML转换等
 *
 * @author bifeng.liu
 * @see org.apache.commons.lang.ObjectUtils
 */
public final class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {
    /**
     * 用于默认XML的导入导出
     */
    private static Cloner cloner = new Cloner();

    /**
     * 私有化构造函数，不允许实例化该类
     */
    private ObjectUtils() {
    }


    /**
     * 对象的深度复制
     *
     * @param instance 对象
     * @return
     */
    public static <T> T deepClone(T instance) {
        return cloner.deepClone(instance);
    }
 

    /**
     * 获取Gson对象
     * @return
     */
    private static Gson getGson() {
        // 类处理器
        Map<Type, Object> typeAdapters = new HashMap<Type, Object>();

        // 子类处理器
        Map<Class<?>, Object> hierarchyTypeAdapters = new HashMap<Class<?>, Object>();
        final GsonBuilder builder = new GsonBuilder().disableHtmlEscaping().serializeNulls();
        
        //builder.excludeFieldsWithoutExposeAnnotation(); //不导出实体中没有用@Expose注解的属性  
        //builder.enableComplexMapKeySerialization(); //支持Map的key为复杂对象的形式
        CollectionUtils.forAllDo(typeAdapters, new MapExecutor() {
            public void execute(Object key, Object value) {
                builder.registerTypeAdapter((Type) key, value);
            }
        });
        CollectionUtils.forAllDo(hierarchyTypeAdapters, new MapExecutor() {
            public void execute(Object key, Object value) {
                builder.registerTypeHierarchyAdapter((Class<?>) key, value);
            }
        });

        return builder.create();
    }

    /**
     * 把对象转换成JSON字符串
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        // 类处理器
        Map<Type, Object> typeAdapters = new HashMap<Type, Object>();

        // 子类处理器
        Map<Class<?>, Object> hierarchyTypeAdapters = new HashMap<Class<?>, Object>();
        return toJson(obj, typeAdapters, hierarchyTypeAdapters);
    }

    /**
     * 把对象转换成JSON字符串
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj, Map<Type, Object> typeAdapters, Map<Class<?>, Object> hierarchyTypeAdapters) {
        final GsonBuilder builder = new GsonBuilder().disableHtmlEscaping().serializeNulls();
        CollectionUtils.forAllDo(typeAdapters, new MapExecutor() {
            public void execute(Object key, Object value) {
                builder.registerTypeAdapter((Type) key, value);
            }
        });
        CollectionUtils.forAllDo(hierarchyTypeAdapters, new MapExecutor() {
            public void execute(Object key, Object value) {
                builder.registerTypeHierarchyAdapter((Class<?>) key, value);
            }
        });
        // 枚举类型适配器
        Gson gson = builder.create();
        return gson.toJson(obj);
    }

    /**
     * JSON字符串转换成对象
     *
     * @param str   json格式字符串
     * @param clazz 对象class
     * @return
     */
    public static <T> T fromJson(String str, Class<T> clazz) {
        return fromJson(str, clazz, false);
    }

    /**
     * JSON字符串转换成对象
     *
     * @param str       json格式字符串
     * @param clazz     对象class
     * @param isProcess 是否处理数据（key首字母、下划线处理）
     * @return
     */
    public static <T> T fromJson(String str, Class<T> clazz, boolean isProcess) {
        if (isProcess) {
            str = processJson(str);
        }
        // 类处理器
        Map<Type, Object> typeAdapters = new HashMap<Type, Object>();
        // 子类处理器
        Map<Class<?>, Object> hierarchyTypeAdapters = new HashMap<Class<?>, Object>();
        return fromJson(str, clazz, typeAdapters, hierarchyTypeAdapters);
    }

    /**
     * 处理json数据首字母小写、驼峰命名
     *
     * @param jsonInput json数据
     * @return string 处理以后的字符串
     */
    public static String processJson(String jsonInput) {
        String originalInput = jsonInput;
        StringBuilder inputStr = new StringBuilder(jsonInput);
        String regex = "\"(\\w+)\":";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(inputStr);
        List<String> result = new ArrayList<String>();
        String valueName = null;
        while (m.find()) {
            valueName = m.group(1);
            // 首字母小写 或者 以下划线分割的命名
            if (Character.isUpperCase(valueName.charAt(0)) || valueName.indexOf("_") != -1) {
                String newValueName = StringUtils.toLowerFirstLetter(valueName); // 首字母小写
                newValueName = StringUtils.toCamelCase(newValueName); // 下划线命名改为驼峰命名
                String regx1 = "\"" + valueName + "\":";
                String replace = "\"" + newValueName + "\":";
                originalInput = originalInput.replaceAll(regx1, replace);
            }
            result.add(valueName);
            inputStr.delete(0, m.end(0));
            m = p.matcher(inputStr);
        }
        return originalInput;
    }

    /**
     * 把对象转换成JSON字符串
     *
     * @param str
     * @param clazz
     * @param typeAdapters
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String str, Class<T> clazz, Map<Type, Object> typeAdapters, Map<Class<?>, Object> hierarchyTypeAdapters) {
        final GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        CollectionUtils.forAllDo(typeAdapters, new MapExecutor() {
            public void execute(Object key, Object value) {
                builder.registerTypeAdapter((Type) key, value);
            }
        });
        CollectionUtils.forAllDo(hierarchyTypeAdapters, new MapExecutor() {
            public void execute(Object key, Object value) {
                builder.registerTypeHierarchyAdapter((Class<?>) key, value);
            }
        });
        Gson gson = builder.create();
        return gson.fromJson(str, clazz);
    }

    /**
     * 把对象转换成JSON字符串，支持各种泛型
     *
     * e.g. <code>ObjectUtils.fromJson(json, new TypeToken<Foo<Bar>>() {}.getType());</code>
     *
     * @param json      JSON字符串
     * @param typeToken 类型Token
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        Gson gson = getGson();
        return (T)gson.fromJson(json, typeToken.getType());
    }

    /**
     * 把对象序列化成字节数组
     * <p/>
     * 对象一定要实现java.io.Serializable接口
     *
     * @param object
     * @return
     * @throws java.io.IOException
     */
    public static byte[] serialize(Object object) throws IOException {
        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

    /**
     * 把字符数据反序列化成对象
     * <p/>
     * 对象一定要实现java.io.Serializable接口
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object unserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        // 反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }
}
