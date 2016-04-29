package nd.esp.service.lifecycle.repository.common;

/**
 * 
 */


/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月28日<br>
 * 修改人:<br>
 * 修改时间:2015年2月28日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */

//@Configuration
//@PropertySource("classpath:/esp_store_cfg.properties")
public class StoreCfgFromSpring {
	//@Value("#{properties['cassandra_addr']}")
	private String cassandraAddr;

	public String getCassandraAddr() {
		return cassandraAddr;
	}

	public void setCassandraAddr(String cassandraAddr) {
		this.cassandraAddr = cassandraAddr;
	}
	
	
}
