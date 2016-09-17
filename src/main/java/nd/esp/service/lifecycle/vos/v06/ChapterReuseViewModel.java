package nd.esp.service.lifecycle.vos.v06;

import java.util.List;
import java.util.Map;

/**
 * 章节关系复用viewModel
 * @author xuzy
 *
 */
public class ChapterReuseViewModel {
	private List<String> resTypes;
	private Map<String,List<String>> datas;
	public List<String> getResTypes() {
		return resTypes;
	}
	public void setResTypes(List<String> resTypes) {
		this.resTypes = resTypes;
	}
	public Map<String, List<String>> getDatas() {
		return datas;
	}
	public void setDatas(Map<String, List<String>> datas) {
		this.datas = datas;
	}
	
}
