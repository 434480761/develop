package nd.esp.service.lifecycle.services.copyright.v06;

import nd.esp.service.lifecycle.models.copyright.v06.CopyrightOwnerModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface CopyrightOwnerService {
	/**
	 * 创建资源版权方
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param rpm
	 * @return
	 */
	public CopyrightOwnerModel createCopyrightOwner(CopyrightOwnerModel com);
	
	/**
	 * 更新资源版权方
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param rpm
	 * @return
	 */
	public CopyrightOwnerModel updateCopyrightOwner(CopyrightOwnerModel com);
	
	/**
	 * 删除资源版权方
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param id
	 * @return
	 */
	public boolean deleteCopyrightOwner(String id);
	
	/**
	 * 查询资源版权方
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param words
	 * @param limit
	 * @return
	 */
	public ListViewModel<CopyrightOwnerModel> getCopyrightOwnerList(String words, String limit);
}
