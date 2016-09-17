package nd.esp.service.lifecycle.educommon.vos.statistics;

import java.util.List;

/**
 * 资源类型每天的增量统计的viewModel
 * <p>Create Time: 2015年8月15日           </p>
 * @author xiezy
 */
public class StatisticsByDayViewModel {
    /**
     * 查询统计结果的条数，条数的数量根据时间单位进行划分，同时跟limit有关
     */
    private int rows;
    /**
     * 返回的结果集
     */
    private List<ResourceStatisticsViewModel> items;
    
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    public List<ResourceStatisticsViewModel> getItems() {
        return items;
    }
    public void setItems(List<ResourceStatisticsViewModel> items) {
        this.items = items;
    }
}
