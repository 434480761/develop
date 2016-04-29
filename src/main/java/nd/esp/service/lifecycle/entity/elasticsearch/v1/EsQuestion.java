package nd.esp.service.lifecycle.entity.elasticsearch.v1;

import java.util.Date;

/**
 * 习题扩展属性：共有12个
 * 
 * @author linsm
 *
 */
public class EsQuestion extends EsNdResource {
	// 用于分辨学生的知识水平和素质高低的试题参数，它是衡量试题对不同水平学生的心理特质的区分程度的指标
	private Float discrimination;
	private String dbanswer;
	private String dbitemContent;
	// 评分标准
	private String dbcriterion;
	// 本试题的建议得分，以该题目在百分制试卷中所占的分数。
	private Float score;
	// 本道试题的保密程度。特等机密（4）、高等机密（3）、机密（2）、半机密（1）、可公开（0）
	private Integer secrecy;
	// 经过抽样测试和试题库的实际运行，实测的试题难度，0-1之间的数值
	private Float modifiedDifficulty;
	// 本试题的难度系数，通过0-1之间的小数表达，不同于教育属性中的难度
	private Float extDifficulty;
	// 经过抽样测试和试题库的实际运行，实测的试题区分度
	private Float modifiedDiscrimination;
	// 本道试题的正式使用次数
	private Integer usedTime;
	// 本道试题被公开的时间
	private Date exposalDate;
	// 本道试题是否支持自动批阅
	private Boolean autoRemark;

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

	public Boolean getAutoRemark() {
		return autoRemark;
	}

	public void setAutoRemark(Boolean autoRemark) {
		this.autoRemark = autoRemark;
	}

}
