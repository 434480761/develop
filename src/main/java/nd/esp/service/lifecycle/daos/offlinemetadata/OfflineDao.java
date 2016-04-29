/* =============================================================
 * Created: [2015年11月5日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.daos.offlinemetadata;

import nd.esp.service.lifecycle.entity.cs.Dentry;
import nd.esp.service.lifecycle.models.AccessModel;

/**
 * @author linsm
 * @since
 */
public interface OfflineDao {

    @Deprecated
    AccessModel getCsInfo(String resType, String uuid);
    
//    /**
//     * 获取session
//     * 
//     * @param resType
//     * @param uuid
//     * @param location
//     * @returnsw
//     * @since
//     */
//    AccessModel getCsInfo(String resType, String uuid,String location);

    String getDetail(String resType, String uuid);

    Dentry upFileToCs(byte[] content, String path, String fileName, String session) throws Exception;

}
