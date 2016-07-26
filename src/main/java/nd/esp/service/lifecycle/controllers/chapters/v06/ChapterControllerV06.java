package nd.esp.service.lifecycle.controllers.chapters.v06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import nd.esp.service.lifecycle.models.chapter.v06.ChapterModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.notify.models.NotifyInstructionalobjectivesRelationModel;
import nd.esp.service.lifecycle.services.tags.ResourceTagService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterService;
import nd.esp.service.lifecycle.services.titan.TitanTreeMoveService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeType;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterConstant;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel4Move;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 章节控制层
 * <p>Create Time: 2015年11月24日           </p>
 * @author xiezy
 */
@RestController
@RequestMapping("/v0.6/{res_type}")
public class ChapterControllerV06 {
    @Autowired
    @Qualifier("chapterServiceV06")
    private ChapterService chapterService;
    
    @Autowired
    private NotifyInstructionalobjectivesService notifyService;
    
    @Autowired
    private ResourceTagService resourceTagService;
    
    @Autowired
    private TitanTreeMoveService titanTreeMoveService;
    /**
     * 创建章节 
     * <p>Create Time: 2015年8月3日   </p>
     * <p>Create author: xiezy   </p>
     * @param chapterViewModel
     * @param validResult
     * @param mid
     * @return
     */
    @RequestMapping(value="/{mid}/chapters",method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ChapterViewModel createChapters(@Valid @RequestBody ChapterViewModel chapterViewModel,BindingResult validResult,
                                           @PathVariable String mid,@PathVariable("res_type") String resourceType){
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        validMidNotEmptyForUserspace(resourceType,mid);
        //入参合法性校验
        ValidResultHelper.valid(validResult, "LC/CREATE_CHAPTER_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "createChapters");
        
        
        //direction入参校验和设置默认值
        if(StringUtils.isEmpty(chapterViewModel.getDirection())){//如果direction为null或者""
            //设置默认值,next
            chapterViewModel.setDirection(ChapterConstant.DIR_NEXT);
        }else if(!ChapterConstant.isDirection(chapterViewModel.getDirection())){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.DirectionParamError.getCode(),
                    "direction的值目前只支持-pre和next");
        }
        
        if (StringUtils.isEmpty(chapterViewModel.getTarget())) {//当target为空的时候
            // 设置默认值,next
            chapterViewModel.setDirection(ChapterConstant.DIR_NEXT);
        }
        
        //转换为ChapterModel
        ChapterModel chapterModel = BeanMapperUtils.beanMapper(chapterViewModel, ChapterModel.class);
        chapterModel = chapterService.createChapter(resourceType,mid, chapterModel);
        
        //titan
        TitanTreeModel titanTreeModel = new TitanTreeModel();

        titanTreeModel.setTreeType(TitanTreeType.chapters);
        titanTreeModel.setTreeDirection(TreeDirection.fromString(chapterViewModel.getDirection()));
        titanTreeModel.setParent(chapterViewModel.getParent());
        titanTreeModel.setTarget(chapterViewModel.getTarget());
        titanTreeModel.setRoot(mid);
        titanTreeModel.setSource(chapterModel.getIdentifier());

        titanTreeMoveService.addNode(titanTreeModel);
        
        return BeanMapperUtils.beanMapper(chapterModel,ChapterViewModel.class);
    }
    
    /**
     * @author linsm
     * @param resourceType
     * @param mid
     * @since 
     */
    private void validMidNotEmptyForUserspace(String resourceType, String mid) {
        if (ChapterConstant.ChapterSupportResourceTypes.userspace.toString().equals(resourceType)) {
            if (StringUtils.isEmpty(mid)) {
                throw new LifeCircleException("LC/CHECK_PARAM_VALID_FAIL","userId can't be empty");
            }
        }

    }

    /**
     * 获取章节详细
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param cid
     * @return
     */
    @RequestMapping(value = "/{mid}/chapters/{cid}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ChapterViewModel getChapterDetail(@PathVariable String cid,@PathVariable("res_type") String resourceType){
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        ChapterModel chapterModel = chapterService.getChapterDetail(cid);
        
        if(chapterModel != null){
            return BeanMapperUtils.beanMapper(chapterModel,ChapterViewModel.class);
        }
        return null;
    }
    
    /**
     * 修改章节 
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param chapterViewModel
     * @param validResult
     * @param mid
     * @param cid
     * @return
     */
    @RequestMapping(value = "/{mid}/chapters/{cid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ChapterViewModel updateChapter(@Valid @RequestBody ChapterViewModel chapterViewModel,BindingResult validResult,
                                          @PathVariable String mid,@PathVariable String cid,@PathVariable("res_type") String resourceType){
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        validMidNotEmptyForUserspace(resourceType,mid);
        //入参合法性校验
        ValidResultHelper.valid(validResult, "LC/UPDATE_CHAPTER_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "updateChapter");
        
        //转换为ChapterModel
        ChapterModel chapterModel = BeanMapperUtils.beanMapper(chapterViewModel, ChapterModel.class);
        chapterModel = chapterService.updateChapter(resourceType,mid, cid, chapterModel);
        
        return BeanMapperUtils.beanMapper(chapterModel,ChapterViewModel.class);
    }
    
    /**
     * 批量获取教材章节元数据
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param cidList
     * @return
     */
    @RequestMapping(value="/{mid}/chapters/bulk",method=RequestMethod.GET,produces={MediaType.APPLICATION_JSON_VALUE})
    public Map<String,ChapterViewModel> batchGetChapterList(@RequestParam(value="cid") List<String> cidList,@PathVariable("res_type") String resourceType){
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        return chapterService.batchGetChapterList(cidList);
    }
    
    /**
     * 查询章节子节点  
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     *         如果为none或者0返回当前教材下的所有章节信息;
     *         如果为root，返回当前教材下的顶级节点;
     *         如果为chapterid(具体的章节uuid)，返回本节点下的子章节信息(只有一层)。
     * @return
     */
    @RequestMapping(value = "/{mid}/chapters/{cid}/subitems", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ListViewModel<ChapterViewModel> queryChapterList(@PathVariable String mid,@PathVariable String cid,@PathVariable("res_type") String resourceType){
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        validMidNotEmptyForUserspace(resourceType,mid);
        String pattern = ChapterConstant.PATTERN_ALL;
        if("root".equals(cid)){
            cid = mid;
        }
        
        if(!cid.equals("none") && !cid.equals("0")){//查询下一级的章节
            pattern = ChapterConstant.PATTERN_CHILDREN;
        }
        
        return chapterService.queryChapterList(resourceType,mid, cid, pattern);
    }
    
    /**
     * 删除章节 
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     * @param isRealDelete      是否真删
     * @return
     */
    @RequestMapping(value = "/{mid}/chapters/{cid}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteChapter(@PathVariable String mid, @PathVariable String cid,
                                                           @PathVariable("res_type") String resourceType,
                                                           @RequestParam(value = "is_real", defaultValue = "false") boolean isRealDelete) {
    	//add by xiezy - 2016.04.15
        List<NotifyInstructionalobjectivesRelationModel> relateRelations = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
    	if(!isRealDelete && resourceType.equals(IndexSourceType.ChapterType.getName())){
    		relateRelations = notifyService.resourceBelongToRelations4LessonOrChapter(resourceType,cid);
        }
    	
    	ChapterConstant.validChapterSupportResourceTypes(resourceType);
        validMidNotEmptyForUserspace(resourceType,mid);
        boolean flag = true;
        if(!isRealDelete){
            flag = chapterService.deleteChapterNotReally(resourceType, mid, cid);
        }else{
            flag = chapterService.deleteChapter(resourceType, mid, cid); 
        }
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        if(!isRealDelete && resourceType.equals(IndexSourceType.ChapterType.getName())){
        	notifyService.asynNotify4LessonOrChapter(resourceType, cid, relateRelations, OperationType.DELETE);
        }
        
        if (!flag) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.DeleteChapterFail);
        }
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteChapterSuccess);
    }
    
    /**
     * 章节节点的移动  
     * <p>Create Time: 2015年8月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     */
    @RequestMapping(value = "/{mid}/chapters/{cid}/actions/move", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public void moveChapter(@Valid @RequestBody ChapterViewModel4Move chapterViewModel4Move,BindingResult validResult,
            @PathVariable String mid,@PathVariable String cid,@PathVariable("res_type") String resourceType){
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        validMidNotEmptyForUserspace(resourceType,mid);
        //入参合法性校验
        ValidResultHelper.valid(validResult, "LC/MOVE_CHAPTER_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "moveChapter");
    
        //direction入参校验和设置默认值
        if(StringUtils.isEmpty(chapterViewModel4Move.getDirection())){//如果direction为null或者""
            //设置默认值,next
            chapterViewModel4Move.setDirection(ChapterConstant.DIR_NEXT);
        }else if(!ChapterConstant.isDirection(chapterViewModel4Move.getDirection())){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.DirectionParamError.getCode(),
                    "direction的值目前只支持-pre和next");
        }
        
        //转换为ChapterModel
        ChapterModel chapterModel = BeanMapperUtils.beanMapper(chapterViewModel4Move, ChapterModel.class);
        chapterService.moveChapter(resourceType,mid, cid, chapterModel);
        
        //titan
        TitanTreeModel titanTreeModel = new TitanTreeModel();
        titanTreeModel.setTreeType(TitanTreeType.chapters);
        titanTreeModel.setTreeDirection(TreeDirection.fromString(chapterViewModel4Move.getDirection()));
        titanTreeModel.setParent(chapterViewModel4Move.getParent());
        titanTreeModel.setTarget(chapterViewModel4Move.getTarget());
        titanTreeModel.setRoot(mid);
        titanTreeModel.setSource(chapterModel.getIdentifier());

        titanTreeMoveService.moveNode(titanTreeModel);
    }
    
    /**
     * 批量获取教材下的章节数量和资源数量(课时)    
     * <p>Create Time: 2015年11月3日   </p>
     * <p>Create author: xiezy   </p>
     * @param mtidList         批量教材的id
     * @param targetType       目标资源的资源类型。统计查询的目标对象的内容
     * @return
     */
    @RequestMapping(value="/chapters/statistics",method=RequestMethod.GET,produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody Map<String,List<Map<String,Object>>> countResourceByMaterials(
            @RequestParam(value="mtid") Set<String> mtidList,
            @RequestParam(value="target_type") String targetType,@PathVariable("res_type") String resourceType){
        //FIXME (暂时不支持 userSpace)
        ChapterConstant.validChapterSupportResourceTypes(resourceType);
        if(ChapterConstant.ChapterSupportResourceTypes.userspace.toString().equals(resourceType)){
            //404
            throw new LifeCircleException(HttpStatus.NOT_FOUND, "LC/API_NOT_SUPPORT", "userspace not support statistics API");
        }
        
        if(StringUtils.isEmpty(targetType)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TargetTypeIsNotExist.getCode(),
                    "targetType不能为空");
        }
        
        return chapterService.countResourceByTeachingMaterials(mtidList, targetType);
    }
    
    /**
     * 新增章节标签统计
     * @return
     */
    @RequestMapping(value="/{cid}/tags",method=RequestMethod.POST,produces={MediaType.APPLICATION_JSON_VALUE},consumes={MediaType.APPLICATION_JSON_VALUE})
    public Map<String,String> addChapterTags(@PathVariable String cid,@RequestBody Map<String,Integer> params,HttpServletResponse response,@RequestParam(required=false,defaultValue="$RA0101") String category){
    	Map<String,String> returnMap = resourceTagService.addResourceTags(cid,category,params);
    	if(CollectionUtils.isNotEmpty(returnMap)){
    		response.setStatus(500);
    	}
    	return returnMap;
    }
    
    /**
     * 章节标签统计查询
     * @return
     */
    @RequestMapping(value="/{cid}/tags",method=RequestMethod.GET,produces={MediaType.APPLICATION_JSON_VALUE})
    public Map<String,Object> queryChapterTagsByCid(@PathVariable String cid,@RequestParam String limit,@RequestParam(required=false) String category){
    	return resourceTagService.queryResourceTagsByCid(cid,category,limit);
    }
    
    /**
     * 根据章节id删除标签接口
     * @return
     */
    @RequestMapping(value="/{cid}/tags",method=RequestMethod.DELETE,produces={MediaType.APPLICATION_JSON_VALUE})
    public Map<String,Object> deleteResourceTagsByCid(@PathVariable String cid){
    	int num = resourceTagService.deleteResourceTagsByCid(cid);
    	Map<String,Object> map = new HashMap<String, Object>();
    	map.put("成功删除记录数", num);
    	return map;
    }
    
    /**
     * 根据id删除标签接口
     * @return
     */
    @RequestMapping(value="/tags/{id}",method=RequestMethod.DELETE,produces={MediaType.APPLICATION_JSON_VALUE})
    public Map<String,Object> deleteResourceTagsById(@PathVariable String id){
    	int num = resourceTagService.deleteResourceTagsById(id);
    	Map<String,Object> map = new HashMap<String, Object>();
    	map.put("成功删除记录数", num);
    	return map;
    }
    
}
