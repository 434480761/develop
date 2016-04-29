package nd.esp.service.lifecycle.services.notify.dao;

import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.services.notify.models.NotifyInstructionalobjectivesRelationModel;

public interface NotifyInstructionalobjectivesDao {
	
	/**
	 * 判断资源是否有ND库的覆盖范围
	 * @author xiezy
	 * @date 2016年4月13日
	 * @param resId
	 * @return
	 */
	public boolean resourceBelongToNDLibrary(String resId);
	
	/**
	 * 查询教学目标相关联的章节id和课时id
	 * @author xiezy
	 * @date 2016年4月14日
	 * @param resId
	 * @return
	 */
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations(String resId);
	
	/**
	 * 已知教学目标id和课时id,查询相关章节id
	 * @author xiezy
	 * @date 2016年4月20日
	 * @param resId
	 * @param lessonId
	 * @return
	 */
	public Set<String> resourceBelongToRelation4ChapterIds(String ioId,String lessonId);
	
	/**
	 * 已知教学目标和课时关联的关系id,查询相关章节id
	 * @author xiezy
	 * @date 2016年4月20日
	 * @param resId
	 * @param lessonId
	 * @return
	 */
	public Set<String> resourceBelongToRelation4ChapterIdsByRid(String rid);
	
	/**
	 * 已知章节和课时id,查询相关的教学目标id(教学目标id状态为ONLINE,enable=1)
	 * @author xiezy
	 * @date 2016年4月20日
	 * @param chapterId
	 * @param lessonId
	 * @return
	 */
	public Set<String> resourceBelongToRelation4instructionalobjectives(String chapterId,String lessonId);
	
	/**
	 * 已知章节和课时关联的关系id,查询相关的教学目标id(教学目标id状态为ONLINE,enable=1)
	 * @author xiezy
	 * @date 2016年4月20日
	 * @param chapterId
	 * @param lessonId
	 * @return
	 */
	public Set<String> resourceBelongToRelation4instructionalobjectivesByRid(String rid);
	
	/**
	 * 已知课时id,查询相关联的章节id和教学目标id(教学目标id状态为ONLINE,enable=1)
	 * @author xiezy
	 * @date 2016年4月19日
	 * @param lessonId
	 * @return
	 */
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations4LessonOrChapter(final String type, final String id);
	
	/**
	 * 获取教学目标对应的title
	 * @author xiezy
	 * @date 2016年4月14日
	 * @param resId
	 * @return
	 */
	public String getResourceTitle(String resId);
	
	/**
	 * 获取教学目标对应的status
	 * @author xiezy
	 * @date 2016年4月14日
	 * @param resId
	 * @return
	 */
	public String getResourceStatus(String resId);
}
