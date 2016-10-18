package nd.esp.service.lifecycle.educommon.models;

import java.util.Map;


/**
 * redis对应的model
 * @author xuzy
 *
 */
public class RedisModel {
	/**
	 * redis对应的key
	 */
	private String indexKey;
	
	/**
	 * 是否马上刷新redis缓存值
	 * 如果为true立即刷新，否则按默认策略更新
	 */
	private boolean refreshFlag;
	
	/**
	 * 分页参数，用于获取列表，例如0,20
	 */
	private String limit;
	
	/**
	 * 用于传参
	 */
	private Map<String,Object> params;

	public String getIndexKey() {
		return indexKey;
	}

	public void setIndexKey(String indexKey) {
		this.indexKey = indexKey;
	}

	public boolean isRefreshFlag() {
		return refreshFlag;
	}

	public void setRefreshFlag(boolean refreshFlag) {
		this.refreshFlag = refreshFlag;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
}
