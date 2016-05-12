package nd.esp.service.lifecycle.controllers.elasticsearch;

import nd.esp.service.lifecycle.services.elasticsearch.IndexConfigureService;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 索引配置
 * 
 * @author linsm
 *
 */
@RestController
@RequestMapping("/elasticsearch/index/configure")
public class IndexConfigureController {

	@Autowired
	private IndexConfigureService indexConfigureService;

	/**
	 * 创建索引(同时创建mapping)
	 * 
	 * @author linsm
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	Boolean createIndex() {
		return indexConfigureService.createIndex() && putMapping();

	}

	/**
	 * 删除索引
	 * 
	 * @author linsm
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	Boolean deleteIndex() {
		return indexConfigureService.deleteIndex();
	}

	/**
	 * 添加或者更新mapping
	 * 
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 * @return
	 */
	@RequestMapping(value = "/mapping/{primaryCategory}", method = RequestMethod.GET)
	Boolean putMapping(
			@PathVariable(value = "primaryCategory") String primaryCategory) {
		ResourceTypeSupport.checkType(primaryCategory);
		return indexConfigureService.putMapping(primaryCategory);
	}

	/**
	 * 添加或者更新mapping
	 * 
	 * @author linsm
	 * @return
	 */
	@RequestMapping(value = "/mapping/all", method = RequestMethod.GET)
	Boolean putMapping() {
		for (String resourceType : ResourceTypeSupport
				.getAllValidEsResourceTypeList()) {
			indexConfigureService.putMapping(resourceType);
		}
		return true;
	}

}
