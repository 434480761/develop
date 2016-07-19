package nd.esp.service.lifecycle.controllers.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCoverageRepository;
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

	@Autowired
	private TitanCoverageRepository titanCoverageRepository;

	/**
	 * 导入数据
	 * 
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@RequestMapping(value = "/{resourceType}", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long index(@PathVariable String resourceType) {
		return titanResourceService.importData(resourceType);
	}

	/**
	 * 导入数据
	 * G
	 * @author linsm
	 */
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public void indexAll() {
		titanResourceService.importData(ResourceNdCode.chapters.toString());
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.importData(resourceType);
		}
//
		titanResourceService.createChapterRelation();
		titanResourceService.createKnowledgeRealtion();
		titanResourceService.importAllRelation();
		titanResourceService.importKnowledgeRelation();
	}
	
	
	/**
	 * 导入数据
	 * G
	 * @author linsm
	 */
	@RequestMapping(value = "/sample", method = RequestMethod.GET)
	public void indexSample() {
		titanResourceService.importData(ResourceNdCode.teachingmaterials.toString());
		titanResourceService.importData(ResourceNdCode.chapters.toString());
		titanResourceService.createChapterRelation();
		titanResourceService.importData(ResourceNdCode.assets.toString());
		titanResourceService.importRelation(ResourceNdCode.chapters.toString(),ResourceNdCode.assets.toString());
	}

	@RequestMapping(value = "/all/time", method = RequestMethod.GET)
	public void indexAllTime(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskImport(page, type);
	}

	@RequestMapping(value = "/all/relation", method = RequestMethod.GET)
	public void indexAllRelation() {
		titanResourceService.importAllRelation();
	}


	@RequestMapping(value = "update/{resourceType}", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long updateVertex(@PathVariable String resourceType) {
		return titanResourceService.updateData(resourceType);
	}

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

	@RequestMapping(value = "/{resourceType}/{id}", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long importOneData(@PathVariable String resourceType, @PathVariable String id) {
		titanResourceService.importOneData(resourceType,id);
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

	@RequestMapping(value = "/relation/knowledgerelation", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long updateKnowledgeRelation(){
		return titanResourceService.importKnowledgeRelation();
	}

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
}
