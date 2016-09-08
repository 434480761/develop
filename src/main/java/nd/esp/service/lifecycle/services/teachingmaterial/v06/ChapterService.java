package nd.esp.service.lifecycle.services.teachingmaterial.v06;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.models.chapter.v06.ChapterModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;

/**
 * 06章节的Service
 * <p>Create Time: 2015年8月4日           </p>
 * @author xiezy
 */
public interface ChapterService {
    /**
     * 创建章节
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param chapterModel
     * @param resourceType
     * @return
     */
    public ChapterModel createChapter(String resourceType,String mid,ChapterModel chapterModel);
    
    /**
     * 获取章节详细
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param cid
     * @return
     */
    public ChapterModel getChapterDetail(String cid);
    
    /**
     * 修改章节
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     * @param chapterModel
     * @param resourceType
     * @return
     */
    public ChapterModel updateChapter(String resourceType,String mid,String cid,ChapterModel chapterModel);
    
    /**
     * 批量获取教材章节元数据	
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param cidList
     * @return
     */
    public Map<String,ChapterViewModel> batchGetChapterList(List<String> cidList);
    
    /**
     * 查询章节子节点
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     * @param pattern
     * @param resourceType
     * @return
     */
    public ListViewModel<ChapterViewModel> queryChapterList(String resourceType,String mid,String cid,String pattern);
    
    /**
     * 兼容以前（在关系接口中使用）
     * @author linsm
     * @param mid
     * @param cid
     * @param pattern
     * @return
     * @since
     */
    public ListViewModel<ChapterViewModel> queryChapterList(String mid,String cid,String pattern);
    
    /**
     * 删除章节	
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     * @param resourceType
     * @return
     */
    public boolean deleteChapter(String resourceType,String mid,String cid);
    
    /**
     * 伪删除章节 
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: linsm   </p>
     * @param mid
     * @param cid
     * @param resourceType
     * @return
     */
    public boolean deleteChapterNotReally(String resourceType,String mid,String cid);
    
    /**
     * 移动章节
     * <p>Create Time: 2015年8月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     * @param cid
     * @param chapterModel
     * @param resourceType
     */
    public void moveChapter(String resourceType,String mid,String cid,ChapterModel chapterModel);
    
    /**
     * 批量获取教材下的章节数量和资源数量(课时)    
     * <p>Create Time: 2015年11月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param mtidList         批量教材的id
     * @param targetType       目标资源的资源类型。统计查询的目标对象的内容
     * @return
     */
    public Map<String,List<Map<String,Object>>> countResourceByTeachingMaterials(Set<String> mtidList,String targetType);

    public ChapterModel findChapterByIdAndType(String id, String type);
}
