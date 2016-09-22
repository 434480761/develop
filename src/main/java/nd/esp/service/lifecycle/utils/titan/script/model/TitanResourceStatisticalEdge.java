package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.*;

import java.sql.Timestamp;

/**
 * Created by Administrator on 2016/9/18.
 */
@TitanEdge(label = "has_resource_statistical")
public class TitanResourceStatisticalEdge extends TitanModel{
    @TitanEdgeTargetKey(target = "identifier")
    @TitanCompositeKey
    @TitanField(name = "identifier")
    private String identifier;

    @TitanField(name = "sta_title")
    private String title;

    @TitanField(name = "sta_key_title")
    private String keyTitle;

    @TitanField(name = "sta_key_value")
    private Double keyValue;

    @TitanField(name = "sta_update_time")
    private Timestamp updateTime;

    @TitanField(name = "sta_data_from")
    private String dataFrom;

    @TitanEdgeResourceKey(source= "identifier")
    @TitanField(name="sta_resource")
    private String resource;

//    @TitanEdgeResourceKey(source = "primary_category")
    @TitanField(name = "sta_res_type")
    private String resType;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }
}
