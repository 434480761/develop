package nd.esp.service.lifecycle.educommon.support;
/**
 * 资源关系类型枚举类
 * @author xiezy
 * @date 2016年7月5日
 */
public enum RelationType {
	ASSOCIATE("ASSOCIATE"),
	VERSION("VERSION")
	;
	
	/**
	 * 目的是为了适配通用查询接口,除了已定义(除ASSOCIATE外)的关系类型,都当做是ASSOCIATE
	 * @author xiezy
	 * @date 2016年7月5日
	 * @param relationType
	 * @return
	 */
	public static boolean shouldBeAssociate(String relationType){
		if(!relationType.equals(RelationType.VERSION.getName())){
			return true;
		}
		
		return false;
	}
	
	private RelationType(String name) {
		this.name = name;
	}
	
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
