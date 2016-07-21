package nd.esp.service.lifecycle.services.instructionalobjectives.v06;

import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;

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
	
}