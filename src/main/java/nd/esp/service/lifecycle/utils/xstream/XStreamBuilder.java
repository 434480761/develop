package nd.esp.service.lifecycle.utils.xstream;

import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.utils.collection.MapExecutor;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.DateUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * XStream的建造类
 */
public class XStreamBuilder {
    private List<Converter> converters;
    private List<SingleValueConverter> valueConverters;
    private Map<String, Class> aliases;
    private Map<String, Class> implicitArrays;
    private Map<String, Class> implicitCollections;
    private Map<String, Class> implicitMaps;

    public XStreamBuilder() {
        converters = new ArrayList<Converter>();
        valueConverters = new ArrayList<SingleValueConverter>();
        valueConverters.add(new InheritDateConverter(DateUtils.DEFAULT_DATEFORMAT, new String[]{DateUtils.DEFAULT_DATEFORMAT}, Constant.DEFAULT_TIMEZONE));
        aliases = new LinkedHashMap<String, Class>();
        implicitArrays = new LinkedHashMap<String, Class>();
        implicitCollections = new LinkedHashMap<String, Class>();
        implicitMaps = new LinkedHashMap<String, Class>();

    }

    public XStreamBuilder(Converter[] converters, Map<String, Class> aliases) {
        this();
        this.addConverters(converters);
        this.addAliases(aliases);
    }

    public XStreamBuilder(SingleValueConverter[] valueConverters, Map<String, Class> aliases) {
        this();
        this.addValueConverters(valueConverters);
        this.addAliases(aliases);

    }

    public void addValueConverter(SingleValueConverter converter) {
        this.valueConverters.add(converter);
    }

    public void addValueConverters(SingleValueConverter[] converters) {
        if (converters != null) {
            this.valueConverters.addAll(Arrays.asList(converters));
        }
    }

    public void addValueConverters(List<SingleValueConverter> converters) {
        if (!CollectionUtils.isEmpty(converters)) {
            this.valueConverters.addAll(converters);
        }
    }

    public List<SingleValueConverter> getValueConverters() {
        return this.valueConverters;
    }

    public void addConverter(Converter converter) {
        this.converters.add(converter);
    }

    public void addConverters(Converter[] converters) {
        if (converters != null) {
            this.converters.addAll(Arrays.asList(converters));
        }
    }

    public void addConverters(List<Converter> converters) {
        if (!CollectionUtils.isEmpty(converters)) {
            this.converters.addAll(converters);
        }
    }

    public List<Converter> getConverters() {
        return this.converters;
    }

    public void addAliases(String name, Class clazz) {
        if (StringUtils.hasText(name) && clazz != null) {
            this.aliases.put(name, clazz);
        }
    }

    public void addAliases(Map<String, Class> aliases) {
        if (aliases != null && !aliases.isEmpty()) {
            this.aliases.putAll(aliases);
        }
    }

    public Map<String, Class> getAliases() {
        return this.aliases;
    }

    public void addImplicitArray(String name, Class clazz) {
        if (StringUtils.hasText(name) && clazz != null) {
            this.implicitArrays.put(name, clazz);
        }
    }

    public void addImplicitArrays(Map<String, Class> implicitArrays) {
        if (implicitArrays != null && !implicitArrays.isEmpty()) {
            this.implicitArrays.putAll(implicitArrays);
        }
    }

    public Map<String, Class> getImplicitArrays() {
        return this.implicitArrays;
    }

    public void addImplicitCollection(String name, Class clazz) {
        if (StringUtils.hasText(name) && clazz != null) {
            this.implicitCollections.put(name, clazz);
        }
    }

    public void addImplicitCollections(Map<String, Class> implicitCollections) {
        if (implicitCollections != null && !implicitCollections.isEmpty()) {
            this.implicitCollections.putAll(implicitCollections);
        }
    }

    public Map<String, Class> getImplicitCollections() {
        return this.implicitCollections;
    }


    public XStream build(boolean isJson, boolean isWrite) {
        XStream xStream = null;
        if (!isJson) {
            xStream = new XStream(new DomDriver(Constant.DEFAULT_CHARSET_NAME));
        } else if (isWrite) {
            xStream = new XStream(new JettisonMappedXmlDriver());
        } else {
            xStream = new XStream(new JsonHierarchicalStreamDriver() {
                public HierarchicalStreamWriter createWriter(Writer out) {
                    return new MapJsonWriter(out, JsonWriter.DROP_ROOT_MODE);
                }
            });
            xStream.addDefaultImplementation(Integer.class, Object.class);
            xStream.addDefaultImplementation(Long.class, Object.class);
            xStream.addDefaultImplementation(String.class, Object.class);
        }
        final XStream doXStream = xStream;
        doXStream.setMode(XStream.NO_REFERENCES);
        doXStream.addDefaultImplementation(Timestamp.class, Date.class);
        doXStream.addDefaultImplementation(java.sql.Date.class, Date.class);
        if (!CollectionUtils.isEmpty(converters)) {
            for (int i = 0; i < converters.size(); i++) {
                doXStream.registerConverter(converters.get(i));
            }
        }
        if (!CollectionUtils.isEmpty(valueConverters)) {
            for (int i = 0; i < valueConverters.size(); i++) {
                doXStream.registerConverter(valueConverters.get(i));
            }
        }
        CollectionUtils.forAllDo(aliases, new MapExecutor() {
            public void execute(Object key, Object value) {
                doXStream.alias((String) key, (Class) value);
            }
        });
        CollectionUtils.forAllDo(implicitArrays, new MapExecutor() {
            public void execute(Object key, Object value) {
                doXStream.addImplicitArray((Class) value, (String) key);
            }
        });
        CollectionUtils.forAllDo(implicitCollections, new MapExecutor() {
            public void execute(Object key, Object value) {
                doXStream.addImplicitCollection((Class) value, (String) key);
            }
        });
        return doXStream;
    }

    public XStream build() {
        return build(false, false);
    }

}
