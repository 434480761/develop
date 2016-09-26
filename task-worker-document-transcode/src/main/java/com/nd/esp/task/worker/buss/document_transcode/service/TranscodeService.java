package com.nd.esp.task.worker.buss.document_transcode.service;

import com.nd.esp.task.worker.container.ext.TaskRun;


/**
 * 资源对象打包的服务。
 * @author qil
 * @version 1.0
 * @created 23-4月-2015 12:06:06
 */
public interface TranscodeService extends TaskRun {

    /**
     * 打包下载的请求，获取下载地址
     * 
     * @param path
     * @param id
     * @param resType
     * @param uid
     * @param icplayer
     */
//    public Map<String, Map<String, String>> archiving(String path, String target
//            , String id, String resType, String uid, String icplayer, 
//            Map<String,CSInstanceInfo> instanceMap, String addonInstance) throws Exception;
    

    /**
     * 从cs获取打包信息xml文件
     * 
     * @param path 文件路径
     * @param header cs实例信息
     */
    //public String AccessXmlFile(String path, String header, Map<String,CSInstanceInfo> instanceMap) throws Exception;

	/**
	 * @desc 创建session接口
	 * @param uid
	 * @return
	 * @author liuwx
	 */
	//public String createSession(String uid);

    /**
     * 调用cs接口获取包文件更新时间
     *
     * @param path sdp-package.xml文件所在路径
     * @param uid  用户id
     * @param target 资源包面向对象 ： default,student,teacher
     * @param pathsGrpMap 返回资源包所需文件及本地存储相对路径， 以cs实例为key分组
     * @param instanceMap cs实例信息，用于请求session
     * @param unzipFiles  返回需在本地解压缩的文件列表
     * @param addonInstance  module的cs实例
     *
     */
//    void GetRefPathsMap(String path, String uid, String target,
//            Map<String, Map<String, String>> pathsGrpMap, Map<String, CSInstanceInfo> instanceMap,
//            Set<String> unzipFiles, String addonInstance) throws Exception;

//    double queryPack(String url, String errMsg);

//    String UploadFileToCS(byte[] bytes, String csPath, String fileName, String session, String csApiUrl)
//            throws Exception;


    /**
     * 打包下载的请求，获取下载地址
     * 
     * @param TranscodeParam  打包参数
     * @param id              打包资源id
     * @param logMsg          输出在任务监控的日志信息
     * 
     */
//    public TranscodeResult transcode(String id, TranscodeParam param, StringBuffer logMsg)
//            throws Exception;
    
}