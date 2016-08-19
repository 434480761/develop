package nd.esp.service.lifecycle.services.elasticsearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperation;
import nd.esp.service.lifecycle.educommon.dao.impl.NDResourceDaoImpl;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResEducationalModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResRightModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.models.TechnologyRequirementModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TmExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.models.v06.QuestionExtPropertyModel;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

@Service
public class ES_SearchImpl implements ES_Search {

	static final private JacksonCustomObjectMapper ObjectMapper = new JacksonCustomObjectMapper();

	@Autowired
	private EsResourceOperation esResourceOperation;

	@Override
	public ListViewModel<ResourceModel> searchByES(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size) {
		// deal with orderMap
		List<SortBuilder> sortBuilders = new ArrayList<SortBuilder>();
		if (CollectionUtils.isEmpty(orderMap)) {
			SortBuilder sortBuilder = SortBuilders
					.fieldSort(
							ES_SearchField.lc_create_time
									.getNameInEsWithNested())
					.order(SortOrder.DESC)
					.setNestedPath(ES_Field.life_cycle.toString());
			sortBuilders.add(sortBuilder);
		} else {
			// bylsm 得注意下顺序，map, 实际为linkedhashmap
			for (Map.Entry<String, String> entry : orderMap.entrySet()) {
				ES_SearchField searchField = ES_SearchField.fromString(entry
						.getKey());
				FieldSortBuilder sortBuilder = SortBuilders
						.fieldSort(searchField.getNameInEsWithNested());
				//need specific set nested path (in version 2.3.1)
				if (searchField.getParent() != ES_SearchField.root) {
					sortBuilder.setNestedPath(searchField.getParent()
							.getNameInEsWithNested());
				}
				if (PropOperationConstant.OP_DESC.equalsIgnoreCase(entry
						.getValue())) {
					sortBuilder.order(SortOrder.DESC);
				} else {
					sortBuilder.order(SortOrder.ASC);
				}
				sortBuilders.add(sortBuilder);
			}
		}
		return _searchByEla(resType, includes, params, from, size, sortBuilders);
	}

	private ListViewModel<ResourceModel> _searchByEla(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> prefieldConditions,
			int from, int size, List<SortBuilder> sortBuilders) {
		Map<ES_SearchField, Map<ES_SearchField, Map<String, List<String>>>> postFieldConditions = new HashMap<ES_SearchField, Map<ES_SearchField, Map<String, List<String>>>>();
		// del with enable
		if (prefieldConditions == null) {
			prefieldConditions = new HashMap<String, Map<String, List<String>>>();
		}
		List<String> enableEqValue = new ArrayList<String>();
		enableEqValue.add("true");
		Map<String, List<String>> enableCondition = new HashMap<String, List<String>>();
		enableCondition.put(PropOperationConstant.OP_EQ, enableEqValue);
		prefieldConditions.put(ES_SearchField.lc_enable.toString(),
				enableCondition);

		for (Map.Entry<String, Map<String, List<String>>> field : prefieldConditions
				.entrySet()) {

			ES_SearchField key = ES_SearchField.fromString(field.getKey());
			if (key == null) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/elasticsearch/notSupportField",
						"LC/elasticsearch/notSupportField");// bylsm
			}
			ES_SearchField parent = key.getParent();
			Map<ES_SearchField, Map<String, List<String>>> objectConditionMap = postFieldConditions
					.get(parent);
			if (objectConditionMap == null) {
				objectConditionMap = new HashMap<ES_SearchField, Map<String, List<String>>>();
				postFieldConditions.put(parent, objectConditionMap);
			}

			// filter coverage and category(code path)
			if (key == ES_SearchField.coverages) {
				objectConditionMap.put(key, filterCoverageOP(field.getValue()));
			} else if (parent == ES_SearchField.category_list) {
				objectConditionMap.put(key, filterCategoryOP(field.getValue()));
			} else {
				objectConditionMap.put(key, field.getValue());
			}

		}

		BoolQueryBuilder finalQueryBuilder = QueryBuilders.boolQuery();
		for (Map.Entry<ES_SearchField, Map<ES_SearchField, Map<String, List<String>>>> objectCondition : postFieldConditions
				.entrySet()) {
			ES_SearchField objectKey = objectCondition.getKey();// root,default,cg,cr,lc
			BoolQueryBuilder objecdQueryBuilder = QueryBuilders.boolQuery();
			for (Map.Entry<ES_SearchField, Map<String, List<String>>> field : objectCondition
					.getValue().entrySet()) {
				ES_SearchField esFiledName = field.getKey();
				Map<String, List<String>> esFiledOpMap = field.getValue();
				if (esFiledOpMap == null || esFiledOpMap.isEmpty()) {
					continue;
				}
				if (esFiledOpMap.get(ES_OP.in.toString()) != null
						&& esFiledOpMap.get(ES_OP.eq.toString()) != null) {
					changeEqToIn(esFiledOpMap);
				}
				BoolQueryBuilder fieldBoolQueryBuilder = QueryBuilders
						.boolQuery();
				for (Map.Entry<String, List<String>> opEntry : esFiledOpMap
						.entrySet()) {
					ES_OP es_OP = ES_OP.fromString(opEntry.getKey());
					if (es_OP == null) {
						continue;
					}
					List<String> values = opEntry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						continue;
					}
					fieldBoolQueryBuilder.must(es_OP.buildQuery(esFiledName,
							values));
				}
				objecdQueryBuilder.must(fieldBoolQueryBuilder);
			}
			if (objectKey != ES_SearchField.category_list) {
				finalQueryBuilder.must(objectKey.build(objecdQueryBuilder));
			} else {
				finalQueryBuilder.must(objecdQueryBuilder);
			}

		}

		SearchHits searchHits = esResourceOperation.search(resType,
				finalQueryBuilder, from, size, sortBuilders);

		try {
			return changeToResourceModel(resType, includes, searchHits);
		} catch (IOException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/ParseResult/Fail", e.getLocalizedMessage());
		} catch (ParseException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/ParseResult/Fail", e.getLocalizedMessage());
		}
	}

	/**
	 * only support ne, eq
	 * 
	 * @param value
	 * @return
	 */
	private Map<String, List<String>> filterCategoryOP(
			Map<String, List<String>> value) {
		if (value != null) {
			value.remove(PropOperationConstant.OP_LIKE);
			value.remove(PropOperationConstant.OP_IN);
		}
		return value;
	}

	/**
	 * only support ne,in
	 * 
	 * @param value
	 * @return
	 */
	private Map<String, List<String>> filterCoverageOP(
			Map<String, List<String>> value) {
		if (value != null) {
			value.remove(PropOperationConstant.OP_LIKE);
			value.remove(PropOperationConstant.OP_EQ);
		}
		return value;
	}

	@SuppressWarnings({ "rawtypes" })
	private ListViewModel<ResourceModel> changeToResourceModel(String resType,
			List<String> includes, SearchHits searchHits)
			throws JsonParseException, JsonMappingException, IOException,
			ParseException {
		ListViewModel<ResourceModel> result = new ListViewModel<ResourceModel>();
		result.setItems(new ArrayList<ResourceModel>());
		if (searchHits == null) {
			result.setTotal(0L);
			return result;

		} else if (searchHits.getHits() == null
				|| searchHits.getHits().length == 0) {
			result.setTotal(searchHits.getTotalHits());
			return result;
		}
		result.setTotal(searchHits.getTotalHits());

		boolean isNeedTI = includes.contains(IncludesConstant.INCLUDE_TI);
		boolean isNeedCG = includes.contains(IncludesConstant.INCLUDE_CG);
		boolean isNeedEDU = includes.contains(IncludesConstant.INCLUDE_EDU);
		boolean isNeedCR = includes.contains(IncludesConstant.INCLUDE_CR);
		boolean isNeedLC = includes.contains(IncludesConstant.INCLUDE_LC);

		for (SearchHit hit : searchHits.getHits()) {
			Map<String, Object> source = hit.getSource();
			ResourceModel item = initModelWithExt(resType, hit);
			item.setIdentifier((String) source.get(ES_Field.identifier
					.toString()));
			item.setDescription((String) source.get(ES_Field.description
					.toString()));
			item.setLanguage((String) (source.get(ES_Field.language.toString())));
			item.setTitle((String) (source.get(ES_Field.title.toString())));
			item.setCustomProperties((String) (source
					.get(ES_Field.custom_properties.toString())));
			item.setKeywords(stringJsonToList(source.get(ES_Field.keywords
					.toString())));

			if (NDResourceDaoImpl.isNeedPreview(resType)) {
				item.setPreview(stringJsonToMap(source.get(ES_Field.preview
						.toString())));
			}

			item.setNdresCode((String) source.get(ES_Field.ndres_code
					.toString()));

			item.setTags(stringJsonToList(source.get(ES_Field.tags.toString())));

			// lc
			if (isNeedLC && source.get(ES_Field.life_cycle.toString()) != null) {
				Map lifecycleMap = (Map) source.get(ES_Field.life_cycle
						.toString());
				ResLifeCycleModel lifeCycle = new ResLifeCycleModel();
				lifeCycle.setCreateTime(new Date(
						adapterLongForDate(lifecycleMap
								.get(ES_Field.create_time.toString()))));
				lifeCycle.setLastUpdate(new Date(
						adapterLongForDate(lifecycleMap
								.get(ES_Field.last_update.toString()))));
				lifeCycle.setCreator((String) (lifecycleMap
						.get(ES_Field.creator.toString())));
				lifeCycle.setEnable(adaptBooleanToBool((lifecycleMap
						.get(ES_Field.enable.toString()))));
				lifeCycle.setProvider((String) (lifecycleMap
						.get(ES_Field.provider.toString())));
				lifeCycle.setProviderSource((String) (lifecycleMap
						.get(ES_Field.provider_source.toString())));
				lifeCycle.setProviderMode((String) (lifecycleMap
						.get(ES_Field.provider_mode.toString())));
				lifeCycle.setPublisher((String) (lifecycleMap
						.get(ES_Field.publisher.toString())));
				lifeCycle.setStatus((String) (lifecycleMap.get(ES_Field.status
						.toString())));
				lifeCycle.setVersion((String) (lifecycleMap
						.get(ES_Field.version.toString())));
				item.setLifeCycle(lifeCycle);
			}

			// edu
			if (isNeedEDU
					&& source.get(ES_Field.education_info.toString()) != null) {
				Map eduMap = (Map) source.get(ES_Field.education_info
						.toString());
				ResEducationalModel educationInfo = new ResEducationalModel();
				educationInfo.setAgeRange((String) (eduMap
						.get(ES_Field.age_range.toString())));
				educationInfo.setContext((String) (eduMap.get(ES_Field.context
						.toString())));
				educationInfo.setDifficulty((String) (eduMap
						.get(ES_Field.difficulty.toString())));
				educationInfo.setEndUserType((String) (eduMap
						.get(ES_Field.end_user_type.toString())));
				// Integer to int
				educationInfo.setInteractivity(adaptIntegerToInt(((eduMap
						.get(ES_Field.interactivity.toString())))));
				educationInfo.setInteractivityLevel(adaptIntegerToInt(((eduMap
						.get(ES_Field.interactivity_level.toString())))));
				educationInfo.setLanguage((String) (eduMap
						.get(ES_Field.language.toString())));// bylsm edu
				educationInfo.setLearningTime((String) (eduMap
						.get(ES_Field.learning_time.toString())));

				// change Integer to Long
				Integer semantic_density = (Integer) (eduMap
						.get(ES_Field.semantic_density.toString()));
				educationInfo.setSemanticDensity(Long
						.valueOf(semantic_density == null ? 0
								: semantic_density));
				educationInfo.setDescription(stringJsonToMap(eduMap
						.get(ES_Field.description.toString())));// bylsm edu
				item.setEducationInfo(educationInfo);
			}

			// cr
			if (isNeedCR && source.get(ES_Field.copyright.toString()) != null) {
				Map crMap = (Map) source.get(ES_Field.copyright.toString());
				ResRightModel copyright = new ResRightModel();
				copyright.setAuthor((String) (crMap.get(ES_Field.author
						.toString())));
				copyright.setDescription((String) (crMap
						.get(ES_Field.description.toString())));// bylsm
																// copyright
				copyright.setRight((String) (crMap.get(ES_Field.right
						.toString())));
				
				if(crMap.get(ES_Field.has_right.toString()) == null){
					copyright.setHasRight(null);
				}else{
					copyright.setHasRight(adaptBooleanToBool((crMap
							.get(ES_Field.has_right.toString()))));
				}
				if(crMap.get(ES_Field.right_start_date.toString()) == null){
					copyright.setRightStartDate(null);
				}else{
					copyright.setRightStartDate(new BigDecimal(
							adapterLongForDate(crMap
									.get(ES_Field.right_start_date.toString()))));
				}
				if(crMap.get(ES_Field.right_end_date.toString()) == null){
					copyright.setRightEndDate(null);
				}else{
					copyright.setRightEndDate(new BigDecimal(
							adapterLongForDate(crMap
									.get(ES_Field.right_end_date.toString()))));
				}
				
				item.setCopyright(copyright);
			}
			// cg
			if (isNeedCG
					&& source.get(ES_Field.category_list.toString()) != null) {
				List<ResClassificationModel> categoryList = new ArrayList<ResClassificationModel>();
				List list = (List) source
						.get(ES_Field.category_list.toString());
				for (Object object : list) {
					ResClassificationModel resClassificationModel = new ResClassificationModel();
					Map cg = (Map) object;
					resClassificationModel.setIdentifier((String) cg
							.get(ES_Field.identifier.toString()));
					resClassificationModel.setTaxonpath((String) cg
							.get(ES_Field.taxonpath.toString()));
					resClassificationModel.setTaxonname((String) cg
							.get(ES_Field.taxonname.toString()));
					resClassificationModel.setTaxoncode((String) cg
							.get(ES_Field.taxoncode.toString()));
					resClassificationModel.setTaxoncodeId((String) cg
							.get(ES_Field.taxoncode_id.toString()));
					resClassificationModel.setShortName((String) cg
							.get(ES_Field.short_name.toString()));
					resClassificationModel.setCategoryCode((String) cg
							.get(ES_Field.category_code.toString()));
					resClassificationModel.setCategoryName((String) cg
							.get(ES_Field.category_name.toString()));
					categoryList.add(resClassificationModel);
				}

				item.setCategoryList(categoryList);
			}
			if (isNeedTI
					&& source.get(ES_Field.tech_info_list.toString()) != null) {
				// List<Tech>
				List<ResTechInfoModel> techInfoList = new ArrayList<ResTechInfoModel>();
				List list = (List) source.get(ES_Field.tech_info_list
						.toString());
				for (Object object : list) {
					ResTechInfoModel resTechInfoModel = new ResTechInfoModel();
					Map ti = (Map) object;
					resTechInfoModel.setFormat((String) ti.get(ES_Field.format
							.toString()));
					resTechInfoModel.setSize(adaptIntegerToInt(ti
							.get(ES_Field.size.toString())));
					resTechInfoModel.setLocation((String) ti
							.get(ES_Field.location.toString()));

					resTechInfoModel.setMd5((String) ti.get(ES_Field.md5
							.toString()));
					resTechInfoModel.setTitle((String) ti.get(ES_Field.title
							.toString()));
					resTechInfoModel.setEntry((String) ti.get(ES_Field.entry
							.toString()));
					resTechInfoModel.setSecureKey((String) ti
							.get(ES_Field.secure_key.toString()));
					resTechInfoModel.setPrintable(adaptBooleanToBool((ti
							.get(ES_Field.printable.toString()))));
					List<TechnologyRequirementModel> technologyRequirementModels = new ArrayList<TechnologyRequirementModel>();
					if (ti.get(ES_Field.requirements.toString()) != null) {
						List requirementList = (List) ti
								.get(ES_Field.requirements.toString());
						for (Object rObject : requirementList) {
							Map reMap = (Map) rObject;
							TechnologyRequirementModel requirementModel = new TechnologyRequirementModel();
							requirementModel.setIdentifier((String) reMap
									.get(ES_Field.identifier.toString()));
							requirementModel.setType((String) reMap
									.get(ES_Field.type.toString()));
							requirementModel.setName((String) reMap
									.get(ES_Field.name.toString()));
							requirementModel.setMinVersion((String) reMap
									.get(ES_Field.min_version.toString()));
							requirementModel.setMaxVersion((String) reMap
									.get(ES_Field.max_version.toString()));
							requirementModel.setInstallation((String) reMap
									.get(ES_Field.installation.toString()));
							requirementModel
									.setInstallationFile((String) reMap
											.get(ES_Field.installation_file
													.toString()));
							requirementModel.setValue((String) reMap
									.get(ES_Field.value.toString()));
							technologyRequirementModels.add(requirementModel);
						}

						resTechInfoModel
								.setRequirements(technologyRequirementModels);
					}

					techInfoList.add(resTechInfoModel);
				}

				item.setTechInfoList(techInfoList);
			}
			result.getItems().add(item);

		}

		return result;
	}

	private long adapterLongForDate(Object time) {
		if (time == null) {
			return 0L;
		} else {
			return Long.parseLong(String.valueOf(time));// exist invalid data ,
														// last_update maybe int
														// (dev lesson
														// dd03931a-b8f7-45dc-a36d-e0161919a757)
		}
	}

	private int adaptIntegerToInt(Object interactivity) {
		return interactivity == null ? 0 : (Integer) interactivity;
	}

	// private long adaptLongTolong(Object interactivity) {
	// return interactivity == null ? 0L : (Long) interactivity;
	// }

	private ResourceModel initModelWithExt(String resType, SearchHit hit)
			throws JsonParseException, JsonMappingException, IOException,
			ParseException {
		Map<String, Object> source = hit.getSource();
		@SuppressWarnings("rawtypes")
		Map extProMap = (Map) source.get(ES_Field.ext_properties.toString());
		ResourceNdCode resourceNdCode = ResourceNdCode.fromString(resType);
		switch (resourceNdCode) {
		case questions:
			QuestionModel questionModel = new QuestionModel();
			QuestionExtPropertyModel questionExtProperties = new QuestionExtPropertyModel();
			// 12个属性
			questionExtProperties.setAnswer(stringJsonToMap(extProMap
					.get(ES_Field.answer.toString())));
			questionExtProperties.setAutoRemark(adaptBooleanToBool(extProMap
					.get(ES_Field.auto_remark.toString())));
			questionExtProperties.setCriterion(stringJsonToMap(extProMap
					.get(ES_Field.criterion.toString())));
			questionExtProperties.setDiscrimination(doubleToFloat(extProMap
					.get(ES_Field.discrimination.toString())));
			questionExtProperties.setExposalDate(adaptStringToDate(extProMap
					.get(ES_Field.exposal_date.toString())));
			questionExtProperties.setExtDifficulty(doubleToFloat(extProMap
					.get(ES_Field.ext_difficulty.toString())));
			questionExtProperties.setItemContent(stringJsonToMap(extProMap
					.get(ES_Field.item_content.toString())));
			questionExtProperties
					.setModifiedDifficulty((doubleToFloat(extProMap
							.get(ES_Field.modified_difficulty.toString()))));
			questionExtProperties
					.setModifiedDiscrimination((doubleToFloat(extProMap
							.get(ES_Field.modified_discrimination.toString()))));
			questionExtProperties.setScore(doubleToFloat(extProMap
					.get(ES_Field.score.toString())));
			questionExtProperties.setSecrecy(adaptIntegerToInt(extProMap
					.get(ES_Field.secrecy.toString())));
			questionExtProperties.setUsedTime(adaptIntegerToInt(extProMap
					.get(ES_Field.used_time.toString())));
			questionModel.setExtProperties(questionExtProperties);
			return questionModel;
		case ebooks:
			EbookModel ebookModel = new EbookModel();
			EbookExtPropertiesModel ebookExtPropertiesModel = new EbookExtPropertiesModel();
			ebookExtPropertiesModel.setAttachments(stringJsonToList(extProMap
					.get(ES_Field.attachments.toString())));
			ebookExtPropertiesModel.setCriterion((String) extProMap
					.get(ES_Field.criterion.toString()));
			ebookExtPropertiesModel.setIsbn((String) extProMap
					.get(ES_Field.isbn.toString()));
			ebookModel.setExtProperties(ebookExtPropertiesModel);
			return ebookModel;
		case teachingmaterials:
		case guidancebooks:
			TeachingMaterialModel teachingMaterialModel = new TeachingMaterialModel();
			TmExtPropertiesModel tmExtPropertiesModel = new TmExtPropertiesModel();
			tmExtPropertiesModel.setAttachments(stringJsonToList(extProMap
					.get(ES_Field.attachments.toString())));
			tmExtPropertiesModel.setCriterion((String) extProMap
					.get(ES_Field.criterion.toString()));
			tmExtPropertiesModel.setIsbn((String) extProMap.get(ES_Field.isbn
					.toString()));
			teachingMaterialModel.setExtProperties(tmExtPropertiesModel);
			return teachingMaterialModel;
		default:
			return new ResourceModel();
		}
	}

	private boolean adaptBooleanToBool(Object object) {
		if (object == null) {
			return false;
		}
		return (Boolean) object;
	}

	private Date adaptStringToDate(Object object) throws ParseException {
		if (object == null) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return simpleDateFormat.parse((String) object);
	}

	private float doubleToFloat(Object object) {
		if (object == null) {
			return 0.0f;
		} else {
			return ((Double) object).floatValue();
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> stringJsonToList(Object object)
			throws JsonParseException, JsonMappingException, IOException {
		if (object == null) {
			return null;
		}
		return (List<String>) ObjectMapper.readValue((String) (object),
				new TypeReference<List<String>>() {
				});
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> stringJsonToMap(Object object)
			throws JsonParseException, JsonMappingException, IOException {
		if (object == null) {
			return null;
		}
		return (Map<String, String>) ObjectMapper.readValue((String) (object),
				new TypeReference<Map<String, String>>() {
				});
	}

	private static void changeEqToIn(Map<String, List<String>> esFiledOpMap) {
		List<String> inValuesList = esFiledOpMap.get(ES_OP.in.toString());
		inValuesList.addAll(esFiledOpMap.get(ES_OP.eq.toString()));
		esFiledOpMap.remove(ES_OP.eq.toString());
	}

}
