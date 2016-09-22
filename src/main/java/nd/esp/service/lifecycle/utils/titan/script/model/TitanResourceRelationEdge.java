package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.utils.titan.script.annotation.*;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Administrator on 2016/9/14.
 */
@TitanEdge(label = "has_relation")
public class TitanResourceRelationEdge extends TitanModel{
    @TitanField(name = "enable")
    private Boolean enable;

    @TitanCompositeKey
    @TitanField(name = "identifier")
    protected String identifier;

    @TitanField(name = "order_num")
    private Integer orderNum;

    @TitanField(name = "relation_type")
    private String relationType;

    @TitanField(name = "sort_num")
    private Float sortNum;

//    @TitanEdgeResourceKey(source = "primary_category")
    @TitanField(name = "res_type")
    private String resType;

    @TitanEdgeResourceKey(source = "identifier")
    @TitanField(name = "source_uuid")
    private String sourceUuid;

    @TitanField(name = "tags")
    private String dbtags;

//    @TitanEdgeTargetKey(target = "primary_category")
    @TitanField(name = "resource_target_type")
    private String resourceTargetType;

    @TitanEdgeTargetKey(target = "identifier")
    @TitanField(name = "target_uuid")
    private String target;

    @TitanField(name = "rr_label")
    private String label;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Float getSortNum() {
        return sortNum;
    }

    public void setSortNum(Float sortNum) {
        this.sortNum = sortNum;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getSourceUuid() {
        return sourceUuid;
    }

    public void setSourceUuid(String sourceUuid) {
        this.sourceUuid = sourceUuid;
    }

    public String getDbtags() {
        return dbtags;
    }

    public void setDbtags(String dbtags) {
        this.dbtags = dbtags;
    }

    public String getResourceTargetType() {
        return resourceTargetType;
    }

    public void setResourceTargetType(String resourceTargetType) {
        this.resourceTargetType = resourceTargetType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
