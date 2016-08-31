package nd.esp.service.lifecycle.support;


import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.entity.lifecycle.AdapterTaskResult;
import nd.esp.service.lifecycle.vos.enums.IndexSourceType;

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
    
    public final static Map<Integer, Map<String,IndexSourceType>>INDEX_SOURCE_MAP=new HashMap<Integer, Map<String,IndexSourceType>>();
    public  final static Map<String,IndexSourceType>AssestTypeList=new HashMap<String,IndexSourceType>();
    public final static Map<String,IndexSourceType>SourceTypeList=new HashMap<String,IndexSourceType>();

    
       
    static {
    	
        AssestTypeList.put(IndexSourceType.AssetImageType.getType(),IndexSourceType.AssetImageType);
    	AssestTypeList.put(IndexSourceType.AssetVideoType.getType(),IndexSourceType.AssetVideoType);
    	AssestTypeList.put(IndexSourceType.AssetAudioType.getType(),IndexSourceType.AssetAudioType);
    	AssestTypeList.put(IndexSourceType.AssetOtherType.getType(),IndexSourceType.AssetOtherType);
    	INDEX_SOURCE_MAP.put(IndexSourceType.AssetType.getValue(), AssestTypeList);
    	SourceTypeList.put(IndexSourceType.SourceCourseWareObjectType.getType(),IndexSourceType.SourceCourseWareObjectType);
    	SourceTypeList.put(IndexSourceType.SourceCourseWareObjectTemplateType.getType(),IndexSourceType.SourceCourseWareObjectTemplateType);
    	SourceTypeList.put(IndexSourceType.SourceCourseWareType.getType(),IndexSourceType.SourceCourseWareType);
    	INDEX_SOURCE_MAP.put(IndexSourceType.SourceType.getValue(), SourceTypeList);
    	
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
     * 数据资源api访问地址
     */
    public static final String SOURCE_DATA_API_URL =LifeCircleApplicationInitializer.properties.getProperty("esp_store_url");
     //public static final String SOURCE_DATA_API_URL = "http://192.168.46.97:8080/edu_store";
    
    /**
     * WafHttpClient和WafSecurityHttpClient重试次数
     */
    public static final int WAF_CLIENT_RETRY_COUNT = 3;
    
    /**
     * 上传默认为空时候传递的值
     */
    public static final String DEFAULT_UPLOAD_URL_ID="none";
    /**
     * 上传下载更新文件path变量定义值
     */
    public static final String FILE_PATH_URL=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_file_path");
    
    public static final String FILE_PATH_URL_ADDON=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_file_path_addon");
    /**
     * 上传下载更新文件时候用户的参数名
     */
    public static final String FILE_OPERATION_UID_REQUEST_PARAM_NAME="uid";
    /**
     * 上传下载更新文件时候用默认超时时间(针对不同业务进行不同区分失效时间)
     */
    public static final Integer FILE_OPERATION_EXPIRETIME=Integer.valueOf(LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_file_session_expiretime"));
    /**
     * 上传下载更新文件时候用默认accessKey(暂时没有被使用)
     */
    public static final String FILE_OPERATION_ACCESSKEY="accessKey";
    /**
     * 上传下载更新文件时候用默认角色(有user和admin)
     * @see http://wiki.sdp.nd/index.php?title=%E5%86%85%E5%AE%B9%E6%9C%8D%E5%8A%A1v01
     */
    public static final String FILE_OPERATION_ROLE=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_file_session_role");
   
    /**
     * 上传下载更新文件时候用默认角色(有user和admin)
     * @see http://wiki.sdp.nd/index.php?title=%E5%86%85%E5%AE%B9%E6%9C%8D%E5%8A%A1v01
     */
    public static final String FILE_OPERATION_USERNAME=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_file_session_username");
   
    
    /**
     * CS服务api访问地址
     * 正式环境：    http://cs.101.com/v0.1
     * dev,debug：http://sdpcs.debug.web.nd/v0.1
     */
    public static final String CS_API_URL=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_api"); 
    /**
     * CS服务ID
     * 正式环境：    cc0d47a0-54d9-442e-bf47-9fb0ca4e6bf8
     * dev,debug：d2bcaabe-7e63-4860-9d86-95d049f62b53
     **/
    public static final String CS_SERVICE_ID=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_service_id"); 
    
    
    /**
     * ICPlayer下载地址
     **/
    public static final String ICPLAYER_DOWNLOAD_URL=LifeCircleApplicationInitializer.properties.getProperty("icplayer_download_url"); 
    
    
    /**
     * session获取的默认path
     **/
    public static final String CS_SESSION_PATH=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_file_session_path"); 
    
    /**
     * UC_API对应的domain
     */
    public static final String UC_API_DOMAIN = LifeCircleApplicationInitializer.properties.getProperty("esp_uc_api_domain");
    
    /**
     * 门户对应的组织
     */
    public static final String FILTER_UC_API = LifeCircleApplicationInitializer.properties.getProperty("filter_uc_api");
    
    /**
     * CS服务api访问地址(Addon)
     * 正式环境：    http://cs.101.com/v0.1
     */
    public static final String CS_ADDON_API_URL="http://cs.101.com/v0.1"; 
    /**
     * CS服务ID(Addon)
     **/
    public static final String CS_ADDON_SERVICE_ID="95a721c7-bffe-4f3c-beb0-c3c11876670f"; 
    
    public static class CSInstanceInfo {
        CSInstanceInfo(){}
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
        private String serviceId;
    }
    /**
    * CS服务实例信息： 存储path，api url，service id
    */
    public final static Map<String,CSInstanceInfo> CS_INSTANCE_MAP = new HashMap<String,CSInstanceInfo>();
    //cs的默认实例(/edu)
    public final static String CS_DEFAULT_INSTANCE = LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_default_instance");
    //cs的另外一个默认实例(/edu_product)
    public final static String CS_DEFAULT_INSTANCE_OTHER = LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_default_instance_other");
    //addon的cs实例(/module_mng)
    public final static String CS_ADDON_INSTANCE = LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_module_instance");
    //icplayer的cs实例
    public final static String CS_ICPLAYER_INSTANCE = "${icplayer}";
    static {
        String propRefKey=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_ref_key");
        String[] refKeys = propRefKey.split(",");
        String propUrl=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_ref_url");
        String[] urls = propUrl.split(",");
        String propPath=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_ref_file_path");
        String[] paths = propPath.split(",");
        String propServiceId=LifeCircleApplicationInitializer.properties.getProperty("sdp_cs_ref_service_id");
        String[] serviceIds = propServiceId.split(",");
        for(int count = 0; count<refKeys.length; ++count) {
            CSInstanceInfo info = new CSInstanceInfo();
            info.setUrl(urls[count]);
            info.setPath(paths[count]);
            info.setServiceId(serviceIds[count]);
            CS_INSTANCE_MAP.put(refKeys[count], info);
        }
    }
    
    //打包接口：   asynpack异步   pack同步
    public final static String CS_PACK_API = LifeCircleApplicationInitializer.worker_properties.getProperty("sdp_cs_pack_api");
    
    // 打包服务service
    public final static String TASK_PACKAGING_SERVICE_ID=LifeCircleApplicationInitializer.worker_properties.getProperty("task_packaging_sevice_id");
    
    // 调度服务任务创建url
    public final static String TASK_SUBMIT_URL=LifeCircleApplicationInitializer.worker_properties.getProperty("task_submit_utl");


    //生命周期地址
    public  static String LIFE_CYCLE_DOMAIN_URL = LifeCircleApplicationInitializer.worker_properties.getProperty("lc_domain_url");
  //生命周期API接口版本号
    public  static String LIFE_CYCLE_API_VERSION = LifeCircleApplicationInitializer.worker_properties.getProperty("lc_api_url_version");
    
    //生命周期API接口地址
    public  static String LIFE_CYCLE_API_URL=LIFE_CYCLE_DOMAIN_URL+"/"+LIFE_CYCLE_API_VERSION;
    
    //拷贝worker的service id
    public  static String WORKER_COPY_SERVICE = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_copy_service");
    //转码worker的service id
    public  static String WORKER_TRANSCODE_SERVICE = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_transcode_service");
    //教案转码worker的service id
    public  static String WORKER_LESSONPLAN_TRANSCODE_SERVICE = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_lessonplan_transcode_service");
    //拷贝worker的service 优先级
    public  static String WORKER_COPY_PRIORITY = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_copy_priority");
    
    //拷贝worker的默认group id
    public  static String WORKER_DEFAULT_GROUP_ID = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_default_group_id");
    // 打包服务优先级
    public final static String PACKAGING_PRIORITY=LifeCircleApplicationInitializer.worker_properties.getProperty("packaging_priority");

    //多实例,默认传入的覆盖范围值
    public final static String DEFAULT_COVERAGE_VALUE="Org/nd";
    
    public final static boolean AUDIO_TRANSCODE = true;
    
    //视频转码service id
    public  static String WORKER_VIDEO_TRANSCODE_SERVICE = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_video_transcode_service");

    public  static String WORKER_IMAGE_TRANSCODE_SERVICE = LifeCircleApplicationInitializer.worker_properties.getProperty("worker_image_transcode_service");

    /**
     * 后门API开关
     * @see nd.esp.service.lifecycle.controllers.v06.BackDoorController
     * @author liuwx
     * */
    public static boolean BACK_DOOR_OPEN = true;

    /**
     * 操作类型 
     * @author xiezy
     */
    public static int FLAG_CREATE = 0;
    public static int FLAG_UPDATE = 1;
    public static int FLAG_DELETE = 2;
    
    //混排resType标识
    public static String RESTYPE_EDURESOURCE = "eduresource";

    //预览图key前置
    public static String RESOURCE_PREIVEW_PREFIX = "Slide";

	// elasticsearch configure
	public final static String ES_DOMAIN = LifeCircleApplicationInitializer.properties
			.getProperty("elasticsearch.domain");
	public final static String ES_PORT = LifeCircleApplicationInitializer.properties
			.getProperty("elasticsearch.port");
	public final static String ES_CLUSTER_NAME = LifeCircleApplicationInitializer.properties
			.getProperty("elasticsearch.cluster.name");
	public final static String ES_INDEX_NAME = LifeCircleApplicationInitializer.properties
			.getProperty("elasticsearch.index.name");

	// local elasticsearch configure (bylsm)
	// public final static String ES_DOMAIN="192.168.253.25";
	// public final static String ES_PORT="9300";
	// public final static String ES_CLUSTER_NAME="lcms_elasticsearch_lcms";
	// public final static String
	// ES_INDEX_NAME="test_full_function_dev_just_for_test";
	
	//titan configure
	public final static String TITAN_DOMAIN = LifeCircleApplicationInitializer.properties
			.getProperty("titan.domain");
	public final static String TITAN_SEARCH_POOL_SIZE = LifeCircleApplicationInitializer.properties
			.getProperty("titan.search.pool.size");
	public final static String TITAN_SINGLE_POOL_SIZE = LifeCircleApplicationInitializer.properties
			.getProperty("titan.single.pool.size");
	
    //课件编辑器域名
    public final static String SLIDES_URI=LifeCircleApplicationInitializer.properties.getProperty("slides.uri");
    //智能出题域名
    public final static String INTELLI_URI=LifeCircleApplicationInitializer.properties.getProperty("intelli.uri");
    //智能出题获取详情url
    public final static String INTELLI_DETAIL_URL="v0.2/api/lcmsquestions/detail/{chapterId}/K1206_en/{questionId}";
    
    public static final   Map<String,AdapterTaskResult>ADAPTER_TASK_RESULT= new HashMap<String, AdapterTaskResult> ();

    public static boolean ADAPTER_TASK_CHARGE=false;

    static {
        ADAPTER_TASK_RESULT.put("coursewareobjects",new AdapterTaskResult());
        ADAPTER_TASK_RESULT.put("questions",new AdapterTaskResult());
    }

    public static boolean ENABLE_PRE_PACK = true;
    
    /**
     * limit允许的最大查询数量
     */
    public static final int MAX_LIMIT = 500;
    
    //101PPT业务方ID
    public static final String BSYSKEY_101PPT = "6ed3fc00-5a24-4daf-9700-42e044e872ee";
    //资源门户业务方ID
    public static final String BSYSKEY_PORTAL = "bb22f321-5b83-4190-941b-a5ae080040c5";
    
    //bsyskey
    public static final String BSYSKEY = "bsyskey";
}
