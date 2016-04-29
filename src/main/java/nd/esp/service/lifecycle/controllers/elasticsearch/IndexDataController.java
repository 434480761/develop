package nd.esp.service.lifecycle.controllers.elasticsearch;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.services.elasticsearch.IndexDataService;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * 用于重建索引，从mysql 数据库取数据
 * 
 * @author linsm
 *
 */
@RestController
@RequestMapping("/elasticsearch/index/data")
public class IndexDataController {
	private static final Logger LOG = LoggerFactory
			.getLogger(IndexDataController.class);

	@Autowired
	private IndexDataService indexDataService;

	/**
	 * 重建索引
	 * 
	 * @param to
	 *            在该时间前
	 * @param from
	 *            在该时间后
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@RequestMapping(value = "/{resourceType}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public long index(@PathVariable String resourceType,
			@RequestParam(value = "to", required = false) String to,
			@RequestParam(value = "from", required = false) String from) {

		// check type
		ResourceTypeSupport.checkType(resourceType);

		String supportFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(supportFormat);
		// lastUpdate
		Date toDate = null;
		Date fromDate = null;

		if (StringUtils.isNotEmpty(to)) {
			try {
				toDate = simpleDateFormat.parse(to);
			} catch (ParseException e) {
				LOG.error(e.getMessage());
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/format/error", "to params only support format: "
								+ supportFormat);
			}

		}
		// else {
		// toDate = new Date(System.currentTimeMillis());
		// }

		if (StringUtils.isNotEmpty(from)) {
			try {
				fromDate = simpleDateFormat.parse(from);
			} catch (ParseException e) {
				LOG.error(e.getMessage());
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/format/error", "from params only support format: "
								+ supportFormat);
			}

		}
		// else {
		// try {
		// fromDate = simpleDateFormat.parse("2000-01-01 00:00:00");
		// } catch (ParseException e) {
		// throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		// "LC/format/error",
		// "default params only support format: " + supportFormat);
		// }
		// }

		return indexDataService.index(resourceType, toDate, fromDate);
	}

	/**
	 * 重建索引
	 * 
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@RequestMapping(value = "/{resourceType}", params = { "uid" }, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public int index(@PathVariable String resourceType,
			@RequestParam(value = "uid", required = true) Set<String> uidSet) {
		if (CollectionUtils.isEmpty(uidSet)) {
			return 0;
		}
		// check type
		ResourceTypeSupport.checkType(resourceType);
		return indexDataService.index(resourceType, uidSet);
	}

	/**
	 * 重建索引
	 * 
	 * @author linsm
	 */
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public void indexAll() {

		for (String resourceType : ResourceTypeSupport
				.getAllValidEsResourceTypeList()) {
			indexDataService.index(resourceType, null, null);
		}

	}

	/**
	 * 删除对应资源索引数据
	 * 
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@RequestMapping(value = "/{resourceType}", method = RequestMethod.DELETE)
	public void deleteResource(@PathVariable String resourceType) {
		// check type
		ResourceTypeSupport.checkType(resourceType);
		indexDataService.deleteResource(resourceType);
	}

	/**
	 * 删除索引数据
	 * 
	 * @author linsm
	 */
	@RequestMapping(value = "/all", method = RequestMethod.DELETE)
	public void deleteResourceAll() {
		for (String resourceType : ResourceTypeSupport
				.getAllValidEsResourceTypeList()) {
			indexDataService.deleteResource(resourceType);
		}

	}

	/**
	 * 资源获取详细接口
	 * 
	 * @param resourceType
	 * @param uuid
	 * @return
	 * @since
	 */
	@RequestMapping(value = "{res_type}/{uuid}/forES", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ResourceModel getDetailForES(
			@PathVariable("res_type") String resourceType,
			@PathVariable("uuid") String uuid) {

		// 调用servicere
		try {
			return indexDataService.getDetailForES(resourceType, uuid);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/getdetailforES", "LC/getdetailforES");
		}
	}

	/**
	 * 资源获取详细接口
	 * 
	 * @param resourceType
	 * @param uuid
	 * @return
	 * @since
	 */
	@RequestMapping(value = "{res_type}/{uuid}/forES/toJson", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody String getDetailJsonForES(
			@PathVariable("res_type") String resourceType,
			@PathVariable("uuid") String uuid) {
		// 调用servicere
		return indexDataService.getJson(resourceType, uuid);
	}

	/**
	 * 批量获取资源详细接口
	 * 
	 * @param resourceType
	 * @param uuid
	 * @param includeString
	 * @return
	 * @since
	 */
	@RequestMapping(value = "{res_type}/list/forES", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody List<ResourceModel> batchDetailForES(
			@PathVariable("res_type") String resourceType,
			@RequestParam("uuid") Set<String> uuids) {

		return indexDataService.batchDetailForES(resourceType, uuids);
	}

}
