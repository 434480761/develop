package nd.esp.service.lifecycle.repository.common;


/**
 * @title 知识点关系类型
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月1日 下午8:42:07
 */
public enum KnowledgeRelationType {
	/**
	 * 关联关系
	 */
	ASSOCIATE("ASSOCIATE",1),
	/**
	 * 前置关系
	 */
	BEFORE("BEFORE",2);
	
	
	private String message;

	public String getMessage() {
		return message;
	}

	public int getValue() {
		return value;
	}

	private int value;

	KnowledgeRelationType(String message, int value) {
		this.message = message;
		this.value = value;
	}
}