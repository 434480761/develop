package nd.esp.service.lifecycle;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.AccessModel;

/**
 * 资源类单元测试接口
 * @author xuzy
 *
 */
public interface JunitTest4Resource {
	/**
	 * 资源上传接口
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public AccessModel testUpload(String resType,String uuid,Boolean renew,String coverage);
	
	/**
	 * 资源下载接口
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public AccessModel testDownload(String resType,String uuid);
	
	/**
	 * 正常创建资源接口（包括所有属性）
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public ResourceViewModel testCreate(String resType,String uuid,String param);
	
	/**
	 * 正常修改资源接口（包括所有属性）
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public ResourceViewModel testUpdate(String resType,String uuid,String param);
	
	/**
	 * 资源删除接口
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public String testDelete(String resType,String uuid);
	
	/**
	 * 资源获取详细接口
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public ResourceViewModel testGetDetail(String resType,String uuid,String include,Boolean isAll);
	
	/**
	 * 批量获取资源详细接口
	 * 对于代码中不同的条件，需要灵活调整入参
	 */
	public Map<String, ResourceViewModel> testBatchDetail(String resType,List<String> uuids,String include);
	
	/**
	 * 部分资源特有的功能
	 */
	public void testSpecialFeature();
}
