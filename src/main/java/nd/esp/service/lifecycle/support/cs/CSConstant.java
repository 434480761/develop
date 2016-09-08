package nd.esp.service.lifecycle.support.cs;
/**
 * CS 常量类
 * 
 * ps：目前还未能将所有CS相关常量整理到位，后期陆续完整整合，以便维护
 * @author xiezy
 * @date 2016年9月8日
 */
public class CSConstant {
	
	/**
	 * CS过期天数默认值   0表示永不过期
	 */
	public final static Integer CS_DEFAULT_EXPIRE_DAYS = 0;
	
	/**
	 * CS文件可见性   1-公开  0-私密（默认）
	 */
	public final static Integer CS_SCOPE_PUBLIC = 1;
	public final static Integer CS_SCOPE_PRIVATE = 0;
	
	/**
	 * CS目录容量限制默认值   0表示不限制
	 */
	public final static Integer CS_DEFAULT_CAPACITY = 0;
	
	/**
	 * CS获取目录项排序   默认按时间倒序
	 */
	public final static String CS_ORDERBY_UPDATEAT_ASC = "updateAt asc";
	public final static String CS_ORDERBY_UPDATEAT_DESC = "updateAt desc";
}
