package nd.esp.service.lifecycle.support.icrs;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
/**
 * 智慧教室-课堂数据统计平台  Controller层
 * @author xiezy
 * @date 2016年9月12日
 */
@RestController
@RequestMapping("/icrs")
public class InitIcrsResource {
	
	/**
	 * 初始化 icrs_resource
	 * @author xiezy
	 * @date 2016年9月12日
	 */
	@RequestMapping(value = "/init", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void init(){
		
	}
	
	private void initTypeByType(String resType){
		String querySql = "select distinct ndr.identifier as id,ndr.create_time as ct,rv.target as target "
				+ "from ndresource ndr inner join res_coverages rv "
				+ "on ndr.identifier=rv.resource ";
		if(resType.equals(IndexSourceType.AssetType.getName()) ||
				resType.equals(IndexSourceType.SourceCourseWareObjectType.getName())){//assets,coursewareobjects
			querySql += " inner join resource_categories rc on ndr.identifier=rc.resource ";
		}
		
		querySql += " where ndr.enable=1 and ndr.primary_category='" + resType + "' "
				+ "and rv.target_type='User' and rv.strategy='OWNER' and rv.res_type='" + resType + "' ";
		if(resType.equals(IndexSourceType.AssetType.getName())){//assets
			querySql += " and rc.primary_category='" + resType + "' and rc.taxOnCode in "
					+ "('$RA0101','$RA0102','$RA0103','$RA0104')";
		}
		if(resType.equals(IndexSourceType.SourceCourseWareObjectType.getName())){//coursewareobjects
			querySql += " and rc.primary_category='" + resType + "' and rc.taxOnCode like '$RE04%'";
		}
	}
}
