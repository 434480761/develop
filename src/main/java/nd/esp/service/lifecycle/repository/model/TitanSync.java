package nd.esp.service.lifecycle.repository.model;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liuran on 2016/6/28.
 * titan异常数据同步
 */
@Entity
@Table(name = "titan_sync")
public class TitanSync extends EspEntity {
    @Column(name ="level" )
    private Integer level;
    @Column(name = "resource")
    private String resource;
    @Column(name="primary_category")
    private String primaryCategory;
    @Column(name = "create_time")
    private Long createTime;
    @Column(name = "execute_times")
    private Integer executeTimes;
    @Column(name = "type")
    private String type;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getExecuteTimes() {
        return executeTimes;
    }

    public void setExecuteTimes(Integer executeTimes) {
        this.executeTimes = executeTimes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public IndexSourceType getIndexType() {
        return null;
    }
}
