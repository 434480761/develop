package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCategoryRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TitanCategoryRepositoryImpl implements TitanCategoryRepository {
	private static Map<String, Long> tanxoncodeCacheMap = new HashMap<>();

	@Autowired
	private TitanCommonRepository titanCommonRepository;

	/**
	 * 1、添加维度数据；2、添加资源冗余数据
	 * */
	@Override
	public ResourceCategory add(ResourceCategory resourceCategory) {
		if(resourceCategory == null){
			return null;
		}
		//添加维度数据
		ResourceCategory rc = addResourceCategory(resourceCategory);
		addPath(rc.getResource(), rc.getPrimaryCategory(), rc.getTaxonpath());

		//更新资源的冗余数据search_path\search_code
		List<String> category = new ArrayList<>();
		category.add(rc.getTaxoncode());
		List<String> pathList = new ArrayList<>();
		pathList.add(rc.getTaxonpath());
		updateResourceProperty(pathList,category ,resourceCategory.getPrimaryCategory(), resourceCategory.getResource());
		return  rc;
	}

	/**
	 * 批添加维度数据：<br>
	 * 		1、只支持对同一个资源的维度数据进行批量增加，多个资源批量增加会出现异常<br>
	 *     	2、批量增加前会先删除历史数据<br>
	 *     	3、更新资源的冗余数据
	 * */
	@Override
	public List<ResourceCategory> batchAdd(
			List<ResourceCategory> resourceCategories) {
		if(resourceCategories == null || resourceCategories.size() == 0){
			return null;
		}

		//FIXME 不是所有的添加都需要删除
		ResourceCategory category = resourceCategories.get(0);
		deleteAll(category.getPrimaryCategory(), category.getResource());

		// FIXME
		List<ResourceCategory> list = new ArrayList<>();
		//批量保存PATH
		List<String> pathList = batchAddPath(resourceCategories);

		//批量保存维度数据
		List<String> categoryList = new ArrayList<>();
		for (ResourceCategory resourceCategory : resourceCategories) {
			ResourceCategory rc = addResourceCategory(resourceCategory);
			if(rc!=null){
				list.add(rc);
			}
			categoryList.add(resourceCategory.getTaxoncode());
		}

		updateResourceProperty(pathList, categoryList, category.getPrimaryCategory() ,category.getResource());
		return list;

	}

	@Override
	public ResourceCategory update(ResourceCategory resourceCategory) {
		/**
		 * 待定
		 * */
		return null;
	}

	@Override
	public List<ResourceCategory> batchUpdate(List<ResourceCategory> entityList) {
		/**
		 * 待定
		 * */
		return null;
	}

	@Override
	/**
	 * 删除维度数据和相关的冗余数据
	 * */
	public void deleteAll(String primaryCategory, String identifier) {
		String deleteScript = "g.V().has(primaryCategory,'identifier',identifier)" +
				".outE().or(hasLabel('has_categories_path'),hasLabel('has_category_code')).drop();";
		deleteScript = deleteScript + "g.V().has(primaryCategory,'identifier',identifier).properties('search_code','search_path').drop()";

		Map<String, Object> param = new HashMap<>();
		param.put("primaryCategory", primaryCategory);
		param.put("identifier", identifier);

		titanCommonRepository.executeScript(deleteScript, param);
	}


	private List<String> batchAddPath(List<ResourceCategory> resourceCategories){
		if(resourceCategories==null||resourceCategories.size()==0){
			return null;
		}
		List<String> pathList = new ArrayList<>();
		String primaryCategory = resourceCategories.get(0).getPrimaryCategory();
		Map<String,List<String>> resourceCategoryMap = new HashMap<>();
		for (ResourceCategory resourceCategory : resourceCategories) {
			String path = resourceCategory.getTaxonpath();
			String resource = resourceCategory.getResource();
			List<String> resourceCategoryList = resourceCategoryMap.get(resource);
			if(resourceCategoryList==null){
				resourceCategoryList = new ArrayList<>();
				resourceCategoryMap.put(resource,resourceCategoryList);
			}

			if(!resourceCategoryList.contains(path)){
				resourceCategoryList.add(path);
			}
		}

		for(String key : resourceCategoryMap.keySet()){
			for(String path : resourceCategoryMap.get(key)){
				String p = addPath(key,primaryCategory,path);
				if(p != null){
					pathList.add(p);
				}
			}
		}
		return pathList;
	}

	private ResourceCategory addResourceCategory(ResourceCategory resourceCategory){
		StringBuffer script;
		Map<String, Object> graphParams;
		Long categoryCodeNodeId = getCategoryCodeId(resourceCategory);
		if (categoryCodeNodeId != null) {
			script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).next()" +
					".addEdge('has_category_code',g.V(categoryCodeNodeId).next(),'identifier',edgeIdentifier)");
			graphParams = new HashMap<String, Object>();
			graphParams.put("primaryCategory",
					resourceCategory.getPrimaryCategory());
			graphParams.put("identifier", resourceCategory.getResource());
			graphParams.put("categoryCodeNodeId", categoryCodeNodeId);
			graphParams.put("edgeIdentifier",resourceCategory.getIdentifier());

			titanCommonRepository.executeScript(script.toString(), graphParams);
		} else {
			script = new StringBuffer(
					"category_code = graph.addVertex(T.label,'category_code'");
			graphParams = TitanScritpUtils.getParamAndChangeScript(script, resourceCategory);
			script.append(");");
			script.append("g.V().hasLabel(primaryCategory).has('identifier',identifier).next()" +
					".addEdge('has_category_code',category_code,'identifier',edgeIdentifier)");

			graphParams.put("primaryCategory",
					resourceCategory.getPrimaryCategory());
			graphParams.put("identifier", resourceCategory.getResource());
			graphParams.put("edgeIdentifier",resourceCategory.getIdentifier());

			titanCommonRepository.executeScript(script.toString(), graphParams);
		}

		return resourceCategory;
	}

	//TODO path没有加入
	private String addPath(String resource ,String resourcePrimaryCategory,String path ){
		if(path==null||path.equals("")){
			return null;
		}
		String queryVScript = "g.V().hasLabel(source_primaryCategory).has('identifier',source_identifier).id()";
		Map<String,Object> queryVParams = new HashMap<>();
		queryVParams.put("source_primaryCategory",resourcePrimaryCategory);
		queryVParams.put("source_identifier",resource);
		Long sourceNodeId = titanCommonRepository.executeScriptUniqueLong(queryVScript,queryVParams);
		if(sourceNodeId == null){
			return null;
		}

		String queryPathScript = "g.V().has('categories_path','cg_taxonpath',taxonpath).id()";
		Map<String,Object> queryPathParams = new HashMap<>();
		queryPathParams.put("taxonpath",path);
		Long sourcePathId = titanCommonRepository.executeScriptUniqueLong(queryPathScript,queryPathParams);

		Map<String,Object> addScriptParams = new HashMap<>();
		StringBuilder addPathScript;
		if(sourcePathId == null){
			addPathScript = new StringBuilder("categories_path = graph.addVertex(T.label,'categories_path','cg_taxonpath',taxonpath);");
			addPathScript.append("g.V(sourceNodeId).next().addEdge('has_categories_path',categories_path)");
			addScriptParams.put("taxonpath",path);
			addScriptParams.put("sourceNodeId",sourceNodeId);

			titanCommonRepository.executeScript(addPathScript.toString(), addScriptParams);
		} else {
			addPathScript = new StringBuilder("g.V(sourceNodeId).next().addEdge('has_categories_path',g.V(sourcePathId).next())");
			addScriptParams.put("sourceNodeId",sourceNodeId);
			addScriptParams.put("sourcePathId",sourcePathId);
			titanCommonRepository.executeScript(addPathScript.toString(), addScriptParams);
		}

		return path;
	}

	private Long getCategoryCodeId(ResourceCategory resCoverage) {
		Long taxoncodeId =  tanxoncodeCacheMap.get(resCoverage.getTaxoncode());

		if(taxoncodeId == null) {
			String scriptString = "g.V().hasLabel('category_code').has('cg_taxoncode',taxoncode).id()";
			Map<String, Object> graphParams = new HashMap<String, Object>();
			graphParams.put("taxoncode",resCoverage.getTaxoncode());
			taxoncodeId = titanCommonRepository.executeScriptUniqueLong(scriptString, graphParams);

			if(tanxoncodeCacheMap.size() < 2000){
				tanxoncodeCacheMap.put(resCoverage.getTaxoncode(), taxoncodeId);
			}
		}
		return taxoncodeId;
	}

	private void updateResourceProperty(List<String> pathList , List<String> codeList , String primaryCategory, String identifier){
		StringBuffer script = new StringBuffer("g.V()has(primaryCategory,'identifier',identifier)");
		Map<String, Object> param = new HashMap<>();
		param.put("primaryCategory" ,primaryCategory);
		param.put("identifier" ,identifier);
		TitanScritpUtils.getSetScriptAndParam(script, param ,"search_code",codeList);

		TitanScritpUtils.getSetScriptAndParam(script, param ,"search_path",pathList);
		titanCommonRepository.executeScript(script.toString(), param);
	}

}
