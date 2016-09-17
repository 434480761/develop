package nd.esp.service.lifecycle.repository.common;



/**
 * 
 * 项目名字:nd edu<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年1月22日<br>
 * 修改人:<br>
 * 修改时间:2015年1月22日<br>
 * 修改备注:<br>
 * @version 0.1<br>
 */
public class Constant {

	final static public String version = "0.1";
	
	final static public String DEF_CHARTSET = "utf-8";
	/**
	 * s3的跟目录
	 */
	final static public String S_ROOT_FOLDER = "root";
	
	final static public String CONTENTTYPE = "text/html; charset=utf-8";
	
	/**
	 * 索引提交
	 */
	public static final String INDEX_POST="INDEX_POST";
	/**
	 * field修改
	 */
	public static final String SCHEMA_POST="SCHEMA_POST";
	/**
	 * 原子更新
	 */
	public static final String AUTO_UPDATE="AUTO_UPDATE";
	/**
	 * 集合重新加载
	 */
	public static final String COLLECTION_RELOAD="COLLECTION_RELOAD";
	
	public static final String DATAIMPORT="dataimport";
	
}
