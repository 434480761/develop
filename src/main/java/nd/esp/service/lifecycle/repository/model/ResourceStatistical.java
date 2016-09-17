package nd.esp.service.lifecycle.repository.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 类描述:bean
 * 创建人:liur
 * 创建时间:2015-11-25 
 * @version
 */

@Entity
@Table(name = "resource_statisticals")
@NamedQueries({ 
    @NamedQuery(name = "getStatisticalBuResource", query = "select rs from ResourceStatistical rs where rs.resource=:resourceId"),
        @NamedQuery(name = "commonQueryGetStatistical", query = "SELECT rs from ResourceStatistical rs where rs.resType IN (:rts) AND rs.resource IN  (:sids)")
})
public class ResourceStatistical extends EspEntity {

    /**统计项目名称*/
    @Column(name = "key_title")
    private String keyTitle;

    /**统计值*/
    @Column(name = "key_value")
    private Double keyValue;

    /**操作时间*/
    @Column(name = "update_time")
    private Timestamp updateTime;

    /**对某类资源的统计*/
    private String resource;

    /**统计数据来源*/
    @Column(name = "data_from")
    private String dataFrom;

    @Column(name = "res_type")
    private String resType;

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getKeyTitle() {
        return keyTitle;
    }

    public void setKeyTitle(String keyTitle) {
        this.keyTitle = keyTitle;
    }

    public Double getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(Double keyValue) {
        this.keyValue = keyValue;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    @Override
    public IndexSourceType getIndexType() {
        return null;
    }

}
