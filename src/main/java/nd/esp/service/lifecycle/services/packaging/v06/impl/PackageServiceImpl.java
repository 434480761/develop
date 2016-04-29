package nd.esp.service.lifecycle.services.packaging.v06.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.persistence.LockModeType;

import nd.esp.service.lifecycle.entity.PackagingParam;
import nd.esp.service.lifecycle.entity.WorkerParam;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;
import nd.esp.service.lifecycle.services.task.v06.TaskService;
import nd.esp.service.lifecycle.services.task.v06.impls.TaskServiceImpl;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ConnectionPoolUtil;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.support.busi.SessionUtil;
import nd.esp.service.lifecycle.utils.ArrayUtils;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.JDomUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.ZipUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.reflect.TypeToken;
import com.mysema.commons.lang.URLEncoder;
import com.nd.gaea.client.http.WafSecurityHttpClient;



/**
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:06
 */
@SuppressWarnings("unchecked")
@Service("PackageServiceImpl")
public class PackageServiceImpl implements PackageService {
    private final static Logger LOG = LoggerFactory.getLogger(PackageServiceImpl.class);
    
    
    public static final String DEFAULT_UID = "777";
    public static final String DEFAULT_TARGET = "default";

    @Autowired
    private JDomUtils jDomUtils;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskStatusInfoRepository taskRepository;
    
    private static CloseableHttpClient httpClient = ConnectionPoolUtil.getHttpClient();
    
    
    /**
     * 创建任务调度，发起打包操作
     * @throws  
     * 
     */
    @Override
    @Transactional
    public void triggerPackaging(PackagingParam param) throws Exception {
        String path = param.getPath();
        String id = param.getUuid();
        String resType = param.getResType();
        String target = StringUtils.isNotEmpty(param.getTarget()) ? param.getTarget() : DEFAULT_TARGET;
        String uid = StringUtils.isNotEmpty(param.getUid()) ? param.getUid() : DEFAULT_UID;

            
        // 策略判断,如果path不符合规范,则提示
        // ${ref-path}/edu/esp/%s/%s.pkg/main.xml
        if ((!path.contains(Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath())
                && !path.contains(Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE_OTHER).getPath()))
              || !path.contains(param.getUuid()+".pkg")) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(), "元数据的href不符合规范格式");
        }

        // 2.开始打包
        String openID = id + "_" + target;
        if(param.isbWebpFirst()) {
            openID += "_webp";
        }
        
        TaskStatusInfo taskInfo=null;
        try {
            taskInfo = taskRepository.get(openID);
        } catch (EspStoreException e2) {
            e2.printStackTrace();
        }
        String oldTaskId = null;
        if(taskInfo!=null) {
            taskRepository.getEntityManager().refresh(taskInfo, LockModeType.PESSIMISTIC_WRITE);
            oldTaskId = taskInfo.getTaskId();
        }

        Map<String, Object> arg = new HashMap<>();
        arg.put("id", id);
        arg.put("type", resType);
        arg.put("target", target);
        arg.put("path", path);
        arg.put("uid", uid);
        arg.put("callback_api", Constant.LIFE_CYCLE_API_URL);
        arg.put("addon_instance", Constant.CS_ADDON_INSTANCE);
        arg.put("instance_map", Constant.CS_INSTANCE_MAP);
        arg.put("webpFirst", param.isbWebpFirst());
        arg.put("noOgg", param.isbNoOgg());
        String argument = ObjectUtils.toJson(arg);
        WorkerParam workerParam = WorkerParam.createPackParam();
        if(param.getPriority()!=null){
            workerParam.setPriority(param.getPriority());
        }
        workerParam.setIdentifier(openID);
        workerParam.setArgument(argument);
        String url = Constant.TASK_SUBMIT_URL;
        String taskId = null;
        

        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            EntityBuilder builder = EntityBuilder.create();
            builder.setText(ObjectUtils.toJson(workerParam));
            builder.setContentType(ContentType.APPLICATION_JSON);
            HttpEntity reqEntity = builder.build();

            httpPost.setEntity(reqEntity);
            response = httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            // 获取返回数据
            HttpEntity resEntity = response.getEntity();
            String rtBody = null;
            if (resEntity != null) {
                InputStream in = resEntity.getContent();
                try {
                    rtBody = IOUtils.toString(in, "utf-8");
                    if (!StringUtils.isEmpty(rtBody)) {
                        Map<String, Object> rtMap = BeanMapperUtils.mapperOnString(rtBody, Map.class);
                        if (rtMap.get("executionId") != null) {
                            taskId = String.valueOf(rtMap.get("executionId"));
                        }
                    }
                } finally {
                    if (in != null)
                        in.close();
                }
            }
            EntityUtils.consume(resEntity);
            if(statusLine == null || statusLine.getStatusCode()!=200) {
                LOG.error("打包请求出错");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + rtBody);
            }
        } catch (Exception e) {
            //处理异常
            LOG.error("打包请求出错", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + e.getMessage());
        } finally {
            if(response != null) { 
                EntityUtils.consume(response.getEntity());
                response.close();
            }
        }
        
        LOG.info("创建打包任务返回的任务ID:" + taskId);

        if(taskInfo==null) {
            taskInfo = new TaskStatusInfo();
        }
        taskInfo.setResType(resType);
        taskInfo.setBussType(TaskServiceImpl.TASK_BUSS_TYPE_PACK);
        taskInfo.setBussId(openID);
        taskInfo.setTaskId(taskId);
        taskInfo.setPriority(-workerParam.getPriority());
        taskService.CreateOrRestartTask(taskInfo);
        
        if(StringUtils.isNotEmpty(oldTaskId)) {
            String cancelUrl = Constant.TASK_SUBMIT_URL.substring(0, Constant.TASK_SUBMIT_URL.lastIndexOf("/"))
                    + "/execution-cancel?executionId=" + oldTaskId;
            try {
                HttpPost httpPost = new HttpPost(cancelUrl);
                httpPost.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
                httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
                response = httpClient.execute(httpPost);
                LOG.info("取消打包任务ID:" + oldTaskId);
            } catch (Exception e) {
                LOG.info("取消打包任务失败："+e.getMessage());
            } finally {
                if(response != null) { 
                    EntityUtils.consume(response.getEntity());
                    response.close();
                }
            }
        }
        
//        cm.closeExpiredConnections();
//        cm.closeIdleConnections(5, TimeUnit.SECONDS);
    }


    //原先是"/edu/lifecycle/dev"; 现为"/edu/esp"
//    private final String PACK_REF_PATH =Constant.CS_INSTANCE_MAP.get
//            (Constant.CS_DEFAULT_INSTANCE).getPath();
    
    // 测试："/module_mng/debug"；生产："/module_mng/release"
    private final String ADDON_PACK_REF_PATH =Constant.CS_INSTANCE_MAP.get
            (Constant.CS_ADDON_INSTANCE).getPath();
    
    private final String zipFileTempDir = FileUtils.getTempDirectoryPath()
            + "lifecircle" + File.separator + "temp";
    
    public final static long chunkMax = 50L*1024*1024;//文件超过chunkMax(Byte)就要进行分块上传
    public final static long chunkSize = 5L*1024*1024;//文件分块上传时每块的大小(Byte)
    
    private final static ExecutorService executorService = CommonHelper.getForkJoinPool();

    public PackageServiceImpl(){

    }

    /**
     * 打包下载的请求，获取下载地址
     *
     * @param path 资源所在地址,即sdp-package.xml文件所在路径（相对路径）
     * @param id 资源uuid
     * @param resType 资源类型
     * @param uid 用户id(暂时未用到)
     * @param icplayer 是否将icplayer打入离线包
     *
     * @return cs打包接口返回信息
     */
    public Map<String, Map<String,Object>> archiving(String path, String target,
            String id, String resType, String uid, String icplayer) throws Exception{
        if(StringUtils.isEmpty(uid)) {
            uid = DEFAULT_UID;
        }
        
        String default_header="${ref-path}"+path.substring(0, path.indexOf('/', path.indexOf('/')+1));
        
        boolean bPackIcplayer = (icplayer!=null&&icplayer.equals("true"));
        boolean bPackAll = (icplayer!=null&&icplayer.equals("both"));
        
        String zipFileName =id;
        if(!StringUtils.isEmpty(target) && !target.equals(PackageUtil.TARGET_DEFALUT)) {
            zipFileName += ("_"+target);
        }
        if(bPackIcplayer) {
            zipFileName+="_icplayer";
        }
        

        //取得打包信息xml文件
        long timeStart = System.currentTimeMillis();
        Map<String, Map<String, String>> pathsGrpMap = new HashMap<String,Map<String, String>>();
        Set<String> unzipFiles = new TreeSet<String>();
        /*Map<String, String> updateMap = */GetRefPathsMap(path, uid, target, pathsGrpMap, unzipFiles);
        LOG.info("解析打包信息所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");

        
        Set<String> setHeaders = pathsGrpMap.keySet();
        timeStart = System.currentTimeMillis();
        
        boolean bIsSingleInst = (setHeaders.size() == 1 && !bPackAll && !bPackIcplayer);
        int count = 0;
        List<Callable<PackThread>> tasks = new ArrayList<Callable<PackThread>>();
        for (String header:setHeaders) { // create and start threads
            PackThread packThread=new PackThread(header, id, target, uid, resType,
                    bPackIcplayer, pathsGrpMap, count, bIsSingleInst);
            tasks.add(packThread);
            ++count;
        }
        
        List<Future<PackThread>> results = executorService.invokeAll(tasks, 10*60, TimeUnit.SECONDS);
        
        
        LOG.info("调用所有打包接口及下载所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");
        
        timeStart = System.currentTimeMillis();
        Map<String, Map<String,Object>> finalResponse = new HashMap<String, Map<String,Object>>();
        for(Future<PackThread> result:results) {
            if(result == null || result.isCancelled() || result.get() == null) {
                throw new ArithmeticException("调用CS接口错误,有实例打包或下载失败");
            }
            else if(result.get().getResponse() == null
                    || StringUtils.isEmpty(String.valueOf(result.get().getResponse().get("path")))
                    || (!StringUtils.isEmpty(result.get().getErrMsg().toString()))) {
                throw new ArithmeticException(result.get().getErrMsg().toString());
            }
            
            if(result.get().getHeader().equals(default_header)) {
                finalResponse.put(PackageUtil.getStoreInfoKey(target, bPackIcplayer), result.get().getResponse());
            }
        }
        if(bIsSingleInst) {
            // delete temp files
            File tempDir = new File(zipFileTempDir);
            if(null != tempDir && null != tempDir.listFiles()) {
                for(File file:tempDir.listFiles()) {
                    if(!bPackIcplayer) {
                        if(file.getName().contains(zipFileName) && !file.getName().contains("icplayer")) {
                            FileUtils.deleteQuietly(file);
                        }
                    } else if(file.getName().contains(zipFileName)) {
                        FileUtils.deleteQuietly(file);
                    }
                }
            }
            return finalResponse;
        }
        
        // unzip files
        for(Future<PackThread> result:results) {
            String filePath = String.valueOf(result.get().getResponse().get("path"));
            ZipUtils.unzip(new File(zipFileTempDir+File.separator
                    +filePath.substring(filePath.lastIndexOf("/")+1)),
                    zipFileTempDir + File.separator + zipFileName);
        }
        for(String file:unzipFiles) {
            File zipFile = new File(zipFileTempDir+File.separator + zipFileName + file);
            ZipUtils.unzip(zipFile, zipFileTempDir + File.separator + zipFileName + file.substring(0, file.lastIndexOf("/")));
            FileUtils.deleteQuietly(zipFile);
        }
        
        // 下载icplayer.zip
        if(bPackIcplayer) {
            DownloadZip(Constant.CS_INSTANCE_MAP.get(Constant.CS_ICPLAYER_INSTANCE).getUrl() + "/"
                  + createSession(uid, 
                          Constant.CS_INSTANCE_MAP.get(Constant.CS_ICPLAYER_INSTANCE).getUrl(), 
                          Constant.CS_INSTANCE_MAP.get(Constant.CS_ICPLAYER_INSTANCE).getPath(),
                          Constant.CS_INSTANCE_MAP.get(Constant.CS_ICPLAYER_INSTANCE).getServiceId())
                  + "/static" + Constant.CS_INSTANCE_MAP.get(Constant.CS_ICPLAYER_INSTANCE).getPath()
                  + "/icplayer.zip", zipFileTempDir + File.separator + zipFileName);
        }
        
        // zip file
        ZipUtils.zip(zipFileTempDir + File.separator + zipFileName, zipFileTempDir);
        
        // upload file
        String rt = UploadZip(resType, id, target, false, createSession(uid, 
                Constant.CS_INSTANCE_MAP.get(default_header).getUrl(), 
                Constant.CS_INSTANCE_MAP.get(default_header).getPath(),
                Constant.CS_INSTANCE_MAP.get(default_header).getServiceId()), 
                default_header);
        Map<String,Object> rtMap = ObjectUtils.fromJson(rt,  new TypeToken<Map<String,Object>>(){});
        finalResponse.put(PackageUtil.getStoreInfoKey(target, bPackIcplayer), rtMap);
        
        // delete temp files
        File tempDir = new File(zipFileTempDir);
        if(null != tempDir && null != tempDir.listFiles()) {
            for(File file:tempDir.listFiles()) {
                if(bPackAll) {
                    if(file.getName().contains(zipFileName)) {
                        FileUtils.deleteQuietly(file);
                    }
                } else if(!bPackIcplayer) {
                    if(file.getName().contains(zipFileName) && !file.getName().contains("icplayer")) {
                        FileUtils.deleteQuietly(file);
                    }
                } else if(file.getName().contains(zipFileName)) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }

        LOG.info("解压，组装，上传所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");


        return finalResponse;
    }

    /**
     * 调用cs接口获取session
     *
     * @param uid 用户id
     * @param url 获取session的api
     * @param path 请求session的作用path
     * @param serviceId 服务id
     *
     * @return session id
     */
    public String  createSession(String uid, String url, String path, String serviceId){
        String rootPath = path;
        if(path.indexOf('/', 1)!=-1) {
            rootPath = path.substring(0, path.indexOf('/', 1));
        }
        return SessionUtil.createSession(uid, url, rootPath, serviceId);
    }
    
    /**
     * 调用cs接口获取 /edu 下session
     *
     * @param uid 用户id
     *
     * @return session id
     */
    @Override
    public String  createSession(String uid){
        return this.createSession(uid, 
                Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl(), 
                Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath(),
                Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getServiceId());
    }



    /**
     * 调用cs接口获取包文件更新时间
     *
     * @param path sdp-package.xml文件所在路径
     *
     * @return 资源包所需文件及其更新时间
     */
    @Override
    public Map<String,String> GetRefPathsMap(String path, String uid, String target, Map<String, Map<String, String>> pathsGrpMap,
            Set<String> unzipFiles) throws Exception{
        String default_header="${ref-path}"+path.substring(0, path.indexOf('/', path.indexOf('/')+1));
        jDomUtils.generatorPackPath(pathsGrpMap, path, null, target, true, path, default_header, unzipFiles);
        
        Map<String, String> updateMap = new HashMap<String, String>();
        Set<String> setHeaders = pathsGrpMap.keySet();
        for(String header:setHeaders) {
            if(!Constant.CS_INSTANCE_MAP.containsKey(header)){
                String errInfo = "path包含不能处理的实例:"+header;
                LOG.error(errInfo);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail
                                .getCode(),errInfo);
            }
        }

        return updateMap;

    }


    
    /**
     * 调用打包进度查询接口
     *
     * @param path 打包文件path
     * @param requestBody 请求
     *
     * @return 打包接口返回信息
     */
    @Override
    public double queryPack(String header, String path, String uid, StringBuffer errMsg) {
        String url = Constant.CS_INSTANCE_MAP.get(header).getUrl()
                +"/dentries/actions/pack/process?path="+path+"&session="
                +createSession(uid, Constant.CS_INSTANCE_MAP.get(header).getUrl(), 
                            Constant.CS_INSTANCE_MAP.get(header).getPath(),
                            Constant.CS_INSTANCE_MAP.get(header).getServiceId());
        Map<String, Object> response =null;
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        response = wafSecurityHttpClient.getForObject( url, Map.class);
        Object msg = null;
        if(response == null || response.get("process") == null) {
            return -1;
        } else {
            msg = response.get("message");
            if(msg!=null && !StringUtils.isEmpty(String.valueOf(msg))) {
                errMsg.append(String.valueOf(msg));
                return -1;
            }
        }

        return Double.parseDouble(String.valueOf(response.get("process")));

    }

    /**
     * 尝试调用1次打包接口
     *
     * @param url 接口url
     * @param requestBody 请求
     *
     * @return 打包接口返回信息
     */
    private Map<String, Object> tryPack(String url,Map<String, Object> requestBody){
        Map<String, Object> response =null;
        //WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setReadTimeout(300 * 1000);
        clientHttpRequestFactory.setConnectTimeout(100000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<Map<String,Object>>(requestBody,headers);
        response = restTemplate.postForObject( url, entity,  Map.class);
        return response;

    }

    
    /**
     * 从cs下载包文件到本地
     *
     * @param path 文件所在下载路径
     * @param id 资源uuid
     *
     * @return
     */
    private boolean DownloadZip(String url, String destDir) throws Exception{
        String destPath = destDir + File.separator + url.substring(url.lastIndexOf("/")+1);
        File destFile = new File(destPath);
        try {
            FileUtils.copyURLToFile(new URL(url), destFile);
        } catch(IOException e) {
            LOG.info("下载zip包失败");
            throw e;
        }
        
        return true;
    }
    
    /**
     * 将本地组装好的包上传cs
     *
     * @param id 资源uuid
     *
     * @return
     */
    private String UploadZip(String res, String id, String targetPlusName, boolean webp,
            String session, String default_header) throws Exception{
        String fileName =id+"_"+targetPlusName;
        if(webp) {
            fileName+="_webp";
        }

        fileName+=".zip";
        String localFilePath = zipFileTempDir + File.separator + fileName;
        File zipFile = new File(localFilePath);
        if(!zipFile.exists()){
            LOG.error("Upload File \""+localFilePath+"\" doesn't exist!");
        }      
        
        
        String rt = null;
        if (zipFile.isFile()) {
            byte[] bytesTotal = FileUtils.readFileToByteArray(zipFile);
            rt = UploadFileToCS(bytesTotal, Constant.CS_INSTANCE_MAP.get(default_header).getPath()
                    +"/packages/"+res+"/"+id, fileName, session, Constant.CS_INSTANCE_MAP.get(default_header).getUrl());
        }
        
        return rt;
   
    }
    
    @Override
    public String UploadFileToCS(byte[] bytes, String csPath, String fileName, String session,
            String csApiUrl) throws Exception{
        String rt = null;
        if(bytes.length >= chunkMax) {
            int nChunks = (int)(Math.ceil(Double.valueOf(bytes.length)/chunkSize));
            List<Callable<String>> tasks = new ArrayList<Callable<String>>();
            for(int i=0; i<nChunks; ++i) {
                Map<String,String> requestBody = new HashMap<String,String>();
                requestBody.put("path", csPath);
                requestBody.put("name", fileName);
                requestBody.put("filePath", csPath+"/"+fileName);
                requestBody.put("scope", "1");
                requestBody.put("size", String.valueOf(bytes.length));
                requestBody.put("chunks", String.valueOf(nChunks));
                int startIndexInclusive = Integer.parseInt(Long.toString(chunkSize*i));
                int endIndexExclusive = 0;
                long end = chunkSize*(i+1);
                if(bytes.length<end){
                    endIndexExclusive = Integer.parseInt(Long.toString(bytes.length));
                }else{
                    endIndexExclusive = Integer.parseInt(Long.toString(end));
                }
                byte[] chunkBytes = ArrayUtils.subarray(bytes, startIndexInclusive, endIndexExclusive);
                
                //设置分块参数
                requestBody.put("chunk", String.valueOf(i));
                requestBody.put("chunkSize", String.valueOf(chunkBytes.length));
                requestBody.put("pos", String.valueOf(startIndexInclusive));
                
                UploadThread uploadThread = new UploadThread(chunkBytes,requestBody,session);
                tasks.add(uploadThread);
            }
                
            List<Future<String>> results = executorService.invokeAll(tasks, 10*60, TimeUnit.SECONDS);
                
            for(Future<String> result:results) {
                String content = "";
                try {
                    content = result.get();
                } catch (ExecutionException e1) {
                    LOG.error("向CS上传文件时错误:"+e1.getMessage());
                    if(e1.getCause()!=null) {
                        throw (Exception)e1.getCause();
                    }
                }
                if(content.contains("dentry_id")) {
                    Map<String,Object>response = ObjectUtils.fromJson(content,
                            new TypeToken<Map<String,Object>>(){});
                    int dealTime=180;
                    do {
                        String queryUrl = csApiUrl+"/dentries/"+response.get("dentry_id")+"?session="+session;
                        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
                        response = wafSecurityHttpClient.getForObject( queryUrl, Map.class);
                        String flag =String.valueOf(response.get("flag"));
                        LOG.error("cs合并返回的flag:" + flag);

                        if(flag.equals("-3")) {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                LOG.error("等待CS上传文件合并时异常:"+e.getMessage());
                                throw e;
                            }
                        }else if(flag.equals("-1")){

                            LOG.error("CS合并的文件被删除:");
                            throw new ArithmeticException("CS合并的文件被删除,返回的flag="+flag);
                        }
                        dealTime--;

                    } while(!String.valueOf(response.get("flag")).equals("1")&&dealTime>0);
                    
                    rt = ObjectUtils.toJson(response);
                    LOG.error("合并后cs返回的response信息:" + rt);
                }
            }
        } else {
            Map<String,String> requestBody = new HashMap<String,String>();
            requestBody.put("path", csPath);
            requestBody.put("name", fileName);
            requestBody.put("filePath", csPath+"/"+fileName);
            requestBody.put("scope", "1");
            rt = UploadChunkToCS(bytes, requestBody, session);
        }
        
        return rt;
    }
    
    class UploadThread implements Callable<String> {
        private byte[] bytes;
        private Map<String,String> requestBody;
        private String session;
        
        UploadThread(byte[] bytes, Map<String,String> requestBody, String session) {
            this.bytes = bytes;
            this.requestBody = requestBody;
            this.session = session;
        }
        
        public String call() throws Exception{
            String result = UploadChunkToCS(bytes,requestBody,session);
            return result;
        }
    }
    
    private String UploadChunkToCS(byte[] bytes, Map<String,String> requestBody, String session) throws Exception{
        String url = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl()
                + "/upload?session=" + session;
        String filepath = requestBody.get("filePath");
        String fileName = filepath.substring(filepath.lastIndexOf('/')+1);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String rt = null;
        try {
            
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient httpclient = httpClientBuilder.build();
            try {
                
                HttpPost httpPost = new HttpPost(url);
    
                MultipartEntityBuilder partBuilder = MultipartEntityBuilder
                        .create();
                for(Map.Entry<String, String> entry:requestBody.entrySet()){
                    partBuilder.addPart(entry.getKey(),
                            new StringBody(entry.getValue(),ContentType.APPLICATION_JSON));
                }
                InputStreamBody inBody = new InputStreamBody(inputStream,
                        ContentType.create("application/zip"), fileName);
                partBuilder.addPart("file", inBody);
    
                HttpEntity reqEntity = partBuilder.build();
    
                httpPost.setEntity(reqEntity);
                CloseableHttpResponse response = httpclient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                try {
                    // 获取返回数据
                    //logger.debug("status line:" + response.getStatusLine());
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        InputStream in = resEntity.getContent();
                        try {
    
                            rt = IOUtils.toString(in, "utf-8");
                        } finally {
                            if (in != null)
                                in.close();
                        }
                    }
                    EntityUtils.consume(resEntity);
                    if(statusLine == null || statusLine.getStatusCode()!=200) {
                        throw new ArithmeticException("上传文件异常："+rt);
                    }
                } finally {
                    response.close();
                }
            } finally {
                httpclient.close();
            }
        } finally {
            inputStream.close();
        }
        
        return rt;
    }
    

    class PackThread implements Callable<PackThread> {

        private final String header;
        private final String id;
        private final String target;
        private final String uid;
        private final String res;
        private final boolean icplayer;
        private final Map<String,Map<String,String>> pathsGrpMap;
        private final int index;
        private final boolean isSingle;
        private Map<String,Object> response;
        private StringBuffer errMsg;

        PackThread(String header, String id, String target, String uid, String res, 
                boolean icplayer, Map<String,Map<String,String>> pathsGrpMap,
                int index, boolean isSingle) {
            this.header = header;
            this.id = id;
            this.target = target;
            this.uid = uid;
            this.res = res;
            this.icplayer = icplayer;
            this.pathsGrpMap = pathsGrpMap;
            this.index = index;
            this.isSingle = isSingle;
            this.errMsg = new StringBuffer();
        }

        public PackThread call(){

            //打包请求参数
            Map<String, Object> requestBody = new HashMap<String, Object>();
            // 异步打包接口asynpack  原同步接口pack
            String session = createSession(uid, Constant.CS_INSTANCE_MAP.get(header).getUrl(), 
                    Constant.CS_INSTANCE_MAP.get(header).getPath(),
                    Constant.CS_INSTANCE_MAP.get(header).getServiceId());
            String url = Constant.CS_INSTANCE_MAP.get(header).getUrl()+Constant.CS_PACK_API
                    +"?session="+session;

            Map<String, String> pathMap = pathsGrpMap.get(header);
//            if(header.equals(Constant.CS_DEFAULT_INSTANCE) && icplayer) {
//                pathMap.put(Constant.CS_INSTANCE_MAP.get(Constant.CS_ICPLAYER_INSTANCE).getPath()
//                        + "/icplayer.zip", "/");
//            }
            if(header.equals(Constant.CS_ADDON_INSTANCE)) {
                Map<String,String> addonPath = new HashMap<String,String>();
                for(Entry<String,String> entry:pathMap.entrySet()) {
                    addonPath.put(ADDON_PACK_REF_PATH+entry.getKey(), entry.getValue());
                }
                requestBody.put("paths", addonPath);
            } else {
                requestBody.put("paths", pathMap);
            }
            //requestBody.put("path", Constant.CS_INSTANCE_MAP.get(header).getPath()+"/packages/"+res);
            String zipFileName =id;
            if(!StringUtils.isEmpty(target) && !target.equals(PackageUtil.TARGET_DEFALUT)) {
                zipFileName += ("_"+target);
            }
            if(icplayer) {
                zipFileName+="_icplayer";
            }
            String tempFileName = zipFileName;
            if(!(header.equals(Constant.CS_DEFAULT_INSTANCE) && isSingle) ) {
                tempFileName += ("(" + index + ")");
            }

            requestBody.put("name", tempFileName+".zip");
            requestBody.put("file_path", 
                    Constant.CS_INSTANCE_MAP.get(header).getPath()+"/packages/"+res
                    +"/"+tempFileName+".zip");
            requestBody.put("scope", "1");
            requestBody.put("expire_days", "0");
            

            //调用cs打包接口， 尝试调用3次（临时方案）
            response =null;
            boolean isOk=false;
            int count=0;
            while(!isOk){
                try {
                    count++;
                    LOG.info("调用cs打包次数:"+count);
                    if(count>=3) {
                        isOk=true;
                    }
                    response = tryPack( url,
                            requestBody);
                    if(response!=null){
                        isOk=true;
                    }
                } catch (Exception e) {
                    LOG.error("调用cs打包接口:"+e.getMessage());
                    errMsg.append("调用cs打包接口:"+e.getMessage());
                    return this;
                }
            }
            
            if(response!=null) {
                if(String.valueOf(response.get("flag")).equals("-4")) {
                    boolean packProcessing = true;
                    while(packProcessing) {
                        double percent = queryPack(header, String.valueOf(response.get("path")),
                                uid, errMsg);
                        if(percent == 100.0) {
                            packProcessing = false;
                            if(header.equals(Constant.CS_DEFAULT_INSTANCE)) {
                                String queryUrl = Constant.CS_INSTANCE_MAP.get(header).getUrl()
                                        +"/dentries/"+response.get("dentry_id")+"?session="+session;
                                WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
                                response = wafSecurityHttpClient.getForObject( queryUrl, Map.class);
                            }
                        } else if(percent == -1){
                            LOG.error("查询打包进度错误");
                            return this;
                        } else {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                LOG.error("等待打包完成时异常:"+e.getMessage());
                                errMsg.append("等待打包完成时异常:"+e.getMessage());
                                return this;
                            }
                        }
                    }
                }
            
                // 单实例，无需下载
                if(isSingle) {
                    return this;
                }
                
                if(response!=null) {
                    String downloadURL = Constant.CS_INSTANCE_MAP.get(header).getUrl()
                            + "/static" + response.get("path");
                    try {
                        DownloadZip(downloadURL, zipFileTempDir);
                        
                    } catch (Exception e) {
                        LOG.info("下载解压错误:"+e.getMessage());
                        errMsg.append("下载解压错误:"+e.getMessage());
                        return this;
                    }
                }
            }
            return this;
        }
        
        public String getHeader() {
            return header;
        }

        public Map<String,Object> getResponse() {
            return response;
        }

        public void setResponse(Map<String,Object> response) {
            this.response = response;
        }

        public StringBuffer getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(StringBuffer errMsg) {
            this.errMsg = errMsg;
        }

    }
    
    
    public String getFileNameNoEx(String filename) { 
        if ((filename != null) && (filename.length() > 0)) { 
            int dot = filename.lastIndexOf('.'); 
            if ((dot >-1) && (dot < (filename.length()))) { 
                return filename.substring(0, dot); 
            } 
        } 
        return filename; 
    }
    
    /**
     * 调用cs接口获取所有文件列表（目录下的文件项）
     *
     * @param path sdp-package.xml文件所在路径
     *
     * @return 资源包所需文件及其更新时间
     */
    public Map<String, String> GetAllFileList(String header, String uid, Map<String,
            String> pathsMap, String localStorePath, boolean webpFirst) throws Exception{
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        Map<String, Object> requestBody = new HashMap<String, Object>();
        String url = Constant.CS_INSTANCE_MAP.get(header).getUrl() + "/dentries?session="
                +createSession(uid, Constant.CS_INSTANCE_MAP.get(header).getUrl(), 
                        Constant.CS_INSTANCE_MAP.get(header).getPath(),
                        Constant.CS_INSTANCE_MAP.get(header).getServiceId());
        
        
        
        Set<String> pathSet = new HashSet<String>();
        Map<String,String> renameMap = new HashMap<String,String>();
        for(String path:pathsMap.keySet()) {
            if(path.contains("?size=")) {
                String nameChanged = path.substring(0, path.lastIndexOf('?'));
                if(!pathSet.contains(nameChanged)) {
                    renameMap.put(nameChanged, path);
                }
                path = nameChanged;
            } else if(renameMap.containsKey(path)) {
                renameMap.remove(path);
            }
            pathSet.add(path);
        }
        String[] pathArray = pathSet.toArray(new String[1]);
        requestBody.put("paths",pathArray);
        org.springframework.http.HttpEntity<Object> requestEntity = new  org.springframework.http.HttpEntity<Object>(requestBody, null);
        Map<String, List<Map<String,Object>>> dentryMap= wafSecurityHttpClient.executeForObject( url,HttpMethod.PATCH, requestEntity, Map.class);

        Map<String, String> fileListMap = GenarateFileList(header, uid, dentryMap.get("items"), pathsMap,
                localStorePath, webpFirst, renameMap);
        return fileListMap;
    }
    
    /**
     * 调用cs接口获取所有文件列表（目录下的文件项）
     *
     * @param path sdp-package.xml文件所在路径
     *
     * @return 资源包所需文件及其更新时间
     */
    public Map<String, String> GenarateFileList(String header, String uid, List<Map<String, Object>> dentryList,
            Map<String,String> pathsMap, String localStorePath, boolean webpFirst, Map<String,String> renameMap) throws Exception{
        Map<String, String> fileListMap = new HashMap<String, String>();
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        
        Set<String> fileNameSet = new HashSet<String>();
        Set<String> repeatedFileNameSet = new HashSet<String>();
        if(webpFirst) {
            for(String path:pathsMap.keySet()) {
                if(!(path.contains("/assets/ppts/")&& path.contains("/data/pres/"))) {
                    break;
                }
                if(path.lastIndexOf('.') == path.length()-4 || path.lastIndexOf('.') == path.length()-5) { //*.xxx的文件
                    String fileName = path.substring(path.lastIndexOf('/')+1);
                    String fileNameNoEx = getFileNameNoEx(fileName);
                    if(fileNameSet.contains(fileNameNoEx)) {
                        repeatedFileNameSet.add(fileNameNoEx);
                    } else {
                        fileNameSet.add(fileNameNoEx);
                    }
                }
            }
        }
        
        for(Map<String,Object> dentry:dentryList){
            String path = String.valueOf(dentry.get("path"));
            if(renameMap.containsKey(path)) {
                path = renameMap.get(path);
            }
//            if(!pathsMap.containsKey(path))
//            {
//                LOG.error("获取目录项信息错误：取得的目录项"+path+"不在请求列表中");
//                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        "LC/REQUEST_ARCHIVING_FAIL","获取目录项信息错误：取得的目录项"+path+"不在请求列表中");
//            }
            //目录项类型为 目录
            if(String.valueOf(dentry.get("type")).equals("0")) {
                int count = 0;
                long lastItemUpdateAt = 0;
                List<Map<String,Object>> subDentryList = new ArrayList<Map<String,Object>>();
                List<Map<String,Object>> subDentryListPart;
                Map<String,String> subPathsMap = new HashMap<String,String>();
                do{
                    String getListUrl = Constant.CS_INSTANCE_MAP.get(header).getUrl() + "/dentries?path="
                            + path + "&$filter=updateAt gt " + lastItemUpdateAt
                            +"&$orderby=updateAt Asc&$limit=1000&session="
                            +createSession(uid, Constant.CS_INSTANCE_MAP.get(header).getUrl(), 
                                    Constant.CS_INSTANCE_MAP.get(header).getPath(),
                                    Constant.CS_INSTANCE_MAP.get(header).getServiceId());
                    Map<String, List<Map<String,Object>>> subDentriesMap = wafSecurityHttpClient.getForObject(getListUrl, Map.class);
                    subDentryListPart = subDentriesMap.get("items");
                    lastItemUpdateAt = Long.parseLong(String.valueOf(subDentryListPart.get(0).get("update_at")))-1;
                    count += subDentryListPart.size();
                    subDentryList.addAll(subDentryListPart);
                    for(Map<String,Object> subDentry:subDentryList){
                        String childPath = String.valueOf(subDentry.get("path"));
                        subPathsMap.put(childPath, pathsMap.get(path)+childPath.replace(path, ""));
                    }
                }while(subDentryListPart.size() == 1000);
                
                LOG.info(path+"包含目录项："+count);
                
                if(0 == count) { //空目录项，创建
                    String localRefPath = pathsMap.get(path);
                    String fileName = path.substring(path.lastIndexOf("/")+1);
                    if(!pathsMap.get(path).endsWith(fileName)) {
                        if(localRefPath.endsWith("/")) {
                            localRefPath += fileName;
                        }
                        else {
                            localRefPath += ("/" + fileName);
                        }
                    }
                    String localFilePath = localStorePath + localRefPath;
                    File directory = new File(localFilePath);
                    FileUtils.forceMkdir(directory);
                } else{
                    fileListMap.putAll(GenarateFileList(header, uid, subDentryList, subPathsMap,
                            localStorePath, webpFirst, renameMap));
                }
            } else { //目录项类型为文件
                if(webpFirst && path.contains("/assets/ppts/")&& path.contains("/data/pres/")) {
                    String fileName = path.substring(path.lastIndexOf('/')+1);
                    String fileNameNoEx = getFileNameNoEx(fileName);
                    //重复文件名文件如：123.png & 123.webp, 过滤
                    if(repeatedFileNameSet.contains(fileNameNoEx)
                            && (fileName.endsWith(".png") || fileName.endsWith(".ogg"))) {
                        continue;
                    }
                }
                
                String localRefPath = pathsMap.get(path);
                String fileName = path.substring(path.lastIndexOf("/")+1);
                if(!pathsMap.get(path).contains(fileName)) {
                    if(localRefPath.endsWith("/")) {
                        localRefPath += fileName;
                    }
                    else {
                        localRefPath += ("/" + fileName);
                    }
                }
                localRefPath = localRefPath.replace("//", "/");
                // 缩略图需要本地保存文件名截取掉 "?"后内容
                if(fileName.contains("?size=")) {
                    localRefPath = localRefPath.substring(0, localRefPath.lastIndexOf('?'));
                }
                fileListMap.put(path, localRefPath);
            }
        }

        return fileListMap;

    }
    
    /**
     * 打包下载的请求，获取下载地址
     *
     * @param path 资源所在地址,即sdp-package.xml文件所在路径（相对路径）
     * @param id 资源uuid
     * @param resType 资源类型
     * @param uid 用户id(暂时未用到)
     * @param icplayer 是否将icplayer打入离线包
     *
     * @return cs打包接口返回信息
     */
    @Override
    public Map<String, Map<String,Object>> archivingLocal(String path, String target,
            String id, String resType, String uid, boolean webpFirst, StringBuffer logMsg) throws Exception{
        if(StringUtils.isEmpty(uid)) {
            uid = DEFAULT_UID;
        }
        Map<String, CSInstanceInfo> instanceMap = Constant.CS_INSTANCE_MAP;
        String default_header="${ref-path}"+path.substring(0, path.indexOf('/', path.indexOf('/')+1));
        

        boolean bPackIcplayer = false;
        boolean bMultiTarget = target.equals(PackageUtil.TARGET_COMBINED);
        List<String> targetList = new ArrayList<String>();
        if(StringUtils.isEmpty(target)) {
            targetList.add(PackageUtil.TARGET_DEFALUT);
        } else if(bMultiTarget) {
            targetList.add(PackageUtil.TARGET_DEFALUT);
            targetList.add(PackageUtil.TARGET_STUDENT);
        } else {
            targetList.add(target);
        }
        
        Map<String, Map<String,Object>> finalResponse = new HashMap<String, Map<String,Object>>();
        Map<String,String> downloadedFiles = new HashMap<String, String>();

        String combinedName =id + "_combined";
        if(webpFirst) {
            combinedName += ("_webp");
        }
        long timeStart = System.currentTimeMillis();
        for(String targetName:targetList) {
            String zipFileName =id + "_" + targetName;
            if(webpFirst) {
                zipFileName += ("_webp");
            }
            
            //取得打包信息xml文件
            Map<String, Map<String, String>> pathsGrpMap = new HashMap<String,Map<String, String>>();
            Set<String> unzipFiles = new TreeSet<String>();
            GetRefPathsMap(path, uid, targetName, pathsGrpMap, unzipFiles);
            
            LOG.info("解析打包信息所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");
    
            timeStart = System.currentTimeMillis();
            
            StringBuffer finalFileList = new StringBuffer();
            List<Callable<DownloadThread>> tasks = new ArrayList<Callable<DownloadThread>>();
            for (Entry<String,Map<String,String>> entry:pathsGrpMap.entrySet()) { // create and start threads
                String header = entry.getKey();
                if(header.equals(Constant.CS_ADDON_INSTANCE)) {
                    Map<String,String> addonPath = new HashMap<String,String>();
                    for(Entry<String,String> addonEntry:entry.getValue().entrySet()) {
                        addonPath.put(instanceMap.get(Constant.CS_ADDON_INSTANCE).getPath()+addonEntry.getKey(),
                                addonEntry.getValue());
                    }
                    pathsGrpMap.put(header, addonPath);
                }
                String localStorePath = zipFileTempDir + File.separator + zipFileName;
                Map<String,String> filePathMap = GetAllFileList(header, uid, entry.getValue(),
                        localStorePath, webpFirst);
                for(Entry<String,String> filePathEntry:filePathMap.entrySet()) {
                    String filePath = filePathEntry.getKey();
                    if(downloadedFiles.containsKey(filePath)) {
                        FileUtils.moveFile(new File(downloadedFiles.get(filePath)), 
                                new File(localStorePath + filePathEntry.getValue()));
                    }
                    else {
                        String encodedFilePath = "";
                        if(filePath.contains("?size=")) {
                            encodedFilePath = URLEncoder.encodeURL(filePath.substring(0, filePath.lastIndexOf('?')));
                            encodedFilePath += filePath.substring(filePath.lastIndexOf('?'));
                        } else {
                            encodedFilePath = URLEncoder.encodeURL(filePath);
                        }
                        
                        String downloadURL = instanceMap.get(header).getUrl()+"/"
                                +createSession(uid, instanceMap.get(header).getUrl(), 
                                        instanceMap.get(header).getPath(),
                                        instanceMap.get(header).getServiceId())
                                + "/static" + encodedFilePath;
                        
                        finalFileList.append(encodedFilePath+"\r\n");
                        String localFilePath = localStorePath + filePathEntry.getValue();
                        DownloadThread downloadThread=new DownloadThread(downloadURL, localFilePath);
                        tasks.add(downloadThread);
                    }
                    downloadedFiles.put(filePath, localStorePath + filePathEntry.getValue());
                }
            }
            
            logMsg.append("下载文件列表：\r\n"+finalFileList.toString());
            
            List<Future<DownloadThread>> results = executorService.invokeAll(tasks, 10*60, TimeUnit.SECONDS);
            
            LOG.info("调用所有CS接口下载所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");
            
            timeStart = System.currentTimeMillis();
            
            for(Future<DownloadThread> result:results) {
                if(result == null || result.isCancelled() || result.get() == null) {
                    throw new ArithmeticException("调用CS接口错误,有实例打包或下载失败");
                }
                else if(!StringUtils.isEmpty(result.get().getErrMsg())) {
                    throw new ArithmeticException(result.get().getErrMsg());
                }
            }
            
            // unzip files
            for(String file:unzipFiles) {
                File zipFile = new File(zipFileTempDir+File.separator + zipFileName + file);
                ZipUtils.unzip(zipFile, zipFileTempDir + File.separator + zipFileName + file.substring(0, file.lastIndexOf("/")));
                FileUtils.deleteQuietly(zipFile);
            }
            
            // zip file
            if(bMultiTarget) {
                ZipUtils.zip(zipFileTempDir + File.separator + zipFileName, zipFileTempDir+File.separator+combinedName);
            } else {
                ZipUtils.zip(zipFileTempDir + File.separator + zipFileName, zipFileTempDir);
            }
            
            
        }
        if(bMultiTarget) {
            ZipUtils.zip(zipFileTempDir+File.separator+combinedName, zipFileTempDir);
        }

        
        // upload file
        LOG.info("default_header:"+default_header+"; url="+instanceMap.get(default_header).getUrl());
        String rt = UploadZip(resType, id, target, webpFirst, createSession(uid, 
                instanceMap.get(default_header).getUrl(), 
                instanceMap.get(default_header).getPath(),
                instanceMap.get(default_header).getServiceId()), default_header);
        Map<String,Object> rtMap = ObjectUtils.fromJson(rt,  new TypeToken<Map<String,Object>>(){});
        finalResponse.put(PackageUtil.getStoreInfoKey(target, bPackIcplayer), rtMap);
        
        LOG.info("压缩，上传所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");
        
        // delete temp files
        File tempDir = new File(zipFileTempDir);
        if(null != tempDir && null != tempDir.listFiles()) {
            for(File file:tempDir.listFiles()) {
                if(file.getName().contains(id)) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }

        return finalResponse;
    }
    
    class DownloadThread implements Callable<DownloadThread> {

        private final String url;
        private final String localPath;
        private String errMsg;

        DownloadThread(String url, String localPath) {
            this.url = url;
            this.localPath = localPath;
            this.errMsg = "";
        }

        public DownloadThread call(){

            File destFile = new File(localPath);
            try {
                HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300)
                {
                    InputStream in = connection.getErrorStream();
                    StringBuffer out = new StringBuffer(); 
                    byte[] b = new byte[4096]; 
                    for (int n; (n = in.read(b)) != -1;) { 
                        out.append(new String(b,0,n,"utf-8")); 
                    } 
                    errMsg = "下载文件："+url+"失败:"+out.toString();
                    LOG.info(errMsg);
                    return this;
                }
                
                InputStream input = connection.getInputStream();
                FileUtils.copyInputStreamToFile(input, destFile);
            } catch (IOException e) {
                errMsg = "下载文件："+url+"失败:"+e.getMessage();
                LOG.info(errMsg);
            }
            
            return this;
        }
        

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }

        public String getUrl() {
            return url;
        }

        public String getLocalPath() {
            return localPath;
        }

    }
    
    public static void main(String[] args) {
        // 2.开始打包
//        String openID = id + "_" + target;
//        if(param.isbWebpFirst()) {
//            openID += "_webp";
//        }
//
//        Map<String, Object> arg = new HashMap<>();
//        arg.put("id", id);
//        arg.put("type", resType);
//        arg.put("target", target);
//        arg.put("path", path);
//        arg.put("uid", uid);
//        arg.put("callback_api", Constant.LIFE_CYCLE_API_URL);
//        arg.put("addon_instance", Constant.CS_ADDON_INSTANCE);
//        arg.put("instance_map", Constant.CS_INSTANCE_MAP);
//        arg.put("webpFirst", param.isbWebpFirst());
//        arg.put("noOgg", param.isbNoOgg());
//        String argument = ObjectUtils.toJson(arg);
//        WorkerParam workerParam = WorkerParam.createPackParam();
//        if(param.getPriority()!=null){
//            workerParam.setPriority(param.getPriority());
//        }
//        workerParam.setIdentifier(openID);
//        workerParam.setArgument(argument);
//        String url = "http://esp-async-task.pre1.web.nd/concurrent/service/proxy/submit-execution";
//        RestTemplate template = new RestTemplate();
//        String taskId = null;
//        
//        ResponseEntity<String> rt;
//        try {
//            rt = template.postForEntity(url, workerParam, String.class);
//            if (!StringUtils.isEmpty(rt.getBody())) {
//                Map<String, Object> rtMap = BeanMapperUtils.mapperOnString(rt.getBody(), Map.class);
//                if (rtMap.get("executionId") != null) {
//                    taskId = String.valueOf(rtMap.get("executionId"));
//                }
//            }
//        } catch (Exception e1) {
//            LOG.error("打包请求出错", e1);
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
//                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + e1.getMessage());
//        }
//        
//        LOG.info("创建打包任务返回的任务ID:" + taskId);
//
//        TaskStatusInfo taskInfo = new TaskStatusInfo();
//        taskInfo.setResType(resType);
//        taskInfo.setBussType(TaskServiceImpl.TASK_BUSS_TYPE_PACK);
//        taskInfo.setBussId(openID);
//        taskInfo.setTaskId(taskId);
//        taskInfo.setPriority(-workerParam.getPriority());
//        String oldTaskId = taskService.CreateOrRestartTask(taskInfo);
//        
//        if(StringUtils.isNotEmpty(oldTaskId)) {
//            String cancelUrl = url.substring(0, url.lastIndexOf("/"))
//                    + "/execution-cancel?executionId=" + oldTaskId;
//            try {
//                rt = template.postForEntity(cancelUrl,null,String.class);
//                LOG.info("取消打包任务ID:" + oldTaskId + "; rt="+rt);
//            } catch (Exception e) {
//                LOG.info("取消打包任务失败："+e.getMessage());
//            }
//        }
    }
    
}