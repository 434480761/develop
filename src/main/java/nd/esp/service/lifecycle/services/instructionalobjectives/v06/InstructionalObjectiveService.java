package nd.esp.service.lifecycle.services.instructionalobjectives.v06;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author xuzy
 * @version 0.6
 * @created 2015-07-02
 */
public interface InstructionalObjectiveService{
	/**
	 * 素材创建
	 * @param rm
	 * @return
	 */
	public InstructionalObjectiveModel createInstructionalObjective(InstructionalObjectiveModel am);
	
	/**
	 * 素材修改
	 * @param rm
	 * @return
	 */
	public InstructionalObjectiveModel updateInstructionalObjective(InstructionalObjectiveModel am);

	/**
	 * 根据教学目标id查询出与之关联的章节信息id
	 * 分两种情况：
	 * 1.教学目标与章节直接关联
	 * 2.教学目标与课时关联，课时与章节关联
	 * @param id
	 * @return
     */
	List<Map<String, Object>> getChapterRelationById(String id);

	/***
	 * 根据教学目标Id查询它的Title
	 * @param ids 教学目标Id
	 */
	String getInstructionalObjectiveTitle(String ids);
	Map<String, String> getInstructionalObjectiveTitle(Collection<String> ids);

	/***
	 * 获取未关联到章节/课时的教学目标
	 * @param limit 分页
	 * @param unrelationCategory 未关联的category ""表示同时未关联章节和课时"chapters"/"lessons"/""
	 * @param knowledgeTypeCode 知识点类型维度编码
	 * @param instructionalObjectiveTypeId 教学目标类型Id
	 */
	ListViewModel<InstructionalObjectiveModel> getUnRelationInstructionalObjective(String knowledgeTypeCode, String instructionalObjectiveTypeId, String unrelationCategory, String limit);

	/**
	 * 获取章节下的有序的教学目标列表
	 * @param includesList
	 * @param relationsMap
	 * @param coveragesList
	 * @param limit
	 * @param reverseBoolean
     * @return
     */
	ListViewModel<ResourceModel> getResourcePageByChapterId(List<String> includesList, List<Map<String, String>> relationsMap, List<String> coveragesList, String limit, boolean reverseBoolean);
}