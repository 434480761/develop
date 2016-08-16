package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 资源版权方 仓储Model
 * @author xiezy
 * @date 2016年8月15日
 */
@Entity
@Table(name="copyright_owners")
public class CopyrightOwner extends EspEntity {

	private static final long serialVersionUID = 8450966832068601665L;

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}