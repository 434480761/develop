package nd.esp.service.lifecycle.services;

import nd.esp.service.lifecycle.models.AccessModel;

/**
 * CS文件上传
 * 
 * <br>Created 2015年5月13日 下午4:04:32
 * @version  
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public interface CSFileService {
	/**
	 * players定制上传接口
	 * 
	 * @param uid
	 * @param coverage
	 * @return
	 * @since
	 */
	AccessModel getPlayerUploadUrl(String uid, String coverage);

}
