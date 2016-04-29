package nd.esp.service.lifecycle.repository.common;


/**
 * @title 知识点标签
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月1日 下午8:42:07
 */
public enum KnowledgeTag {
	/**
	 * 难点
	 */
	DIFFICULTY_POINT("难点",1),
	KEY_POINT("重点",2),
	EXAMINATION_POINT("考点",3);
	
	
	private String message;

	public String getMessage() {
		return message;
	}

	public int getValue() {
		return value;
	}

	private int value;

	KnowledgeTag(String message, int value) {
		this.message = message;
		this.value = value;
	}
}