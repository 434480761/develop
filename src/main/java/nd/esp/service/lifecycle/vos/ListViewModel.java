package nd.esp.service.lifecycle.vos;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 定义查询列表对象
 * 
 * @author johnny
 *
 */
public class ListViewModel<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	@JsonInclude(Include.NON_NULL)
	private String limit;
	private Long total;
	private List<T> items;

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

}
