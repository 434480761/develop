package nd.esp.service.lifecycle.repository.common;

/**
 * @title 知识点应用上下文类型
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月1日 下午8:42:07
 */
public enum KnowledgeContextType {

	
	MATERIAL("教材", 1),
	PERSON("用于", 2),
	SUBJECT("学科", 3),
	MATERIAL_CHAPTER("教材章节", 5);

	private String message;

	public String getMessage() {
		return message;
	}

	public int getValue() {
		return value;
	}

	private int value;

	KnowledgeContextType(String message, int value) {
		this.message = message;
		this.value = value;
	}

}
