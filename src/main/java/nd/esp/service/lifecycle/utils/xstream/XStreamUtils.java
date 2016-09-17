package nd.esp.service.lifecycle.utils.xstream;


import java.util.LinkedHashMap;
import java.util.Map;

import nd.esp.service.lifecycle.utils.ArrayUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import com.thoughtworks.xstream.converters.Converter;

/**
 * XStream的工具类
 */
public class XStreamUtils {
    /**
     * 私有化构造函数，不允许实例化该类
     */
    private XStreamUtils() {
    }

    /**
     * 把Classes列表转换成Map，Key由类的名字组成
     *
     * @param classes
     * @return
     */
    private static Map<String, Class> toClassMap(Class[] classes) {
        Map<String, Class> aliases = new LinkedHashMap<String, Class>();
        if (!ArrayUtils.isEmpty(classes)) {
            for (int i = 0; i < classes.length; i++) {
                Class clazz = classes[i];
                aliases.put(StringUtils.toLowerFirstLetter(clazz.getSimpleName()), clazz);
            }
        }
        return aliases;
    }

    /**
     * 使用默认的XStream对象把对象转换成XML
     *
     * @param instance 要转换的对象
     * @return Xml字符串
     */
    public static String toXml(Object instance, Map<String, Class> classes) {
        return toXmlInner(instance, classes);
    }

    /**
     * 使用默认的XStream对象把对象转换成XML
     *
     * @param instance 要转换的对象
     * @return Xml字符串
     */
    public static String toXml(Object instance, Class[] classes) {
        return toXmlInner(instance, toClassMap(classes));
    }

    /**
     * 使用默认的XStream对象把对象转换成XML
     *
     * @param instance 要转换的对象
     * @return Xml字符串
     */
    public static String toXml(Object instance) {
        return toXmlInner(instance, null);
    }

    private static String toXmlInner(Object instance, Map<String, Class> classes) {
        XStreamBuilder builder = new XStreamBuilder();
        builder.addAliases(classes);
        return builder.build().toXML(instance);
    }

    /**
     * 使用XStream把XML转换成对象
     *
     * @param xml Xml字符串
     * @return 转换后的对象
     */
    public static Object fromXml(String xml) {
        return fromXmlInner(xml, null);
    }

    /**
     * 使用XStream把XML转换成对象
     *
     * @param xml Xml字符串
     * @return 转换后的对象
     */
    public static Object fromXml(String xml, Class... classes) {
        return fromXmlInner(xml, toClassMap(classes));
    }

    /**
     * 使用XStream把XML转换成对象
     *
     * @param xml Xml字符串
     * @return 转换后的对象
     */
    public static Object fromXml(String xml, Map<String, Class> classes) {
        return fromXmlInner(xml, classes);
    }

    /**
     * 使用XStream把XML转换成对象
     *
     * @param xml Xml字符串
     * @return 转换后的对象
     */
    private static Object fromXmlInner(String xml, Map<String, Class> classes) {
        XStreamBuilder builder = new XStreamBuilder();
        builder.addAliases(classes);
        return builder.build().fromXML(xml);
    }

    /**
     * 使用默认的XStream对象把对象转换成Json
     *
     * @param instance 要转换的对象
     * @return Json字符串
     */
    public static String toJson(Object instance, Class[] classes, Converter... converters) {
        XStreamBuilder builder = new XStreamBuilder();
        builder.addAliases(toClassMap(classes));
        builder.addConverters(converters);
        return builder.build(true, false).toXML(instance);
    }

    /**
     * 使用默认的XStream对象把对象转换成Json
     *
     * @param instance 要转换的对象
     * @return Json字符串
     */
    public static String toJson(Object instance, Class... classes) {
        return toJson(instance, classes, null);
    }


    /**
     * 使用默认的XStream对象把JSON字符串转换成对象
     *
     * @param json 要转换的字符串
     * @return 转换成功的对象
     */
    public static Object fromJson(String json, Class[] classes, Converter... converters) {
        XStreamBuilder builder = new XStreamBuilder();
        builder.addAliases(toClassMap(classes));
        builder.addConverters(converters);
        return builder.build(true, true).fromXML(json);
    }

    /**
     * 使用默认的XStream对象把JSON字符串转换成对象
     *
     * @param json 要转换的字符串
     * @return 转换成功的对象
     */
    public static Object fromJson(String json, Class... classes) {
        return fromJson(json, classes, null);
    }
}
