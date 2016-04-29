package nd.esp.service.lifecycle.educommon.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.vos.statistics.ResourceStatisticsViewModel;
import nd.esp.service.lifecycle.educommon.vos.statistics.StatisticsByDayViewModel;
import nd.esp.service.lifecycle.educommon.vos.statistics.TimingStatisticsViewModel;

/**
 * 资源统计Service
 * <p>Create Time: 2015年8月15日           </p>
 * @author xiezy
 */
public interface NDResourceStatisticsService {
    
    /**
     * 资源类型统计	
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param categoryPath
     * @param resourceTypes
     * @param relationsMap
     * @param coveragesList
     * @param propsMap
     */
    public List<ResourceStatisticsViewModel> resourceStatistics(String categoryPath,Set<String> resourceTypes,
            Map<String,String> relationsMap,List<String> coveragesList,Map<String,Set<String>> propsMap);
    
    /**
     * 资源类型的定时统计
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param resourceTypes     资源类型的编码
     * @param timeUnit          支持none、day、mouth、year，默认是day。当为none的时候，返回资源类型的总数，只有一条记录，limit无效
     * @param offset            
     * @param limit
     */
    public TimingStatisticsViewModel resourceTimingStatistics(Set<String> resourceTypes,String timeUnit,int offset, int limit);
    
    /**
     * 资源类型每天的增量统计  
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param offset            
     * @param limit
     */
    public StatisticsByDayViewModel resourceStatisticsByDay(int offset, int limit);
    
}
