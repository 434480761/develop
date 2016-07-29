package nd.esp.service.lifecycle.controllers.titan;

import nd.esp.service.lifecycle.services.titan.TitanResourceServiceNew;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by liuran on 2016/7/28.
 */
@RestController
@RequestMapping("/titan/index/data/new")
public class TitanResourceNewController {
    @Autowired
    private TitanResourceServiceNew titanResourceService ;

    @RequestMapping(value = "/{resourceType}/{id}/script", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public void importOneData4Script(@PathVariable String resourceType, @PathVariable String id) {
        titanResourceService.importOneData4Script(resourceType,id);
    }

    /**
     * 导入一类资源
     * */
    @RequestMapping(value = "/{resourceType}/script", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public void importData(@PathVariable String resourceType) {
        titanResourceService.importData4Script(resourceType);
    }

    /**
     * 导入所有的资源
     * */
    @RequestMapping(value = "/all/resource/script", method = RequestMethod.GET)
    public void importAllResource() {
        titanResourceService.importData4Script(ResourceNdCode.chapters.toString());
        for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
            titanResourceService.importData4Script(resourceType);
        }
    }

    /**
     * 导入所有的资源和关系
     * */
    @RequestMapping(value = "/all/resourceAndRelation/script", method = RequestMethod.GET)
    public void importAllResourceAndRelation() {
        titanResourceService.importData4Script(ResourceNdCode.chapters.toString());
        for (String resourceType : ResourceTypeSupport.getAllValidEsResourceTypeList()) {
            titanResourceService.importData4Script(resourceType);
        }
//
        titanResourceService.createChapterRelation();
        titanResourceService.createKnowledgeRelation();
        titanResourceService.importAllRelation();
        titanResourceService.importKnowledgeRelation();
    }
    /**
     * 导入所有的关系
     * */
    @RequestMapping(value = "/all/relation", method = RequestMethod.GET)
    public void indexAllRelation() {
        titanResourceService.importAllRelation();
    }


    @RequestMapping(value = "/relation/{resourceType}", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public void chapterCreateRelation(@PathVariable String resourceType) {
        if("chapters".equals(resourceType)){
            titanResourceService.createChapterRelation();
        }
        if("knowledges".equals(resourceType)){
            titanResourceService.createKnowledgeRelation();
        }

    }

    @RequestMapping(value = "/{resourceType}/check/exist", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public void checkData(@PathVariable String resourceType) {
        titanResourceService.checkResource(resourceType);
    }

    @RequestMapping(value = "/{resourceType}/{id}/check", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public void checkOneData(@PathVariable String resourceType, @PathVariable String id) {
        titanResourceService.checkOneData(resourceType, id);
    }
    @RequestMapping(value = "/{resourceType}/check", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public void checkAllData(@PathVariable String resourceType) {
        titanResourceService.checkAllData(resourceType);
    }
    @RequestMapping(value = "importStatus", method = RequestMethod.GET,
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public String importStatus(){
        return titanResourceService.importStatus();
    }

}
