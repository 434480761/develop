package nd.esp.service.lifecycle.repository.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.IndexMapper;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 资源评注
 * @author caocr
 *
 */
@Entity
@Table(name = "resource_annotations")
public class ResourceAnnotation extends EspEntity implements IndexMapper {
    private static final long serialVersionUID = 1L;

    //资源类型
    @Column(name = "res_type")
    private String resType;
    
    //资源标识
    @Column(name = "resource")
    private String resource;
    
    //评价者的标识
    @Column(name = "entity_identifier")
    private String entityIdentifier;

    //创建时间
    @Transient
    protected Timestamp createTime;
    
    @Column(name = "create_time")
    protected BigDecimal dbcreateTime;

    //评价内容
    @Column(name = "content")
    private String content;

    //评价类型，暂时默认
    @Column(name = "annotation_type")
    private String annotationType;

    //评价分值
    @Column(name = "score")
    private Double score;

    //评价级别
    @Column(name = "score_level")
    private Integer scoreLevel;

    //评价来源
    @Column(name = "annotation_from")
    private String annotationFrom;

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public Timestamp getCreateTime() {
        if(this.dbcreateTime != null){
            this.createTime = new Timestamp(dbcreateTime.longValue());
        } 
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
        if(this.createTime != null){
            this.dbcreateTime = new BigDecimal(createTime.getTime());
        }
    }
    
    public BigDecimal getDbcreateTime() {
        return dbcreateTime;
    }

    public void setDbcreateTime(BigDecimal dbcreateTime) {
        this.dbcreateTime = dbcreateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getScoreLevel() {
        return scoreLevel;
    }

    public void setScoreLevel(Integer scoreLevel) {
        this.scoreLevel = scoreLevel;
    }

    public String getAnnotationFrom() {
        return annotationFrom;
    }

    public void setAnnotationFrom(String annotationFrom) {
        this.annotationFrom = annotationFrom;
    }

    @Override
    public IndexSourceType getIndexType() {
        return IndexSourceType.ResourceAnnotationType;
    }

}
