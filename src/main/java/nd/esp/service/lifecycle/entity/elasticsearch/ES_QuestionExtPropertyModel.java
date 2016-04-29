package nd.esp.service.lifecycle.entity.elasticsearch;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperationImpl;

public class ES_QuestionExtPropertyModel {

	private float discrimination;

	private Map<String, String> answer;

	private Map<String, String> itemContent;

	private Map<String, String> criterion;

	private float score;

	private int secrecy;

	private float modifiedDifficulty;

	private float modifiedDiscrimination;

	private int usedTime;

	private Date exposalDate;

	private boolean autoRemark;

	private float extDifficulty;

	public float getDiscrimination() {
		return discrimination;
	}

	public void setDiscrimination(float discrimination) {
		this.discrimination = discrimination;
	}

	public String getAnswer() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper
				.writeValueAsString(this.answer);
	}

	public void setAnswer(Map<String, String> answer) {
		this.answer = answer;
	}

	public String getItemContent() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper
				.writeValueAsString(this.itemContent);
	}

	public void setItemContent(Map<String, String> itemContent) {
		this.itemContent = itemContent;
	}

	public String getCriterion() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper
				.writeValueAsString(this.criterion);
	}

	public void setCriterion(Map<String, String> criterion) {
		this.criterion = criterion;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public int getSecrecy() {
		return secrecy;
	}

	public void setSecrecy(int secrecy) {
		this.secrecy = secrecy;
	}

	public float getModifiedDifficulty() {
		return modifiedDifficulty;
	}

	public void setModifiedDifficulty(float modifiedDifficulty) {
		this.modifiedDifficulty = modifiedDifficulty;
	}

	public float getModifiedDiscrimination() {
		return modifiedDiscrimination;
	}

	public void setModifiedDiscrimination(float modifiedDiscrimination) {
		this.modifiedDiscrimination = modifiedDiscrimination;
	}

	public int getUsedTime() {
		return usedTime;
	}

	public void setUsedTime(int usedTime) {
		this.usedTime = usedTime;
	}

	public Date getExposalDate() {
		return exposalDate;
	}

	public void setExposalDate(Date exposalDate) {
		this.exposalDate = exposalDate;
	}

	public boolean isAutoRemark() {
		return autoRemark;
	}

	public void setAutoRemark(boolean autoRemark) {
		this.autoRemark = autoRemark;
	}

	public float getExtDifficulty() {
		return extDifficulty;
	}

	public void setExtDifficulty(float extDifficulty) {
		this.extDifficulty = extDifficulty;
	}

}
