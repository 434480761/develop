
package nd.esp.service.lifecycle.utils;

import com.alibaba.fastjson.JSONArray;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * <p>Title: Assert工具类 </p>
 * <p>Description: Assert工具类 </p>
 * <p>Copyright: Copyright (c) 2015 </p>
 * <p>Company: ND Websoft Inc. </p>
 * <p>Create Time: 2015年12月03日 </p>
 * @author lianggz
 * @version 0.1
 */
public abstract class AssertUtils {

    private AssertUtils(){}
    

	/**
	 * 断言 为空
	 * @param str
	 * @param message
	 * @author lianggz
	 */
	public static void isEmpty(String str, String message) {
	    if(StringUtils.isBlank(str)){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.checkParamEmpty.getCode()
                    ,message + LifeCircleErrorMessageMapper.checkParamEmpty.getMessage());
		}
	}


    /**
     * 断言 Json字符串数组
     * @param json
     * @param message
     * @author lianggz
     */
    public static void isJsonArray(String json, String message) {
        boolean state = false;
        try {
            JSONArray.parse(json);
            state = true;
        } catch (Exception e) {
            state = false;
        }
        if (!state){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }


    /**
     * 断言  字符串长度范围
     * @param value
     * @param min
     * @param max
     * @param message
     * @author lianggz
     */
    public static void rangeLength(String value, int min, int max, String message) {
        if (value.length()<min || value.length()>max){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }
    
    /**
     * 断言  数字
     * @param str
     * @param message
     * @author lianggz
     */
    public static void isNumber(String str, String message) {
        if (!str.matches("^[0-9]*$")){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }

    /**
     * 断言  数字主键Integer
     * @param str
     * @param message
     * @author lsk
     */
    public static void isInteger(String str, String message) {
        isNumber(str,message);
        try {
            Integer.valueOf(str);
        } catch (Exception e) {
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }

    /**
     * 断言  数字主键Long
     * @param str
     * @param message
     * @author lsk
     */
    public static void isLong(String str, String message) {
        isNumber(str,message);
        try {
            Long.valueOf(str);
        } catch (Exception e) {
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }
    
    /**
     * 对象是否null	  
     * @param obj
     * @param message
     * @author lianggz
     */
    public static void isObjectNull(Object obj, String message) {
        if (obj==null){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }


    /**
     * 断言 是否合法的数字串
     * @param value
     * @param message
     * @author lianggz
     */
    public static void isValidIds(String value, String message) {
        String[] arrStr = StringUtils.split(value, ",");
        for (String str : arrStr) {
            isNumber(str, message);
        }
    }
    
    /**
     * 断言 是否合法的数字串      
     * @param value
     * @param message
     * @author lianggz
     */
    public static void isValidIds4Int(String value, String message) {
        String[] arrStr = StringUtils.split(value, ",");
        for (String str : arrStr) {
            isInteger(str, message);
        }
    }
    /**
     * 断言 是否合法的数字串      
     * @param value
     * @param message
     * @author lianggz
     */
    public static void isValidIds4Long(String value, String message) {
        String[] arrStr = StringUtils.split(value, ",");
        for (String str : arrStr) {
            isLong(str, message);
        }
    }

    /**
     * 断言 正则表达式匹配
     * @param str
     * @param message
     * @author lianggz
     */
    public static void isMatches(String str, String regex, String message) {
        if (!str.matches(regex)){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }

    /**
     * 断言 正则表达式匹配
     * @param strList
     * @param message
     * @author lianggz
     */
    public static void isMatches(List<String> strList, String regex, String message) {
        if(strList != null && strList.size() > 0){
            for(int i = 0; i <strList.size(); i++){
                if (!strList.get(i).matches(regex)){
                    throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                            ,message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
                }
            }
        }
    }

}
