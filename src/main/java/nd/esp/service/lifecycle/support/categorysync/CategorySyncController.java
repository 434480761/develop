package nd.esp.service.lifecycle.support.categorysync;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/category/sync")
public class CategorySyncController {
	
	@Autowired
	private CategorySyncServiceHelper categorySyncServiceHelper;
	
	/**
	 * 初始化,处理旧数据
	 * @author xiezy
	 * @date 2016年10月20日
	 * @return
	 */
//	@RequestMapping(value = "/init", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Integer> init(){
		return categorySyncServiceHelper.initSync();
	}
	
	/**
	 * 实时同步维度数据
	 * @author xiezy
	 * @date 2016年10月19日
	 */
//	@RequestMapping(value = "/fresh", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void realTimeFresh(){
		categorySyncServiceHelper.syncCategory();
	}
}
