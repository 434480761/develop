package nd.esp.service.lifecycle.services.instructionalobjectives.v06.impls;

import java.util.*;

import nd.esp.service.lifecycle.educommon.models.ResCoverageModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.chapter.v06.ChapterModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationLifeCycleModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.v02.ResourceRelationApiService;
import nd.esp.service.lifecycle.services.coverages.v06.CoverageService;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.services.instructionalobjectives.v06.InstructionalObjectiveService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.TeachingMaterialServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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

	private static final Logger LOG = LoggerFactory.getLogger(InstructionalObjectiveServiceImpl.class);

    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    @Qualifier("educationRelationServiceV06")
    private EducationRelationServiceV06 educationRelationService;
    
    @Autowired
    @Qualifier(value="coverageServiceImpl")
    private CoverageService coverageService;
    
    @Autowired
    private JdbcTemplate jt;

	@Autowired
	private ResourceRelationApiService resourceRelationApiService;

	@Autowired
	private ChapterService chapterService;

	@Autowired
	private TeachingMaterialServiceV06 teachingMaterialService;



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
		}else if(instructionalObjectiveModel.getCustomProperties() != null && instructionalObjectiveModel.getCustomProperties().contains("onlystatus")){
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
			
			//查找oldId的覆盖范围
			List<CoverageViewModel> cvList = coverageService.getCoveragesByResource("instructionalobjectives",oldId,null,null,null);
			if(CollectionUtils.isNotEmpty(cvList)){
				List<ResCoverageModel> coverages = new ArrayList<ResCoverageModel>();
				for (CoverageViewModel cvm : cvList) {
					ResCoverageModel c = BeanMapperUtils.beanMapper(cvm, ResCoverageModel.class);
					coverages.add(c);
				}
				instructionalObjectiveModel.setCoverages(coverages);;
			}
			
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
			
			
			//将target为oldId所对应的关系复制一份到newId中
			String relationSql = "select res_type,source_uuid from resource_relations where resource_target_type='instructionalobjectives' and target = '"+oldId+"'";
			List<Map<String,Object>> l = jt.queryForList(relationSql);
			if(CollectionUtils.isNotEmpty(l)){
				for (Map<String, Object> map : l) {
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
					educationRelationModels.add(tmp);
				}
			}
			educationRelationService.createRelation(educationRelationModels, false);
			return instructionalObjectiveModel;
			
		}
    }

	/**
	 * 根据教学目标id查询出与之关联的章节信息id
	 * 分两种情况：
	 * 1.教学目标与章节直接关联
	 * 2.教学目标与课时关联，课时与章节关联
	 * @param id
	 * @return
     */
	@Override
	public List<Map<String, Object>> getChapterRelationById(String id) {
		try {
			LinkedList<Map<String, Object>> pathList = new LinkedList<>();//教材章节路径list
			Set<String> chapterIdSet = new HashSet<>();//与教学目标相关联的chapterId集合，包含直接与chapter关联的和与lesson关联然后再与chapter关联。去重。
			//查询与教学目标直接关联的chapter的关系，并且把关系当中的chapterId放到set里面。begin
			List<ResourceRelation> fromChapterToInstructionalObjectiveRelationList = resourceRelationApiService.getByResTypeAndTargetTypeAndTargetId(IndexSourceType.ChapterType.getName(), IndexSourceType.InstructionalObjectiveType.getName(), id);
			if (fromChapterToInstructionalObjectiveRelationList != null && fromChapterToInstructionalObjectiveRelationList.size() > 0) {
				for (ResourceRelation rr : fromChapterToInstructionalObjectiveRelationList) {
					chapterIdSet.add(rr.getSourceUuid());
				}
			}
			//查询与教学目标直接关联的chapter的关系，并且把关系当中的chapterId放到set里面。end

			//查询与教学目标间接关联（通过lesson关联到chapter）的chapter的关系，并把关系当中的chapterId放到set里面。begin
			List<ResourceRelation> lessonRelationList = resourceRelationApiService.getByResTypeAndTargetTypeAndTargetId(IndexSourceType.LessonType.getName(), IndexSourceType.InstructionalObjectiveType.getName(), id);
			for (ResourceRelation rr : lessonRelationList) {
				String lessonId = rr.getSourceUuid();
				List<ResourceRelation> fromChaptersToLessonsRelationList = resourceRelationApiService.getByResTypeAndTargetTypeAndTargetId(IndexSourceType.ChapterType.getName(), IndexSourceType.LessonType.getName(), lessonId);
				if (fromChaptersToLessonsRelationList != null && fromChaptersToLessonsRelationList.size() > 0) {
					for (ResourceRelation rr2 : fromChaptersToLessonsRelationList) {
						chapterIdSet.add(rr2.getSourceUuid());
					}
				}
			}
			//查询与教学目标间接关联（通过lesson关联到chapter）的chapter的关系，并把关系当中的chapterId放到set里面。end


			if (chapterIdSet != null && chapterIdSet.size() > 0) {
				for (String chapterId : chapterIdSet) {
					if (chapterId != null && !"".equals(chapterId)) {
						//循环获取章节，以及该节的父章节的信息。begin
						LinkedList<ChapterModel> chapterList = new LinkedList<>();//存储每个章节及其父章节一级级往上查找的信息。
						ChapterModel cm;
						do {
							cm = chapterService.getChapterDetail(chapterId);
							if (cm != null) {
								chapterList.add(cm);
								chapterId = cm.getParent();
							}
						}
						while (cm != null && !cm.getParent().equals(cm.getTeachingMaterial()));
						//循环获取章节，以及该节的父章节的信息。end
						TeachingMaterial teachingMaterial = teachingMaterialService.getById(cm.getTeachingMaterial());//获取教材信息

						//拼接返回的字符串。begin
						String pathStr = teachingMaterial.getIdentifier();
						String textStr = teachingMaterial.getTitle();
						String chapterIdStr = "";
						if (chapterList != null && chapterList.size() > 0) {
							for (int i = chapterList.size() - 1; i >= 0; i--) {
								ChapterModel chapterModel = new ChapterModel();
								chapterModel = chapterList.get(i);
								if (i == 0) {
									chapterIdStr = chapterModel.getIdentifier();
								}
								pathStr += "/" + chapterModel.getIdentifier();
								textStr += "/" + chapterModel.getTitle();
							}
						}
						LinkedHashMap<String, Object> pathItem = new LinkedHashMap<>();
						pathItem.put("path", pathStr);
						pathItem.put("text", textStr);
						pathItem.put("chapter_id", chapterIdStr);
						pathList.add(pathItem);
						//拼接返回的字符串。end
					}
				}
			}
			return pathList;
		} catch (EspStoreException e) {
			LOG.error("根据教学目标获取章节关联关系出错！", e);
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getLocalizedMessage());
		}
	}

}
