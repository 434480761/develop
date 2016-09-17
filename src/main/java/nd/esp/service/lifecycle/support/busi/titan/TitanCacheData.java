package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理titan缓存id
 * 
 * @author linsm
 *
 */
public enum TitanCacheData {
	coverage, taxoncode, ;

	private static final Logger LOG = LoggerFactory
			.getLogger(TitanCacheData.class);
	private final Map<String, Long> cacheMap;

	private TitanCacheData() {
		cacheMap = new HashMap<String, Long>();
	}

	/**
	 * 获取缓存数据（可改）
	 * 
	 * @return
	 */
	public Map<String, Long> getCacheMap() {
		return cacheMap;
	}

	private void clearCacheMap() {
		this.cacheMap.clear();
	}

	public static void clearAllCacheMap() {
		for (TitanCacheData cacheDataType : TitanCacheData.values()) {
			LOG.info("cacheDataType: {}, cache size: {} ", cacheDataType,
					cacheDataType.getCacheMap().size());
			cacheDataType.clearCacheMap();
		}
	}
}
