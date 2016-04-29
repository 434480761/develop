package nd.esp.service.lifecycle.utils.gson;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 枚举类型处理
 *
 * @author jsc
 */
public class DefaultEnumTypeAdapter {

    // 处理枚举类必须包含的方法名
    private static final String GET_VALUE_METHOD_NAME = "getValue";

    // 日志
    private static final Logger LOGGER = Logger.getLogger(DefaultEnumTypeAdapter.class);

    // 枚举类型适配器工厂
    public static final TypeAdapterFactory ENUM_FACTORY = createEnumTypeHierarchyFactory();

    /**
     * 静态类私有构造
     */
    private DefaultEnumTypeAdapter() {
    }

    /**
     * 获取适配器工厂
     *
     * @return 适配器工厂
     */
    public static TypeAdapterFactory createEnumTypeHierarchyFactory() {
        return new TypeAdapterFactory() {
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                // 枚举类型
                if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
                    return null;
                }
                // 判断参数个数大于2，且必须有getValue方法
                try {
                    Method method = rawType.getDeclaredMethod(GET_VALUE_METHOD_NAME);
                    if (rawType.getDeclaredConstructors()[0].getParameterTypes().length == 2 || !support(method)) {
                        return null;
                    }
                } catch (NoSuchMethodException e) {
                    LOGGER.warn(rawType.getName() + " no " + GET_VALUE_METHOD_NAME + " method!", e);
                    return null;
                }
                if (!rawType.isEnum()) {
                    rawType = rawType.getSuperclass(); // handle anonymous subclasses
                }
                return (TypeAdapter<T>) new EnumTypeAdapter(rawType);
            }
        };
    }

    /**
     * 是否支持的方法
     *
     * @param method 方法
     * @return boolean 是否支持
     */
    private static boolean support(Method method) {
        Class<?> typeClass = method.getReturnType();
        if (typeClass.equals(char.class) ||
                typeClass.equals(int.class) ||
                typeClass.equals(long.class) ||
                typeClass.equals(double.class) ||
                typeClass.equals(boolean.class) ||
                typeClass.equals(String.class) ||
                Number.class.isAssignableFrom(typeClass)) {
            return true;
        }
        return false;
    }

    /**
     * 枚举类型适配器（内部类）
     *
     * @param <T>
     */
    private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {

        // 枚举类型定义的枚举列表,getValue的返回值作为key
        private final Map<String, T> valueToEnum = new HashMap<String, T>();
        private final Map<T, Object> enumToValue = new HashMap<T, Object>();

        /**
         * 枚举适配器构造
         *
         * @param classOfT 类型
         */
        public EnumTypeAdapter(Class<T> classOfT) {
            try {
                Method method = classOfT.getDeclaredMethod(GET_VALUE_METHOD_NAME);
                for (T constant : classOfT.getEnumConstants()) {
                    Object value = method.invoke(constant);
                    valueToEnum.put(value.toString(), constant);
                    enumToValue.put(constant, value);
                }
            } catch (Exception e) {
                LOGGER.warn("EnumTypeAdapter Constructor error!", e);
                throw new AssertionError();
            }
        }

        /**
         * json格式的数据转为枚举对象
         *
         * @param in json数据
         * @return 枚举对象
         * @throws IOException 异常
         */
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return valueToEnum.get(in.nextString());
        }

        /**
         * 枚举对象转为json格式的数据
         *
         * @param out   json数据
         * @param value 枚举对象
         * @throws IOException 异常
         */
        public void write(JsonWriter out, T value) throws IOException {
            if (value != null) {
                Object outValue = enumToValue.get(value);
                if (outValue instanceof Character) {
                    out.value((Character) outValue);
                } else if (outValue instanceof Integer) {
                    out.value((Integer) outValue);
                } else if (outValue instanceof Long) {
                    out.value((Long) outValue);
                } else if (outValue instanceof Double) {
                    out.value((Double) outValue);
                } else if (outValue instanceof Number) {
                    out.value((Number) outValue);
                } else if (outValue instanceof Boolean) {
                    out.value((Boolean) outValue);
                } else if (outValue instanceof String) {
                    out.value((String) outValue);
                } else {
                    out.nullValue();
                }
            } else {
                out.nullValue();
            }
        }
    }
}
