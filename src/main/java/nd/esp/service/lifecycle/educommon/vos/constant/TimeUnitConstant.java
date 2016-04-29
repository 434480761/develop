package nd.esp.service.lifecycle.educommon.vos.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源类型定时统计中time_unit的常量
 * <p>Create Time: 2015年8月15日           </p>
 * @author xiezy
 */
public class TimeUnitConstant {
    
    public final static String TIMEUNIT_NONE  = "none";
    public final static String TIMEUNIT_DAY   = "day";
    public final static String TIMEUNIT_MOUTH = "mouth";
    public final static String TIMEUNIT_YEAR  = "year";
    
    public final static String NONE_KEY = "total";
    
    /**
     * 返回有效的TimeUnit的常量集合
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    public static List<String> getTimeUnitList(){
        List<String> list = new ArrayList<String>();
        list.add(TIMEUNIT_NONE);
        list.add(TIMEUNIT_DAY);
        list.add(TIMEUNIT_MOUTH);
        list.add(TIMEUNIT_YEAR);
        
        return list;
    }
    
    /**
     * 判断是否是有效的TimeUnit
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param timeUnit
     * @return
     */
    public static boolean isValidTimeUnit(String timeUnit){
        List<String> validTimeUnitList = getTimeUnitList();
        if(validTimeUnitList.contains(timeUnit.trim())){
            return true;
        }
        
        return false;
    }
}
