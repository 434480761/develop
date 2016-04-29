package nd.esp.service.lifecycle.educommon.vos.statistics;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 资源统计的viewModel
 * <p>Create Time: 2015年8月15日           </p>
 * @author xiezy
 */
@JsonInclude(Include.NON_NULL)
public class ResourceStatisticsViewModel {
    /**
     * 资源类型的维度编码ndcode
     */
    private String resourceType;
    /**
     * 统计查询数量值
     */
    private int count;
    /**
     * 统计记录的时间
     */
    private Date recordDate;
    
    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public Date getRecordDate() {
        return recordDate;
    }
    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
}
