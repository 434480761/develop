package nd.esp.service.lifecycle.vos.resourceannotations.v06;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;

/**
 * Created by caocr on 2015/11/25 0025.
 */
public class ResourceAnnotationViewModel {
    //评注标识
    private String identifier;

    //评价者的标识
    @NotBlank(message="{resourceAnnotationViewModel.entityIdentifier.notBlank.validmsg}")
    private String entityIdentifier;

    //创建时间
    private Date createTime;
    
    //资源id
    private String resource;

    //评价内容
    @NotBlank(message = "{resourceAnnotationViewModel.content.notBlank.validmsg}")
    @Length(message = "{resourceAnnotationViewModel.content.maxlength.validmsg}", max = 1000)
    private String content;

    //评价类型，暂时默认
    @NotBlank(message="{resourceAnnotationViewModel.annotationType.notBlank.validmsg}")
    private String annotationType;

    //评价分值
    private Double score;

    //评价级别
    private Integer scoreLevel;

    //评价来源
    private String annotationFrom;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
    
    public String getResource() {
       return resource;
    }

    public void setResource(String resId) {
        this.resource = resId;
    }
}
