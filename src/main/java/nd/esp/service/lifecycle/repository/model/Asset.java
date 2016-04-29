package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

@Entity
@Table(name = "assets")
public class Asset extends Education {
	
	/**
	 * 需要转码
	 */
	public static final String CONVERT_UN = "CONVERT_UN";
	/**
	 * 正在转码
	 */
	public static final String CONVERT_ING = "CONVERT_ING";
	/**
	 * 转码成功
	 */
	public static final String CONVERT_ED = "CONVERT_ED";
	/**
	 * 转码失败
	 */
	public static final String CONVERT_ER = "CONVERT_ER";
	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */ 
		
	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.AssetType.getName());
		return IndexSourceType.AssetType;
	}

}
