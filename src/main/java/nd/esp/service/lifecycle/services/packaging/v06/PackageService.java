package nd.esp.service.lifecycle.services.packaging.v06;

import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.entity.PackagingParam;




/**
 * 资源对象打包的服务。
 * @author qil
 * @version 1.0
 * @created 23-4月-2015 12:06:06
 */
public interface PackageService{

    /**
     * 打包下载的请求，开始发起打包
     * 
     * @param path
     * @param id
	 * @param target
     * @param resType
     * @param uid
     * @param icplayer
     */
    public Map<String, Map<String, Object>> archiving(String path, String target
            , String id, String resType, String uid, String icplayer) throws Exception;

    /**
     * 从cs获取打包信息xml文件
     * 
     * @param path 文件路径
     * @param header cs实例信息
     */
    //public String AccessXmlFile(String path, String header) throws Exception;

	/**
	 * @desc 创建session接口
	 * @param uid
	 * @return
	 * @author liuwx
	 */
	public String createSession(String uid);


    double queryPack(String header, String path, String uid, StringBuffer errMsg);

    String UploadFileToCS(byte[] bytes, String csPath, String fileName, String session, String csApiUrl)
            throws Exception;

    // 解析打包信息文件
    Map<String, String> GetRefPathsMap(String path, String uid, String target,
            Map<String, Map<String, String>> pathsGrpMap, Set<String> unzipFiles) throws Exception;

	/**
     * 打包下载的请求，本地下载打包，不调用CS打包接口  ---保留测试用
     * 
     * @param path
     * @param id
	 * @param target
     * @param resType
     * @param uid
     * @param icplayer
     */
    Map<String, Map<String, Object>> archivingLocal(String path, String target, String id, String resType, String uid,
            boolean webpFirst, StringBuffer logMsg) throws Exception;
    
    
    /**
     * 创建任务调度，发起打包操作	
     * <p>Description:              </p>
     * <p>Create Time: 2015年12月15日   </p>
     * <p>Create author: qil   </p>
     * @param param
     * @throws Exception 
     */
    void triggerPackaging(PackagingParam param) throws Exception;   
    
    
    
}