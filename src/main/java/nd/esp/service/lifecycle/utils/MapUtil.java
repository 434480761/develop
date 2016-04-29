package nd.esp.service.lifecycle.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

/**
 * @title TODO
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年1月27日 上午11:29:36
 */
public class MapUtil {
    /**
     * 将参数统统转为Json格式
     *
     * @param paramMap 参数
     * @return
     */
    public static final Map<String, String> toStringMap(Map<String, Object> paramMap) {
        Map<String, String> params = new IdentityHashMap<String, String>(paramMap.size());
        Iterator<Map.Entry<String, Object>> iterator = paramMap.entrySet().iterator();
        Map.Entry<String, Object> entry;
        String key;
        Object value;
        while (iterator.hasNext()) {
            entry = iterator.next();
            key = entry.getKey();
            value = entry.getValue();
            // String 不转json，否则会加上双引号
            if (null == value) {
                params.put(key, null);
            } else if (value instanceof String) {
                params.put(key, value.toString());
            }  else if (value instanceof Number) {
                params.put(key, value.toString());
            }else if (value instanceof Collection) {
                for (Object o : (Collection) value) {
                    if (o instanceof String) {
                        // String 不转json，否则会加上双引号
                        params.put(new String(key), o.toString());
                    } else {
                        params.put(new String(key), ObjectUtils.toJson(o));
                    }
                }
            } else if (value.getClass().isArray()) {
                Object o;
                for (int i = 0; i < Array.getLength(value); i++) {
                    o = Array.get(value, i);
                    if (o instanceof String) {
                        // String 不转json，否则会加上双引号
                        params.put(new String(entry.getKey()), o.toString());
                    } else {
                        params.put(new String(entry.getKey()), ObjectUtils.toJson(o));
                    }
                }
            } else {
                params.put(entry.getKey(), ObjectUtils.toJson(entry.getValue()));
            }
        }
        return params;
    }
}
