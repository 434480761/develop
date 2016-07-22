package nd.esp.service.lifecycle.controllers.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	 * @param script
	 * @return
	 * @author linsm
	 */
	@Deprecated
	@RequestMapping(value = "/actions/gremlin/{script}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<String> executScript(@PathVariable String script) {
		List<String> result = new ArrayList<String>();
		if (StringUtils.isEmpty(script)) {
			result.add("script is empty");
			return result;
		}
		ResultSet resultSet = null;
		try {
			resultSet = titanCommonRepository.executeScriptResultSet(script);
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

		checkResourceTypeAndId(resourceType, uuid);
		// check include;
		List<String> includeList = IncludesConstant.getValidIncludes(include);

		StringBuilder scriptBuilder = new StringBuilder(
				"g.V().has('identifier',identifier).has('primary_category',primary_category)");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("primary_category", resourceType);
		params.put("identifier", uuid);
		scriptBuilder.append(TitanUtils.generateScriptForInclude(includeList));
		scriptBuilder.append(".valueMap();");

		System.out.println(scriptBuilder);
		ResultSet resultSet = null;
		try {
			resultSet = titanCommonRepository.executeScriptResultSet(
					scriptBuilder.toString(), params);
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/TITAN", "submit script and has errors");
		}

		List<String> result = new ArrayList<String>();
		getResult(resultSet, result);

		String mainResult = null;
		List<String> otherLines = new ArrayList<String>();
		String taxOnPath = null;
		for (String line : result) {
			if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
				Map<String, String> map = TitanResultParse.toMap(line);
				taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
			} else if (line.contains(ES_SearchField.lc_create_time.toString())) {
				System.out.println(line);
				mainResult = line;
			} else {
				otherLines.add(line);
			}
		}
		System.out.println(mainResult);
		ResourceModel resourceModel = TitanResultParse.parseResource(
				resourceType, mainResult, otherLines, taxOnPath);
		return CommonHelper.changeToView(resourceModel, resourceType,
				includeList, commonServiceHelper);

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
}