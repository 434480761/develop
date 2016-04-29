package nd.esp.service.lifecycle.vos;

import java.util.List;

/**
 *  用于查询关系时的viewModel
 * 
 * <br>Created 2015年5月7日 下午2:33:22
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class QueryRelationViewModel {

	/**
	 * 第几个维度（在相应的模式中）
	 */
	private int level;
	/**
	 * 每一个维度中的维度数据（在关系中是顶层）
	 */
	private List<RelationViewModel> items;
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public List<RelationViewModel> getItems() {
		return items;
	}
	public void setItems(List<RelationViewModel> items) {
		this.items = items;
	}

}
