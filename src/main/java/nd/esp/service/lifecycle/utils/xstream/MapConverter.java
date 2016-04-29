package nd.esp.service.lifecycle.utils.xstream;


import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.*;

/**
 * Map转换类，使用Key/Value的方法展现
 *
 * @author bifeng.liu
 */
public class MapConverter extends com.thoughtworks.xstream.converters.collections.MapConverter {

    private boolean isOriginal = false;

    public MapConverter(Mapper mapper) {
        super(mapper);
    }

    public MapConverter(Mapper mapper, boolean isOriginal) {
        super(mapper);
        this.isOriginal = isOriginal;
    }

    /**
     * 对象转换成JSON字符串
     *
     * @param source
     * @param writer
     * @param context
     */
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map map = (Map) source;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iterator.next();
            writeItem(entry, context, writer);
        }
    }

    protected void writeItem(Map.Entry entry, MarshallingContext context, HierarchicalStreamWriter writer) {
        // PUBLISHED API METHOD! If changing signature, ensure backwards compatibility.
        Object key = entry.getKey();
        Object value = entry.getValue();
        String name = key == null ? "null" : key.toString();
        if (value == null) {
            ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, Mapper.Null.class);
            writer.endNode();
        } else {
            ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, value.getClass());
            context.convertAnother(value);
            writer.endNode();
        }
    }

    public String serializedClass(Class type) {
        return type.getName();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (isOriginal) {
            return super.unmarshal(reader, context);
        } else {
            Map result = (Map) createObject(context.getRequiredType());
            populateNewMap(reader, context, result);
            return result;
        }
    }

    protected void populateNewMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map result) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            putCurrentEntryIntoMap(reader, context, result);
            reader.moveUp();
        }
    }

    protected void putCurrentEntryIntoMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map result) {
        String key = reader.getNodeName();
        if (reader.hasMoreChildren()) {
            putChildrenMap(reader, context, result);
            return;
        }
        List list = wrapList(result, key);
        if (list != null) {
            list.add(reader.getValue());
        } else {
            result.put(key, reader.getValue());
        }
    }

    protected void putChildrenMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map result) {
        String key = reader.getNodeName();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Map value = null;
            boolean flag = false;
            if (result.containsKey(key)) {
                Object obj = result.get(key);
                if (obj != null && Map.class.isAssignableFrom(obj.getClass())) {
                    value = (Map) result.get(key);
                } else {
                    List list = wrapList(result, key);
                    value = (Map) createObject(context.getRequiredType());
                    list.add(value);
                    flag = true;
                }
            } else {
                value = (Map) createObject(context.getRequiredType());
            }
            putCurrentEntryIntoMap(reader, context, value);
            if (!flag) {
                result.put(key, value);
            }
            reader.moveUp();
        }
    }

    private List wrapList(Map result, String key) {
        Object obj = result.get(key);
        List list = null;
        if (obj != null && List.class.isAssignableFrom(obj.getClass())) {
            list = (List) obj;
        } else if (result.containsKey(key)) {
            list = new ArrayList();
            list.add(obj);
            result.put(key, list);
        }
        return list;
    }

    public boolean canConvert(Class type) {
        return type.equals(HashMap.class)
                || type.equals(Hashtable.class)
                || type.getName().equals("java.util.LinkedHashMap")
                || type.getName().equals("java.util.concurrent.ConcurrentHashMap")
                || type.getName().equals("sun.font.AttributeMap") // Used by java.awt.Font in JDK 6
                ;
    }

    protected Object createObject(Class type) {
        if (!Map.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Donot support class " + type.getName());
        }
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new ConversionException("Cannot instantiate " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ConversionException("Cannot instantiate " + type.getName(), e);
        }
    }
}
