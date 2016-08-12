package nd.esp.service.lifecycle.controllers.titan;

import nd.esp.service.lifecycle.services.titan.TitanResourceService;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 用于导数据到titan，从mysql 数据库取数据
 *
 * @author linsm
 *
 */
@RestController
@RequestMapping("/titan/index/data")
public class TitanResourceController {
	// private static final Logger LOG = LoggerFactory
	// .getLogger(TitanResourceController.class);

	@Autowired
	private TitanResourceService titanResourceService;

	/**
	 * 通过脚本导入数据
	 * */
	@RequestMapping(value = "/{resourceType}/script", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long import4Script(@PathVariable String resourceType) {
		return titanResourceService.importData4Script(resourceType);
	}

	/**
	 * 导入所有数据
	 * */
	@RequestMapping(value = "/all/script", method = RequestMethod.GET)
	public void indexAllScript() {
		titanResourceService.importData4Script(ResourceNdCode.chapters.toString());
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.importData4Script(resourceType);
		}
//
		titanResourceService.createChapterRelation();
		titanResourceService.createKnowledgeRealtion();
		titanResourceService.importAllRelation();
		titanResourceService.importKnowledgeRelation();
	}

	/**
	 * 修复数据
	 * */
	@RequestMapping(value = "/all/repair/data", method = RequestMethod.GET)
	public void repairAllData() {
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.repairData(resourceType);
		}
	}

	/**
	 * 修复所有关系
	 * */
	@RequestMapping(value = "/all/repair/relation", method = RequestMethod.GET)
	public void repairAllRelation() {

		titanResourceService.repairAllRelation();
	}

	/**
	 * 修复所有的关系和数据
	 * */
	@RequestMapping(value = "/all/repair/dataandrelation", method = RequestMethod.GET)
	public void repairAll() {
		titanResourceService.repairData(ResourceNdCode.chapters.toString());
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.repairData(resourceType);
		}
		titanResourceService.repairAllRelation();
	}

	@RequestMapping(value = "/{resourceType}/{id}/repair", method = RequestMethod.GET)
	public void repairOne(@PathVariable String resourceType, @PathVariable String id){
		titanResourceService.repairOne(resourceType, id);
	}

	@RequestMapping(value = "/all/time/script", method = RequestMethod.GET)
	public void indexAllTime(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskImport(page, type);
	}

	/**
	 * 指定分页的方式修复数据
	 * */
	@RequestMapping(value = "/all/time/repair", method = RequestMethod.GET)
	public void indexAllTimeRepair(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskRepair(page, type);
	}

	/**
	 * 导入所有的关系
	 * */
	@RequestMapping(value = "/all/relation", method = RequestMethod.GET)
	public void indexAllRelation() {
		titanResourceService.importAllRelation();
	}

	/**
	 * 创建章节和知识点关系
	 * */
	@RequestMapping(value = "/relation/{resourceType}", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long chapterCreateRelation(@PathVariable String resourceType) {
		if("chapters".equals(resourceType)){
			return titanResourceService.createChapterRelation();
		}
		if("knowledges".equals(resourceType)){
			return  titanResourceService.createKnowledgeRealtion();
		}

		return 0;
	}

	@RequestMapping(value = "/relation/{resourceType}/update", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public void updateChapterRelation(@PathVariable String resourceType){
		if("chapters".equals(resourceType)){
			titanResourceService.updateChapterRelation();
		}
		if("knowledges".equals(resourceType)){
			titanResourceService.updateKnowledgeRelation();
		}

		return ;
	}

	/**
	 * 创建知识点关系
	 * */
	@RequestMapping(value = "/relation/knowledgerelation", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long updateKnowledgeRelation(){
		return titanResourceService.importKnowledgeRelation();
	}

	/**
	 * 修复检索字段数据
	 * */
	@RequestMapping(value = "/all/time/update_source_property", method = RequestMethod.GET)
	public void indexAllTimeUpdate(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskImport4Update(page, type);
	}

	@RequestMapping(value = "/{resourceType}/{id}/script", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long importOneData4Script(@PathVariable String resourceType, @PathVariable String id) {
		titanResourceService.importOneData4Script(resourceType,id);
		return 0;
	}

	/**
	 * 检查资源是否存在
	 * */
	@RequestMapping(value = "/{resourceType}/check/exist", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long checkData(@PathVariable String resourceType) {
		titanResourceService.checkResource(resourceType);
		return 0;
	}

	/**
	 * 检查一条数据
	 * */
	@RequestMapping(value = "/{resourceType}/{id}/check", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long checkOneData(@PathVariable String resourceType, @PathVariable String id) {
		titanResourceService.checkOneData(resourceType, id);
		return 0;
	}
	@RequestMapping(value = "/{resourceType}/check", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long checkAllData(@PathVariable String resourceType) {
		titanResourceService.checkAllData(resourceType);
		return 0;
	}
	@RequestMapping(value = "importStatus", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public String importStatus(){
		return titanResourceService.importStatus();
	}

	@RequestMapping(value = "code", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public String importCode(){
	   	titanResourceService.code();
		return null;
	}
}
