package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 资源提供商 仓储Model
 * @author xiezy
 * @date 2016年8月15日
 */
@Entity
@Table(name="resource_providers")
public class ResourceProvider extends EspEntity {

	private static final long serialVersionUID = 2848781430853852987L;

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}