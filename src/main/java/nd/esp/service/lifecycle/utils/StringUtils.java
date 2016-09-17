package nd.esp.service.lifecycle.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符串工具类
 * <p/>
 *
 * @author bifeng.liu
 */
public final class StringUtils {

	private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);
    private static long uniqueId = 0L;
    /**
     * The empty String <code>""</code>.
     */
    public static final String EMPTY = "";
    /**
     * 默认单词分割字符<code>"_"</code>
     */
    public static final char DEFAULT_WORD_SEPARATOR_CHAR = '_';
    /**
     * 随机对象
     */
    private static Random random = new Random();

    /**
     * 私有化构造函数，不允许实例化该类
     */
    private StringUtils() {
    }
    
    /**
     * 将时间戳转化为Date 1464764846605=>Date
     * @param timeStr
     * @return
     */
    public static Date strTimeStampToDate(String timeStr) {
        if (timeStr == null) return null;
        Long time = Long.parseLong(timeStr.trim());
        return new Date(time);
    }

    /**
     * 字符串date将转化为时间戳
     * @param date
     * @return
     */
    public static Long strDateToTimeStamp(String date) {
        if (date == null) return null;
        if(date.endsWith(".")){
            date=date.substring(0,date.length()-1);
        }
        SimpleDateFormat format =null;
        if (date.contains(".")) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        Date d=new Date();
        try {
            d = format.parse(date);
        } catch (ParseException e) {
        	LOG.error(e.getLocalizedMessage());
        }

        return d.getTime();
    }

    /**
     * 检查指定字符串长度，表示是否有值
     * <p><pre>
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength(" \t\n") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     *
     * @param str 要检查的字符串
     * @return <code>true</code> 如果不为null或者空，则返回真
     * @see #hasText(CharSequence)
     * @see #isEmpty(String)
     */
    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    /**
     * 检查指定字符串是否有内容，如果为null或者全为空白字符，则返回false
     * NOTE:空字符包括空格、换行符、Tab符等
     * <p><pre>
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText(" \n\t") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str 要检查的字符串
     * @return 如果不为null且不全为空字符，则返回<code>true</code> e
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查指定字符串长度是否为空字符串
     * <p><pre>
     * StringUtils.isEmpty(null) = true
     * StringUtils.isEmpty("") = true
     * StringUtils.isEmpty(" ") = false
     * StringUtils.isEmpty("Hello") = false
     * </pre>
     *
     * @param str 要检查的字符串
     * @return 如果为null或者空，则返回<code>true</code>
     * @see #hasLength(CharSequence)
     */
    public static boolean isEmpty(String str) {
        return !hasLength(str);
    }


    /**
     * 检查指定字符串长度是否为非空字符串
     * <p><pre>
     * StringUtils.isEmpty(null) = true
     * StringUtils.isEmpty("") = true
     * StringUtils.isEmpty(" ") = false
     * StringUtils.isEmpty("Hello") = false
     * </pre>
     *
     * @param str 要检查的字符串
     * @return 如果为null或者空，则返回<code>false</code>
     * @see #hasLength(CharSequence)
     */
    public static boolean isNotEmpty(String str) {
        return hasLength(str);
    }

    /**
     * 检查指定的字符串是否存在空字符
     * NOTE:空字符包括空格、换行符、Tab符等
     *
     * @param str 要检查的字符串
     * @return 字符串不为空，且至少存在一个空字符串，返回<code>true</code>
     * @see Character#isWhitespace
     */
    public static boolean containsWhitespace(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 去掉指定字符串前后的空字符串
     * NOTE:空字符包括空格、换行符、Tab符等
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     * @see Character#isWhitespace
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 去掉指定字符串所有的空字符串，包括前面、后面、中间的空字符串
     * NOTE:空字符包括空格、换行符、Tab符等
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     * @see Character#isWhitespace
     */
    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int index = 0;
        while (sb.length() > index) {
            if (Character.isWhitespace(sb.charAt(index))) {
                sb.deleteCharAt(index);
            } else {
                index++;
            }
        }
        return sb.toString();
    }

    /**
     * 去掉指定字符串前面的空字符串
     * NOTE:空字符包括空格、换行符、Tab符等
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     * @see Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * 去掉指定字符串后面的空字符串
     * NOTE:空字符包括空格、换行符、Tab符等
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     * @see Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 替换所有的指定字符串所有遇到的子字符串，
     * NOTE:在该替换中不能使用正则
     *
     * @param inString   要处理的字符串
     * @param oldPattern 旧的字符串
     * @param newPattern 新的字符串
     * @return 替换后的字符串
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; // our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        // remember to append any characters to the right of a match
        return sb.toString();
    }

    /**
     * 删除所有的指定字符串所有遇到的子字符串，
     * NOTE:在该替换中不能使用正则
     *
     * @param inString 要处理的字符串
     * @param pattern  旧的字符串
     * @return 处理后的字符串
     */
    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

    /**
     * 判断两个字符串是否相等
     *
     * @param srcStr  源字符串
     * @param destStr 目标字符串
     * @return 是否相同
     */
    public static boolean equals(String srcStr, String destStr) {
        return srcStr != null ? srcStr.equals(destStr) : destStr == null;
    }

    /**
     * 判断两个字符串是否相等，忽略大小写
     *
     * @param srcStr  源字符串
     * @param destStr 目标字符串
     * @return 是否相同
     */
    public static boolean equalsIgnoreCase(String srcStr, String destStr) {
        return srcStr != null ? srcStr.equalsIgnoreCase(destStr) : destStr == null;
    }

    /**
     * 从首位置开始，根据限制长度截取字符串
     * <p/>
     * 其中截取长度的计算：全角字符算两个字符，全角字符包括汉字等
     *
     * @param str         要截取的字符串
     * @param limitLength 截取的长度
     * @return 截取后的字符串
     * @see #subString(String, int, int)
     */
    public static String subString(String str, int limitLength) {
        return subString(str, 0, limitLength);
    }

    /**
     * 从传入的位置开始，根据限制长度截取字符串
     * <p/>
     * 其中截取长度的计算：全角字符算两个字符，全角字符包括汉字等
     *
     * @param str         要截取的字符串
     * @param startIndex  开始位置
     * @param limitLength 截取的长度
     * @return 截取后的字符串
     */
    public static String subString(String str, int startIndex, int limitLength) {
        if (!hasLength(str) || limitLength <= 0 || startIndex >= str.length()) {
            return "";
        }
        startIndex = startIndex < 0 ? 0 : startIndex;
        String doStr = str.substring(startIndex);
        int byteLen = 0; // 将汉字转换成两个字符后的字符串长度
        int strPos = 0;  // 对原始字符串截取的长度
        byte[] strBytes = null;
        try {
            strBytes = doStr.getBytes("gbk");// 将字符串转换成字符数组
        } catch (Exception ex) {
            strBytes = doStr.getBytes();
        }
        for (int i = 0; i < strBytes.length; i++) {
            if (strBytes[i] >= 0) {
                byteLen = byteLen + 1;
            } else {
                byteLen = byteLen + 2;// 一个汉字等于两个字符
                i++;
            }
            strPos++;

            if (byteLen >= limitLength) {
                if (strBytes[byteLen - 1] < 0) {
                    strPos--;
                }
                return doStr.substring(0, strPos);
            }
        }
        return doStr;
    }


    /**
     * 把字符串的首字母大写
     *
     * @param str 要转换的字符串
     * @return A new string that is <code>str</code> capitalized.
     * Returns <code>null</code> if str is null.
     */
    public static String toUpperFirstLetter(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        } else {
            return (new StringBuilder(strLen)).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).toString();
        }
    }

    /**
     * 把字符串的首字母小写
     *
     * @param str 要转换的字符串
     * @return A new string that is <code>str</code> capitalized.
     * Returns <code>null</code> if str is null.
     */
    public static String toLowerFirstLetter(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        } else {
            return (new StringBuilder(strLen)).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();
        }
    }

    /**
     * 生成唯一的字符串
     * <p/>
     * 字符串一共24位,全部由数字组成
     *
     * @return
     */
    public static String generateUniqueId() {
        String id = Long.toString(++uniqueId);
        if (uniqueId >= 1000000L) {
            uniqueId = 0L;
        }
        id = (new StringBuilder("000000")).append(id).toString().substring(id.length());
        return new Date().getTime() + "" + (random.nextInt() & 65535) + id;
    }

    /**
     * 生成UUID
     * <p/>
     * 当hasSymbol为false时，返回字符串格式为32位16进制数字
     * 当hasSymbol为true时，返回字符串格式为：xxxxxxxx-xxxx-xxxx-xxxxxx-xxxxxxxxxx (8-4-4-4-12)
     *
     * @return
     */
    public static String generateUUID(boolean hasSymbol) {
        UUID uuid = UUID.randomUUID();
        String result = uuid.toString();
        return hasSymbol ? result : result.replace("-", "");
    }

    /**
     * 把字符串转换成JSON格式
     *
     * @param str
     * @return
     */
    public static String toJsonQuote(String str) {
        if ((str == null) || (str.length() == 0)) {
            return "\"\"";
        }
        if ("true".equalsIgnoreCase(str)) {
            return "true";
        }
        if ("false".equalsIgnoreCase(str)) {
            return "false";
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(len + 4);

        sb.append('"');
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                case '/':
                case '\\':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if ((c < ' ') || (c >= '')) {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u");
                        sb.append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
                    break;
                    // no break
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * 使用默认的分隔符，转换成驼峰的显示方式
     *
     * @param str
     * @return
     */
    public static String toCamelCase(String str) {
        return toCamelCase(str, DEFAULT_WORD_SEPARATOR_CHAR);
    }

    /**
     * 使用字符串转换成驼峰显示方式
     *
     * @param str
     * @return
     */
    public static String toCamelCase(String str, char separatorChar) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(str.length());
        boolean upperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == separatorChar) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 把驼峰的显示方式转换成用某字符分隔的字符串
     *
     * @param str
     * @param separatorChar
     * @return
     */
    public static String revertCamelCase(String str, char separatorChar) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean nextUpperCase = true;
            if (i < (str.length() - 1)) {
                nextUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }
            if ((i >= 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    if (i > 0) {
                        sb.append(separatorChar);
                    }
                }
                upperCase = true;
            } else {
                upperCase = false;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 将正则表达式的符号转义掉
     * @param str
     * @return
     */
    public static String convertRegexp(String str) {
        return str.replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\$", "\\\\$")
                .replaceAll("\\^", "\\\\^")
                .replaceAll("\\{", "\\\\{")
                .replaceAll("\\}", "\\\\}")
                .replaceAll("\\[", "\\\\[")
                .replaceAll("\\]", "\\\\]")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\|", "\\\\|")
                .replaceAll("\\*", "\\\\*")
                .replaceAll("\\+", "\\\\+")
                .replaceAll("\\?", "\\\\?");
    }

    /**
     * <p>Joins the elements of the provided <code>Collection</code> into
     * a single String containing the provided elements.</p>
     *
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").</p>
     *
     * @param collection  the <code>Collection</code> of values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     * @since 2.3
     */
    public static String join(Collection collection, String separator) {
        return org.apache.commons.lang.StringUtils.join(collection, separator);
    }
}
