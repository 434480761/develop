package nd.esp.service.lifecycle.services.instructionalobjectives.v06.impls;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
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
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;

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

	@Override
	public String getInstructionalObjectiveTitle(String id) {
		List<String> ids = new ArrayList<>();
		ids.add(id);
		return getInstructionalObjectiveTitle(ids).get(id);
	}

	@Override
	public Map<String, String> getInstructionalObjectiveTitle(Collection<String> ids) {

		if (0 == ids.size()) {
			return Collections.emptyMap();
		}

		try {
			Collection<String> idString = Collections2.transform(ids, new Function<String, String>() {
				@Nullable
				@Override
				public String apply(@Nullable String s) {
					return String.format("\"%s\"", s);
				}
			});

			// 查找教学目标关联的教学目标类型
			String SQLQueryInstructionalObjectiveType = String.format("SELECT ndr.identifier AS id,ndr.title AS title,ndr.description AS description,rr.target AS target from `ndresource` AS ndr" +
					" inner join `resource_relations` AS rr on ndr.identifier=rr.source_uuid and rr.res_type=\"assets\" and rr.resource_target_type=\"instructionalobjectives\"" +
					" WHERE ndr.identifier in ( select resource from `resource_categories` where taxOnCode='$RA0503') AND rr.target in(%s)", StringUtils.join(idString, ","));
			List<Map<String, Object>> instructionalObjectiveTypeList = jt.queryForList(SQLQueryInstructionalObjectiveType);
			// 以教学目标Id为key的查询结果
			Map<String, Map<String, Object>> instructionalObjective2TypeMap = new HashMap<>();
			if(CollectionUtils.isNotEmpty(instructionalObjectiveTypeList)){
				for (Map<String, Object> map : instructionalObjectiveTypeList) {
					String target = (String) map.get("target");
					instructionalObjective2TypeMap.put(target, map);
				}
			}

			// 查找教学目标关联的知识点
			String SQLQueryKnowledges = String.format("SELECT ndr.identifier AS id,ndr.title AS title,ndr.description as description,rr.target AS target,rr.order_num as orderNum from `ndresource` AS ndr" +
					" inner join `resource_relations` AS rr on ndr.identifier=rr.source_uuid and rr.res_type=\"knowledges\" and rr.resource_target_type=\"instructionalobjectives\"" +
					" WHERE rr.target in (%s)", StringUtils.join(idString, ","));
			List<Map<String, Object>> knowledgesList = jt.queryForList(SQLQueryKnowledges);
			// 以教学目标Id为key的查询结果
			Map<String, List<Map<String, Object>>> knowledgesMap = new HashMap<>();
			if(CollectionUtils.isNotEmpty(knowledgesList)) {
				for (Map<String, Object> map : knowledgesList) {
					String target = (String) map.get("target");
					List<Map<String, Object>> list = knowledgesMap.get(target);
					if (null == list) {
						list = new ArrayList<>();
						knowledgesMap.put(target, list);
					}
					list.add(map);
					// 多个知识点有排序
					Collections.sort(list, new Comparator<Map<String, Object>>() {
						@Override
						public int compare(Map<String, Object> o1, Map<String, Object> o2) {
							Integer order1 = (Integer) o1.get("orderNum");
							Integer order2 = (Integer) o2.get("orderNum");
							return order1 - order2;
						}
					});
				}
			}

			Map<String, String> results = new HashMap<>();

			for (String id : ids) {
				Map<String, Object> instructionalObjective2Type = instructionalObjective2TypeMap.get(id);
				List<Map<String, Object>> knowledges = knowledgesMap.get(id);
				if (CollectionUtils.isEmpty(knowledges) || CollectionUtils.isEmpty(instructionalObjective2Type)) {
					continue;
				}
				// 获取多个知识点的title
				Collection<String> knowledgesTitle = Collections2.transform(knowledges, new Function<Map<String, Object>, String>() {
					@Nullable
					@Override
					public String apply(Map<String, Object> stringObjectMap) {
						return (String)stringObjectMap.get("title");
					}
				});

				String typeString = (String) instructionalObjective2Type.get("description");
				results.put(id, toInstructionalObjectiveTitle(typeString, knowledgesTitle));
			}

			return results;
		} catch (DataAccessException e) {
			LOG.error("根据教学目标获取Title出错！", e);
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getLocalizedMessage());
		}
	}

	@Override
	public ListViewModel<InstructionalObjectiveModel> getUnRelationInstructionalObjective(String knowledgeTypeCode, String instructionalObjectiveTypeId, String unrelationCategory, String limit) {

		try {

			IndexSourceType[] unrelationType = new IndexSourceType[]{IndexSourceType.ChapterType,IndexSourceType.LessonType};
			Collection<String> unrelationCategoryString = new ArrayList<>();
			for (IndexSourceType indexSourceType : unrelationType) {
				if (!indexSourceType.getName().equals(unrelationCategory)) {
					unrelationCategoryString.add(String.format("res_type=\"%s\"",indexSourceType.getName()));
				}
			}

			String SQLFmt = "SELECT %s from `ndresource` as ndr LEFT join  `resource_relations` as rr ON ndr.identifier=rr.target" +
					" AND rr.enable=1 AND rr.resource_target_type=\"instructionalobjectives\" AND (" + StringUtils.join(unrelationCategoryString, " or ") + ")" +
					" WHERE ndr.enable=1 AND rr.res_type is null\n";
			if (org.apache.commons.lang3.StringUtils.isNotBlank(knowledgeTypeCode)) {
				SQLFmt += String.format(" and ndr.identifier in (SELECT target FROM `resource_relations` WHERE ENABLE=1 and res_type=\"knowledges\"" +
						" AND resource_target_type=\"instructionalobjectives\" AND source_uuid in (select resource from `resource_categories` WHERE taxonCode=\"%s\"))", knowledgeTypeCode);
			}
			if (org.apache.commons.lang3.StringUtils.isNotBlank(instructionalObjectiveTypeId)) {
				SQLFmt += String.format("and ndr.identifier in (SELECT target from `resource_relations` WHERE ENABLE=1 AND resource_target_type=\"instructionalobjectives\"" +
						" and source_uuid=\"%s\")", instructionalObjectiveTypeId);
			}

			Integer[] limits = ParamCheckUtil.checkLimit(limit);

			// result
			final List<InstructionalObjectiveModel> results = new ArrayList<>();
			jt.query(String.format(SQLFmt + String.format(" LIMIT %d,%d", limits[0], limits[1]), "ndr.*"), new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet resultSet) throws SQLException {
					Collection<String> array = new ArrayList<>();

					ResultSetMetaData rsmd = resultSet.getMetaData();
					int columnCount = rsmd.getColumnCount();

					for (int i = 1; i <= columnCount; i++) {
						String columnName = JdbcUtils.lookupColumnName(rsmd, i);
						Field field = ReflectionUtils.findField(InstructionalObjectiveModel.class, StringUtils.toCamelCase(columnName));
						if (null != field) {
							if (String.class == field.getType()) {
								String content = resultSet.getString(i);
								if (StringUtils.isNotEmpty(content) && content.contains("\"")) {
									content = StringUtils.replace(content, "\"", "\\\"");
								}
								StringUtils.hasText("\"");
								array.add(String.format("\"%s\":\"%s\"", columnName, content));
							} else {
								array.add(String.format("\"%s\":%s", columnName, JdbcUtils.getResultSetValue(resultSet, i)));
							}
						}
					}

					String jsonString = String.format("{%s}", StringUtils.join(array, ","));

					try {
						JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
						InstructionalObjectiveModel instructionalObjectiveModel = mapper.readValue(jsonString, InstructionalObjectiveModel.class);

						results.add(instructionalObjectiveModel);
					} catch (IOException e) {
						LOG.error("查询未关联教学目标转换InstructionalObjectiveModel出错！", e);
					}
				}
			});
			// count
			Map<String, Object> count = jt.queryForMap(String.format(SQLFmt, "count(*) as count"));

			ListViewModel<InstructionalObjectiveModel> listViewModel = new ListViewModel<>();
			listViewModel.setLimit(limit);
			listViewModel.setItems(results);
			listViewModel.setTotal((Long) count.get("count"));

			return listViewModel;
		} catch (Exception e) {
			LOG.error("查询未关联教学目标出错！", e);
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getLocalizedMessage());
		}
	}

	/***
	 * 根据教学目标类型与知识点拼接知识点title
	 * @param typeString 教学目标类型描述
	 * @param knowledgeTitle 知识点title
	 * @return 拼接后的字符串
	 */
	private String toInstructionalObjectiveTitle(String typeString, Collection<String> knowledgeTitle) {
		Pattern pattern = Pattern.compile("<span.*?>.*?</span>");
		String[] split = pattern.split(typeString);
		String[] knowledges = knowledgeTitle.toArray(new String[knowledgeTitle.size()]);

		StringBuilder sb = new StringBuilder();
		for (int i = 0;i < split.length;i++) {
			sb.append(split[i]);
			sb.append(knowledges.length > i ? knowledges[i]:"");
		}

		return sb.toString();
	}
}
