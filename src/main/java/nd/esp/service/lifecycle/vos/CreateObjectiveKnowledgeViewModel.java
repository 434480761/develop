package nd.esp.service.lifecycle.vos;

import java.util.List;

/**
 * 教学目标和知识点关联-输入和输出
 * 
 * <br>Created 2015年4月1日 下午10:26:59
 * @version  
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class CreateObjectiveKnowledgeViewModel {
	private String objectives;//教学目标id
	private String knowledges;//知识点id
	private List<String> outline; //多个章节id
	
	public String getObjectives() {
		return objectives;
	}
	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}
	public String getKnowledges() {
		return knowledges;
	}
	public void setKnowledges(String knowledges) {
		this.knowledges = knowledges;
	}
	public List<String> getOutline() {
		return outline;
	}
	public void setOutline(List<String> outline) {
		this.outline = outline;
	}
	
}
