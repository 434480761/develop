package nd.esp.service.lifecycle.services.coveragesharing.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.coveragesharing.v06.CoverageSharingModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

import com.nd.gaea.rest.security.authens.UserInfo;

public interface CoverageSharingService {
	/**
	 * 创建库之间的分享
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param csm
	 * @return
	 */
	public CoverageSharingModel createCoverageSharing(CoverageSharingModel csm, UserInfo userInfo);
	
	/**
	 * 删除库分享
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param id
	 * @return
	 */
	public boolean deleteCoverageSharing(String id);
	
	/**
	 * 获取库分享的列表
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param source
	 * @param target
	 * @param limit
	 * @return
	 */
	public ListViewModel<CoverageSharingModel> getCoverageSharingList(
			String source, String target, String limit);
	
	/**
	 * 判断两个库之间是否已经分享
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean judgeSharingExistOrNot(String source,String target); 
	
	/**
	 * 获取所有源覆盖范围
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param target
	 * @return
	 */
	public List<CoverageSharingModel> getCoverageSharingByTarget(String target);
}
