package nd.esp.service.lifecycle.services.staticdatas;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcConfigModel;

public interface StaticDataService {
	public void updateLastTime(int taskId);
	
	public void updateNowStatus(String name,int value);
	
	public void updateIvcMapNow();
	
	public void updateIvcUserMapNow();
	
	public void updateCPMapNow();

	public List<Map<String, Integer>> queryNowStatus();
	
	public List<String> getStaticDatasName();

	public Long queryLastUpdateTime(int taskId);

	public void setValues(String name, Integer value);
	
	public boolean getValues(String name);

	public void flashIvcConfigMap(Map<String, IvcConfigModel> configMap);
	
	public void flashIvcUserMap(Map<String, String> ivcUserMap);
	
	public Map<String, CategoryPatternModel> getCategoryPatternMap();
}
