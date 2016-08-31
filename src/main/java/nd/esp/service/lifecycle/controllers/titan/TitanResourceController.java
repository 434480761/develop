package nd.esp.service.lifecycle.controllers.titan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nd.esp.service.lifecycle.services.titan.TitanResourceService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4ImportData;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.titan.TitanCacheData;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于导数据到titan，从mysql 数据库取数据
 *
 * @author linsm
 *
 */
@RestController
@RequestMapping("/titan/index/data")
public class TitanResourceController {
	private static final Logger LOG = LoggerFactory
			.getLogger(TitanResourceController.class);

	@Autowired
	private TitanResourceService titanResourceService;

	/**
	 * 通过脚本导入数据
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/{resourceType}/script", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long import4Script(@PathVariable String resourceType) {
		return titanResourceService.importData4Script(resourceType);
	}

	/**
	 * 导入所有数据
	 * */
	@MarkAspect4ImportData
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

	@MarkAspect4ImportData
	@RequestMapping(value = "/all/relation/ckr", method = RequestMethod.GET)
	public void importAllRealtion(){
		titanResourceService.createChapterRelation();
		titanResourceService.createKnowledgeRealtion();
		titanResourceService.importAllRelation();
		titanResourceService.importKnowledgeRelation();
	}

	/**
	 * 修复数据
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/all/repair/data", method = RequestMethod.GET)
	public void repairAllData() {
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.repairData(resourceType);
		}
	}
	
	/**
	 * 修复数据
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/{resourceType}/repair", method = RequestMethod.GET)
	public void repairData(@PathVariable String resourceType) {
		titanResourceService.repairData(resourceType);
	}

	/**
	 * 修复所有关系
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/all/repair/relation", method = RequestMethod.GET)
	public void repairAllRelation() {

		titanResourceService.repairAllRelation();
	}

	/**
	 * 修复所有的关系和数据
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/all/repair/dataandrelation", method = RequestMethod.GET)
	public void repairAll() {
		titanResourceService.repairData(ResourceNdCode.chapters.toString());
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.repairData(resourceType);
		}
		titanResourceService.repairAllRelation();
	}

	@MarkAspect4ImportData
	@RequestMapping(value = "/{resourceType}/{id}/repair", method = RequestMethod.GET)
	public void repairOne(@PathVariable String resourceType, @PathVariable String id){
		titanResourceService.repairOne(resourceType, id);
	}

	@MarkAspect4ImportData
	@RequestMapping(value = "/all/time/script", method = RequestMethod.GET)
	public void indexAllTime(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskImport(page, type);
	}

	/**
	 * 指定分页的方式修复数据
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/all/time/repair", method = RequestMethod.GET)
	public void indexAllTimeRepair(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskRepair(page, type);
	}

	/**
	 * 导入所有的关系
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/all/relation", method = RequestMethod.GET)
	public void indexAllRelation() {
		titanResourceService.importAllRelation();
	}

	/**
	 * 创建章节和知识点关系
	 * */
	@MarkAspect4ImportData
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

	@MarkAspect4ImportData
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
	@MarkAspect4ImportData
	@RequestMapping(value = "/relation/knowledgerelation", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long updateKnowledgeRelation(){
		return titanResourceService.importKnowledgeRelation();
	}

	/**
	 * 修复检索字段数据
	 * */
	@MarkAspect4ImportData
	@RequestMapping(value = "/all/time/update_source_property", method = RequestMethod.GET)
	public void indexAllTimeUpdate(@RequestParam Integer page , @RequestParam String type) {
		titanResourceService.timeTaskImport4Update(page, type);
	}

	@MarkAspect4ImportData
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
    public String checkAllData(@PathVariable String resourceType, @RequestParam(required = true,value="beginDate") String beginDate, @RequestParam(required = true,value="endDate")String endDate) {
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date begin = sdf.parse(beginDate);
            Date end = sdf.parse(endDate);
            if (begin.after(end)) {
                return "开始时间必须小于结束时间";
            }
            titanResourceService.checkOneResourceTypeData(resourceType, begin, end);
        } catch (ParseException e) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "时间格式错误,格式为:yyyy-MM-dd HH:mm:ss或 yyyy-MM-dd HH:mm:ss.SSS");
        }
        return "执行成功";
    }

	@RequestMapping(value = "/all/check/exist", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public long checkAllData() {
		titanResourceService.checkResource(ResourceNdCode.chapters.toString());
		for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
			titanResourceService.checkResource(resourceType);
		}
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


	@MarkAspect4ImportData
	@RequestMapping(value = "/all/statistical", method = RequestMethod.GET)
	public void importAllStatistical() {
		titanResourceService.importStatistical();
	}
	
	/**
	 * 测试导数据时：一个环境只允许一个任务
	 * 
	 * @author linsm
	 * @return
	 */
	@MarkAspect4ImportData
	@RequestMapping(value = "testImportDataSync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public String testImportDataSync() {
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.info(e.getLocalizedMessage());
			}
			LOG.info("task_running");
		}
		return "task_complete";
	}
	
	/**
	 * 清理Titan缓存数据
	 */
	@RequestMapping(value = "/clearTitanCache", method = RequestMethod.GET)
	public void clearTitanCache() {
		// TitanCacheData.coverage.getCacheMap().put("123", 333L);
		TitanCacheData.clearAllCacheMap();
	}
}
