package nd.esp.service.lifecycle.daos.teachingmaterial.v06.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import javax.persistence.Query;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

/**
 * 06章节的Dao实现
 * <p>Create Time: 2015年8月4日           </p>
 * @author xiezy
 */
@Repository
public class ChapterDaoImpl implements ChapterDao{
	private static final Logger LOG = LoggerFactory.getLogger(ChapterDaoImpl.class);
    
    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;
    
    
    @SuppressWarnings("unchecked")
    @Override
    public Chapter getLastChapterOnSameLevel(String mid, String parent) {
        Chapter lastOne = null;
        
        Query query = chapterRepository.getEntityManager().createNamedQuery("getLastChapterOnSameLevel");
        query.setParameter("tmid", mid);
        query.setParameter("pid", parent);
        query.setFirstResult(0);
        query.setMaxResults(1);
        
        List<Chapter> result = query.getResultList();
        
        if(CollectionUtils.isNotEmpty(result)){
            lastOne = result.get(0);
        }
        
        return lastOne;
    }

    @Override
    public void moveChapters(String mid, int offset, int target, String operation,String leftOrRight) {
        //由SQL改为HQL的适配
//        if(leftOrRight.equals("tree_left")){
//            leftOrRight = "left";
//        }
//        if(leftOrRight.equals("tree_right")){
//            leftOrRight = "right";
//        }
        
        //Update
        String hqlUpdate = "UPDATE chapters SET " + leftOrRight + "=" + leftOrRight + "+" + offset;
        //Where
        String hqlWhere = "WHERE teaching_material=:tmid AND " + leftOrRight + operation + target;
        
        String executeHql = hqlUpdate + " " + hqlWhere;
        LOG.info("移动章节" + leftOrRight + "的HQL语句:" + executeHql);
        
        Query query = chapterRepository.getEntityManager().createNativeQuery(executeHql);
        query.setParameter("tmid", mid);

        query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ChapterViewModel> queryChapterList(String mid, String parent) {
        
        Query query = null;
        if(StringUtils.isEmpty(parent)){
            query = chapterRepository.getEntityManager().createNamedQuery("queryChapterListWithoutParent");
            query.setParameter("tmid", mid);
        }else{
            query = chapterRepository.getEntityManager().createNamedQuery("queryChapterListWithParent");
            query.setParameter("tmid", mid);
            query.setParameter("pid", parent);
        }
        
        List<Chapter> result = query.getResultList();
        List<ChapterViewModel> resultList = new ArrayList<ChapterViewModel>();
        
        if(CollectionUtils.isNotEmpty(result)){
            for(Chapter chapter : result){
                // ChapterViewModel chapterViewModel = BeanMapperUtils.beanMapper(chapter, ChapterViewModel.class);
                // resultList.add(chapterViewModel);
                if (chapter != null) {
                    resultList.add(CommonHelper.changeChapterToChapterViewModel(chapter));
                }
            }
        }
        return resultList;
    }

    @Override
    public long countQueryChapterList(String resourceType,String mid, String parent) {
        Query query = null;
        if(StringUtils.isEmpty(parent)){
            query = chapterRepository.getEntityManager().createNamedQuery("countQueryChapterListWithoutParent");
            query.setParameter("tmid", mid);
        }else{
            query = chapterRepository.getEntityManager().createNamedQuery("countQueryChapterListWithParent");
            query.setParameter("tmid", mid);
            query.setParameter("pid", parent);
        }
        query.setParameter("resourceType", resourceType);
        
        Long total = (Long)query.getSingleResult();
        
        return total;
    }
    
    @Override
    public long countQueryChapterList(String mid, String parent) {
        return countQueryChapterList(ResourceNdCode.chapters.toString(), mid, parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getChaptersByLeftAndRight(String mid, Integer left, Integer right) {
        Query query = chapterRepository.getEntityManager().createNamedQuery("getChaptersByLeftAndRight");
        query.setParameter("tmid", mid);
        query.setParameter("tleft", left);
        query.setParameter("tright", right);
        
        List<Chapter> result = query.getResultList();
        List<String> resultList = new ArrayList<String>();
        
        if(CollectionUtils.isNotEmpty(result)){
            for(Chapter chapter : result){
                String chapterId = chapter.getIdentifier();
                resultList.add(chapterId);
            }
        }
        
        return resultList;
    }

    @Override
    public boolean deleteRelationByChapterIds(List<String> chapterIds) {
        Query query = chapterRepository.getEntityManager().createNamedQuery("deleteRelationByChapterIds");
        query.setParameter("sids", chapterIds);
        query.setParameter("tids", chapterIds);
        
        query.executeUpdate();
        
        return true;
    }

    @Override
    public void moveChapters2TargetPosition(String mid, int difference, int compareLeft, int compareRight) {
        //Update
        String hqlUpdate = "UPDATE chapters SET tree_left=tree_left+" + difference + ",tree_right=tree_right+" + difference;
        //Where
        String hqlWhere = "WHERE teaching_material=:tmid AND tree_left>=:tleft AND tree_right<=:tright";
        
        String executeHql = hqlUpdate + " " + hqlWhere;
        LOG.info("执行将需要移动的章节移动到目标位置的HQL语句:" + executeHql);
        
        Query query = chapterRepository.getEntityManager().createNativeQuery(executeHql);
        query.setParameter("tmid", mid);
        query.setParameter("tleft", compareLeft);
        query.setParameter("tright", compareRight);
        
        LOG.error("lsmTest moveChapters2TargetPosition: "+"executeHql:"+executeHql+" tmid:"+mid+" tleft:"+compareLeft+" tright:"+compareRight);
        
        query.executeUpdate();
    }
    
    @Override
    public void updateChapterParent(String chapterId, String changeParent) {
        String hqlUpdate = "UPDATE chapters SET parent='" + changeParent + "'";
        String hqlWhere = "WHERE identifier=:cid";
        
        String executeHql = hqlUpdate + " " + hqlWhere;
        
        Query query = chapterRepository.getEntityManager().createNativeQuery(executeHql);
        query.setParameter("cid", chapterId);
        
        query.executeUpdate();
    }
    
    @Override
    public List<Map<String, Object>> countResourceWithChapters(List<String> chapterIds, List<String> targetTypes) {
        
        if(CollectionUtils.isNotEmpty(targetTypes) && CollectionUtils.isNotEmpty(chapterIds)){
            List<String> queryHqlList = new ArrayList<String>();
            
            for(String targetType : targetTypes){
                //需要统计资源对应的数据库表名
                String resourceTableName = LifeCircleApplicationInitializer.tablenames_properties.getProperty(targetType+"_entity"); 
                
                if(StringUtils.isEmpty(resourceTableName)){
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.TargetTypeIsNotExist.getCode(),
                            targetType + "--不是可统计资源类型");
                }
                
                //select sql
                String selectHql = "SELECT COUNT(DISTINCT a.identifier) AS counts";
                //from sql
                String fromHql = "FROM ResourceRelation r," + resourceTableName + " a";
                //where sql
                String whereHql = "WHERE a.primaryCategory='" + targetType + "' AND r.target=a.identifier AND r.enable=1 AND a.enable=1 "
                        + "AND r.resType='chapters' AND r.resourceTargetType='" + targetType + "' "
                        + "AND r.sourceUuid IN (:sids)";
                
                //统计查询sql
                String queryHql = selectHql + " " + fromHql + " " + whereHql;
                queryHqlList.add(queryHql);
            }
            
            //查询 + fork join
            if(CollectionUtils.isNotEmpty(queryHqlList)){
            	
                Future<List<Map<String, Object>>> result = CommonHelper.getForkJoinPool().submit(new ResourceStatisticsThread(targetTypes,queryHqlList,chapterIds));
                List<Map<String, Object>> resultList = null;
                try {
                    resultList = result.get();
                } catch (InterruptedException e) {
                    LOG.error("教材章节下资源数量统计出错了！",e);
                } catch (ExecutionException e) {
                    LOG.error("教材章节下资源数量统计出错了！",e);
                }
                
                return resultList;
            }
        }
        
        return null;
    }
    
    /**
     * 教材章节下资源数量统计-ForkJoin类
     * <p>Create Time: 2015年11月4日           </p>
     * @author xiezy
     */
    class ResourceStatisticsThread extends RecursiveTask<List<Map<String, Object>>>{
        private static final long serialVersionUID = 1L;
        
        private List<String> targetTypes;
        private List<String> hqlList;
        private List<String> chapterIds;
        public ResourceStatisticsThread(List<String> targetTypes,List<String> hqlList,List<String> chapterIds) {
           this.targetTypes = targetTypes;
           this.hqlList = hqlList;  
           this.chapterIds = chapterIds;
        } 
        
        @Override
        protected List<Map<String, Object>> compute() {
            List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
            
            List<ResourceStatisticsThread> threads = new ArrayList<ChapterDaoImpl.ResourceStatisticsThread>();
            if(hqlList.size() == 1){  
                Query query = chapterRepository.getEntityManager().createQuery(hqlList.get(0));
                query.setParameter("sids", chapterIds);
                Long resourceCounts = (Long)query.getSingleResult();
                
                Map<String, Object> returnMap = new HashMap<String, Object>();
                returnMap.put("resource_type", targetTypes.get(0));
                returnMap.put("count", resourceCounts);
                
                returnList.add(returnMap);
            }else{
                for (int i = 0; i< hqlList.size() ;i++) {
                    List<String> tyTmp = new ArrayList<String>();
                    List<String> sqlTmp = new ArrayList<String>();
                    tyTmp.add(targetTypes.get(i));
                    sqlTmp.add(hqlList.get(i));
                    
                    ResourceStatisticsThread thread = new ResourceStatisticsThread(tyTmp,sqlTmp,chapterIds);
                    threads.add(thread);
                    thread.fork();
                }
            }
            
            if(CollectionUtils.isNotEmpty(threads)){
                for(ResourceStatisticsThread rst : threads){
                    returnList.addAll(rst.join());
                }
            }
            
            return returnList;
        }
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao#moveForwardChapters(java.lang.String, int, int, int)
     */
    @Override
    public void moveForwardChapters(String mid, int offset, int oldLeft, int newLeft) {
        moveForwardChapters(mid,offset,oldLeft,newLeft,"tree_left");
        moveForwardChapters(mid,offset,oldLeft,newLeft,"tree_right");

    }
    
    /**
     * 向前移动（处理其余部分）左值或右值
     * @author linsm
     * @param mid
     * @param offset
     * @param oldLeft
     * @param newLeft
     * @param leftOrRight 处理左值还是右值： 取值：（tree_left，tree_right）
     * @since 
     */
    private void moveForwardChapters(String mid, int offset, int oldLeft, int newLeft,String leftOrRight) {
        // Update
        String hqlUpdate = "UPDATE chapters SET "+leftOrRight + "=" + leftOrRight+ "+"  + offset;
        // Where
        String hqlWhere = "WHERE teaching_material=:tmid AND "+leftOrRight+">=:newLeft AND "+leftOrRight+"<:oldLeft";

        String executeHql = hqlUpdate + " " + hqlWhere;
        LOG.info("向前移动章节的HQL语句:" + executeHql);

        Query query = chapterRepository.getEntityManager().createNativeQuery(executeHql);
        query.setParameter("tmid", mid);
        query.setParameter("newLeft", newLeft);
        query.setParameter("oldLeft", oldLeft);
        LOG.error("lsmTest moveForwardChapters: " + "executeHql:" + executeHql + " tmid:" + mid + " newLeft:" + newLeft
                + " oldLeft:" + oldLeft);
        query.executeUpdate();
        
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao#moveBackChapters(java.lang.String, int, int, int)
     */
    @Override
    public void moveBackChapters(String mid, int offset, int oldRight, int newLeft) {
        
        moveBackChapters(mid,offset,oldRight,newLeft,"tree_left");
        moveBackChapters(mid,offset,oldRight,newLeft,"tree_right");
    }

    /**
     * 向右移动（处理其余部分）左值或右值
     * @author linsm
     * @param mid
     * @param offset
     * @param oldRight
     * @param newLeft
     * @param string
     * @param leftOrRight
     * @since 
     */
    private void moveBackChapters(String mid, int offset, int oldRight, int newLeft, String leftOrRight) {
        // Update
        String hqlUpdate = "UPDATE chapters SET " + leftOrRight + "=" + leftOrRight + "+" + offset;
        // Where
        String hqlWhere = "WHERE teaching_material=:tmid AND " + leftOrRight + ">=:oldRight AND " + leftOrRight
                + "<:newLeft";

        String executeHql = hqlUpdate + " " + hqlWhere;
        LOG.info("移动章节的HQL语句:" + executeHql);

        Query query = chapterRepository.getEntityManager().createNativeQuery(executeHql);
        query.setParameter("tmid", mid);
        query.setParameter("newLeft", newLeft);
        query.setParameter("oldRight", oldRight);
        LOG.error("lsmTest moveBackChapters: " + "executeHql:" + executeHql + " tmid:" + mid + " newLeft:" + newLeft
                + " oldRight:" + oldRight);
        query.executeUpdate();

    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao#getSubTreeByLeftAndRight(java.lang.String, java.lang.Integer, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Chapter> getSubTreeByLeftAndRight(String mid, Integer left, Integer right) {
        Query query = chapterRepository.getEntityManager().createNamedQuery("getChaptersByLeftAndRight");
        query.setParameter("tmid", mid);
        query.setParameter("tleft", left);
        query.setParameter("tright", right);
        
       return query.getResultList();
    }
    
    @Override
    public List<Chapter> getParents(String mid, Integer left, Integer right) {
        Query query = chapterRepository.getEntityManager().createNamedQuery("getParentsByLeftAndRight");
        query.setParameter("tmid", mid);
        query.setParameter("tleft", left);
        query.setParameter("tright", right);
        
       return query.getResultList();
    }

    /*
     * (non-Javadoc)
     * @see
     * nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao#deleteRelationByChapterIdsNotReally(java.util.List)
     */
    @Override
    public boolean deleteRelationByChapterIdsNotReally(List<String> chapterIds) {
        // 关系
        String sql = "UPDATE resource_relations SET enable = '0' WHERE (resource_target_type = :resourceType AND target in (:ids)) OR (res_type = :resourceType  AND source_uuid in (:ids))";
        Query query = chapterRepository.getEntityManager().createNativeQuery(sql);
        query.setParameter("resourceType", "chapters");
        query.setParameter("ids", chapterIds);
        query.executeUpdate();

        //titan delete
        titanRelationRepository.batchDeleteRelationSoft("chapters", chapterIds);

        return true;
    }
    
    /**
     * 获取教材下的章节
     * 新增当章节不存在时，抛出异常（不管逻辑还是物理） 
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     * @param resourceType
     * @return
     */
    public Chapter getChapterFromSdk(String resourceType, String mid,String cid){
        Chapter chapter = new Chapter();
        chapter.setIdentifier(cid);
        chapter.setTeachingMaterial(mid);
        chapter.setEnable(true);
        chapter.setPrimaryCategory(resourceType);
        
        try {
            chapter = chapterRepository.getByExample(chapter);
            
        } catch (EspStoreException e) {
            //FIXME
            LOG.error("教材章节V0.6获取章节详细出错", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
        }
        
        return chapter;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ResourceCategory> batchGetCategories(List<String> resIds) {
        if(CollectionUtils.isEmpty(resIds)){
            return null;
        }
        
        Query query = chapterRepository.getEntityManager().createNamedQuery("batchGetCategories");
        query.setParameter("resIds", resIds);
        query.setParameter("rts", "knowledges");
        
        List<ResourceCategory> resourceCategories = query.getResultList();
        
        return resourceCategories;
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao#queryChapterListWithEnableTrue(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ChapterViewModel> queryChapterListWithEnableTrue(String mid, String parent) {
        Query query = null;
        if(StringUtils.isEmpty(parent)){
            query = chapterRepository.getEntityManager().createNamedQuery("queryChapterListWithoutParentWithEnableTrue");
            query.setParameter("tmid", mid);
        }else{
            query = chapterRepository.getEntityManager().createNamedQuery("queryChapterListWithParentWithEnableTrue");
            query.setParameter("tmid", mid);
            query.setParameter("pid", parent);
        }
        
        List<Chapter> result = query.getResultList();
        List<ChapterViewModel> resultList = new ArrayList<ChapterViewModel>();
        
        if(CollectionUtils.isNotEmpty(result)){
            for(Chapter chapter : result){
//                ChapterViewModel chapterViewModel = BeanMapperUtils.beanMapper(chapter, ChapterViewModel.class);
//                resultList.add(chapterViewModel);
                if(chapter != null){
                    resultList.add(CommonHelper.changeChapterToChapterViewModel(chapter));
                }
            }
        }
        return resultList;
    }

}


