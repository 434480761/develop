package nd.esp.service.lifecycle.services.assets.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.AssetModel;
import nd.esp.service.lifecycle.services.assets.v06.AssetServiceV06;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 业务实现类
 * @author xuzy
 *
 */
@Service("assetServiceV06")
@Transactional
public class AssetServiceImplV06 implements AssetServiceV06 {
	@Autowired
	private NDResourceService ndResourceService;
	
	@Override
	public AssetModel createAsset(AssetModel am) {
		return (AssetModel)ndResourceService.create(ResourceNdCode.assets.toString(), am);
	}

	@Override
	public AssetModel updateAsset(AssetModel am) {
		return (AssetModel)ndResourceService.update(ResourceNdCode.assets.toString(), am);
	}
}
