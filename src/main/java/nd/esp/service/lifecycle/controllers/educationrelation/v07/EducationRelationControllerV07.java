package nd.esp.service.lifecycle.controllers.educationrelation.v07;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.services.titan.TitanSearchService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 教育资源关系Controller(V0.6--增加生命周期)
 * 
 * @author caocr
 *
 */
@RestController
@RequestMapping("/v0.7/{res_type}")
public class EducationRelationControllerV07 {
    private final static Logger LOG = LoggerFactory.getLogger(EducationRelationControllerV07.class);
    
    @Autowired

    private TitanSearchService titanSearchService;

    
    /**
     * 关系目标资源检索 
     * <p>Create Time: 2015年5月18日   </p>
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param categories       分类维度数据
     * @param targetType       目标资源类型
     * @param label            资源关系标识
     * @param tags             资源关系标签
     * @param relationType     关系类型
     * @param limit            分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @param recursion        是否根据源资源进行递归查询,举例：通过给定的章节id，递归查询其子章节下的所有知识点信息(新增参数)
     * @param ctType           指定覆盖范围的查询类型，具体选值是Org，Role，User，Time，Space，Group
     * @param ct               指定覆盖范围的查询类型，可以是VIEW，PLAY，SHAREING，REPORTING,COPY，NONE
     * @param cTarget          指定查询覆盖范围目标的具体值
     * @return
     */
    @RequestMapping(value = "/{source_uuid}/targets", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<RelationForQueryViewModel> searchByResType(@PathVariable(value = "res_type") String resType,
                                                                                  @PathVariable(value = "source_uuid") String sourceUuid,
                                                                                  @RequestParam(required = false) String categories,
                                                                                  @RequestParam(value = "target_type") String targetType,
                                                                                  @RequestParam(required = false) String label,
                                                                                  @RequestParam(required = false, value="relation_tags") String tags,
                                                                                  @RequestParam(value = "relation_type") String relationType,
                                                                                  @RequestParam String limit,
                                                                                  @RequestParam(required = false) String reverse,
                                                                                  @RequestParam(required = false) String recursion,
                                                                                  @RequestParam(required = false, value = "ct_type") String ctType,
                                                                                  @RequestParam(required = false) String ct,
                                                                                  @RequestParam(required = false, value = "ct_target") String cTarget) {
        if (StringUtils.isEmpty(targetType)) {

            LOG.error("目标资源类型必须要传值，不能为空");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckTargetTypeIsNull);
        }
        
        //覆盖范围参数处理
        if(StringUtils.isEmpty(ctType)){
            ctType = "*";
        }else{
            if(!CoverageConstant.isCoverageTargetType(ctType,true)){
                
                LOG.error("覆盖范围类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
            }
        }
        if(StringUtils.isEmpty(ct)){
            ct = "*";
        }else{
            if(!CoverageConstant.isCoverageStrategy(ct,true)){
                
                LOG.error("资源操作类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
            }
        }
        if(StringUtils.isEmpty(cTarget)){
            cTarget = "*";
        }
        String coverage = ctType + "/" + cTarget + "/" + ct;
        
        if(coverage.equals("*/*/*")){
            coverage = null;
        }
        
        //反向查询boolean,默认为false
        boolean reverseBoolean = false;
        if("true".equals(reverse)){
            reverseBoolean = true;
        }
        //递归查询boolean,默认为false
        boolean recursionBoolean = false;
        if("true".equals(recursion)){
            recursionBoolean = true;
        }
        
        limit = CommonHelper.checkLimitMaxSize(limit);
        ListViewModel<RelationForQueryViewModel> listViewModel = null;
        if (recursionBoolean) {
            if (!IndexSourceType.ChapterType.getName().equals(resType)) {
                LOG.error("递归查询res_type目前仅支持chapters");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.RelationSupportTypeError);
            }
        }

        try {
            listViewModel = titanSearchService.queryListByResType(
                    resType, sourceUuid, categories, targetType, label, tags, relationType, limit, reverseBoolean,recursionBoolean, coverage);
        } catch (Exception e) {
            LOG.error("titan--通过资源关系获取资源列表失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetEducationRelationListFail.getCode(),e.getMessage());
        }
        
        return listViewModel;
    }
    
    /**
     * 在有些情景下，单个的获取源资源的目标资源列表的接口，业务系统使用起来过于频繁。此时业务方提出需要能够进行设置批量的源资源ID，
     * 通过源资源的ID快速的查询目标资源的列表。
                 1.接口提供设置源资源ID的列表进行批量查询
                 2.接口提供设置关系的类型
                 3.接口提供设置目标资源的类型	
     * <p>Create Time: 2015年10月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType 源资源类型
     * @param sids 源资源id，可批量
     * @param targetType 目标资源类型
     * @param label            资源关系标识
     * @param tags             资源关系标签
     * @param relationType 关系类型
     * @param limit 分页参数
     */
    @RequestMapping(value = "/resources/relations/targets/bulk", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<RelationForQueryViewModel> batchQueryResources(@PathVariable(value="res_type") String resType,
            @RequestParam(value="sid") Set<String> sids,
            @RequestParam(value="target_type") String targetType,
            @RequestParam(required = false) String label,
            @RequestParam(required = false, value="relation_tags") String tags,
            @RequestParam(required=false,value="relation_type") String relationType,
            @RequestParam String limit,
            @RequestParam(required=false) boolean reverse){
        if (StringUtils.isEmpty(targetType)) {

            LOG.error("目标资源类型必须要传值，不能为空");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckTargetTypeIsNull);
        }
        
        limit = CommonHelper.checkLimitMaxSize(limit);
        
        return titanSearchService.batchQueryResources(resType, sids, targetType, label, tags, relationType, limit,reverse);
    }
}
