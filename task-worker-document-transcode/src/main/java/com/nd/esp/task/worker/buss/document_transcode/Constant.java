package com.nd.esp.task.worker.buss.document_transcode;


import java.nio.charset.Charset;
import java.util.TimeZone;

import com.nd.esp.task.worker.buss.document_transcode.config.PackApplicationInitializer;


/**
 * @title 系统中使用到的常量
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年1月27日 上午11:28:39
 */
public class Constant {
    /**
     * 私有化构造函数，不允许实例化该类
     */
    private Constant() {
    }
    
    /**
     * 默认的时区，中国、东八区
     */
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT+8");
    /**
     * 默认的字符集名称
     */
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    /**
     * 默认的字符集
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);
    
    /**
     * 上传下载更新文件时候用默认超时时间(针对不同业务进行不同区分失效时间)
     */
    public static final Integer FILE_OPERATION_EXPIRETIME=Integer.valueOf(PackApplicationInitializer.properties.getProperty("sdp_cs_file_session_expiretime"));

    /**
     * 上传下载更新文件时候用默认角色(有user和admin)
     * @see http://wiki.sdp.nd/index.php?title=%E5%86%85%E5%AE%B9%E6%9C%8D%E5%8A%A1v01
     */
    public static final String FILE_OPERATION_ROLE=PackApplicationInitializer.properties.getProperty("sdp_cs_file_session_role");
   
    /**
     * 上传下载更新文件时候用默认角色(有user和admin)
     * @see http://wiki.sdp.nd/index.php?title=%E5%86%85%E5%AE%B9%E6%9C%8D%E5%8A%A1v01
     */
    public static final String FILE_OPERATION_USERNAME=PackApplicationInitializer.properties.getProperty("sdp_cs_file_session_username");
       
       
    
    public static class CSInstanceInfo {
        public CSInstanceInfo(){}
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
        public String getServiceId() {
            return serviceId;
        }
        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }
        private String url;
        private String path;
        //@SerializedName("service_id")
        private String serviceId;
    }
    /**
    * CS服务实例信息： 存储path，api url，service id
    */
    //public final static Map<String,CSInstanceInfo> CS_INSTANCE_MAP = new HashMap<String,CSInstanceInfo>();
    //cs的默认实例(/edu)
    public final static String CS_DEFAULT_INSTANCE = PackApplicationInitializer.properties.getProperty("sdp_cs_default_instance");
    //addon的cs实例(/module_mng)
    public final static String CS_ADDON_INSTANCE = PackApplicationInitializer.properties.getProperty("sdp_cs_addon_instance");
    //icplayer的cs实例
    public final static String CS_ICPLAYER_INSTANCE = "${icplayer}";
//    static {
//        String propRefKey=PackApplicationInitializer.properties.getProperty("sdp_cs_ref_key");
//        String[] refKeys = propRefKey.split(",");
//        String propUrl=PackApplicationInitializer.properties.getProperty("sdp_cs_ref_url");
//        String[] urls = propUrl.split(",");
//        String propPath=PackApplicationInitializer.properties.getProperty("sdp_cs_ref_file_path");
//        String[] paths = propPath.split(",");
//        String propServiceId=PackApplicationInitializer.properties.getProperty("sdp_cs_ref_service_id");
//        String[] serviceIds = propServiceId.split(",");
//        for(int count = 0; count<refKeys.length; ++count) {
//            CSInstanceInfo info = new CSInstanceInfo();
//            info.setUrl(urls[count]);
//            info.setPath(paths[count]);
//            info.setServiceId(serviceIds[count]);
//            CS_INSTANCE_MAP.put(refKeys[count], info);
//        }
//    }
    
    //默认CS接口url地址
    public static String CS_EDU_DOMAIN_API = "";
    
    //打包接口：   asynpack异步   pack同步
    public final static String CS_PACK_API = PackApplicationInitializer.properties.getProperty("sdp_cs_pack_api");
    
    //打包完成回调接口
    public final static String LC_PACK_CALLBACK_API = PackApplicationInitializer.properties.getProperty("sdp_lc_pack_callback_api");
    public final static String LC_TRANSCODE_CALLBACK_API = PackApplicationInitializer.properties.getProperty("sdp_lc_transcode_callback_api");
    public final static String LC_COPY_API_CALLBACK = PackApplicationInitializer.properties.getProperty("lc_copy_api_callback");
    
    //生命周期地址
    public  static String LIFE_CYCLE_DOMAIN_URL = "";
  //生命周期API接口版本号
    public  static String LIFE_CYCLE_API_VERSION = PackApplicationInitializer.properties.getProperty("lc_api_url_version");
    
    //生命周期API接口地址
    public  static String LIFE_CYCLE_API_URL="";
    
    //占位符 none
    public  static String LC_UPLOAD_PLACEHOLDER = PackApplicationInitializer.properties.getProperty("lc_upload_placeholder");
    
    public static String DISABLE_CS_PACKING = PackApplicationInitializer.properties.getProperty("disable_cs_packing", "0");

}
