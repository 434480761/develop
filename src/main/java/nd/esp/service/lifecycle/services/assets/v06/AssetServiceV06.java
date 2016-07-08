package nd.esp.service.lifecycle.services.assets.v06;

import nd.esp.service.lifecycle.models.v06.AssetModel;

/**
 * @author xuzy
 * @version 0.6
 * @created 2015-07-02
 */
public interface AssetServiceV06{
	/**
	 * 素材创建
	 * @param rm
	 * @return
	 */
	public AssetModel createAsset(AssetModel am);
	
	/**
	 * 素材修改
	 * @param rm
	 * @return
	 */
	public AssetModel updateAsset(AssetModel am);

	AssetModel patchAsset(AssetModel am);
}