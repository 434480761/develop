package nd.esp.service.lifecycle.educommon.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.services.NDResourceStatisticsService;
import nd.esp.service.lifecycle.educommon.vos.statistics.ResourceStatisticsViewModel;
import nd.esp.service.lifecycle.educommon.vos.statistics.StatisticsByDayViewModel;
import nd.esp.service.lifecycle.educommon.vos.statistics.TimingStatisticsViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NDResourceStatisticsServiceImpl implements NDResourceStatisticsService{
//	private static final Logger LOG = LoggerFactory.getLogger(NDResourceStatisticsServiceImpl.class);
    
    @Autowired
    private NDResourceDao ndResourceDao;
    
    @Override
    public List<ResourceStatisticsViewModel> resourceStatistics(String categoryPath, Set<String> resourceTypes, Map<String,String> relationsMap, 
            List<String> coveragesList,Map<String, Set<String>> propsMap) {
//        return ndResourceDao.resourceStatistics(categoryPath, resourceTypes, relationsMap, coveragesList, propsMap);
        return null;
    }
    
    @Override
    public TimingStatisticsViewModel resourceTimingStatistics(Set<String> resourceTypes, String timeUnit, int offset, int limit) {
//        return ndResourceDao.resourceTimingStatistics(resourceTypes, timeUnit, offset, limit);
        return null;
    }
    
    @Override
    public StatisticsByDayViewModel resourceStatisticsByDay(int offset, int limit) {
//        return ndResourceDao.resourceStatisticsByDay(offset, limit);
        return null;
    }
}
