package nd.esp.service.lifecycle.daos.teachingmaterial.v06;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;

import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;


/**
 * 06章节的Dao
 * <p>Create Time: 2015年8月4日           </p>
 * @author xiezy
 */
public interface ChapterDao {
    /**
     * 找到在同级节点的最末一个节点,取得其right和left值	
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param parent
     * @return
     */
    public Chapter getLastChapterOnSameLevel(String mid,String parent);
    
    /**
     * 移动章节
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param offset
     * @param target
     * @param operation
     * @param leftOrRight
     */
    public void moveChapters(String mid,int offset,int target,String operation,String leftOrRight);
    /**
     * 向前移动章节（方向考虑的是移动部分）（处理的是其余部分）
     * @author linsm
     * @param mid
     * @param offset
     * @param oldLeft
     * @param newLeft
     * @since
     */
    public void moveForwardChapters(String mid,int offset,int oldLeft,int newLeft );
    
    /**
     * 向后移动章节（方向考虑的是移动部分）（处理的是其余部分）
     * @author linsm
     * @param mid
     * @param offset
     * @param oldRight
     * @param newLeft
     * @since
     */
    public void moveBackChapters(String mid,int offset,int oldRight,int newLeft );
    
    /**
     * 查询章节子节点	
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param parent
     * @return
     */
    public List<ChapterViewModel> queryChapterList(String mid, String parent);
    
    /**
     * 查询章节子节点  
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param parent
     * @return
     */
    public List<ChapterViewModel> queryChapterListWithEnableTrue(String mid, String parent);
    
    /**
     * 查询章节子节点的总数	
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param resourceType
     * @param mid
     * @param parent
     * @return
     */
    public long countQueryChapterList(String resourceType,String mid, String parent);
    public long countQueryChapterList(String mid, String parent);
    
    /**
     * 根据左右值查询本身以及子章节	
     * <p>Create Time: 2015年8月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param left
     * @param right
     * @return
     */
    public List<String> getChaptersByLeftAndRight(String mid,Integer left,Integer right);
    
    /**
     * 根据左右值查询本身以及子章节   
     * <p>Create author: linsm   </p>
     * @param mid
     * @param left
     * @param right
     * @return
     */
    public List<Chapter> getSubTreeByLeftAndRight(String mid,Integer left,Integer right);
    
    /**
     * 根据左右值查询本身以及父章节
     * 
     * @param mid
     * @param left
     * @param right
     * @return
     * @since
     */
    public List<Chapter> getParents(String mid,Integer left,Integer right);
    
    /**
     * 删除与chapterIds有关的所有资源关系
     * <p>Create Time: 2015年8月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param chapterIds
     * @return
     */
    public boolean deleteRelationByChapterIds(List<String> chapterIds);
    
    
    /**
     * 伪删除与chapterIds有关的所有资源关系
     * <p>Create Time: 2015年8月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param chapterIds
     * @return
     */
    public boolean deleteRelationByChapterIdsNotReally(List<String> chapterIds);
    
    /**
     * 移动章节到目标位置
     * <p>Create Time: 2015年8月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param difference
     * @param compareLeft
     * @param compareRight
     */
    public void moveChapters2TargetPosition(String mid,int difference,int compareLeft,int compareRight);
    
    /**
     * 修改章节parent	
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: xiezy   </p>
     * @param chapterId
     * @param changeParent
     */
    public void updateChapterParent(String chapterId,String changeParent);
    
    /**
     * 统计教材章节下的资源数量
     * <p>Create Time: 2015年11月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param chapterIds
     * @param targetTypes
     */
    public List<Map<String, Object>> countResourceWithChapters(List<String> chapterIds,List<String> targetTypes);
    
    public Chapter getChapterFromSdk(String resourceType, String mid,String cid);
    
    public List<ResourceCategory> batchGetCategories(List<String> resIds);
}
