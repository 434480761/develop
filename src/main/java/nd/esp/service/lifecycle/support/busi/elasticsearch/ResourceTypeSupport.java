package nd.esp.service.lifecycle.support.busi.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.http.HttpStatus;

/**
 * 资源类型工具类
 * 
 * @author linsm
 *
 */
public class ResourceTypeSupport {
	/**
	 * 检查支持的资源类型
	 * 
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	public static void checkType(String resourceType) {
		if (!isValidEsResourceType(resourceType)) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/elasticsearch/type", "not support the index type "
							+ resourceType);
		}

	}

	public static List<String> getAllValidEsResourceTypeList() {
		List<String> resourceTypes = new ArrayList<String>();
		for (ResourceNdCode resourceNdCode : ResourceNdCode.values()) {
			if (isValidEsResourceType(resourceNdCode)) {
				resourceTypes.add(resourceNdCode.toString());
			}
		}
		return resourceTypes;
	}

	public static boolean isValidEsResourceType(String resourceType) {
		ResourceNdCode resourceNdCode = ResourceNdCode.fromString(resourceType);
		if (isValidEsResourceType(resourceNdCode)) {
			return true;
		}
		return false;
	}

	private static boolean isValidEsResourceType(ResourceNdCode resourceNdCode) {
		if (resourceNdCode == null || resourceNdCode == ResourceNdCode.chapters
				|| resourceNdCode == ResourceNdCode.instructionalprototypes
				|| resourceNdCode == ResourceNdCode.prototypeactivities
				|| resourceNdCode == ResourceNdCode.activitiesteps) {
			return false;
		}
		return true;
	}
}
