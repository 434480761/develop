package nd.esp.service.lifecycle.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcConfigModel;

/**
 * LCMS开关常量
 * <p>Create Time: 2016年3月2日           </p>
 * @author Administrator
 */
public class StaticDatas {
    /*控制通用查询是否允许查到QA覆盖范围的数据的开关*/
    public static boolean CAN_QUERY_QA_DATA = true;
    
    /*控制通用查询是否允许查到提供商为智能出题的数据的开关*/
    public static boolean CAN_QUERY_PROVIDER = true;
    
    /*用来暂停定时任务(删除脏数据)执行*/
    public static boolean suspendFlag = false;
    
    /*访问控制策略的开关*/
    public static boolean IS_IVC_CONFIG_ENABLED = false;
    
    /*通用查询是否优先使用ES查询的开关*/
    public static boolean QUERY_BY_ES_FIRST = true;
    
    /*同步推送数据给报表系统*/
    public static boolean SYNC_REPORT_DATA = true;
    
    /*访问控制策略的Map*/
    public static ConcurrentMap<String, IvcConfigModel> IVC_CONFIG_MAP = new ConcurrentHashMap<String, IvcConfigModel>();
    
    /*维度模式Map*/
    public static Map<String, CategoryPatternModel> CATEGORY_PATTERN_MAP = new HashMap<String, CategoryPatternModel>();
}
