package nd.esp.service.lifecycle.repository.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 提供给资源preview数据修复使用
 * <p>Create Time: 2015年12月23日           </p>
 * @author ql
 */
@Entity
@Table( name ="resource_previews")
public class ResourcePreviews extends EspEntity {

    @Column(name = "res_type")
    private String resType;
    
    @Column(name = "question_big")
    private String questionBig;
    
    @Column(name = "question_small")
    private String questionSmall;
    
    @Transient
    private Map<String, String> preview = new HashMap<String, String>();
    
    @DataConverter(target="preview", type=Map.class)
    @Column(name = "preview")
    private String dbpreview;
    
    @Column(name = "update_time")
    private Timestamp updateTime;
    
    
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getQuestionBig() {
        return questionBig;
    }

    public void setQuestionBig(String questionBig) {
        this.questionBig = questionBig;
    }

    public String getQuestionSmall() {
        return questionSmall;
    }

    public void setQuestionSmall(String questionSmall) {
        this.questionSmall = questionSmall;
    }

    public Map<String, String> getPreview() {
        return preview;
    }

    public void setPreview(Map<String, String> preview) {
        this.preview = preview;
    }

    public String getDbpreview() {
        return dbpreview;
    }

    public void setDbpreview(String dbpreview) {
        this.dbpreview = dbpreview;
    }

    @Override
    public IndexSourceType getIndexType() {
        // TODO Auto-generated method stub
        return null;
    }
}
