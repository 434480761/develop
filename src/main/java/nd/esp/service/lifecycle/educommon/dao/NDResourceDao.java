package nd.esp.service.lifecycle.educommon.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.DbName;

public interface NDResourceDao {

    /**
     * 通用查询	
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param resCodes          支持多种资源查询,resType=eduresource时生效
     * @param includes
     * @param categories
     * @param relations
     * @param coverages
     * @param propsMap
     * @param words
     * @param limit
     * @param isNotManagement 判断是否需要对ND库下的资源只允许查出ONLINE的限制
     * @param reverse 判断关系查询是否反转
     * @return
     */
    public List<ResourceModel> commomQueryByDB(final String resType, String resCodes, final List<String> includes,
            Set<String> categories, Set<String> categoryExclude, List<Map<String, String>> relations, List<String> coverages,
            Map<String, Set<String>> propsMap, Map<String, String> orderMap, String words, String limit,
            boolean isNotManagement, boolean reverse, boolean useIn, Boolean printable, String printableKey,
            String statisticsType,String statisticsPlatform,boolean forceStatus,List<String> tags);

    /**
     * 查询总数
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param resCodes          支持多种资源查询,resType=eduresource时生效
     * @param categories
     * @param relations
     * @param coverages
     * @param propsMap
     * @param words
     * @param limit
     * @param isNotManagement 判断是否需要对ND库下的资源只允许查出ONLINE的限制
     * @param reverse 判断关系查询是否反转
     * @return
     */
    public long commomQueryCount(String resType, String resCodes, Set<String> categories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, String limit, boolean isNotManagement, boolean reverse, boolean useIn,
            Boolean printable, String printableKey,boolean forceStatus,List<String> tags);
    
    /**
     * 资源统计
     * @param resType
     * @param categories
     * @param coverages
     * @param propsMap
     * @param isNotManagement
     * @return
     */
    public Map<String, Integer> resourceStatistics(String resType, Set<String> categories, List<String> coverages,
    		Map<String, Set<String>> propsMap, String groupBy, boolean isNotManagement);
    
    /**
     * 判断使用IN 还是 EXISTS 
     * TODO 该判断与业务关联比较大,存在不确定性
     * <p>Create Time: 2015年12月2日   </p>
     * <p>Create author: xiezy   </p>
     * @param relations
     * @param coverages
     * @return  true == IN  false == EXISTS
     */
    public boolean judgeUseInOrExists(String resType, String resCodes, Set<String> categories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, boolean isNotManagement, boolean reverse, Boolean printable, String printableKey,
            boolean forceStatus,List<String> tags);
    
    /**
     * 判断是否使用Redis.
     * 
     *  不是使用Redis的场景:
     *  a.limit=(a,b)， a+b>500时
     *  b.管理端接口,即带management的
     *  c.coverage参数不传时
     *  d.coverage参数中有非Org/nd/，即非nd库的
     *  
     * <p>Create Time: 2016年1月12日   </p>
     * <p>Create author: xiezy   </p>
     * @param limit
     * @param isNotManagement
     * @param coverages
     * @return
     */
    public boolean judgeUseRedisOrNot(String limit, boolean isNotManagement, List<String> coverages);

    /**
     * 获取通用查询的count值
     * 
     * @author:xuzy
     * @date:2015年11月30日
     * @param sql		查询的sql
     * @param param		查询的参数
     * @param flag		是否需要更新数据
     * @return
     */
    public int getResourceQueryCount(String sql, Map<String, Object> param, boolean flag, DbName dbName);

    /**
     * 查询维度数据（批量resource id）
     * @author linsm
     * @param resTypes
     * @param keySet
     * @return
     * @since
     */
    public List<ResourceCategory> queryCategoriesUseHql(List<String> resTypes, Set<String> keySet);

    /**
     * 查询技术属性（批量resource id）
     * @author linsm
     * @param resTypes
     * @param keySet
     * @return
     * @since
     */
    public List<TechInfo> queryTechInfosUseHql(List<String> resTypes, Set<String> keySet);
    
    /**
     * 根据资源类型与id查找数目
     * 
     * @author:xuzy
     * @date:2016年1月26日
     * @param resType
     * @param identifier
     * @return
     */
    public int queryCountByResId(String resType,String identifier);
	
	
	/**
     * 根据资源类型与id查找数目(习题库)
     * 
     * @author:xuzy
     * @date:2016年2月15日
     * @param resType
     * @param identifier
     * @return
     */
    public int queryCountByResId4QuestionDb(String resType, String identifier);
    
    /**
     * 判断资源编码是否重复
     * @param resType
     * @param identifier
     * @param code
     * @return
     */
    public int queryCodeCountByResId(String resType, String identifier,String code);
    
    /**
     * 判断资源编码是否重复(习题库)
     * @param resType
     * @param identifier
     * @param code
     * @return
     */
    public int queryCodeCountByResId4QuestionDb(String resType, String identifier,String code);
    
//    /**
//     * 删除章节相关的资源关系
//     * <p>Create Time: 2016年1月28日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param mid
//     */
//    public void deleteRelationByChapters(String mid);
//    
//    /**
//     * 删除章节	
//     * <p>Create Time: 2016年1月28日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param mid
//     */
//    public void deleteChapters(String mid);
    
    //****************************资源统计模块-暂不使用****************************\\
//    /**
//     * 资源类型统计   
//     * <p>Create Time: 2015年8月15日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param categoryPath
//     * @param resourceTypes
//     * @param relationsMap
//     * @param coverage
//     * @param propsMap
//     */
//    public List<ResourceStatisticsViewModel> resourceStatistics(String categoryPath, Set<String> resourceTypes,
//            Map<String, String> relationsMap, List<String> coverages, Map<String, Set<String>> propsMap);
//
//    /**
//     * 资源类型的定时统计
//     * <p>Create Time: 2015年8月15日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param resourceTypes     资源类型的编码
//     * @param timeUnit          支持none、day、mouth、year，默认是day。当为none的时候，返回资源类型的总数，只有一条记录，limit无效
//     * @param offset            
//     * @param limit             
//     */
//    public TimingStatisticsViewModel resourceTimingStatistics(Set<String> resourceTypes, final String timeUnit,
//            int offset, int limit);
//
//    /**
//     * 资源类型每天的增量统计  
//     * <p>Create Time: 2015年8月15日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param offset            
//     * @param limit
//     */
//    public StatisticsByDayViewModel resourceStatisticsByDay(int offset, int limit);
    //****************************资源统计模块-暂不使用****************************\\
}
