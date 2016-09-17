package nd.esp.service.lifecycle.educommon.vos;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 创建新版本viewModel
 * @author xuzy
 *
 */
public class VersionViewModel {
	/**
	 * 生命周期
	 */
	@Valid
	@JsonInclude(Include.NON_NULL)
	private ResLifeCycleViewModel lifeCycle;
	
	private Map<String,List<String>> relations;

	public ResLifeCycleViewModel getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(ResLifeCycleViewModel lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public Map<String, List<String>> getRelations() {
		return relations;
	}

	public void setRelations(Map<String, List<String>> relations) {
		this.relations = relations;
	}
}
