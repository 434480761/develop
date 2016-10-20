package nd.esp.service.lifecycle.services.knowledges.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.v06.ChapterKnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeRelationsModel;

/**
 * 知识点业务service层接口
 * 
 * @author caocr
 *
 */
public interface KnowledgeService {
    /**
     * 创建知识点
     * 
     * @param model 知识点
     * @return
     * @since
     */
    public KnowledgeModel createKnowledge(KnowledgeModel model);
    
    /**
     * 更新知识点
     * 
     * @param model 知识点
     * @return
     * @since
     */
    public KnowledgeModel updateKnowledge(KnowledgeModel model);

    /**
     * 增加知识点关联
     * 
     * @param model 知识点关联
     * @return
     * @since
     */
    public KnowledgeRelationsModel addKnowledgeRelation(KnowledgeRelationsModel model);

    /**
     * 查看知识点关联
     * 
     * @param contexttype
     * @param relationtype
     * @param contextobjectid
     * @param knowledge
     * @return
     * @since
     */
    public List<KnowledgeRelationsModel> getKnowledgeRelations(String contexttype,
                                                                        String relationtype,
                                                                        String contextobjectid,
                                                                        String knowledge);

    /**
     * 删除知识点关联
     * 
     * @param id
     * @since
     */
    public void deleteKnowledgeRelation(String id);

    /**
     * 添加知识点标签
     * 
     * @param models 知识点标签
     * @return
     * @since
     */
    public List<ChapterKnowledgeModel> addBatchChapterKnowledges(List<ChapterKnowledgeModel> models);
    
    /**
     * 删除知识点标签
     * 
     * @param id 标签id
     * @param tag 标签
     * @param outline 章节id
     * @since
     */
    public void deleteKnowledgeChapterKnowledge(String id, String tag, String outline);

    /**
     * 移动知识点
     * 
     * @param kid 知识点id
     * @param knowledgeModel 知识点模型 
     * @since
     */
    public void moveKnowledge(String kid, KnowledgeModel knowledgeModel);

    KnowledgeModel patchKnowledge(KnowledgeModel model, boolean isObvious);

    /**
     * 判断是否有孩子节点
     * 
     * @param parent
     * @return
     * @since
     */
    public void isHaveChildrens(String parent);

    /**
     * 判断知识点title是否已经存在
     * @param title 知识点title
     * @return 存在：true，不存在：false
     */
    boolean isExistKnowledgeTitle(String title);

}
