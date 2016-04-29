package nd.esp.service.lifecycle.educommon.vos.statistics;

import java.util.List;
import java.util.Map;

/**
 * 资源定时统计的viewModel
 * <p>Create Time: 2015年8月15日           </p>
 * @author xiezy
 */
public class TimingStatisticsViewModel {
    /**
     * 查询统计结果的条数，条数的数量根据时间单位进行划分，同时跟limit有关
     */
    private int rows;
    /**
     * Map的key规则:
     *          如果当time_unit=none的时候，只返回一条查询记录数的时候，默认的key值为total，
     *          其他查询时间单位按照单位时间日期进行查询。显示key值和time_unit单位有关，如果是month，只能是月份值，不能日期
     */
    private Map<String, List<ResourceStatisticsViewModel>> items;
    
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    public Map<String, List<ResourceStatisticsViewModel>> getItems() {
        return items;
    }
    public void setItems(Map<String, List<ResourceStatisticsViewModel>> items) {
        this.items = items;
    }
}
