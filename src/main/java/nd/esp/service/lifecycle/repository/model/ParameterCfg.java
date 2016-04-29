/**
 * 
 */
package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Lob;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:参数配置<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年5月20日<br>
 * 修改人:<br>
 * 修改时间:2015年5月20日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */
public class ParameterCfg {
	/**
	 * 对应应用参数，如果自己用app=system,
	 * 调度 app=sch
	 * 生命周期 app=lifecycle
	 */
	private String app;
	/**
	 * 应用对应具体的哪个配置
	 */
	private String modu;
	/**
	 * 配置值
	 */
	@Column(name="value")
	@Lob
	private String value;
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	public String getModu() {
		return modu;
	}
	public void setModu(String modu) {
		this.modu = modu;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
