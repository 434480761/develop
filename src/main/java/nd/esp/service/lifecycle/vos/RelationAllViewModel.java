package nd.esp.service.lifecycle.vos;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系viewModel(用于查询关系中，包含部分维度数据详情)
 * 
 * <br>Created 2015年5月7日 下午2:37:31
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class RelationAllViewModel{
	/**
	 * uuid
	 */
	private String identifier;
	/**
	 * 目标viewModel
	 */
	private TargetViewModel target;
	/**
	 * 在同一级别的顺序
	 */
	private float orderNum;
	/**
	 * 是否可用
	 */
	private String enable;
	/**
	 * 分类的编码路径
	 */
	private String patternPath;
	
	private List<RelationAllViewModel> items = new ArrayList<RelationAllViewModel>();
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public TargetViewModel getTarget() {
		return target;
	}
	public void setTarget(TargetViewModel target) {
		this.target = target;
	}
	public float getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(float orderNum) {
		this.orderNum = orderNum;
	}
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	public String getPatternPath() {
		return patternPath;
	}
	public void setPatternPath(String patternPath) {
		this.patternPath = patternPath;
	}
	public List<RelationAllViewModel> getItems() {
		return items;
	}
	public void setItems(List<RelationAllViewModel> levelItems) {
		this.items = levelItems;
	}
	
	

}
