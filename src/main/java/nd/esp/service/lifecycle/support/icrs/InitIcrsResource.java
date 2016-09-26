package nd.esp.service.lifecycle.support.icrs;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
/**
 * 智慧教室-课堂数据统计平台  Controller层
 * @author xiezy
 * @date 2016年9月12日
 */
@RestController
@RequestMapping("/icrs")
public class InitIcrsResource {
	@Autowired
	private IcrsServiceHelper icrsServiceHelper;
	
	/**
	 * 初始化 icrs_resource
	 * @author xiezy
	 * @date 2016年9月12日
	 */
	@RequestMapping(value = "/init", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void init(){
		icrsServiceHelper.syncIcrsByType(IndexSourceType.AssetType.getName(), true);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.SourceCourseWareType.getName(), true);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.SourceCourseWareObjectType.getName(), true);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.QuestionType.getName(), true);
	}
	
	/**
	 * 实时刷新 icrs_resource
	 * @author xiezy
	 * @date 2016年9月12日
	 */
	@RequestMapping(value = "/fresh", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void realTimeFresh(){
		icrsServiceHelper.syncIcrsByType(IndexSourceType.AssetType.getName(), false);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.SourceCourseWareType.getName(), false);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.SourceCourseWareObjectType.getName(), false);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.QuestionType.getName(), false);
	}
}
