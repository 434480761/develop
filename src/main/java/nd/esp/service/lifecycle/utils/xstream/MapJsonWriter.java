package nd.esp.service.lifecycle.utils.xstream;

import com.thoughtworks.xstream.io.json.JsonWriter;

import java.io.Externalizable;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

/**
 * 为了实现Map的Key/Value方式实现JSON
 *
 * @author bifeng.liu
 */
public class MapJsonWriter extends JsonWriter {
    public MapJsonWriter(Writer writer) {
        super(writer);
    }

    public MapJsonWriter(Writer writer, int mode) {
        super(writer, mode);
    }

    /**
     * 重写判断是否为数组的方法，去掉Map.class的判断
     *
     * @param clazz the type
     * @return <code>true</code> if handles as array
     * @since 1.4
     */
    protected boolean isArray(Class clazz) {
        return clazz != null && (clazz.isArray()
                || Collection.class.isAssignableFrom(clazz)
                || Externalizable.class.isAssignableFrom(clazz)
//                || Map.class.isAssignableFrom(clazz)
                || Map.Entry.class.isAssignableFrom(clazz)
        );
    }
}
