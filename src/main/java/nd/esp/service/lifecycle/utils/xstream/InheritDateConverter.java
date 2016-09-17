package nd.esp.service.lifecycle.utils.xstream;


import java.util.Date;
import java.util.TimeZone;

/**
 * 日期类型转换类
 * 继承自<code>com.thoughtworks.xstream.converters.basic.DateConverter</code>
 * 扩展canConvert方法，只要基于<code>java.util.Date</code>都会被该类转换
 *
 * @author bifeng.liu
 */
public class InheritDateConverter extends com.thoughtworks.xstream.converters.basic.DateConverter {

    public InheritDateConverter(String defaultFormat, String[] acceptableFormats) {
        super(defaultFormat, acceptableFormats);
    }

    public InheritDateConverter(String defaultFormat, String[] acceptableFormats, TimeZone timeZone) {
        super(defaultFormat, acceptableFormats, timeZone);
    }

    /**
     * @param type
     * @return
     */
    public boolean canConvert(Class type) {
        return Date.class.isAssignableFrom(type);
    }
}
