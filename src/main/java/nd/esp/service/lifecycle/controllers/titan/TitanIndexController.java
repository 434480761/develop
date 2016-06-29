package nd.esp.service.lifecycle.controllers.titan;

import nd.esp.service.lifecycle.services.titan.TitanIndexService;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/titan/index/configure")
public class TitanIndexController {

	@Autowired
	private TitanIndexService titanIndexService;

	/**
	 * 创建索引
	 * 
	 * @author linsm
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	Boolean createIndex() {
		return titanIndexService.createScehma();

	}


}
