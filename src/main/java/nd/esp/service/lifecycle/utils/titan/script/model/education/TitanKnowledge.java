package nd.esp.service.lifecycle.utils.titan.script.model.education;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;

import java.util.Date;

/**
 * Created by Administrator on 2016/9/19.
 */
@TitanVertex(label = "knowledges")
public class TitanKnowledge extends TitanEducation{
    @TitanField(name = "ext_is_auto_remark")
    private Boolean isAutoRemark;
    @TitanField(name = "ext_question_type")
    private String questionType;
    @TitanField(name = "ext_subject")
    private String dbsubject;
    @TitanField(name = "ext_suggest_duration")
    private String suggestDuration;
    @TitanField(name = "ext_discrimination")
    private Float discrimination;
    @TitanField(name = "ext_answer")
    private String dbanswer;
    @TitanField(name = "ext_item_content")
    private String dbitemContent;
    @TitanField(name = "ext_criterion")
    private String dbcriterion;
    @TitanField(name = "ext_score")
    private Float score;
    @TitanField(name = "ext_source")
    private String dbsource;
    @TitanField(name = "ext_secrecy")
    private Integer secrecy;
    @TitanField(name = "ext_modified_difficulty")
    private Float modifiedDifficulty;
    @TitanField(name = "ext_ext_difficulty")
    private Float extDifficulty;
    @TitanField(name = "ext_modified_discrimination")
    private Float modifiedDiscrimination;
    @TitanField(name = "ext_used_time")
    private Integer usedTime;
    @TitanField(name = "ext_exposal_date")
    private Date exposalDate;
    @TitanField(name = "ext_auto_remark")
    private Boolean autoRemark;

    public Boolean getAutoRemark() {
        return isAutoRemark;
    }

    public void setAutoRemark(Boolean autoRemark) {
        isAutoRemark = autoRemark;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getDbsubject() {
        return dbsubject;
    }

    public void setDbsubject(String dbsubject) {
        this.dbsubject = dbsubject;
    }

    public String getSuggestDuration() {
        return suggestDuration;
    }

    public void setSuggestDuration(String suggestDuration) {
        this.suggestDuration = suggestDuration;
    }

    public Float getDiscrimination() {
        return discrimination;
    }

    public void setDiscrimination(Float discrimination) {
        this.discrimination = discrimination;
    }

    public String getDbanswer() {
        return dbanswer;
    }

    public void setDbanswer(String dbanswer) {
        this.dbanswer = dbanswer;
    }

    public String getDbitemContent() {
        return dbitemContent;
    }

    public void setDbitemContent(String dbitemContent) {
        this.dbitemContent = dbitemContent;
    }

    public String getDbcriterion() {
        return dbcriterion;
    }

    public void setDbcriterion(String dbcriterion) {
        this.dbcriterion = dbcriterion;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getDbsource() {
        return dbsource;
    }

    public void setDbsource(String dbsource) {
        this.dbsource = dbsource;
    }

    public Integer getSecrecy() {
        return secrecy;
    }

    public void setSecrecy(Integer secrecy) {
        this.secrecy = secrecy;
    }

    public Float getModifiedDifficulty() {
        return modifiedDifficulty;
    }

    public void setModifiedDifficulty(Float modifiedDifficulty) {
        this.modifiedDifficulty = modifiedDifficulty;
    }

    public Float getExtDifficulty() {
        return extDifficulty;
    }

    public void setExtDifficulty(Float extDifficulty) {
        this.extDifficulty = extDifficulty;
    }

    public Float getModifiedDiscrimination() {
        return modifiedDiscrimination;
    }

    public void setModifiedDiscrimination(Float modifiedDiscrimination) {
        this.modifiedDiscrimination = modifiedDiscrimination;
    }

    public Integer getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(Integer usedTime) {
        this.usedTime = usedTime;
    }

    public Date getExposalDate() {
        return exposalDate;
    }

    public void setExposalDate(Date exposalDate) {
        this.exposalDate = exposalDate;
    }
}
