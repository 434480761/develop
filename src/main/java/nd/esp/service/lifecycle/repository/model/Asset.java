package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

@Entity
@Table(name = "assets")
@NamedQueries({
    @NamedQuery(name="queryAssetsByCategory",query="SELECT e FROM Asset e,ResourceCategory rc WHERE e.primaryCategory = 'assets' AND rc.primaryCategory = 'assets' AND e.enable = true AND e.identifier = rc.resource AND rc.taxoncode = :category ORDER BY e.dbcreateTime desc"),
    @NamedQuery(name="queryInsTypesByCategory",query="SELECT e FROM Asset e,ResourceCategory rc WHERE e.primaryCategory = 'assets' AND rc.primaryCategory = 'assets' AND e.enable = true AND e.identifier = rc.resource AND rc.taxoncode = :category AND e.title like :likeName ORDER BY e.dbcreateTime desc"),
    @NamedQuery(name="queryAssetsBySourceId",query="SELECT t FROM Asset e,Asset t,ResourceRelation rr,ResourceCategory rc WHERE e.primaryCategory = 'assets' AND t.primaryCategory = 'assets' AND rc.primaryCategory = 'assets' AND rr.resType = 'assets' AND rr.resourceTargetType = 'assets' AND e.enable = true AND t.enable = true AND e.identifier = rr.sourceUuid AND t.identifier = rr.target AND t.identifier = rc.resource AND e.identifier = :sourceId AND rc.taxoncode = :category ORDER BY t.dbcreateTime desc")
})
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
