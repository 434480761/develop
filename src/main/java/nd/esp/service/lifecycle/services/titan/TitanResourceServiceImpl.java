package nd.esp.service.lifecycle.services.titan;

import java.util.*;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TitanResourceServiceImpl implements TitanResourceService {
	private static String s_primaryCategory ;
	private static Integer s_page;
	private static Integer s_totalPage;
	private static final Logger LOG = LoggerFactory
			.getLogger(TitanResourceServiceImpl.class);
	@Autowired
	private CoverageDao coverageDao;

	@Autowired
	private NDResourceDao ndResourceDao;

	@Autowired
	private EducationRelationDao educationRelationdao;

	@Autowired
	private TitanChapterRelationRepository titanChapterRelationRepository;

	@Autowired
	private TitanKnowledgeRelationRepository titanKnowledgeRelationRepository;

	@Autowired
	private ResourceRelation4QuestionDBRepository resourceRelation4QuestionDBRepository;

	@Autowired
	private ResourceRelationRepository resourceRelationRepository;

	@Autowired
	private KnowledgeRelationRepository knowledgeRelationRepository;

	@Autowired
	private TitanCommonRepository titanCommonRepository;

	@Autowired
	private TitanImportRepository titanImportRepository;

	@Autowired
	private CategoryDataRepository categoryDataRepository;


	@Autowired
	private TitanUpdateDataRepository titanUpdateDataRepository;

	@Autowired
	@Qualifier(value = "defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Override
	public long importData4Script(String primaryCategory) {
		AbstractPageQuery abstractPageQuery = new ImprotData4ScriptPageQuery();
		abstractPageQuery.doing(primaryCategory);
		return 0;
	}

	@Override
	public long createChapterRelation() {
		AbstractPageQuery abstractPageQuery = new CreateRelation4Chapter();

		return abstractPageQuery.doing("chapters");
	}

	@Override
	public long createKnowledgeRealtion() {
		AbstractPageQuery abstractPageQuery = new CreateRelation4Knowledge();
		return abstractPageQuery.doing("knowledges");
	}

	@Override
	public void updateChapterRelation() {
		AbstractPageQuery abstractPageQuery = new UpdateDataPageQuery();
		abstractPageQuery.doing("chapters");
	}

	@Override
	public void updateKnowledgeRelation() {
		AbstractPageQuery abstractPageQuery = new UpdateDataPageQuery();
		abstractPageQuery.doing("knowledges");
	}

	@Override
	public void timeTaskImport(Integer page, String type) {
		TimeTaskPageQuery timeTaskPageQuery = new TimeTaskPageQuery4Import(page, type);
		timeTaskPageQuery.schedule();
	}

	@Override
	public long importAllRelation() {
		long size = 0L;
		AbstractPageQueryRelation abstractPageQueryRelation = new AbstractPageQueryRelationCreate();
		size =  abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);
		size = size + abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
		return size;
	}

	@Override
	public void repairAllRelation() {
		AbstractPageQueryRelation abstractPageQueryRelation = new AbstractPageQueryRelationRepair();
		abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);
		abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
	}

	@Override
	public void repairOne(String primaryCategory, String id) {

		EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
		Education educationOld = null;
		try {
			educationOld = (Education) espRepository.get(id);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}

		if (educationOld == null){
			return;
		}

		List<Education> educations = new ArrayList<>();
		educations.add(educationOld);

		repairData(educations, primaryCategory);
	}

	@Override
	public void timeTaskRepair(Integer page, String type) {
		TimeTaskPageQuery timeTaskPageQuery = new TimeTaskPageQuery4Repair(page, type);
		timeTaskPageQuery.schedule();
	}

	@Override
	public long importKnowledgeRelation() {
		return pageQueryKnowledgeRelation(knowledgeRelationRepository);
	}

	@Override
	public void timeTaskImport4Update(Integer page, String type) {
		TimeTaskPageQuery4Update timeTaskPageQuery4Update = new TimeTaskPageQuery4Update(page, type);
		timeTaskPageQuery4Update.schedule();
	}

	@Override
	public void importOneData4Script(String primaryCategory, String id) {
		EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
		Education education = null;
		try {
			education = (Education) espRepository.get(id);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}

		if (education == null){
			return;
		}
		importData(education,primaryCategory);
	}

	@Override
	public void checkOneData(String primaryCategory, String id) {
		EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
		Education education = null;
		try {
			education = (Education) espRepository.get(id);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}

		if (education == null){
			return;
		}
		Set<String> uuids = new HashSet<>();
		uuids.add(education.getIdentifier());

		List<ResCoverage> resCoverageList =getResCoverage(
				coverageDao.queryCoverageByResource(primaryCategory, uuids));

		List<String> resourceTypes = new ArrayList<String>();
		resourceTypes.add(primaryCategory);
		List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);

		List<String> primaryCategorys = new ArrayList<>();
		primaryCategorys.add(primaryCategory);
		List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);

		List<ResourceRelation> resourceRelations =  getResourceRelation(
				educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids));

		titanImportRepository.checkResourceAllInTitan(education,resCoverageList,resourceCategoryList,techInfos, resourceRelations);

	}

	@Override
	public String importStatus() {
		return "primaryCategory:" +s_primaryCategory +"  totalPage:" + s_totalPage +"  page"+ s_page;
	}

	@Override
	public void code() {
		String sql = "select nd_code from category_datas";
		List<String> codes =  jdbcTemplate.queryForList(sql, String.class);
		for(String code : codes){
			String script = "g.V().has('cg_taxoncode',code).has('identifier')";
			Map<String, Object> param = new HashMap<>();
			param.put("code", code);
			try {
				ResultSet resultSet = titanCommonRepository.executeScriptResultSet(script, param);
				Iterator<Result> iterator = resultSet.iterator();
				if (iterator.hasNext()) {
					Integer id = iterator.next().getInt();
					if(id > 1){
						LOG.info(code);
					}
				}
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void repairData(String primaryCategory) {
		AbstractPageQuery abstractPageQuery = new RepairDataPageQuery();
		abstractPageQuery.doing(primaryCategory);
	}

	@Override
	public void checkAllData(String primaryCategory){
		AbstractPageQuery abstractPageQuery = new CheckResourceAllPageQuery();
		abstractPageQuery.doing(primaryCategory);
	}

	@Override
	public void checkResource(String primaryCategory) {
		AbstractPageQuery abstractPageQuery = new CheckResourcePageQuery();
		abstractPageQuery.doing(primaryCategory);
	}

	private List<ResCoverage> getResCoverage(List<ResCoverage> resCoverageList ){
		if(CollectionUtils.isEmpty(resCoverageList)){
			return new ArrayList<>();
		}
		List<ResCoverage> resCoverageListNew = new ArrayList<>();
		Map<String,List<ResCoverage>> resCoverageMap = new HashMap<>();
		for(ResCoverage rc : resCoverageList){
			List<ResCoverage> list = resCoverageMap.get(rc.getResource());
			if(list == null){
				list = new ArrayList<>();
				resCoverageMap.put(rc.getResource(),list);
			}

			if(!checkListContainCoverage(list ,rc)){
				list.add(rc);
				resCoverageListNew.add(rc);
			}
		}
		return resCoverageListNew;
	}

	//去除重复关系
	private List<ResourceRelation> getResourceRelation(List<ResourceRelation> resourceRelations){
		Map<String , List<ResourceRelation>> resourceRelationMap4Source = new HashMap<>();
		Set<ResourceRelation> resourceRelationSet = new HashSet<>();
		for(ResourceRelation rr : resourceRelations){
			List<ResourceRelation> list = resourceRelationMap4Source.get(rr.getSourceUuid());
			if(list==null){
				list = new ArrayList<>();
				resourceRelationMap4Source.put(rr.getSourceUuid(),list);
			}

			if(!checkResourceRelationExist(list, rr)){
				list.add(rr);
				resourceRelationSet.add(rr);
			}

		}

		Map<String , List<ResourceRelation>> resourceRelationMap4Target = new HashMap<>();
		for(ResourceRelation rr : resourceRelations){
			List<ResourceRelation> list = resourceRelationMap4Target.get(rr.getTarget());
			if(list==null){
				list = new ArrayList<>();
				resourceRelationMap4Target.put(rr.getTarget(),list);
			}

			if(!checkResourceRelationExist(list, rr)){
				list.add(rr);
				resourceRelationSet.add(rr);
			}
		}

		return new ArrayList<>(resourceRelationSet);
	}

	private boolean checkResourceRelationExist(List<ResourceRelation> list ,ResourceRelation source){
		boolean exist = false;
		if(list==null || list.size()==0){
			return false;
		}
		for (ResourceRelation resourceRelation : list){
			if(resourceRelation.getTarget().equals(source.getTarget())
					&&resourceRelation.getResourceTargetType().equals(source.getResourceTargetType())
					&&resourceRelation.getResType().equals(source.getResType())
					&&resourceRelation.getSourceUuid().equals(source.getSourceUuid())
					&&resourceRelation.getSourceUuid().equals(source.getSourceUuid())){
				exist = true;
				break;
			} else {
				exist = false;
				try {
					if(source.getEnable()){
						resourceRelation.setEnable(true);
					}
				} catch (Exception e){
					System.out.println(source);
					e.printStackTrace();
				}

			}
		}
		return exist;
	}

	private boolean checkListContainCoverage(List<ResCoverage> list, ResCoverage target){
		if(list == null){
			return false;
		}
		boolean exist = false;
		for(ResCoverage source : list){
			if(source.getResType().equals(target.getResType())
					&&source.getResource().equals(target.getResource())
					&&source.getTarget().equals(target.getTarget())
					&&source.getTargetType().equals(target.getTargetType())
					&&source.getStrategy().equals(target.getStrategy())){
				exist = true;
				break;
			} else {
				exist = false;

			}
		}

		return exist;
	}

	public long pageQueryKnowledgeRelation(ResourceRepository resourceRepository) {
		String fieldName = "identifier";

		long total = 0;
		// 分页
		int page = 0;
		int row = 500;
		@SuppressWarnings("rawtypes")
		Page resourcePage = new PageImpl(new ArrayList());
		@SuppressWarnings("rawtypes")
		List entitylist = null;

		List<Item<? extends Object>> items = new ArrayList<>();

		Sort sort = new Sort(Direction.ASC, fieldName);
		do {
			Pageable pageable = new PageRequest(page, row, sort);

			try {
				resourcePage = resourceRepository.findByItems(items, pageable);
				if (resourcePage == null) {
					break;
				}
				entitylist = resourcePage.getContent();
				if (entitylist == null) {
					continue;
				}
				List<KnowledgeRelation> knowledgeRelations = new ArrayList<KnowledgeRelation>();
				for (Object object : entitylist) {
					KnowledgeRelation relation = (KnowledgeRelation) object;
					knowledgeRelations.add(relation);
				}
				if(entitylist.size()==0){
					continue;
				}
				//TODO check
				total = total + titanKnowledgeRelationRepository.batchAdd(knowledgeRelations).size();

				LOG.info("import relation:totalPage:{}  page:{}",resourcePage.getTotalPages(),page);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
		} while (++page < resourcePage.getTotalPages());

		return total;
	}

	public abstract class AbstractPageQueryRelation{
		public long pageQueryRelation(ResourceRepository resourceRepository) {
			String fieldName = "identifier";

			long indexNum = 0;
			// 分页
			int page = 0;
			int row = 500;
			@SuppressWarnings("rawtypes")
			Page resourcePage = new PageImpl(new ArrayList());;
			@SuppressWarnings("rawtypes")
			List entitylist = null;

			List<Item<? extends Object>> items = new ArrayList<>();

			Sort sort = new Sort(Direction.ASC, fieldName);
			do {
				Pageable pageable = new PageRequest(page, row, sort);

				try {
					resourcePage = resourceRepository.findByItems(items, pageable);
					if (resourcePage == null) {
						break;
					}
					entitylist = resourcePage.getContent();
					if (entitylist == null) {
						continue;
					}
					List<ResourceRelation> resourceRelations = new ArrayList<ResourceRelation>();
					for (Object object : entitylist) {
						ResourceRelation relation = (ResourceRelation) object;
						resourceRelations.add(relation);
					}
					if(entitylist.size()==0){
						continue;
					}
					method(resourceRelations);
					LOG.info("import relation:totalPage:{}  page:{}",resourcePage.getTotalPages(),page);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
				setStatisticParam("relations", resourcePage.getTotalPages(), page);
			} while (++page < resourcePage.getTotalPages());

			return indexNum;
		}

		public abstract void method(List<ResourceRelation> resourceRelations);
	}

 	public class AbstractPageQueryRelationCreate extends AbstractPageQueryRelation{

		@Override
		public void method(List<ResourceRelation> resourceRelations) {
			titanImportRepository.batchImportRelation(resourceRelations);
		}
	}

	public class AbstractPageQueryRelationRepair extends AbstractPageQueryRelation{

		@Override
		public void method(List<ResourceRelation> resourceRelations) {
			titanUpdateDataRepository.batchUpdateRelation(resourceRelations);
		}
	}




	abstract class  AbstractPageQuery{
		public long doing(String primaryCategory) {
			String fieldName = "dblastUpdate";

			long indexNum = 0;
			// 分页
			int page = 0;
			int row = 500;
			EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
			@SuppressWarnings("rawtypes")
			Page resourcePage = new PageImpl(new ArrayList());
			@SuppressWarnings("rawtypes")
			List entitylist = null;

			List<Item<? extends Object>> items = new ArrayList<>();

			Item<String> resourceTypeItem = new Item<String>();
			resourceTypeItem.setKey("primaryCategory");
			resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
			resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
			resourceTypeItem.setValue(ValueUtils.newValue(primaryCategory));
			items.add(resourceTypeItem);

			Sort sort = new Sort(Direction.ASC, fieldName);
			do {
				Pageable pageable = new PageRequest(page, row, sort);

				try {
					resourcePage = espRepository.findByItems(items, pageable);
					if (resourcePage == null) {
						break;
					}
					entitylist = resourcePage.getContent();
					if (entitylist == null) {
						continue;
					}
					List<Education> resources = new ArrayList<Education>();
					for (Object object : entitylist) {
						Education education = (Education) object;
						resources.add(education);
					}
					if(entitylist.size()==0){
						continue;
					}
					long size = operate(resources,primaryCategory);
					indexNum += size;
					LOG.info( "type: {} totalPage:{} page:{}",primaryCategory, resourcePage.getTotalPages(),page);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e.getMessage());
					LOG.error("importTitanMySqlError page:{} primaryCategory:{}",page,primaryCategory);
				}
				setStatisticParam(primaryCategory,resourcePage.getTotalPages(),page);
			} while (++page < resourcePage.getTotalPages());

			return indexNum;
		}

		abstract long operate(List<Education> educations ,String primaryCategory);
	}

	class CheckResourcePageQuery extends  AbstractPageQuery{

		@Override
		long operate(List<Education> educations, String primaryCategory) {
			for(Education education : educations){
				titanImportRepository.checkResourceExistInTitan(education);
			}
			return 0;
		}
	}

	class CheckResourceAllPageQuery extends  AbstractPageQuery{

		@Override
		long operate(List<Education> educations, String primaryCategory) {

			if(CollectionUtils.isEmpty(educations)){
				return 0L;
			}
			Set<String> uuids = new HashSet<String>();
			for (Education education : educations) {
				uuids.add(education.getIdentifier());
			}

			List<ResCoverage> resCoverageList =getResCoverage(
					coverageDao.queryCoverageByResource(primaryCategory, uuids));
			Map<String, List<ResCoverage>> resCoverageMap = new HashMap<>();
			for (ResCoverage resCoverage : resCoverageList){
				List<ResCoverage> resCoverages = resCoverageMap.get(resCoverage.getResource());
				if(resCoverages == null){
					resCoverages = new ArrayList<>();
					resCoverageMap.put(resCoverage.getResource(), resCoverages);
				}

				resCoverages.add(resCoverage);
			}

			List<String> resourceTypes = new ArrayList<String>();
			resourceTypes.add(primaryCategory);
			List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
			Map<String, List<ResourceCategory>> resourceCategoryMap = new HashMap<>();
			for (ResourceCategory resourceCategory : resourceCategoryList){
				List<ResourceCategory> resourceCategories = resourceCategoryMap.get(resourceCategory.getResource());
				if(resourceCategories == null){
					resourceCategories = new ArrayList<>();
					resourceCategoryMap.put(resourceCategory.getResource(), resourceCategories);
				}

				resourceCategories.add(resourceCategory);
			}

			List<String> primaryCategorys = new ArrayList<>();
			primaryCategorys.add(primaryCategory);
			List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
			Map<String, List<TechInfo>> techInfoMap = new HashMap<>();
			for (TechInfo techInfo : techInfos){
				List<TechInfo> techInfoList = techInfoMap.get(techInfo.getResource());
				if(techInfoList == null){
					techInfoList = new ArrayList<>();
					techInfoMap.put(techInfo.getResource(), techInfoList);
				}

				techInfoList.add(techInfo);
			}
			for (Education education : educations){
				List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
				List<ResCoverage> sourceResCoverage = getResCoverage(resCoverageMap.get(education.getIdentifier()));
				List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
				titanImportRepository.checkResourceAllInTitan(education,sourceResCoverage,resourceCategory,sourceTechInfo ,null);
			}

			return 0;
		}
	}




	/**
	 * 创建章节关系
	 * */
	class CreateRelation4Chapter extends  AbstractPageQuery{
		@Override
		long operate(List<Education> educations,String primaryCategory) {

			List<Chapter> resources = new ArrayList<>();
			for (Education object : educations) {
				Chapter chapter = (Chapter) object;
				resources.add(chapter);
			}
			//TODO check
		    long size =	titanChapterRelationRepository.batchCreateRelation(resources);
			titanChapterRelationRepository.updateRelationOrderValue(resources,primaryCategory);
			return size;
		}
	}


	class UpdateDataPageQuery extends  AbstractPageQuery{
		@Override
		long operate(List<Education> educations,String primaryCategory) {
			List<Chapter> resources = new ArrayList<>();
			for (Education object : educations) {
				Chapter chapter = (Chapter) object;
				resources.add(chapter);
			}
			//TODO check
			titanChapterRelationRepository.updateRelationOrderValue(resources,primaryCategory);
			return educations.size();
		}
	}


	/**
	 * 导入知识点关系
	 * */
	class CreateRelation4Knowledge extends  AbstractPageQuery{
		@Override
		long operate(List<Education> educations, String primaryCategory) {
			List<Chapter> resources = new ArrayList<>();
			for (Education object : educations) {
				Chapter knowledge = (Chapter) object;
				resources.add(knowledge);
			}
			//TODO check
			titanKnowledgeRelationRepository.batchCreateRelation4Tree(resources);
			titanChapterRelationRepository.updateRelationOrderValue(resources,primaryCategory);
			return 0L;
		}
	}


	public class ImprotData4ScriptPageQuery extends AbstractPageQuery{

		@Override
		long operate(List<Education> educations, String primaryCategory) {

			return importData(educations, primaryCategory);
		}
	}

	public class RepairDataPageQuery extends AbstractPageQuery{

		@Override
		long operate(List<Education> educations, String primaryCategory) {
			return repairData(educations, primaryCategory);
		}
	}

	private long repairData(List<Education> educations, String primaryCategory){
		if(CollectionUtils.isEmpty(educations)){
			return 0L;
		}
		Set<String> uuids = new HashSet<String>();
		for (Education education : educations) {
			uuids.add(education.getIdentifier());
		}

		List<ResCoverage> resCoverageList =getResCoverage(
				coverageDao.queryCoverageByResource(primaryCategory, uuids));
		Map<String, List<ResCoverage>> resCoverageMap = new HashMap<>();
		for (ResCoverage resCoverage : resCoverageList){
			List<ResCoverage> resCoverages = resCoverageMap.get(resCoverage.getResource());
			if(resCoverages == null){
				resCoverages = new ArrayList<>();
				resCoverageMap.put(resCoverage.getResource(), resCoverages);
			}

			resCoverages.add(resCoverage);
		}

		List<String> resourceTypes = new ArrayList<String>();
		resourceTypes.add(primaryCategory);
		List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
		Map<String, List<ResourceCategory>> resourceCategoryMap = new HashMap<>();
		for (ResourceCategory resourceCategory : resourceCategoryList){
			List<ResourceCategory> resourceCategories = resourceCategoryMap.get(resourceCategory.getResource());
			if(resourceCategories == null){
				resourceCategories = new ArrayList<>();
				resourceCategoryMap.put(resourceCategory.getResource(), resourceCategories);
			}

			resourceCategories.add(resourceCategory);
		}

		List<String> primaryCategorys = new ArrayList<>();
		primaryCategorys.add(primaryCategory);
		List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
		Map<String, List<TechInfo>> techInfoMap = new HashMap<>();
		for (TechInfo techInfo : techInfos){
			List<TechInfo> techInfoList = techInfoMap.get(techInfo.getResource());
			if(techInfoList == null){
				techInfoList = new ArrayList<>();
				techInfoMap.put(techInfo.getResource(), techInfoList);
			}

			techInfoList.add(techInfo);
		}
		for (Education education : educations){
			List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
			List<ResCoverage> sourceResCoverage = getResCoverage(resCoverageMap.get(education.getIdentifier()));
			List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
			titanUpdateDataRepository.updateOneData(education,sourceResCoverage,resourceCategory,sourceTechInfo);
		}

		return educations.size();
	}

	private long importData(Education education, String primaryCategory){
		if(education == null){
			return 0L;
		}

		List<Education> educationList = new ArrayList<>();
		educationList.add(education);
		return  importData(educationList, primaryCategory);
	}

	private long importData(List<Education> educations, String primaryCategory){
		if(CollectionUtils.isEmpty(educations)){
			return 0L;
		}
		Set<String> uuids = new HashSet<String>();
		for (Education education : educations) {
			uuids.add(education.getIdentifier());
		}

		List<ResCoverage> resCoverageList =getResCoverage(
				coverageDao.queryCoverageByResource(primaryCategory, uuids));
		Map<String, List<ResCoverage>> resCoverageMap = new HashMap<>();
		for (ResCoverage resCoverage : resCoverageList){
			List<ResCoverage> resCoverages = resCoverageMap.get(resCoverage.getResource());
			if(resCoverages == null){
				resCoverages = new ArrayList<>();
				resCoverageMap.put(resCoverage.getResource(), resCoverages);
			}

			resCoverages.add(resCoverage);
		}

		List<String> resourceTypes = new ArrayList<String>();
		resourceTypes.add(primaryCategory);
		List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
		Map<String, List<ResourceCategory>> resourceCategoryMap = new HashMap<>();
		for (ResourceCategory resourceCategory : resourceCategoryList){
			List<ResourceCategory> resourceCategories = resourceCategoryMap.get(resourceCategory.getResource());
			if(resourceCategories == null){
				resourceCategories = new ArrayList<>();
				resourceCategoryMap.put(resourceCategory.getResource(), resourceCategories);
			}

			resourceCategories.add(resourceCategory);
		}

		List<String> primaryCategorys = new ArrayList<>();
		primaryCategorys.add(primaryCategory);
		List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
		Map<String, List<TechInfo>> techInfoMap = new HashMap<>();
		for (TechInfo techInfo : techInfos){
			List<TechInfo> techInfoList = techInfoMap.get(techInfo.getResource());
			if(techInfoList == null){
				techInfoList = new ArrayList<>();
				techInfoMap.put(techInfo.getResource(), techInfoList);
			}

			techInfoList.add(techInfo);
		}
		for (Education education : educations){
			List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
			List<ResCoverage> sourceResCoverage = getResCoverage(resCoverageMap.get(education.getIdentifier()));
			List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
			titanImportRepository.importOneData(education,sourceResCoverage,resourceCategory,sourceTechInfo);
		}

		return educations.size();
	}


	abstract private class TimeTaskPageQuery{

		private String primaryCategory = null;
		private Integer page =0;
		private Integer totalPage = null;
		private List<String> primaryCategorys = null;
		private Iterator<String> iterator = null;
		public TimeTaskPageQuery(Integer page, String type){
			primaryCategorys = new LinkedList<>();
			primaryCategorys.add("chapters");
			primaryCategorys.addAll(ResourceTypeSupport.getAllValidEsResourceTypeList());
			iterator = primaryCategorys.iterator();
			//把遍历器遍历到指定的位置
			if(type!=null && !type.equals("")){
				while (iterator.hasNext()){
					String next = iterator.next();
					if(type.equals(next)){
						break;
					}
				}
			}

			this.page = page;
			totalPage = 10000;
			primaryCategory = type;
		}
		public void schedule(){
			while (true){
				if(isWeek() || !isWeek() && isScheduleTime()){
					//更新分页条件
					if((primaryCategory == null|| totalPage==null ||page > totalPage) && iterator.hasNext()){
						primaryCategory = iterator.next();
						page = 0;
					}

					if(page > totalPage && !iterator.hasNext()){
						LOG.info("数导入完成");
						break;
					}

					LOG.info("importing... primaryCategory:{} totalPage:{} page:{}",primaryCategory,totalPage,page);

					totalPage = pageQuery(primaryCategory, page);
					page ++ ;
				} else {
					try {
						LOG.info("sleeping... 执行时间:周末、21:00-5:00  primaryCategory:{} totalPage:{} page:{}",
								primaryCategory,totalPage,page);
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private Integer pageQuery(String primaryCategory, Integer page){
			String fieldName = "dblastUpdate";

			int row = 500;
			EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
			@SuppressWarnings("rawtypes")
			Page resourcePage = null;
			@SuppressWarnings("rawtypes")
			List entitylist = null;

			List<Item<? extends Object>> items = new ArrayList<>();

			Item<String> resourceTypeItem = new Item<String>();
			resourceTypeItem.setKey("primaryCategory");
			resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
			resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
			resourceTypeItem.setValue(ValueUtils.newValue(primaryCategory));
			items.add(resourceTypeItem);

			Sort sort = new Sort(Direction.ASC, fieldName);
				Pageable pageable = new PageRequest(page, row, sort);

			try {
				resourcePage = espRepository.findByItems(items, pageable);
				if (resourcePage == null) {
					return null;
				}
				entitylist = resourcePage.getContent();
				if (entitylist == null) {
					return resourcePage.getTotalPages();
				}
				List<Education> resources = new ArrayList<Education>();
				for (Object object : entitylist) {
					Education education = (Education) object;
					resources.add(education);
				}
				if(entitylist.size()==0){
					return resourcePage.getTotalPages();
				}

				importDataOperate(resources,primaryCategory);

			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
			if(resourcePage == null){
				return null;
			}

			return resourcePage.getTotalPages();
		}

		private boolean isWeek(){
			int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			if(week == 1 || week == 7){
				return true;
			}

			return true;
		}

		private boolean isScheduleTime(){
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			if(hour <5 || hour >20){
				return true;
			}
			return false;
		}

		abstract   long importDataOperate(List<Education> educations,String primaryCategory);
	}

	public class TimeTaskPageQuery4Import extends TimeTaskPageQuery{

		public TimeTaskPageQuery4Import(Integer page, String type) {
			super(page, type);
		}

		@Override
		long importDataOperate(List<Education> educations, String primaryCategory) {
			return 0;
		}
	}

	public class TimeTaskPageQuery4Repair extends TimeTaskPageQuery{

		public TimeTaskPageQuery4Repair(Integer page, String type) {
			super(page, type);
		}

		@Override
		long importDataOperate(List<Education> educations, String primaryCategory) {
			return repairData(educations, primaryCategory);
		}
	}

	private class TimeTaskPageQuery4Update{

		private String primaryCategory = null;
		private Integer page =0;
		private Integer totalPage = null;
		private List<String> primaryCategorys = null;
		private Iterator<String> iterator = null;
		public TimeTaskPageQuery4Update(Integer page, String type){
			primaryCategorys = new LinkedList<>();
			primaryCategorys.add("chapters");
			primaryCategorys.addAll(ResourceTypeSupport.getAllValidEsResourceTypeList());
			iterator = primaryCategorys.iterator();
			//把遍历器遍历到指定的位置
			if(type!=null && !type.equals("")){
				while (iterator.hasNext()){
					String next = iterator.next();
					if(type.equals(next)){
						break;
					}
				}
			}

			this.page = page;
			totalPage = 10000;
			primaryCategory = type;
		}
		public void schedule(){
			while (true){
				if(isWeek() || !isWeek() && isScheduleTime()){
					//更新分页条件
					if((primaryCategory == null|| totalPage==null ||page > totalPage) && iterator.hasNext()){
						primaryCategory = iterator.next();
						page = 0;
					}

					if(page > totalPage && !iterator.hasNext()){
						LOG.info("数导入完成");
						break;
					}

					LOG.info("当前执行--primaryCategory:{};totalPage:{};page:{}",primaryCategory,totalPage,page);
					totalPage = pageQuery(primaryCategory, page);
					page ++ ;
				} else {
					try {
						LOG.info("sleep");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private Integer pageQuery(String primaryCategory, Integer page){
			String fieldName = "dblastUpdate";

			int row = 500;
			EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
			@SuppressWarnings("rawtypes")
			Page resourcePage = null;
			@SuppressWarnings("rawtypes")
			List entitylist = null;

			List<Item<? extends Object>> items = new ArrayList<>();

			Item<String> resourceTypeItem = new Item<String>();
			resourceTypeItem.setKey("primaryCategory");
			resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
			resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
			resourceTypeItem.setValue(ValueUtils.newValue(primaryCategory));
			items.add(resourceTypeItem);

			Sort sort = new Sort(Direction.ASC, fieldName);
			Pageable pageable = new PageRequest(page, row, sort);

			try {
				resourcePage = espRepository.findByItems(items, pageable);
				if (resourcePage == null) {
					return 0;
				}
				entitylist = resourcePage.getContent();
				if (entitylist == null) {
					return resourcePage.getTotalPages();
				}
				List<Education> resources = new ArrayList<Education>();
				for (Object object : entitylist) {
					Education education = (Education) object;
					resources.add(education);
				}
				if(entitylist.size()==0){
					return resourcePage.getTotalPages();
				}

				importDataOperate4update(resources,primaryCategory);

			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
			if(resourcePage == null){
				return 0;
			}
			setStatisticParam(primaryCategory, resourcePage.getTotalPages(),page);

			return resourcePage.getTotalPages();
		}

		private boolean isWeek(){
			int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			if(week == 1 || week == 7){
				return true;
			}

			return true;
		}

		private boolean isScheduleTime(){
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			if(hour <5 || hour >20){
				return true;
			}
			return false;
		}
	}

	private void importDataOperate4update(List<Education> educations , String primaryCategory){
        Set<String> uuids = new HashSet<String>();
        for (Education education : educations) {
            uuids.add(education.getIdentifier());
        }
        //后去coverage、category
        List<ResCoverage> resCoverageList =getResCoverage(coverageDao.queryCoverageByResource(primaryCategory, uuids));
        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceRepositoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);

        Map<String, List<ResCoverage>> coverageMap = new HashMap<>();
        Map<String, List<ResourceCategory>> categoryMap = new HashMap<>();
        for (ResCoverage resCoverage : resCoverageList){
            List<ResCoverage> coverageList = coverageMap.get(resCoverage.getResource());
            if(coverageList == null){
                coverageList = new ArrayList<>();
                coverageMap.put(resCoverage.getResource(), coverageList);
            }

            coverageList.add(resCoverage);
        }

        for(ResourceCategory category : resourceRepositoryList){
            List<ResourceCategory> categoryList = categoryMap.get(category.getResource());
            if(categoryList == null){
                categoryList = new ArrayList<>();
                categoryMap.put(category.getResource(), categoryList);
            }

            categoryList.add(category);
        }

        //保存数据
        for(Education education : educations){
            String uuid = education.getIdentifier();
            Set<String> resCoverages = new HashSet<>() ;
            Set<String> categoryCodes = new HashSet<>();
            Set<String> paths = new HashSet<>();
            List<ResCoverage> tempCoverageList = coverageMap.get(uuid);
            List<ResourceCategory> tempCategoryList = categoryMap.get(uuid);
			if(CollectionUtils.isNotEmpty(tempCoverageList)){
				for(ResCoverage resCoverage : tempCoverageList){
					String setValue4 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy()+"/"+education.getStatus();
					String setValue3 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//"+education.getStatus();
					String setValue2 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy()+"/";
					String setValue1 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//";
					resCoverages.add(setValue1);
					resCoverages.add(setValue2);
					resCoverages.add(setValue3);
					resCoverages.add(setValue4);
				}
			}

			if(CollectionUtils.isNotEmpty(tempCategoryList)){
				for(ResourceCategory category : tempCategoryList){
					if(StringUtils.isNotEmpty(category.getTaxonpath())){
						paths.add(category.getTaxonpath());
					}
					if(StringUtils.isNotEmpty(category.getTaxoncode())){
						categoryCodes.add(category.getTaxoncode());
					}

				}
			}

			String dropScript = "g.V().has(primaryCategory,'identifier',identifier)." +
					"properties('search_coverage','search_code','search_path','search_path_string','search_code_string','search_coverage_string').drop()";
			Map<String, Object> dropParam = new HashMap<>();
			dropParam.put("primaryCategory",primaryCategory);
			dropParam.put("identifier",education.getIdentifier());

            StringBuffer script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).property('primary_category',primaryCategory)");
            Map<String, Object> param = new HashMap<>();
            param.put("primaryCategory",primaryCategory);
            param.put("identifier",education.getIdentifier());

            addSetProperty("search_coverage",resCoverages,script,param);
            addSetProperty("search_code",categoryCodes,script,param);
            addSetProperty("search_path",paths,script,param);

			if(CollectionUtils.isNotEmpty(paths)){
				String searchPathString = StringUtils.join(paths, ",").toLowerCase();
				script.append(".property('search_path_string',searchPathString)");
				param.put("searchPathString", searchPathString);

			}

			if(CollectionUtils.isNotEmpty(categoryCodes)){
				String searchCodeString = StringUtils.join(categoryCodes, ",").toLowerCase();
				script.append(".property('search_code_string',searchCodeString)");
				param.put("searchCodeString", searchCodeString);
			}

			if(CollectionUtils.isNotEmpty(resCoverages)){
				String searchCoverageString = StringUtils.join(resCoverages,",").toLowerCase();
				script.append(".property('search_coverage_string',searchCoverageString)");
				param.put("searchCoverageString", searchCoverageString);
			}


            try {
				titanCommonRepository.executeScript(dropScript, dropParam);
                titanCommonRepository.executeScript(script.toString(),param);
            } catch (Exception e) {
				LOG.error("titan_repository error:{}" ,e.getMessage());
            }
        }
	}

	private void addSetProperty(String fieldName,
								Set<String> values ,StringBuffer script ,Map<String, Object> param) {
		if(values == null || values.size() == 0){
			return;
		}
		int index = 0;
		for (String value : values){
			String paramKey = fieldName+index;
			index ++ ;
			script.append(".property(set,'").append(fieldName).append("',").append(paramKey).append(")");
			param.put(paramKey , value);
		}
	}

	private void setStatisticParam(String primaryCategory , Integer totalPage , Integer page){
		s_primaryCategory = primaryCategory;
		s_totalPage = totalPage;
		s_page = page;
	}

}
