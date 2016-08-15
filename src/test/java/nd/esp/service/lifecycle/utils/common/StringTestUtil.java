package nd.esp.service.lifecycle.utils.common;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: 字符串操作类 </p>
 * <p>Description: StringUtil </p>
 * <p>Copyright: Copyright (c) 2015 </p>
 * <p>Company: ND Websoft Inc. </p>
 * <p>Create Time: 2016年07月01日 </p>
 * @author lianggz
 * @version 0.1
 */
public class StringTestUtil {
    private static Random random = new Random();
    private static char[] ch = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    /**
     * 获取随机数字
     * @param len
     * @author lianggz
     */
    public static String randomNumbers(int len) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len; ++i) {
            int index = random.nextInt(10);
            sb.append(ch[index]);
        }
        return sb.toString();
    }

    /**
     * 是否匹配字符
     * @param text
     * @param regex
     * @author lianggz
     */
    public static boolean isMatches(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()) {
            return true;
        }
        return false;
    }
}