package nd.esp.service.lifecycle.services.teachingmaterial.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.models.chapter.v06.ChapterModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.repository.v02.ResourceRelationApiService;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeService;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeTrargetAndParentModel;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterConstant;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 06章节的Service实现
 * <p>Create Time: 2015年8月4日           </p>
 * @author xiezy
 */
@Service(value="chapterServiceV06")
@Transactional
public class ChapterServiceImpl implements ChapterService{
	private final static Logger LOG = LoggerFactory.getLogger(ChapterServiceImpl.class);
    
	
	@Autowired
	private TreeService treeService;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private TeachingMaterialRepository teachingMaterialRepository;
    @Autowired
    private ChapterDao chapterDao;
    @Autowired
    private NotifyReportService nrs;
    @Autowired
    private ResourceRelationApiService resourceRelationService;
    
    @Override
    public ChapterModel createChapter(String resourceType,String mid, ChapterModel chapterModel) {
        
        TreeModel current = null;
        TreeDirection treeDirection = TreeDirection.fromString(chapterModel.getDirection());
        
        //0.判断教材是否存在
        if(!isTeachingMaterialExist(resourceType,mid)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TeachingMaterialNotFound);
        }
        chapterModel.setIdentifier(UUID.randomUUID().toString());
        chapterModel.setTeachingMaterial(mid);
        
//        if(chapterModel.getDirection().equals(ChapterConstant.DIR_PRE)){
//            treeDirection = TreeDirection.pre;
//        }
        Chapter chapter  =null;
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        synchronized (treeService) {
            current = treeService.insertLeaf(getTreeTargetAndParent(chapterModel), treeDirection);
            
            //3.转换为SDK的Chapter
           chapter = BeanMapperUtils.beanMapper(chapterModel, Chapter.class);
//            chapter.setOrderNum(current.getLeft());
            chapter.setLeft(current.getLeft());
            chapter.setRight(current.getRight());
            chapter.setParent(current.getParent());
            
            //添加创建时间与修改时间
            chapter.setCreateTime(ts);
            chapter.setLastUpdate(ts);
            
            try {
                chapter = chapterRepository.add(chapter);
            } catch (EspStoreException e) {
                LOG.error("教材章节V0.6添加章节出错", e);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                        e.getLocalizedMessage());
            }
        }
        
        
        if(chapter == null){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateChapterFail);
        }
        
        if(LOG.isInfoEnabled()){
        	LOG.info("教材章节V0.6添加章节成功，id:{}",chapter.getIdentifier());
        }
        
        return changeChapterToChapterModel(chapter);
    }
    
//    /**
//     * @author linsm
//     * @param targetChapter
//     * @return
//     * @since 
//     */
//    public static TreeModel changeChapterToTreeModel(Chapter targetChapter) {
//        TreeModel target = new TreeModel();
//        target.setIdentifier(targetChapter.getIdentifier());
//        target.setLeft(targetChapter.getLeft());
//        target.setRight(targetChapter.getRight());
//        target.setParent(targetChapter.getParent());
//        target.setRoot(targetChapter.getTeachingMaterial());
//        
//        
//        return target;
//    }

   
    
    /**
     * 判断教材是否存在
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param resourceType
     * @return
     */
    private boolean isTeachingMaterialExist(String resourceType, String mid){
        // userspace 时，不需要校验
        if (ChapterConstant.ChapterSupportResourceTypes.userspace.toString().equals(resourceType)) {
            return true;
        }
        try {
            TeachingMaterial teachingMaterial = teachingMaterialRepository.get(mid);
            if(teachingMaterial == null || (!teachingMaterial.getEnable())){
                return false;
            }
            if(!teachingMaterial.getPrimaryCategory().equals(resourceType)){
            	return false;
            }
        } catch (EspStoreException e) {
            LOG.error("教材章节V0.6获取教材出错", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        return true;
    }

    @Override
    public ChapterModel getChapterDetail(String cid) {
        Chapter chapter = null;
        try {
            chapter = chapterRepository.get(cid);
        } catch (EspStoreException e) {
            LOG.error("教材章节V0.6获取章节详细出错", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        // FIXME 后期需要考虑是否处理成一致的，直接抛出异常（但可能会影响其它系统）
        if (chapter == null || chapter.getEnable() == null || !chapter.getEnable()
                || !ResourceNdCode.chapters.toString().equals(chapter.getPrimaryCategory())) {
            return null;
        }
        
        return changeChapterToChapterModel(chapter);
    }

    @Override
    public ChapterModel updateChapter(String resourceType,String mid, String cid, ChapterModel chapterModel) {
        //判断教材是否存在
        if(!isTeachingMaterialExist(resourceType,mid)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TeachingMaterialNotFound);
        }
        
        //判断章节是否存在
        Chapter chapter4Detail = getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid,cid);
        
        //补全chapterModel
        chapterModel.setParent(chapter4Detail.getParent());
        chapterModel.setIdentifier(cid);
        chapterModel.setTeachingMaterial(mid);
        
        //转换为SDK的Chapter
        Chapter chapter = BeanMapperUtils.beanMapper(chapterModel, Chapter.class);
//        chapter.setOrderNum(chapter4Detail.getOrderNum());
        chapter.setLeft(chapter4Detail.getLeft());
        chapter.setRight(chapter4Detail.getRight());
        
        //时间处理
        chapter.setCreateTime(chapter4Detail.getCreateTime());
        chapter.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        
        try {
            chapter = chapterRepository.update(chapter);
        } catch (EspStoreException e) {
            LOG.error("教材章节V0.6修改章节失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                            e.getLocalizedMessage());
        }
        
        if(chapter == null){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateChapterFail);
        }
        
        
        if(LOG.isInfoEnabled()){
        	LOG.info("教材章节V0.6修改章节成功，id:{}",chapter.getIdentifier());
        }
        
        return changeChapterToChapterModel(chapter);
    }

    @Override
    public Map<String, ChapterViewModel> batchGetChapterList(List<String> cidList) {
        Map<String, ChapterViewModel> map = new HashMap<String, ChapterViewModel>();
        List<Chapter> chapters = null;
        try {
            chapters = chapterRepository.getAll(cidList);
        } catch (EspStoreException e) {
            LOG.error("教材章节V0.6批量获取章节的详细信息失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                e.getLocalizedMessage());
        }
        
        if(CollectionUtils.isNotEmpty(chapters)){
            for(Chapter chapter : chapters){
                if(chapter != null&&chapter.getEnable()!=null && chapter.getEnable()){
                    map.put(chapter.getIdentifier(), CommonHelper.changeChapterToChapterViewModel(chapter));
                }
            }
        }
        
        return map;
    }

    @Override
    public ListViewModel<ChapterViewModel> queryChapterList(String resourceType,String mid, String cid, String pattern) {
        //判断教材是否存在
        if(!isTeachingMaterialExist(resourceType,mid)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TeachingMaterialNotFound);
        }
        
        //返回的结果集
        ListViewModel<ChapterViewModel> listViewModel = new ListViewModel<ChapterViewModel>();
        List<ChapterViewModel> items = new ArrayList<ChapterViewModel>();
        Chapter condition = new Chapter();
        condition.setEnable(true);
        condition.setTeachingMaterial(mid);
        if(pattern.equals(ChapterConstant.PATTERN_ALL)){//获取教材下的全部章节
            items = chapterDao.queryChapterListWithEnableTrue(mid, null);
        }else{//获取下一级的子节点(一层)
            if(!cid.equals(mid)){//不是查教材的一级节点,cid是真正的章节id,判断章节是否存在
                getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid,cid);
            }
            
            items = chapterDao.queryChapterListWithEnableTrue(mid, cid);
        }
        listViewModel.setItems(items);
        listViewModel.setLimit("");
        listViewModel.setTotal((long)(items.size()));
        
        return listViewModel;
    }

    @Override
    public boolean deleteChapter(String resourceType,String mid, String cid) {
        //1.判断教材是否存在
        if(!isTeachingMaterialExist(resourceType,mid)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TeachingMaterialNotFound);
        }
        
        //2.判断章节是否存在
        Chapter chapter4Detail = getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid,cid);
        
        //3.根据cid的左右值获取cid以及cid下的全部子章节(chapterId集合)
        List<String> chapterIds = 
                chapterDao.getChaptersByLeftAndRight(mid, chapter4Detail.getLeft(), chapter4Detail.getRight());
        
        //4.删除chapterIds集合中所有章节相关的资源关系
        chapterDao.deleteRelationByChapterIds(chapterIds);
        
        //5.删除chapterIds对应的章节
        try {
            chapterRepository.batchDel(chapterIds);
        } catch (EspStoreException e) {
            LOG.error("教材章节V0.6批量删除章节出错", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        
        TreeModel current = new TreeModel(chapter4Detail);
        treeService.removeSubTree(current);
        if(LOG.isInfoEnabled()){
        	LOG.info("教材章节V0.6批量删除章节成功,mid:{},cids:{}",mid,chapterIds);
        }
        
        return true;
    }

    @Override
    public void moveChapter(String resourceType,String mid, String cid, ChapterModel chapterModel) {
        
        TreeDirection treeDirection = TreeDirection.fromString(chapterModel.getDirection());

        // 1.判断教材是否存在
        if(!isTeachingMaterialExist(resourceType,mid)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TeachingMaterialNotFound);
        }
        
        //2.判断章节是否存在
        Chapter chapter4Detail = getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid,cid);
        
        TreeModel current = new TreeModel(chapter4Detail);
        
        //3.补全chapterModel所需的字段
        chapterModel.setTeachingMaterial(mid);
        chapterModel.setIdentifier(cid);
        
        treeService.moveSubTree(getTreeTargetAndParent(chapterModel), current, treeDirection);
        
        if(LOG.isInfoEnabled()){
        	LOG.info("教材章节V0.6移动章节成功，mid:{},cid:{}",mid,cid);
        }
    }
    
    
    /**
     * @author linsm
     * @param chapterModel
     * @return
     * @since 
     */
    private TreeTrargetAndParentModel getTreeTargetAndParent(ChapterModel chapterModel) {
      TreeTrargetAndParentModel model = new TreeTrargetAndParentModel();
        //target and parent
      String mid = chapterModel.getTeachingMaterial();
        if(StringUtils.isNotEmpty(chapterModel.getTarget())){
            Chapter targetChapter = getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid,chapterModel.getTarget());
            model.setTarget(new TreeModel(targetChapter));
        }else if(StringUtils.isNotEmpty(chapterModel.getParent())&& !chapterModel.getParent().equals(mid)){
            //存在对应的章节parent -> uuid
            Chapter parentChapter = getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid,chapterModel.getParent());
            model.setParent(new TreeModel(parentChapter));
        }else{
            //root 默认为mid
            int nodeNum = (int)chapterDao.countQueryChapterList(mid, null);
            //虚根
            TreeModel parent = new TreeModel();
            parent.setLeft(0);
            parent.setRight(nodeNum*2+1);
            parent.setRoot(mid);
            parent.setParent(null);
            parent.setIdentifier(mid);
            model.setParent(parent);
        }
        
        return model;
    }

    @Override
    public Map<String, List<Map<String, Object>>> countResourceByTeachingMaterials(Set<String> mtidList,
            String targetType) {
        //返回值
        Map<String,List<Map<String,Object>>> resultMap = new HashMap<String,List<Map<String,Object>>>();
        
        //循环教材id(mtidList)
        if(CollectionUtils.isNotEmpty(mtidList)){
        	for(String mtid : mtidList){
                //map中的map
                List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
                
                //获取教材
                TeachingMaterial teachingMaterial = null;
                try {
                    teachingMaterial = teachingMaterialRepository.get(mtid);
                } catch (EspStoreException e) {
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                            e.getLocalizedMessage());
                }
                
                if(teachingMaterial != null && teachingMaterial.getEnable()!=null && teachingMaterial.getEnable()){//教材存在
                    //获取教材下的所有章节
                    Chapter chapterCondition = new Chapter();
                    chapterCondition.setTeachingMaterial(mtid);
                    chapterCondition.setEnable(true);
                    List<Chapter> chapters = new ArrayList<Chapter>();
                    try {
                        chapters = chapterRepository.getAllByExample(chapterCondition);
                    } catch (EspStoreException e) {
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                e.getLocalizedMessage());
                    }
                    
                    if(CollectionUtils.isNotEmpty(chapters)){//教材下有章节
                        //1.返回章节的数量,默认必须返回
                        Map<String,Object> chapterCount = new HashMap<String, Object>();
                        chapterCount.put("resource_type", IndexSourceType.ChapterType.getName());
                        chapterCount.put("count", chapters.size());
                        items.add(chapterCount);
                        
                        //获取章节id集合
                        List<String> chapterIds = new ArrayList<String>();
                        for(Chapter chapter : chapters){
                            String chapterId = chapter.getIdentifier();
                            chapterIds.add(chapterId);
                        }
                        //获取targetType集合
                        List<String> targetTypes = new ArrayList<String>();
                        if(StringUtils.isNotEmpty(targetType)){
                            String[] targetArray = targetType.split(",");
                            
                            for(int i=0;i<targetArray.length;i++){
                                if(!targetArray[i].equals("chapters")){
                                    targetTypes.add(targetArray[i]);
                                }
                            }
                        }
                        
                        //2.获取章节下的所需要的资源数量
                        List<Map<String, Object>> resourceCounts = null;
                        if(CollectionUtils.isNotEmpty(targetTypes) && CollectionUtils.isNotEmpty(chapterIds)){
                            resourceCounts = chapterDao.countResourceWithChapters(chapterIds, targetTypes);
                        }
                        
                        if(CollectionUtils.isNotEmpty(resourceCounts)){
                            items.addAll(resourceCounts);
                        }
                    }else{//教材下没有任何章节
                        Map<String,Object> resourceCount = new HashMap<String, Object>();
                        resourceCount.put("resource_type", IndexSourceType.ChapterType.getName());
                        resourceCount.put("count", 0);
                        items.add(resourceCount);
                    }
                    
                    resultMap.put(mtid, items);
                }else{//教材不存在
                    resultMap.put(mtid, null);
                }
            }
        }
        
        return resultMap;
    }
    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterServiceV06#queryChapterList(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ListViewModel<ChapterViewModel> queryChapterList(String mid, String cid, String pattern) {
        //FIXME 暂时先使用“教材 ”，这样一来就会校验“教材”是否存在（注意，当资源的惟一性不能通过uuid来保证时，就会需要资源类型）
        return queryChapterList(ChapterConstant.ChapterSupportResourceTypes.teachingmaterials.toString(),mid,cid,pattern);
    }

    /*
     * (non-Javadoc)
     * @see
     * nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterServiceV06#deleteChapterNotReally(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteChapterNotReally(String resourceType, String mid, String cid) {
        // 1.判断教材是否存在
        if (!isTeachingMaterialExist(resourceType, mid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.TeachingMaterialNotFound);
        }

        // 2.判断章节是否存在
        Chapter chapter4Detail = getChapterFromSdkWithCheck(ResourceNdCode.chapters.toString(),mid, cid);

        // 3.根据cid的左右值获取cid以及cid下的全部子章节(chapterId集合)
        List<Chapter> chapters = chapterDao.getSubTreeByLeftAndRight(mid,
                                                                     chapter4Detail.getLeft(),
                                                                     chapter4Detail.getRight());

        List<String> chapterIds = new ArrayList<String>();

        if (CollectionUtils.isNotEmpty(chapters)) {
            Timestamp time = new Timestamp(new Date().getTime());
            for (Chapter chapter : chapters) {
                chapterIds.add(chapter.getIdentifier());
                //处理成伪删除
                chapter.setEnable(false);
                chapter.setLastUpdate(time);
            }
            // 4.伪删除chapterIds集合中所有章节相关的资源关系
            chapterDao.deleteRelationByChapterIdsNotReally(chapterIds);

            // 5.伪删除chapters对应的章节 (批量更新)
            try {
                chapterRepository.batchAdd(chapters);
            } catch (EspStoreException e) {
                LOG.error("教材章节V0.6批量删除章节出错", e);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getLocalizedMessage());
            }
        }
        

        // TreeModel current = changeChapterToTreeModel(chapter4Detail);
        // treeService.removeSubTree(current);
        if (LOG.isInfoEnabled()) {
            LOG.info("教材章节V0.6批量删除章节成功,mid:{},cids:{}", mid, chapterIds);
        }
        
        return true;
    }
    
    private Chapter getChapterFromSdkWithCheck(String resourceType, String mid,String cid){
        Chapter chapter = chapterDao.getChapterFromSdk(resourceType, mid, cid);
        if(chapter == null || chapter.getEnable()==null || !chapter.getEnable()){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChapterNotFound);
        }
        return chapter;
    }
    
    /**
     * 模型转换: chapter->chapterModel
     * @author linsm
     * @param chapter
     * @return
     * @since
     */
    private static ChapterModel changeChapterToChapterModel(Chapter chapter){
        
        ChapterModel chapterModel = new ChapterModel();
        chapterModel.setIdentifier(chapter.getIdentifier());
        chapterModel.setDescription(chapter.getDescription());
        chapterModel.setParent(chapter.getParent());
        chapterModel.setTeachingMaterial(chapter.getTeachingMaterial());
        chapterModel.setTitle(chapter.getTitle());
        chapterModel.setTags(chapter.getTags());
        
        return chapterModel;
    }

    /**
     * id有可能是章节id也有可能是课时id
     * 1、如果是章节id，则直接查询章节对象
     * 2、如果是课时id，则从关系表中查询对应关系的章节id，再根据章节id查询章节对象
     *
     * @param id
     * @param type
     * @return
     */
    @Override
    public ChapterModel findChapterByIdAndType(String id, String type) {
        Chapter chapter = null;
        try {
            if (IndexSourceType.ChapterType.getName().equals(type)) {
                chapter = chapterRepository.get(id);
            } else if (IndexSourceType.LessonType.getName().equals(type)) {
                List<ResourceRelation> relationList = resourceRelationService.getByResTypeAndTargetTypeAndTargetId(
                        IndexSourceType.ChapterType.getName(), IndexSourceType.LessonType.getName(), id);

                if (relationList != null && relationList.size() > 0) {
                    chapter = chapterRepository.get(relationList.get(0).getSourceUuid());
                }
            }
        } catch (EspStoreException e) {
            LOG.error("根据id类型获取章节数据出错！", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        // FIXME 后期需要考虑是否处理成一致的，直接抛出异常（但可能会影响其它系统）
        if (chapter == null || chapter.getEnable() == null || !chapter.getEnable()
                || !ResourceNdCode.chapters.toString().equals(chapter.getPrimaryCategory())) {
            return null;
        }
        return changeChapterToChapterModel(chapter);
    }

}
