package nd.esp.service.lifecycle.controllers.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.services.titanV07.NDResourceTitanService;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 用于辅助调试问题（titan）
 *
 * @author linsm
 *
 */
@RestController
@RequestMapping("/titan")
public class TitanHelperController {

	private static final Logger LOG = LoggerFactory
			.getLogger(TitanHelperController.class);

	@Autowired
	private TitanCommonRepository titanCommonRepository;

	@Autowired
	private CommonServiceHelper commonServiceHelper;

	@Autowired
	private NDResourceTitanService ndResourceTitanService;

	/**
	 * 用于查找结点的相关信息
	 *
	 * @param resourceType
	 * @param uuid
	 * @return
	 * @author linsm
	 */
	@RequestMapping(value = "/vertexAndEdge/{resourceType}/{uuid}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<String> getVertexAndEdge(@PathVariable String resourceType,
										 @PathVariable String uuid) {
		checkResourceTypeAndId(resourceType, uuid);
		List<String> result = new ArrayList<String>();
		ResourceNdCode resourceNdCode = ResourceNdCode.fromString(resourceType);
		StringBuilder scriptBuilder = new StringBuilder(
				"g.V().has('identifier',identifier).has('primary_category',primary_category).as('v').union(select('v'),bothE('has_relation'),both('has_relation'),both('has_coverage'),both('has_categories_path'),both('has_category_code')");
		switch (resourceNdCode) {
			case chapters:
				scriptBuilder.append(",bothE('has_chapter'),both('has_chapter')");
				break;
			case knowledges:
				scriptBuilder
						.append(",bothE('has_knowledge'),both('has_knowledge'),bothE('has_knowledge_relation'),both('has_knowledge_relation')");
				break;
			default:
				break;
		}
		scriptBuilder.append(").valueMap(true)");

		System.out.println(scriptBuilder);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("primary_category", resourceType);
		params.put("identifier", uuid);
		ResultSet resultSet = null;
		try {
			resultSet = titanCommonRepository.executeScriptResultSet(
					scriptBuilder.toString(), params);
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/TITAN", "submit script and has errors");
		}

		getResult(resultSet, result);
		return result;
	}

	/**
	 * 用于执行脚本（预生产，生产环境，无法访问到titan）, 用get暂时不行（url转义）
	 *
	 * @param map
	 * @return
	 * @author linsm
	 */
	@Deprecated
	@RequestMapping(value = "/actions/gremlin/script", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<String> executeScript(@RequestBody Map<String, Object> map) {
		List<String> result = new ArrayList<String>();

		if(CollectionUtils.isEmpty(map)){
			result.add("script is empty");
			return result;
		}

		String script = map.get("script").toString();
		if (StringUtils.isEmpty(script)) {
			result.add("script is empty");
			return result;
		}
		// 由于脚本中空格不影响查询(如："g.V().count()"可以写成"g.    V    (   ).   count   (   )")
		// 需要把脚本中多余空格去掉，防止类似这样的失误：'xxx.drop()'==> 'xxx.    drop     ()'
		script = CommonHelper.checkBlank(script);
		// 检查脚本
		checkScript(script);
		ResultSet resultSet = null;
		try {
			LOG.info("running script:{}",script);
			resultSet = titanCommonRepository.executeScriptResultSet(script);
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/TITAN", "submit script and has errors");
		}
		getResult(resultSet, result);
		return result;
	}


	@Deprecated
	@RequestMapping(value = "/actions/gremlin/script/param", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<String> executeScript4Params(@RequestBody Map<String, Object> scriptAndParam) {
		List<String> result = new ArrayList<String>();
		if(CollectionUtils.isEmpty(scriptAndParam)){
			result.add("script is empty");
			return result;
		}

		String script = scriptAndParam.get("script").toString();
		Map<String, Object> param = (Map<String, Object>) scriptAndParam.get("param");

		if (StringUtils.isEmpty(script)) {
			result.add("script is empty");
			return result;
		}
		script = CommonHelper.checkBlank(script);
		checkScript(script);
		ResultSet resultSet = null;
		try {
			resultSet = titanCommonRepository.executeScriptResultSet(script, param);
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/TITAN", "submit script and has errors");
		}
		getResult(resultSet, result);
		return result;
	}


	/**
	 * 获取资源详情
	 *
	 * @param resourceType
	 * @param uuid
	 * @param include
	 * @return
	 * @author linsm
	 */
	@RequestMapping(value = "vertex/{resourceType}/{uuid}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResourceViewModel getDetail(
			@PathVariable String resourceType,
			@PathVariable String uuid,
			@RequestParam(value = "include", required = false, defaultValue = "") String include) {
		//check include;
		List<String> includeList = IncludesConstant.getValidIncludes(include);

		ResourceModel resourceModel = ndResourceTitanService.getDetail(resourceType,uuid,includeList,true);

		return CommonHelper.changeToView(resourceModel,resourceType,includeList,commonServiceHelper);
	}

	/**
	 * 检查类型与uuid
	 *
	 * @param resourceType
	 * @param uuid
	 * @author linsm
	 */
	private void checkResourceTypeAndId(String resourceType, String uuid) {
		ResourceNdCode resourceNdCode = ResourceNdCode.fromString(resourceType);
		if (resourceNdCode == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/TITAN", "resource type is invalid");
		}

		if (StringUtils.isEmpty(uuid)) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/TITAN", "uuid is invalid");
		}
	}

	/**
	 * 简单处理结果
	 *
	 * @param resultSet
	 * @param result
	 * @author linsm
	 */
	private void getResult(ResultSet resultSet, List<String> result) {
		Iterator<Result> iterator = resultSet.iterator();
		while (iterator.hasNext()) {
			result.add(iterator.next().getString());
		}

	}

	/**
	 * 检查脚本
	 * @param script
     */
	private void checkScript(String script) {
		if (script != null) {
			for (ShieldOpt opt : ShieldOpt.values()) {
				if (script.contains("." + opt.toString() + "(") || script.contains(". " + opt.toString() + " (")) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
							"LC/TITAN", "脚本中带有非法操作");
				}
			}
		}
	}

	/**
	 * 屏蔽的操作
	 */
	private enum ShieldOpt {
		drop, addVertex, addEdge, property, updateIndex, buildMixedIndex, buildCompositeIndex, buildIndex, addKey, makePropertyKey, openManagement
	}
}
