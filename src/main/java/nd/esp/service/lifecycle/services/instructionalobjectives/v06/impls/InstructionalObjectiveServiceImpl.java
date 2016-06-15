package nd.esp.service.lifecycle.services.instructionalobjectives.v06.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.EducationRelationLifeCycleModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.services.instructionalobjectives.v06.InstructionalObjectiveService;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务实现类
 * 
 * @author linsm
 */
@Service("instructionalObjectiveServiceV06")
@Transactional
public class InstructionalObjectiveServiceImpl implements InstructionalObjectiveService {

    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    @Qualifier("educationRelationServiceV06")
    private EducationRelationServiceV06 educationRelationService;
    
    @Autowired
    private JdbcTemplate jt;

    @Override
    public InstructionalObjectiveModel createInstructionalObjective(InstructionalObjectiveModel instructionalObjectiveModel) {
        // 调用通用创建接口
        instructionalObjectiveModel.setTechInfoList(null);
        instructionalObjectiveModel = (InstructionalObjectiveModel) ndResourceService.create(ResourceNdCode.instructionalobjectives.toString(),
                                                                                             instructionalObjectiveModel);
        instructionalObjectiveModel.setPreview(null);
        instructionalObjectiveModel.setEducationInfo(null);

        return instructionalObjectiveModel;
    }

    @Override
    public InstructionalObjectiveModel updateInstructionalObjective(InstructionalObjectiveModel instructionalObjectiveModel) {
    	/**
    	 *由于教学目标的演示demo需要有版本的信息
    	 *目前采用的做法
    	 *修改教学目标时，如果状态不是为ONLINE，则新增一条教学目标，并与之前的教学目标（有可能需要通过关系查询最原始的教学目标）建立关系
    	 *修改教学目标时，如果状态为ONLINE，则需要找到最原始的教学目标，并将新的内容赋值给原始的教学目标
    	 */

    	//1、找原始的教学目标数据
    	String initalId = null;
		String initalSql = "SELECT nd.identifier from resource_relations rr,ndresource nd "
				+ "where nd.primary_category='instructionalobjectives' and rr.res_type='instructionalobjectives' "
				+ "and rr.resource_target_type='instructionalobjectives' and rr.target = '"+instructionalObjectiveModel.getIdentifier()+"' "
				+ "and rr.source_uuid = nd.identifier and nd.enable = 1";
		
		List<Map<String,Object>> list = jt.queryForList(initalSql);
		if(CollectionUtils.isNotEmpty(list)){
			Map<String, Object> map = list.get(0);
			initalId = (String)map.get("identifier");
		}
		
		String status = instructionalObjectiveModel.getLifeCycle().getStatus();
		if("ONLINE".equals(status)){
			if(initalId != null){
				instructionalObjectiveModel.setIdentifier(initalId);
			}
	        instructionalObjectiveModel.setTechInfoList(null);
	        instructionalObjectiveModel = (InstructionalObjectiveModel) ndResourceService.update(ResourceNdCode.instructionalobjectives.toString(),
	                                                                                             instructionalObjectiveModel);
	        instructionalObjectiveModel.setPreview(null);
	        instructionalObjectiveModel.setEducationInfo(null);

	        return instructionalObjectiveModel;
		}else{
			String oldId = instructionalObjectiveModel.getIdentifier();
			if(initalId != null){
				oldId = initalId;
			}
			
			String newId = UUID.randomUUID().toString();
			instructionalObjectiveModel.setIdentifier(newId);
			instructionalObjectiveModel.setRelations(null);
			instructionalObjectiveModel.setCoverages(null);
			instructionalObjectiveModel = (InstructionalObjectiveModel) ndResourceService.create(ResourceNdCode.instructionalobjectives.toString(),
                     instructionalObjectiveModel);
			instructionalObjectiveModel.setPreview(null);
			instructionalObjectiveModel.setEducationInfo(null);
			
			
			
			List<EducationRelationModel> educationRelationModels = new ArrayList<EducationRelationModel>();
			EducationRelationModel erm = new EducationRelationModel();
			erm.setIdentifier(UUID.randomUUID().toString());
//			erm.setOrderNum(5000);
			erm.setResType("instructionalobjectives");
			erm.setResourceTargetType("instructionalobjectives");
			erm.setSource(oldId);
			erm.setTarget(newId);
			erm.setLabel("版本迭代");
			erm.setRelationType("ASSOCIATE");
			EducationRelationLifeCycleModel lc = new EducationRelationLifeCycleModel();
			lc.setEnable(true);
			lc.setStatus("CREATED");
			erm.setLifeCycle(lc);
			educationRelationModels.add(erm);
			educationRelationService.createRelation(educationRelationModels, false);
			
			//将target为oldId所对应的关系复制一份到newId中
			String relationSql = "select res_type,source_uuid from resource_relations where resource_target_type='instructionalobjectives' and target = '"+oldId+"'";
			List<Map<String,Object>> l = jt.queryForList(relationSql);
			if(CollectionUtils.isNotEmpty(l)){
				for (Map<String, Object> map : l) {
					List<EducationRelationModel> lt = new ArrayList<EducationRelationModel>();
					String resType = (String)map.get("res_type");
					String source = (String)map.get("source_uuid");
					
					EducationRelationModel tmp = new EducationRelationModel();
					tmp.setIdentifier(UUID.randomUUID().toString());
//					erm.setOrderNum(5000);
					tmp.setResType(resType);
					tmp.setResourceTargetType("instructionalobjectives");
					tmp.setSource(source);
					tmp.setTarget(newId);
					tmp.setLabel("版本迭代");
					tmp.setRelationType("ASSOCIATE");
					EducationRelationLifeCycleModel tmpLc = new EducationRelationLifeCycleModel();
					tmpLc.setEnable(true);
					tmpLc.setStatus("CREATED");
					tmp.setLifeCycle(tmpLc);
					lt.add(tmp);
					educationRelationService.createRelation(lt, false);
				}
			}
			return instructionalObjectiveModel;
			
		}
    }

}
