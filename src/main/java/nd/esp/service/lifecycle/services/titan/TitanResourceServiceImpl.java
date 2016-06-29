package nd.esp.service.lifecycle.services.titan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCategoryRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanChapterRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCoverageRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanKnowledgeRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanTechInfoRepository;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.KnowledgeRelation;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
public class TitanResourceServiceImpl implements TitanResourceService {

	private static final Logger LOG = LoggerFactory
			.getLogger(TitanResourceServiceImpl.class);

	@Autowired
	private TitanResourceRepository<Education>  titanResourceRepository;

	@Autowired
	private TitanCoverageRepository titanCoverageRepository;

	@Autowired
	private TitanCategoryRepository titanCategoryRepository;

	@Autowired
	private TitanRelationRepository titanRelationRepository;

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
	private TitanTechInfoRepository titanTechInfoRepository;

	@Autowired
	private ResourceRelation4QuestionDBRepository resourceRelation4QuestionDBRepository;

	@Autowired
	private ResourceRelationRepository resourceRelationRepository;

	@Autowired
	private KnowledgeRelationRepository knowledgeRelationRepository;

	@Autowired
	private Client client;

	@Override
	public long importData(String primaryCategory) {
		AbstractPageQuery abstractPageQuery = new ImportDataPageQuery();
		return abstractPageQuery.doing(primaryCategory);
	}

	@Override
	public long importRelation(String primaryCategory) {
		AbstractPageQuery abstractPageQuery = new ImportRelationPageQuery();
		return abstractPageQuery.doing(primaryCategory);
	}

	@Override
	public long updateData(String primaryCategory){

		AbstractPageQuery abstractPageQuery = new ImportDateCheckExist();

		return abstractPageQuery.doing(primaryCategory);
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
	public void importKnowledge() {
		AbstractPageQuery abstractPageQuery = new ImportDateCheckExist();
		abstractPageQuery.doing("knowledges");
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
		TimeTaskPageQuery timeTaskPageQuery = new TimeTaskPageQuery(page, type);
		timeTaskPageQuery.schedule();
	}

	@Override
	public long importAllRelation() {
		long size = 0L;
		size =  pageQueryRelation(resourceRelationRepository);
		size = size + pageQueryRelation(resourceRelation4QuestionDBRepository);
		return size;
	}

	@Override
	public long importKnowledgeRelation() {
		return pageQueryKnowledgeRelation(knowledgeRelationRepository);
	}

	//临时测试用方法
	@Override
	public void importOneData(String primaryCategory, String id){
		EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
		Education education = null;
		try {
			education = (Education) espRepository.get(id);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}

		if (education != null){
			Set<String> uuids = new HashSet<>();
			uuids.add(education.getIdentifier());
			titanResourceRepository.add(education);

			List<ResCoverage> resCoverageList =getResCoverage(
					coverageDao.queryCoverageByResource(primaryCategory, uuids));
			titanCoverageRepository.batchAdd(resCoverageList);


			List<String> resourceTypes = new ArrayList<String>();
			resourceTypes.add(primaryCategory);
			titanCategoryRepository.batchAdd(ndResourceDao
					.queryCategoriesUseHql(resourceTypes, uuids));


			//FIXME
//			List<ResourceRelation> resourceRelations =  getResourceRelation(
//					educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids));
//			titanRelationRepository.batchAdd(resourceRelations);

			List<String> primaryCategorys = new ArrayList<>();
			primaryCategorys.add(primaryCategory);
			List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
			titanTechInfoRepository.batchAdd(techInfos);
		}
	}

	@Override
	public long count(String primaryCategory) {
		return titanResourceRepository.count(primaryCategory);
	}


	private List<ResCoverage> getResCoverage(List<ResCoverage> resCoverageList ){
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
		Page resourcePage = null;
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

				total = total + titanKnowledgeRelationRepository.batchAdd(knowledgeRelations).size();

				LOG.info("import relation:totalPage:{}  page:{}",resourcePage.getTotalPages(),page);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
		} while (++page < resourcePage.getTotalPages());

		return total;
	}

	public long pageQueryRelation(ResourceRepository resourceRepository) {
		String fieldName = "identifier";

		long indexNum = 0;
		// 分页
		int page = 0;
		int row = 500;
		@SuppressWarnings("rawtypes")
		Page resourcePage = null;
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

				resourceRelations = titanRelationRepository.batchAdd(resourceRelations);
				indexNum += resourceRelations.size();

				LOG.info("import relation:totalPage:{}  page:{}",resourcePage.getTotalPages(),page);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
		} while (++page < resourcePage.getTotalPages());

		return indexNum;
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
					LOG.info("type:"+primaryCategory+"  page:"+page);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
			} while (++page < resourcePage.getTotalPages());

			return indexNum;
		}

		abstract long operate(List<Education> educations ,String primaryCategory);
	}

	/**
	 * 导入除关系外的其它节点
	 * */
	class ImportDataPageQuery extends  AbstractPageQuery{
		@Override
		long operate(List<Education> educations,String primaryCategory) {
			return importDataOperate(educations,primaryCategory);
		}
	}

	private  long importDataOperate(List<Education> educations,String primaryCategory){
		List<Education> resources = new ArrayList<Education>();
		Set<String> uuids = new HashSet<String>();
		for (Education education : educations) {
			resources.add(education);
			uuids.add(education.getIdentifier());
		}
		titanResourceRepository.batchAdd(educations);

		List<ResCoverage> resCoverageList =getResCoverage(
				coverageDao.queryCoverageByResource(primaryCategory, uuids));
		titanCoverageRepository.batchAdd(resCoverageList);

		List<String> resourceTypes = new ArrayList<String>();
		resourceTypes.add(primaryCategory);
		titanCategoryRepository.batchAdd(ndResourceDao
				.queryCategoriesUseHql(resourceTypes, uuids));

		//FIXME
//		List<ResourceRelation> resourceRelations =  getResourceRelation(
//				educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids));
//		titanRelationRepository.batchAdd(resourceRelations);

		List<String> primaryCategorys = new ArrayList<>();
		primaryCategorys.add(primaryCategory);
		List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
		titanTechInfoRepository.batchAdd(techInfos);
		return resources.size();
	}

	/**
	 * 导入关系
	 * */
	class ImportRelationPageQuery extends AbstractPageQuery{
		@Override
		long operate(List<Education> educations, String primaryCategory) {
			Set<String> uuids = new HashSet<String>();
			for (Education education : educations) {
				uuids.add(education.getIdentifier());
			}

			//FIXME
//			List<ResourceRelation> resourceRelations =  getResourceRelation(
//					educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids));
//			titanRelationRepository.batchAdd(resourceRelations);
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
		    long size =	titanChapterRelationRepository.batchCreateRelation(resources);
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
			titanChapterRelationRepository.updateRelationOrderValue(resources,primaryCategory);
			return educations.size();
		}
	}

	/**
	 * 导入前检查是否已经导入
	 * */
	class ImportDateCheckExist extends  AbstractPageQuery{
		@Override
		long operate(List<Education> educations, String primaryCategory) {
			for(Education edu : educations){
				if (!checkVertexExist(edu.getPrimaryCategory(),edu.getIdentifier())){
					importOneData(edu.getPrimaryCategory(),edu.getIdentifier());
					LOG.info("primaryCategory:"+edu.getIdentifier());
				}
			}

			return 0;
		}

		private  boolean checkVertexExist(String primary ,String identifier){
			String checkQuery = "g.V().has('identifier',identifier).next().id()";
			Map<String,Object> param = new HashMap<>();
			param.put("identifier",identifier);
			Long id = TitanScritpUtils.getOneVertexOrEdegeIdByResultSet(client.submit(checkQuery,param));
			if(id==null){
				return false;
			}
			return true;
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
			return titanKnowledgeRelationRepository.batchCreateRelation4Tree(resources);
		}
	}


	private class TimeTaskPageQuery{

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

			return false;
		}

		private boolean isScheduleTime(){
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			if(hour <5 || hour >20){
				return true;
			}
			return false;
		}
	}

}
