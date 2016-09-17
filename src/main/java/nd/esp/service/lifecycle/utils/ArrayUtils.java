package nd.esp.service.lifecycle.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展Apache Commons ArrayUtils, 提供对字符串数组的连接处理.
 *
 * @author bifeng.liu
 * @see org.apache.commons.lang.ArrayUtils
 */
public final class ArrayUtils extends org.apache.commons.lang.ArrayUtils {
    /**
     * 私有化构造函数，不允许实例化该类
     */
    private ArrayUtils() {
    }

    /**
     * The empty String <code>""</code>.
     */
    public static final String EMPTY = "";

    /**
     * <p>连接字符串数组</p>
     * <p/>
     * <p>使用空字符串连接
     * 如果数组中的字符串为空或者NULL，则会被忽略</p>
     * <p/>
     * <pre>
     * ArrayUtils.join(null)            = null
     * ArrayUtils.join([])              = ""
     * ArrayUtils.join([null])          = ""
     * ArrayUtils.join(["a", "b", "c"]) = "abc"
     * ArrayUtils.join([null, "", "a"]) = "a"
     * </pre>
     *
     * @param array 要连接字符串数组
     * @return 连接后的字符串, 如果为null返回<code>null</code>
     */
    public static String join(Object[] array) {
        return join(array, null);
    }

    /**
     * <p>使用特定的字符串，连接字符串数组</p>
     * <p/>
     * <p>使用特定的字符串连接
     * 如果数组中的字符串为空或者NULL，设置为空字符串</p>
     * <p/>
     * <pre>
     * ArrayUtils.join(null, *)                = null
     * ArrayUtils.join([], *)                  = ""
     * ArrayUtils.join([null], *)              = ""
     * ArrayUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * ArrayUtils.join(["a", "b", "c"], null)  = "abc"
     * ArrayUtils.join(["a", "b", "c"], "")    = "abc"
     * ArrayUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array     要连接字符串数组
     * @param separator 分隔符
     * @return 连接后的字符串, 如果为null返回<code>null</code>
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>使用特定的字符串，连接字符串数组</p>
     * <p/>
     * <p>使用特定的字符串从开始到结束索引连接
     * 如果数组中的字符串为空或者NULL，设置为空字符串</p>
     * <p/>
     * <pre>
     * ArrayUtils.join(null, *)                = null
     * ArrayUtils.join([], *)                  = ""
     * ArrayUtils.join([null], *)              = ""
     * ArrayUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * ArrayUtils.join(["a", "b", "c"], null)  = "abc"
     * ArrayUtils.join(["a", "b", "c"], "")    = "abc"
     * ArrayUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array      要连接字符串数组
     * @param separator  分隔符
     * @param startIndex 开始索引
     * @param length     长度
     * @return 连接后的字符串, 如果为null返回<code>null</code>
     */
    public static String join(Object[] array, String separator, int startIndex, int length) {
        if (array == null) {
            return null;
        }
        startIndex = startIndex < 0 ? 0 : startIndex;
        if (startIndex >= array.length || length <= 0) {
            return EMPTY;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        int endIndex = startIndex + length;
        endIndex = endIndex > array.length ? array.length : endIndex;
        StringBuilder buf = new StringBuilder();

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * <p>把数组转换成List</p>
     * <p>该转换后还可以自由添加元素</p>
     *
     * @param array 数组
     * @param <T>
     * @return
     * @see java.util.Arrays#asList(T[])
     */
    public static <T> List<T> asList(T... array) {
        if (array == null) {
            return new ArrayList<T>(0);
        }
        List<T> result = new ArrayList<T>(array.length);
        for (int i = 0; i < array.length; i++) {
            T t = array[i];
            result.add(t);
        }
        return result;
    }
}
