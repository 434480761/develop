package com.nd.esp.task.worker.buss.packaging.service.impls;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.mysema.commons.lang.URLEncoder;
import com.nd.esp.task.worker.buss.packaging.Constant;
import com.nd.esp.task.worker.buss.packaging.Constant.CSInstanceInfo;
import com.nd.esp.task.worker.buss.packaging.model.PackagingParam;
import com.nd.esp.task.worker.buss.packaging.service.PackageService;
import com.nd.esp.task.worker.buss.packaging.support.LifeCircleException;
import com.nd.esp.task.worker.buss.packaging.utils.ArrayUtils;
import com.nd.esp.task.worker.buss.packaging.utils.CollectionUtils;
import com.nd.esp.task.worker.buss.packaging.utils.HttpClientUtils;
import com.nd.esp.task.worker.buss.packaging.utils.JDomUtils;
import com.nd.esp.task.worker.buss.packaging.utils.PackageUtil;
import com.nd.esp.task.worker.buss.packaging.utils.SessionUtil;
import com.nd.esp.task.worker.buss.packaging.utils.StringUtils;
import com.nd.esp.task.worker.buss.packaging.utils.ZipUtils;
import com.nd.esp.task.worker.buss.packaging.utils.gson.ObjectUtils;
import com.nd.esp.task.worker.container.ext.TaskTraceResult;
import com.nd.esp.task.worker.container.service.task.ExtFunService;
import com.nd.gaea.client.http.WafSecurityHttpClient;



/**
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:06
 */
@SuppressWarnings("unchecked")
@Service("packageService")
public class PackageServiceImpl implements PackageService {

    private final static Logger LOG = LoggerFactory
            .getLogger(PackageServiceImpl.class);

    @Autowired
    private JDomUtils jDomUtils;

    private final static String zipFileTempDir = PackageServiceImpl.class.getClassLoader().getResource("").getPath()
            .substring(1).replace("/classes/", "/temp").replace("/WEB-INF", "");
//    private final static String zipFileTempDir = FileUtils.getTempDirectoryPath().endsWith(File.separator)?
//            FileUtils.getTempDirectoryPath() + "lifecircle" + File.separator + "temp" :
//            FileUtils.getTempDirectoryPath() + File.separator + "lifecircle" + File.separator + "temp";
    
    public final static long chunkMax = 50L*1024*1024;//文件超过chunkMax(Byte)就要进行分块上传
    public final static long chunkSize = 5L*1024*1024;//文件分块上传时每块的大小(Byte)
    
//    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
    private final static ForkJoinPool mainPool = new ForkJoinPool();
    
    public static final String ERR_JSONPARAM = "err_jsonparam";
    public static final String ERR_PACK_PATH = "err_pack_path";
    

    
    public PackageServiceImpl(){

    }

    public void finalize() throws Throwable {

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
    public static String  createSession(String uid, String url, String path, String serviceId){
        String rootPath = path;
        if(path.indexOf('/', 1)!=-1) {
            rootPath = path.substring(0, path.indexOf('/', 1));
        }
        return SessionUtil.createSession(uid, url, rootPath, serviceId);
    }



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
    public static void GetRefPathsMap(String path, String uid, String target, 
	        Map<String, Map<String, String>> pathsGrpMap, Map<String,CSInstanceInfo> instanceMap, 
            Set<String> unzipFiles, String addonInstance) throws Exception{
        //需要根据情况,传入具体的header
        String default_header="${ref-path}"+path.substring(0, path.indexOf('/', path.indexOf('/')+1));
        JDomUtils.generatorPackPath(pathsGrpMap, path, null, target, true, path, default_header, instanceMap, unzipFiles, addonInstance);
        
        Set<String> setHeaders = pathsGrpMap.keySet();
        for(Entry<String, Map<String, String>> entry:pathsGrpMap.entrySet()) {
            if(!instanceMap.containsKey(entry.getKey())){
                String errInfo = "path包含不能处理的实例:"+entry.getKey()+";paths="+ObjectUtils.toJson(entry.getValue());
                LOG.error(errInfo);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/REQUEST_ARCHIVING_FAIL",errInfo);
            }
        }

    }


    
    /**
     * 调用打包进度查询接口
     *
     * @param path 打包文件path
     * @param requestBody 请求
     *
     * @return 打包接口返回信息
     */
//    @Override
//    public double queryPack(String url, String errMsg){
//        Map<String, Object> response =null;
//        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//        response = wafSecurityHttpClient.getForObject( url, Map.class);
//        Object msg = null;
//        if(response == null || response.get("process") == null) {
//            return -1;
//        } else {
//            msg = response.get("message");
//            if(msg!=null && !StringUtils.isEmpty(String.valueOf(msg))) {
//                errMsg = String.valueOf(msg);
//                return -1;
//            }
//        }
//
//        return Double.parseDouble(String.valueOf(response.get("process")));
//
//    }

    /**
     * 尝试调用1次打包接口
     *
     * @param url 接口url
     * @param requestBody 请求
     *
     * @return 打包接口返回信息
     */
//    private Map<String, Object> tryPack(String url,Map<String, Object> requestBody){
//        Map<String, Object> response =null;
//        //WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//        clientHttpRequestFactory.setReadTimeout(300 * 1000);
//        clientHttpRequestFactory.setConnectTimeout(100000);
//        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<Map<String,Object>>(requestBody,headers);
//        response = restTemplate.postForObject( url, entity,  Map.class);
//        return response;
//
//    }

    /**
     * 取得打包信息xml文件
     *
     * @param path sdp-package.xml文件所在路径
     * @param isFullPath 是否是绝对路径
     *
     * @return sdp-package.xml文件内容
     */
//    public String AccessXmlFile(String path,String header,
//            Map<String,CSInstanceInfo> instanceMap) throws Exception{
//        if(header.equals(Constant.CS_ADDON_INSTANCE)) {
//            path = instanceMap.get(Constant.CS_ADDON_INSTANCE).getPath()+path;
//        }
//        String url = instanceMap.get(header).getUrl()+"/"
//                +createSession("777", instanceMap.get(header).getUrl(),
//                        instanceMap.get(header).getPath(),
//                        instanceMap.get(header).getServiceId())
//                +"/static"+path+"/sdp-package.xml";
//
//        String xmlFile = null;
//        try{
//            xmlFile = HttpClientUtils.httpGet(url);
//        } catch (Exception e) {
//            LOG.info(path+"未取得打包信息xml:"+e.getMessage());
//        }
//        return xmlFile;
//    }
    
    /**
     * 从cs下载包文件到本地
     *
     * @param url 文件所在下载路径
     * @param destDir 存储目标路径
     *
     * @return
     */
//    private boolean DownloadZip(String url, String destDir) throws Exception{
//        String destPath = destDir + File.separator + url.substring(url.lastIndexOf("/")+1);
//        File destFile = new File(destPath);
//        try {
//            FileUtils.copyURLToFile(new URL(url), destFile);
//        } catch(IOException e) {
//            LOG.info("下载zip包失败");
//            throw e;
//        }
//        
//        return true;
//    }
    
    /**
     * 将本地组装好的包上传cs
     *
     * @param id 资源uuid
     *
     * @return
     */
    private static String UploadZip(String res, String id, String targetPlusName, boolean webp, String session, 
            Map<String,CSInstanceInfo> instanceMap, String default_header, StringBuffer logMsg) throws Exception{
        String fileName =id+"_"+targetPlusName;
        if(webp) {
            fileName+="_webp";
        }
//        if(icplayer) {
//            fileName+="_icplayer";
//        }
        fileName+=".zip";
        String localFilePath = zipFileTempDir + File.separator + fileName;
        File zipFile = new File(localFilePath);
        if(!zipFile.exists()){
            LOG.error("Upload File \""+localFilePath+"\" doesn't exist!");
        }      
        
        
        String rt = null;
        if (zipFile.isFile()) {
            rt = UploadFileToCS(zipFile, instanceMap.get(default_header).getPath()
                    + "/packages/" + res + "/" + id, fileName, session, instanceMap.get(default_header).getUrl(), logMsg);
        }
        
        return rt;
   
    }
    
    private static byte[] toByteArray(InputStream input, int size) throws IOException {

        byte[] data = new byte[size];
        int offset = 0;
        int readed;
//        input.reset();
//        input.skip(start);
        while (offset < size && (readed = input.read(data, offset, size - offset)) != -1) {
            offset += readed;
        }

        if (offset != size) {
            throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
        }

        return data;
    }
    
    
    
    public static String UploadFileToCS(File file, String csPath, String fileName, String session,
            String csApiUrl, StringBuffer logMsg) throws Exception{
        String rt = null;
        
        InputStream in = null;
        try {
            in = FileUtils.openInputStream(file);
        
            if(file.length() >= chunkMax) {
                int nChunks = (int)(Math.ceil(Double.valueOf(file.length())/chunkSize));
    
                int chunkNumSent = 0;
                do {
                    List<Callable<UploadThread>> tasks = new ArrayList<Callable<UploadThread>>();
                    int chunkIndexBegin = chunkNumSent;
                    int chunkIndexEnd = Math.min(chunkNumSent+20, nChunks);
                    for(int i=chunkIndexBegin; i<chunkIndexEnd; ++i) {
                        Map<String,String> requestBody = new HashMap<String,String>();
                        requestBody.put("path", csPath);
                        requestBody.put("name", fileName);
                        requestBody.put("filePath", csPath+"/"+fileName);
                        requestBody.put("scope", "1");
                        requestBody.put("size", String.valueOf(file.length()));
                        requestBody.put("chunks", String.valueOf(nChunks));
                        long startIndexInclusive = chunkSize*i;
                        long endIndexExclusive = 0;
                        long end = chunkSize*(i+1);
                        if(file.length()<end){
                            endIndexExclusive = file.length();
                        }else{
                            endIndexExclusive = end;
                        }
                        byte[] chunkBytes = toByteArray(in, (int)(endIndexExclusive-startIndexInclusive));
                        
                        //设置分块参数
                        requestBody.put("chunk", String.valueOf(i));
                        requestBody.put("chunkSize", String.valueOf(chunkBytes.length));
                        requestBody.put("pos", String.valueOf(startIndexInclusive));
                        
                        UploadThread uploadThread = new UploadThread(chunkBytes,requestBody,session,csApiUrl);
                        tasks.add(uploadThread);
                        ++chunkNumSent;
                    }
                    
                    List<Future<UploadThread>> results = mainPool.invokeAll(tasks);
                    
                    for(Future<UploadThread> result:results) {
                        try {
                            logMsg.append(result.get().getLogMsg());
                        } catch (ExecutionException e) {
                            LOG.error("向CS上传文件时错误:"+e.getMessage());
                            if(e.getCause()!=null) {
                                throw (Exception)e.getCause();
                            } else {
                                throw e;
                            }
                        }
                    };
                    
                    if(chunkIndexEnd==nChunks) {        //最后一批分块上传
                        for(Future<UploadThread> result:results) {
                            String content = "";
                            try {
                                content = result.get().getResult();
                            } catch (ExecutionException e1) {
                                LOG.error("向CS上传文件时错误:"+e1.getMessage());
                                if(e1.getCause()!=null) {
                                    throw (Exception)e1.getCause();
                                } else {
                                    throw e1;
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
                            }
                        }
                    }
                }while(chunkNumSent<nChunks);
                
            } else {
                byte [] bytes = IOUtils.toByteArray(in);
                Map<String,String> requestBody = new HashMap<String,String>();
                requestBody.put("path", csPath);
                requestBody.put("name", fileName);
                requestBody.put("filePath", csPath+"/"+fileName);
                requestBody.put("scope", "1");
                rt = UploadChunkToCS(bytes, requestBody, session, csApiUrl, logMsg);
            }
        
        } finally {
            IOUtils.closeQuietly(in);
        }
        
        return rt;
    }
    
    private static String UploadChunkToCS(byte[] bytes, Map<String,String> requestBody, String session,
            String csApiUrl, StringBuffer logMsg) throws Exception{
        String url = csApiUrl + "/upload?session=" + session;
        LOG.info("分块信息chunk"+requestBody.get("chunk")+":"+ObjectUtils.toJson(requestBody));
        logMsg.append("分块信息chunk"+requestBody.get("chunk")+":"+ObjectUtils.toJson(requestBody));
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
                            LOG.info("分块chunk"+requestBody.get("chunk")+"上传返回值："+rt);
                            logMsg.append("分块chunk"+requestBody.get("chunk")+"上传返回值："+rt);
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
            if (inputStream != null)
                inputStream.close();
        }
        
        return rt;
    }
    
    static class UploadThread implements Callable<UploadThread> {
        private final byte[] bytes;
        private final Map<String,String> requestBody;
        private final String session;
        private final String csApiUrl;
        private StringBuffer logMsg;
        private String result;
        
        public StringBuffer getLogMsg() {
            return logMsg;
        }

        public String getResult() {
            return result;
        }

        UploadThread(byte[] bytes, Map<String,String> requestBody, String session,
                String csApiUrl) {
            this.bytes = bytes;
            this.requestBody = requestBody;
            this.session = session;
            this.csApiUrl = csApiUrl;
            this.logMsg=new StringBuffer();
        }
        
        public UploadThread call() throws Exception{
            this.result = UploadChunkToCS(bytes,requestBody,session,csApiUrl,logMsg);
            return this;
        }
    }
    
    
    public static String getFileNameNoEx(String filename) { 
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
     * @param header CS实例名
     * @param uid 用户id
     * @param pathsMap sdp-package.xml解析得到的需下载的文件、目录
     * @param localStorePath 本地存储路径
     * @param webpFirst 是否优先下载.webp文件（过滤同名.png)
     *
     * @return 资源包所需文件及其存储相对路径
     */
    public static Map<String, String> GetAllFileList(String header, String uid, Map<String,String> pathsMap,
            Map<String, CSInstanceInfo> instanceMap, String localStorePath, boolean webpFirst, boolean noOgg) throws Exception{
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        Map<String, Object> requestBody = new HashMap<String, Object>();
        String url = instanceMap.get(header).getUrl() + "/dentries?session="
                +createSession(uid, instanceMap.get(header).getUrl(), 
                        instanceMap.get(header).getPath(),
                        instanceMap.get(header).getServiceId());
        
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

        Map<String, String> fileListMap = GenarateFileList(header, uid, dentryMap.get("items"), pathsMap, instanceMap,
                localStorePath, webpFirst, renameMap, true);
        return fileListMap;
    }
    
    /**
     * 调用cs接口获取所有文件列表（目录下的文件项）
     *
     * @param header CS实例名
     * @param uid 用户id
     * @param dentryList 从cs取得的目录项列表
     * @param pathsMap sdp-package.xml解析得到的需下载的文件、目录
     * @param localStorePath 本地存储路径
     * @param webpFirst 是否优先下载.webp文件（过滤同名.png)
     * @param renameMap 重名名过的文件项（包含?size)
     *
     * @return 资源包所需文件及其存储相对路径
     */
    public static Map<String, String> GenarateFileList(String header, String uid, List<Map<String, Object>> dentryList,
            Map<String,String> pathsMap, Map<String, CSInstanceInfo> instanceMap, String localStorePath,
            boolean webpFirst, Map<String,String> renameMap, boolean bFirst) throws Exception{
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
            String pathWithSize = path;
            //只有初次的地址列表需考虑size问题
            if(bFirst && renameMap.containsKey(path)) {
                pathWithSize = renameMap.get(path);
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
                List<Map<String,Object>> subDentryListPart = new ArrayList<Map<String,Object>>();
                Map<String,String> subPathsMap = new HashMap<String,String>();
                do{
                    String getListUrl = instanceMap.get(header).getUrl() + "/dentries?path="
                            + path + "&$filter=updateAt gt " + lastItemUpdateAt
                            +"&$orderby=updateAt Asc&$limit=1000&session="
                            +createSession(uid, instanceMap.get(header).getUrl(), 
                                    instanceMap.get(header).getPath(),
                                    instanceMap.get(header).getServiceId());
                    Map<String, List<Map<String,Object>>> subDentriesMap = wafSecurityHttpClient.getForObject(getListUrl, Map.class);
                    subDentryListPart = subDentriesMap.get("items");
                    if(CollectionUtils.isNotEmpty(subDentryListPart)) {
                        lastItemUpdateAt = Long.parseLong(String.valueOf(subDentryListPart.get(0).get("update_at")))-1;
                        count += subDentryListPart.size();
                        subDentryList.addAll(subDentryListPart);
                        for(Map<String,Object> subDentry:subDentryListPart){
                            String childPath = String.valueOf(subDentry.get("path"));
                            subPathsMap.put(childPath, pathsMap.get(path)+childPath.replace(path, ""));
                        }
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
                    fileListMap.putAll(GenarateFileList(header, uid, subDentryList, subPathsMap, instanceMap,
                            localStorePath, webpFirst, renameMap, false));
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
                
                String localRefPath = pathsMap.get(pathWithSize);
                String fileName = path.substring(path.lastIndexOf("/")+1);
                if(!pathsMap.get(pathWithSize).contains(fileName)) {
                    if(localRefPath.endsWith("/")) {
                        localRefPath += fileName;
                    }
                    else {
                        localRefPath += ("/" + fileName);
                    }
                }

                fileListMap.put(pathWithSize, localRefPath);
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
    public static Map<String, Map<String,String>> archivingLocal(String id, PackagingParam param, StringBuffer logMsg) throws Exception{
        String uid = param.getUid();
        String path = param.getPath();
        String addonInstance = param.getAddon_instance();
        String target = param.getTarget();
        String resType = param.getType();
        Map<String, CSInstanceInfo> instanceMap = param.getInstance_map();
        if(StringUtils.isEmpty(uid)) {
            uid = "777";
        }
        
        String default_header="${ref-path}"+path.substring(0, path.indexOf('/', path.indexOf('/')+1));
        LOG.info("default_header="+default_header+"; addonInstance="+addonInstance); 
        
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
        
        
        Map<String, Map<String,String>> finalResponse = new HashMap<String, Map<String,String>>();
        Map<String,String> downloadedFiles = new HashMap<String, String>();

        String combinedName =id + "_" + PackageUtil.TARGET_COMBINED;
        if(param.isWebpFirst()) {
            combinedName += ("_webp");
        }
        long timeStart = System.currentTimeMillis();
        for(String targetName:targetList) {
            String zipFileName =id + "_" + targetName;
            if(param.isWebpFirst()) {
                zipFileName += ("_webp");
            }
            
            //取得打包信息xml文件
            Map<String, Map<String, String>> pathsGrpMap = new HashMap<String,Map<String, String>>();
            Set<String> unzipFiles = new TreeSet<String>();
            GetRefPathsMap(path, uid, targetName, pathsGrpMap, instanceMap, unzipFiles, addonInstance);
            
            LOG.info("解析打包信息所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");
    
            
            timeStart = System.currentTimeMillis();
            
            StringBuffer finalFileList = new StringBuffer();
            List<Callable<DownloadThread>> tasks = new ArrayList<Callable<DownloadThread>>();
            for (Entry<String,Map<String,String>> entry:pathsGrpMap.entrySet()) { // create and start threads
                String header = entry.getKey();
                if(header.equals(addonInstance)) {
                    Map<String,String> addonPath = new HashMap<String,String>();
                    for(Entry<String,String> addonEntry:entry.getValue().entrySet()) {
                        addonPath.put(instanceMap.get(addonInstance).getPath()+addonEntry.getKey(),
                                addonEntry.getValue());
                    }
                    entry.setValue(addonPath);
                }
                String localStorePath = zipFileTempDir + File.separator + zipFileName;
                Map<String,String> filePathMap = GetAllFileList(header, uid, entry.getValue(), instanceMap,
                        localStorePath, param.isWebpFirst(), param.isNoOgg());
                for(Entry<String,String> filePathEntry:filePathMap.entrySet()) {
                    String filePath = filePathEntry.getKey();
                    if(downloadedFiles.containsKey(filePath)) {
                        FileUtils.moveFile(new File(downloadedFiles.get(filePath)), 
                                new File(localStorePath + filePathEntry.getValue()));
                    }
                    else {
                        String encodedFilePath = "";
                        if(filePath.contains("?size=")) {
                            encodedFilePath = URLEncoder.encodeURL(filePath.substring(0, filePath.lastIndexOf('?'))).replace("+", "%20").replace("*","%2A").replace("%7E", "~");
                            encodedFilePath += filePath.substring(filePath.lastIndexOf('?'));
                        } else {
                            encodedFilePath = URLEncoder.encodeURL(filePath).replace("+", "%20").replace("*","%2A").replace("%7E", "~");
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
            
            List<Future<DownloadThread>> results = mainPool.invokeAll(tasks, 10*60, TimeUnit.SECONDS);
            
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
        String rt = UploadZip(resType, id, target, param.isWebpFirst(), createSession(uid, 
                instanceMap.get(default_header).getUrl(), 
                instanceMap.get(default_header).getPath(),
                instanceMap.get(default_header).getServiceId()), instanceMap, default_header, logMsg);
        Map<String,Object> rtMap = ObjectUtils.fromJson(rt,  new TypeToken<Map<String,Object>>(){});
        finalResponse.put(PackageUtil.getStoreInfoKey(target, bPackIcplayer), PackageUtil.convertTostoreInfoMap(rtMap));
        
        LOG.info("压缩，上传所用时间:"+(System.currentTimeMillis()-timeStart)/1000+"s");
        
        // delete temp files
        File tempDir = new File(zipFileTempDir);
        File[] listFiles = tempDir.listFiles();
        if(null!=listFiles) {
            for(File file:listFiles) {
                if(file.getName().contains(id)) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }

        return finalResponse;
    }
    
    static class DownloadThread implements Callable<DownloadThread> {

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
                        out.append(new String(b, 0, n)); 
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


    /**
     * argument
     * {identifier:xxx,service:transcoding-ppt-to-html-service,location:xxxx}
     */
    @Override
    public void run(String identifier, String argument, TaskTraceResult taskResult,
            ExtFunService extFunService) {

        StringBuffer logMsg = new StringBuffer("BEGING\r\n");
        /**
         * 完全确认相信对方传输
         */
        String id = identifier.split("_")[0];
        PackagingParam packagingParam = null;
        try {
            packagingParam = ObjectUtils.fromJson(argument,
                    PackagingParam.class);
        } catch (Exception e1) {
            LOG.error("解析打包json参数错误", e1);
            taskResult.setExitCode(ERR_JSONPARAM);
            taskResult.setTracemsg(e1.getMessage());
            extFunService.callTraceLog(taskResult);
            extFunService.callFail(taskResult);
            return;
        }
        String failCallBackUrl = packagingParam.getCallback_api()
                + "/" + packagingParam.getType()
                + Constant.LC_PACK_CALLBACK_API
                + "?identifier=" + id
                + "&target="
                + packagingParam.getTarget()
                + "&icplayer="
                + packagingParam.getIcplayer()
                + "&webp_first="
                + packagingParam.isWebpFirst()
                + "&status=0"
                + "&err_msg=";
        String path = packagingParam.getPath();
        if (path == null) {
            LOG.error("打包路径path为null");
            taskResult.setExitCode(ERR_PACK_PATH);
            taskResult.setTracemsg("打包路径空");
            extFunService.callTraceLog(taskResult);
            failCallBackUrl += URLEncoder.encodeURL("打包路径path为null");
            taskResult.setCallBackUrl(failCallBackUrl);
            extFunService.callFail(taskResult);
            return;
        }

        
        Map<String, Map<String,String>> result = null;
        try {
            Map<String,CSInstanceInfo> instanceMap = packagingParam.getInstance_map();
            if(null == instanceMap) {
                instanceMap = GetCSInstanceMap(packagingParam.getCallback_api(),
                        packagingParam.getType());
            }
            result = archivingLocal(id, packagingParam, logMsg);
        } catch (Exception e) {
            // delete temp files
            File tempDir = new File(zipFileTempDir);
            File[] listFiles = tempDir.listFiles();
            if(null!=listFiles) {
                for(File file:tempDir.listFiles()) {
                    if(file.getName().contains(id)) {
                        FileUtils.deleteQuietly(file);
                    }
                }
            }
            taskResult.setExitCode("打包失败");
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            logMsg.insert(0, out.toString());
            taskResult.setTracemsg(logMsg.toString());
            extFunService.callTraceLog(taskResult);
            if(e.getMessage() != null) {
                failCallBackUrl += URLEncoder.encodeURL(e.getMessage()).replace("+", "%20").replace("*","%2A").replace("%7E","~");
            }
            LOG.error(logMsg.toString());
            taskResult.setCallBackUrl(failCallBackUrl);
            extFunService.callFail(taskResult);
            return;
        }
        
        if(null != result) {
            String callBackUrl = packagingParam.getCallback_api()
                    + "/" + packagingParam.getType()
                    + Constant.LC_PACK_CALLBACK_API
                    + "?identifier=" + id
                    + "&target="
                    + packagingParam.getTarget()
                    + "&icplayer="
                    + packagingParam.getIcplayer()
                    + "&webp_first="
                    + packagingParam.isWebpFirst()
                    + "&status=1"
                    + "&pack_info="
                    + ObjectUtils.toJson(result);
            taskResult.setExitCode("打包完成");
            logMsg.insert(0, "成功回调："+callBackUrl);
            taskResult.setTracemsg(logMsg.toString());
            extFunService.callTraceLog(taskResult);
            LOG.info(logMsg.toString());
            taskResult.setCallBackUrl(callBackUrl);
            extFunService.callSucceed(taskResult);
        }

    }
    
    public Map<String,CSInstanceInfo> GetCSInstanceMap(String apiUrl, String resType) throws Exception{
        Map<String,CSInstanceInfo> instanceMap = new HashMap<String,CSInstanceInfo>();
        String url = apiUrl + "/" + resType + "/packaging/instancemap";
        try{
            String response = HttpClientUtils.httpGet(url);
            instanceMap = ObjectUtils.fromJson(response, new TypeToken<Map<String,CSInstanceInfo>>(){});
        } catch (Exception e) {
            LOG.error("获取session信息失败:"+e.getMessage());
            throw e;
        }
        return instanceMap;
    }
    
    public static void main(String[] args) {
        
        System.out.println(Integer.MAX_VALUE);
        String param = "{\"uid\":\"777\",\"id\":\"0c7e31b0-9316-4a5a-8012-f54f7cdb469f\",\"noOgg\":true,\"webpFirst\":true,\"instance_map\":{\"${ref-path}/prepub_content_edu\":{\"url\":\"http://betacs.101.com/v0.1\",\"path\":\"/prepub_content_edu/esp\",\"serviceId\":\"a4b26064-595f-47a5-9f81-6afcb2d296c2\"},\"${icplayer}\":{\"url\":\"http://betacs.101.com/v0.1\",\"path\":\"/prepub_content_edu/icplayer/latest\",\"serviceId\":\"a4b26064-595f-47a5-9f81-6afcb2d296c2\"},\"${ref-path-addon}/preproduction_content_module_mng\":{\"url\":\"http://betacs.101.com/v0.1\",\"path\":\"/preproduction_content_module_mng/test\",\"serviceId\":\"40afb964-78c0-4be9-b929-3f6f909c3ecf\"},\"${ref-path}/prepub_content_edu_product\":{\"url\":\"http://betacs.101.com/v0.1\",\"path\":\"/prepub_content_edu_product/esp\",\"serviceId\":\"e49f1c47-01aa-4326-9525-ce25064ace63\"}},\"callback_api\":\"http://esp-lifecycle.pre1.web.nd/v0.6\",\"path\":\"/prepub_content_edu/esp/coursewares/0c7e31b0-9316-4a5a-8012-f54f7cdb469f.pkg\",\"target\":\"default\",\"addon_instance\":\"${ref-path-addon}/preproduction_content_module_mng\",\"type\":\"coursewares\"}";
        PackagingParam packagingParam = null;
        packagingParam = ObjectUtils.fromJson(param,
                PackagingParam.class);
        
        StringBuffer logMsg = new StringBuffer();
        Map<String, Map<String,String>> result = null;
        try {
            result = archivingLocal("22d66ed8-81ad-462c-a369-dfc9ee4091f2", packagingParam, logMsg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            String err = e.getMessage();
            e.printStackTrace();
        }
        
    }
}