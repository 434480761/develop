package nd.esp.service.lifecycle.repository.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.solr.client.solrj.beans.Field;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 类描述:bean 创建人: 创建时间:2015-05-13 18:52:3
 * 
 * @version
 */

@Entity
@Table(name = "questions")
public class Question extends Education {

	public static final String PROP_SOURCE = "source";
	public static final String PROP_SUBJECT = "subject";
	public static final String PROP_QUESTIONTYPE = "questiontype";

	/**
	* 
	*/
	@Column(name = "is_auto_remark")
	private Boolean isAutoRemark;
	
	/**
	* 
	*/

	@Column(name = "question_type")
	private String questionType;


	/**
	* 
	*/
	@DataConverter(target="source", type=List.class)
	@Column(name = "source")
	private String dbsource;
	@Transient
	private List<String> source;

	/**
	* 
	*/
	@DataConverter(target="subject", type=List.class)
	@Column(name = "subject")
	private String dbsubject;
	@Transient
	private List<String> subject;

	/**
	* 
	*/
	@Column(name = "suggest_duration")
	private String suggestDuration;

	public void setSuggestDuration(String suggestduration) {
		this.suggestDuration = suggestduration;
	}

	public String getSuggestDuration() {
		return this.suggestDuration;
	}

	/*陈允进*/
    /****************************ext_properties begin***************************/
    // 用于分辨学生的知识水平和素质高低的试题参数，它是衡量试题对不同水平学生的心理特质的区分程度的指标
    @Column(name = "discrimination")
    private Float discrimination;
    // 试题的答案
    @DataConverter(target = "answer", type = Map.class)
    @Column(name = "answer")
    private String dbanswer;
    @Transient
    Map<String, String> answer;
    // 试题的具体内容
    @DataConverter(target = "itemContent", type = Map.class)
    @Column(name = "item_content")
    private String dbitemContent;
    @Transient
    Map<String, String> itemContent;
    // 评分标准
    @DataConverter(target = "criterion", type = Map.class)
    @Column(name = "criterion")
    private String dbcriterion;
    @Transient
    Map<String, String> criterion;
    // 本试题的建议得分，以该题目在百分制试卷中所占的分数。
    @Column(name = "score")
    private Float score;
    // 本道试题的保密程度。特等机密（4）、高等机密（3）、机密（2）、半机密（1）、可公开（0）
    @Column(name = "secrecy")
    private Integer secrecy;
    // 经过抽样测试和试题库的实际运行，实测的试题难度，0-1之间的数值
    @Column(name = "modified_difficulty")
    private Float modifiedDifficulty;
    // 本试题的难度系数，通过0-1之间的小数表达，不同于教育属性中的难度
    @Column(name = "ext_difficulty")
    private Float extDifficulty;
    // 经过抽样测试和试题库的实际运行，实测的试题区分度
    @Column(name = "modified_discrimination")
    private Float modifiedDiscrimination;
    // 本道试题的正式使用次数
    @Column(name = "used_time")
    private Integer usedTime;
    // 本道试题被公开的时间
    @Column(name = "exposal_date")
    private Date exposalDate;
    // 本道试题是否支持自动批阅
    @Column(name = "auto_remark")
    private Boolean autoRemark;
    /****************************ext_properties end***************************/

	
    
    
    
	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.QuestionType.getName());
		return IndexSourceType.QuestionType;
	}

	public String getDbitemContent() {
        return dbitemContent;
    }

    public void setDbitemContent(String dbitemContent) {
        this.dbitemContent = dbitemContent;
    }

    public Map<String, String> getItemContent() {
        return itemContent;
    }

    public void setItemContent(Map<String, String> itemContent) {
        this.itemContent = itemContent;
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

    public Boolean getAutoRemark() {
        return autoRemark;
    }

    public void setAutoRemark(Boolean autoRemark) {
        this.autoRemark = autoRemark;
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

    public Map<String, String> getAnswer() {
        return answer;
    }

    public void setAnswer(Map<String, String> answer) {
        this.answer = answer;
    }


    public String getDbcriterion() {
        return dbcriterion;
    }

    public void setDbcriterion(String dbcriterion) {
        this.dbcriterion = dbcriterion;
    }

    public Map<String, String> getCriterion() {
        return criterion;
    }

    public void setCriterion(Map<String, String> criterion) {
        this.criterion = criterion;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Integer getSecrecy() {
        return secrecy;
    }

    public void setSecrecy(Integer secrecy) {
        this.secrecy = secrecy;
    }

    public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questiontype) {
		this.questionType = questiontype;
	}

	public String getDbsource() {
		return dbsource;
	}

	public void setDbsource(String dbsource) {
		this.dbsource = dbsource;
	}

	public List<String> getSource() {
		return source;
	}

	public void setSource(List<String> source) {
		this.source = source;
	}

	public String getDbsubject() {
		return dbsubject;
	}

	public void setDbsubject(String dbsubject) {
		this.dbsubject = dbsubject;
	}

	public List<String> getSubject() {
		return subject;
	}

	public void setSubject(List<String> subject) {
		this.subject = subject;
	}

	public Boolean getIsAutoRemark() {
		return isAutoRemark;
	}

	public void setIsAutoRemark(Boolean isAutoRemark) {
		this.isAutoRemark = isAutoRemark;
	}

}