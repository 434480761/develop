package nd.esp.service.lifecycle.models;

import java.io.Serializable;

public class QueryResultModel implements Serializable{
	private static final long serialVersionUID = 1L;

	private Integer count;
	
	private Long lastUpdate;

	public Integer getCount() {
		return count;
	}


	public void setCount(Integer count) {
		this.count = count;
	}

	public Long getLastUpdate() {
		return lastUpdate;
	}


	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
