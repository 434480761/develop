package nd.esp.service.lifecycle.educommon.vos.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.http.HttpStatus;
/**
 * Includes常量 + 检验方法
 * <p>Create Time: 2015年7月2日           </p>
 * @author xiezy
 */
public class IncludesConstant {
    /**
     * TI：技术属性
     */
    public final static String INCLUDE_TI  = "TI";
    /**
     * LC：生命周期属性
     */
    public final static String INCLUDE_LC  = "LC";
    /**
     * EDU：教育属性
     */
    public final static String INCLUDE_EDU = "EDU";
    /**
     * CG：分类维度数据属性
     */
    public final static String INCLUDE_CG  = "CG";
    /**
     * CR:版权信息
     */
    public final static String INCLUDE_CR  = "CR";
    
    /**
     * 返回有效的includes的常量集合
     * <p>Create Time: 2015年7月2日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    public static List<String> getIncludesList(){
        List<String> list = new ArrayList<String>();
        list.add(INCLUDE_TI);
        list.add(INCLUDE_LC);
        list.add(INCLUDE_EDU);
        list.add(INCLUDE_CG);
        list.add(INCLUDE_CR);
        
        return list;
    }
    
    /**
     * 判读includes是否有效,并返回有效集合.一般用于参数验证	
     * <p>Create Time: 2015年7月2日   </p>
     * <p>Create author: xiezy   </p>
     * @param includes
     * @return
     */
    public static List<String> getValidIncludes(String includes){
        if(StringUtils.isEmpty(includes)){
            return new ArrayList<String>();
        }
        
        Set<String> set = new HashSet<String>(Arrays.asList(includes.split(",")));
        List<String> includesList = getIncludesList();
        for(String include : set){
            if(!includesList.contains(include.trim())){
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.IncludesParamError.getCode(),
                        "includes中的:" + include + ",不在规定范围内");
            }
        }
        
        return new ArrayList<String>(set);
    }
    
    /**
     * 判断includes中是否不包含某些属性
     * <p>Create Time: 2015年7月2日   </p>
     * <p>Create author: xiezy   </p>
     * @param includesList
     * @param attributes
     * @return
     */
//    public static boolean isNotContainsAttributes(List<String> includesList,List<String> attributes){
//        for(String include : includesList){
//            for(String attribute : attributes){
//                if((include.trim()).equals(attribute.trim())){
//                    return false;
//                }
//            }
//        }
//        
//        return true;
//    }
}
