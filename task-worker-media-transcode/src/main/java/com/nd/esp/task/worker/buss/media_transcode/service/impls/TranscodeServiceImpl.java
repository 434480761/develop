package com.nd.esp.task.worker.buss.media_transcode.service.impls;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.reflect.TypeToken;
import com.mysema.commons.lang.URLEncoder;
import com.nd.esp.task.worker.buss.media_transcode.Constant;
import com.nd.esp.task.worker.buss.media_transcode.Constant.CSInstanceInfo;
import com.nd.esp.task.worker.buss.media_transcode.model.MediaInfo;
import com.nd.esp.task.worker.buss.media_transcode.model.TranscodeParam;
import com.nd.esp.task.worker.buss.media_transcode.model.TranscodeResult;
import com.nd.esp.task.worker.buss.media_transcode.service.TranscodeService;
import com.nd.esp.task.worker.buss.media_transcode.support.LifeCircleException;
import com.nd.esp.task.worker.buss.media_transcode.utils.ArrayUtils;
import com.nd.esp.task.worker.buss.media_transcode.utils.CollectionUtils;
import com.nd.esp.task.worker.buss.media_transcode.utils.HttpClientUtils;
import com.nd.esp.task.worker.buss.media_transcode.utils.JDomUtils;
import com.nd.esp.task.worker.buss.media_transcode.utils.PackageUtil;
import com.nd.esp.task.worker.buss.media_transcode.utils.SessionUtil;
import com.nd.esp.task.worker.buss.media_transcode.utils.StringUtils;
import com.nd.esp.task.worker.buss.media_transcode.utils.gson.ObjectUtils;
import com.nd.esp.task.worker.container.ext.TaskTraceResult;
import com.nd.esp.task.worker.container.service.task.ExtFunService;
import com.nd.gaea.client.http.WafSecurityHttpClient;



/**
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:06
 */
@SuppressWarnings("unchecked")
@Service("mediaTranscodeService")
public class TranscodeServiceImpl implements TranscodeService {

    private final static Logger LOG = LoggerFactory
            .getLogger(TranscodeServiceImpl.class);

    @Autowired
    private JDomUtils jDomUtils;


    private final static String zipFileTempDir = FileUtils.getTempDirectoryPath().endsWith(File.separator)?
            FileUtils.getTempDirectoryPath() + "lifecircle" + File.separator + "transcode_temp" :
            FileUtils.getTempDirectoryPath() + File.separator + "lifecircle" + File.separator + "transcode_temp";
    
    public final static long chunkMax = 50L*1024*1024;//文件超过chunkMax(Byte)就要进行分块上传
    public final static long chunkNumMax = 20;//最多并行发送的分块数
    public final static long chunkSize = 5L*1024*1024;//文件分块上传时每块的大小(Byte)
    
    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

    public static final String ERR_JSONPARAM = "err_jsonparam";
    public static final String ERR_PACK_PATH = "err_pack_path";
    
    public static final String TRANSCODE_SUBTYPE = "subtype";
    public static final String SUBTYPE_VIDEO = "video";
    public static final String SUBTYPE_AUDIO = "audio";

    
    public TranscodeServiceImpl(){

    }

    public void finalize() throws Throwable {

    }



    
    /**
     * 从cs下载文件到本地
     *
     * @param url 文件所在下载路径
     * @param destDir 存储目标路径
     *
     * @return
     */
    private static boolean DownloadFile(String url, String destPath, StringBuffer errMsg, String session) throws Exception{
        File destFile = new File(destPath);

        HttpURLConnection connection = (HttpURLConnection)new URL(url+"&session="+session).openConnection();
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
            errMsg.append("下载文件："+url+"失败:"+out.toString());
            LOG.info(errMsg.toString());
            return false;
        }
        
        InputStream input = connection.getInputStream();
        FileUtils.copyInputStreamToFile(input, destFile);
        
        return true;
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
                    
                    List<Future<UploadThread>> results = executorService.invokeAll(tasks);
                    
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
//        logMsg.append("分块信息chunk"+requestBody.get("chunk")+":"+ObjectUtils.toJson(requestBody));
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
//                            logMsg.append("分块chunk"+requestBody.get("chunk")+"上传返回值："+rt);
                        } finally {
                            if (in != null)
                                in.close();
                        }
                    }
                    EntityUtils.consume(resEntity);
                    if(statusLine == null || statusLine.getStatusCode()!=200) {
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "LC/REQUEST_ARCHIVING_FAIL","上传文件异常："+rt);
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
            this.logMsg = new StringBuffer();
        }
        
        public UploadThread call() throws Exception{
            result = UploadChunkToCS(bytes,requestBody,session,csApiUrl, logMsg);
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
    
    
    
    
    public static int RunCommand(String cmd, StringBuffer outputMsg, String path, StringBuffer logMsg) throws Exception {
        logMsg.append("  Run command:"+cmd+System.getProperty("line.separator"));
        LOG.info("Run command:"+cmd);
        
        ProcessBuilder builder = null;
        if(SystemUtils.IS_OS_LINUX){
            builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        } else if(SystemUtils.IS_OS_WINDOWS){
            builder = new ProcessBuilder("cmd", "/c", cmd);
            builder.directory(new File(path));
        }
        
        //both read inputstream and errstream
        builder.redirectErrorStream(true);
        Process process = builder.start();
        Scanner scanner = new Scanner(process.getInputStream(), "GBK");
        StringBuilder rt = new StringBuilder();
        while (scanner.hasNextLine()) {
            rt.append(scanner.nextLine());
            rt.append(System.getProperty("line.separator"));
        }
        scanner.close();
        outputMsg.append(rt.toString()+System.getProperty("line.separator"));
        
        int resultValue = process.waitFor();

        return resultValue;
    }
    
    public static TranscodeResult transcodeAudio(String id, TranscodeParam param, StringBuffer logMsg) throws Exception{
        TranscodeResult result = new TranscodeResult();
        Map<String,String> extParam = param.getExt_param();
        
        String srcFileName = param.getLocation().substring(param.getLocation().lastIndexOf("/")+1);
        if(srcFileName.contains("%")) {
            srcFileName = System.currentTimeMillis()/1000 + srcFileName.substring(srcFileName.lastIndexOf("."));
        }
        String NameWithoutEx = getFileNameNoEx(srcFileName);
        if(extParam.get("targetFmt")==null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/MEDIA_TRANSCODE_FAIL","目标格式targetFmt为空");
        }
        String targetFileName = NameWithoutEx+"."+extParam.get("targetFmt");
        String srcDir = zipFileTempDir+File.separator+id+File.separator+"src";
        String destDir = zipFileTempDir+File.separator+id+File.separator+"targets";
        
        List<String> cmds = param.getCommands();
        extParam.put("src", srcDir+File.separator+srcFileName);
        extParam.put("target", destDir+File.separator+targetFileName);
        FileUtils.forceMkdir(new File(srcDir));
        FileUtils.forceMkdir(new File(destDir));
        
        long timeStart = System.currentTimeMillis();
        StringBuffer errMsg = new StringBuffer();
        logMsg.append("  Download path:"+param.getLocation()+"&session="+param.getSession()+System.getProperty("line.separator"));
        LOG.info("Download path:"+param.getLocation()+"&session="+param.getSession());
        if(!DownloadFile(param.getLocation(), srcDir+File.separator+srcFileName, errMsg, param.getSession())) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                  "LC/MEDIA_TRANSCODE_FAIL",errMsg.toString());
        }
        
        LOG.info("下载消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("下载消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();
        
        String path = URLDecoder.decode(TranscodeServiceImpl.class.getClassLoader().getResource("tools").getPath().substring(1).
                replace("/", File.separator), "UTF-8");
        if(!path.endsWith(File.separator)) {
            path += File.separator;
        }
        if(SystemUtils.IS_OS_LINUX && !path.startsWith(File.separator)){
            path = File.separator + path;
        }
        
        Map<String,String> srcMetadata = new HashMap<String,String>();
        long srcDuration = getMediaMetadata(extParam.get("src"), path, logMsg, srcMetadata);
        LOG.info("srcMetadata: "+ObjectUtils.toJson(srcMetadata));
        logMsg.append("  srcMetadata: "+ObjectUtils.toJson(srcMetadata)+System.getProperty("line.separator"));
        
        if(!cmds.isEmpty() && StringUtils.isNotEmpty(cmds.get(0))) {
            executeTranscodeCmds(cmds, extParam, path, logMsg);
        } else {
            extParam.put("target", extParam.get("src"));
        }
        
        LOG.info("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();

        String localFilePath = extParam.get("target");
        Map<String,String> targetMetadata = new HashMap<String,String>();
        if(!extParam.get("src").equals(localFilePath)) {
            long targetDuration = getMediaMetadata(localFilePath, path, logMsg, targetMetadata);
            LOG.info("  targetMetadata: "+ObjectUtils.toJson(targetMetadata));
            logMsg.append("  targetMetadata: "+ObjectUtils.toJson(targetMetadata)+System.getProperty("line.separator"));
            if(srcDuration>0 && targetDuration>0 && !logMsg.toString().contains("Run command:ffprobe") 
                    && java.lang.Math.abs(targetDuration-srcDuration)>20.0) {
                
                LOG.warn("转码目标文件与原文件时长不符: src="+srcDuration+"s; target="+targetDuration+"s");
                logMsg.append("转码目标文件与原文件时长不符: src="+srcDuration+"s; target="+targetDuration+"s");
                result.setErrMsg("转码目标文件与原文件时长不符: src="+srcDuration+"s; target="+targetDuration+"s");    
            }
            File targetFile = new File(localFilePath);
            if(!targetFile.exists()){
                String msg = "Upload File \""+localFilePath+"\" doesn't exist!";
                LOG.error(msg);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/MEDIA_TRANSCODE_FAIL",msg);
            }      
            
            logMsg.append("  Upload File: \""+localFilePath+System.getProperty("line.separator"));
            LOG.info("Upload File: \""+localFilePath);
            String rt = null;
            if (targetFile.isFile()) {
                rt = UploadFileToCS(targetFile, param.getTarget_location()+"/transcode/audios",
                        targetFileName, param.getSession(),
                        param.getCs_api_url(), logMsg);
            }
            result.setHref("${ref-path}"+param.getTarget_location()+"/transcode/audios/"+targetFileName);
        } else {
            targetMetadata = srcMetadata;
            result.setHref("${ref-path}"+param.getLocation().replaceAll(".*path=", ""));
        }
        
        FileUtils.deleteQuietly(new File(zipFileTempDir+File.separator+id));
        
        LOG.info("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        
        
        Map<String,String> metaMap = new HashMap<String,String>();
        metaMap.put("source", ObjectUtils.toJson(srcMetadata));
        metaMap.put(extParam.get("targetFmt"), ObjectUtils.toJson(targetMetadata));
        Map<String,String> locations = new HashMap<String,String>();
        locations.put("href", result.getHref());
        result.setLocations(locations);
        result.setMetadata(metaMap);
        result.setStatus(1);
        
        return result;
    }
    
    public static TranscodeResult transcode(String id, TranscodeParam param, StringBuffer logMsg) throws Exception{
        TranscodeResult result = new TranscodeResult();
        Map<String,String> extParam = param.getExt_param();
        
        String srcFileName = param.getLocation().substring(param.getLocation().lastIndexOf("/")+1);
        if(srcFileName.contains("%")) {
            srcFileName = System.currentTimeMillis()/1000 + srcFileName.substring(srcFileName.lastIndexOf("."));
        }
        String NameWithoutEx = getFileNameNoEx(srcFileName);
        if(extParam.get("targetFmt")==null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/MEDIA_TRANSCODE_FAIL","目标格式targetFmt为空");
        }
        String targetFileName = NameWithoutEx+"."+extParam.get("targetFmt");
        String srcDir = zipFileTempDir+File.separator+id+File.separator+"src";
        String destDir = zipFileTempDir+File.separator+id+File.separator+"targets";
        String previewDir = zipFileTempDir+File.separator+id+File.separator+"previews";
        String coverDir = zipFileTempDir+File.separator+id+File.separator+"cover";
        File preview = new File(previewDir);
        FileUtils.forceMkdir(preview);
        FileUtils.forceMkdir(new File(coverDir));
        
        List<String> cmds = param.getCommands();
        extParam.put("src", srcDir+File.separator+srcFileName);
        extParam.put("target", destDir+File.separator+targetFileName);
        extParam.put("targetPreview", previewDir);
        extParam.put("targetCover", coverDir);
        
        
        long timeStart = System.currentTimeMillis();
        StringBuffer errMsg = new StringBuffer();
        logMsg.append("  Download path:"+param.getLocation()+"&session="+param.getSession()+System.getProperty("line.separator"));
        LOG.info("Download path:"+param.getLocation()+"&session="+param.getSession());
        if(!DownloadFile(param.getLocation(), srcDir+File.separator+srcFileName, errMsg, param.getSession())) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                  "LC/MEDIA_TRANSCODE_FAIL",errMsg.toString());
        }
        
        LOG.info("下载消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("下载消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();
        
        String path = URLDecoder.decode(TranscodeServiceImpl.class.getClassLoader().getResource("tools").getPath().substring(1).
                replace("/", File.separator), "UTF-8");
        if(!path.endsWith(File.separator)) {
            path += File.separator;
        }
        if(SystemUtils.IS_OS_LINUX && !path.startsWith(File.separator)){
            path = File.separator + path;
        }
        
        Map<String,String> srcMetadata = new HashMap<String,String>();
        long srcDuration = getMediaMetadata(extParam.get("src"), path, logMsg, srcMetadata);
        LOG.info("srcMetadata: "+ObjectUtils.toJson(srcMetadata));
        logMsg.append("  srcMetadata: "+ObjectUtils.toJson(srcMetadata)+System.getProperty("line.separator"));
        
        int coverNum = 16;
        if(extParam.get("coverNum")!=null) {
            coverNum = new BigDecimal(extParam.get("coverNum")).intValue();
        }
        if(srcDuration<16) {
            extParam.put("intervalTime", "1");
        } else {
            extParam.put("intervalTime", String.valueOf(srcDuration/coverNum));
        }
        
        Map<String,String> targetsMap = new HashMap<String,String>();
        String defaultKey = filterMutiTargetCmds(cmds, srcMetadata, targetsMap, destDir, targetFileName);
        
        executeTranscodeCmds(cmds, extParam, path, logMsg);
        
        LOG.info("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();
        Map<String,Map<String,String>> targetsMetadata = new HashMap<String,Map<String,String>>();
        for(String key : targetsMap.keySet()) {
            String localFilePath = targetsMap.get(key);
            Map<String,String> targetMetadata = new HashMap<String,String>();
            long targetDuration = getMediaMetadata(localFilePath, path, logMsg, targetMetadata);
            targetsMetadata.put(key, targetMetadata);
            LOG.info("  targetMetadata: "+ObjectUtils.toJson(targetMetadata));
            logMsg.append("  targetMetadata: "+ObjectUtils.toJson(targetMetadata)+System.getProperty("line.separator"));
            if(srcDuration>0 && targetDuration>0 && !logMsg.toString().contains("Run command:ffprobe") 
                    && java.lang.Math.abs(targetDuration-srcDuration)>20.0) {
                
//                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        "LC/MEDIA_TRANSCODE_FAIL","转码目标视频与原视频时长不符: src="+srcDuration+"s; target="+targetDuration+"s");
                LOG.warn("转码目标文件与原文件时长不符: src="+srcDuration+"s; target="+targetDuration+"s");
                logMsg.append("转码目标文件与原文件时长不符: src="+srcDuration+"s; target="+targetDuration+"s");
                result.setErrMsg("转码目标文件与原文件时长不符: src="+srcDuration+"s; target="+targetDuration+"s");
            }
            
            File targetFile = new File(localFilePath);
            if(!targetFile.exists()){
                String msg = "Upload File \""+localFilePath+"\" doesn't exist!";
                LOG.error(msg);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/MEDIA_TRANSCODE_FAIL",msg);
            }      
            
            logMsg.append("  Upload File: \""+localFilePath+System.getProperty("line.separator"));
            LOG.info("Upload File: \""+localFilePath);
            String rt = null;
            if (targetFile.isFile()) {
                rt = UploadFileToCS(targetFile, param.getTarget_location()+"/transcode/videos/"+key,
                        targetFileName, param.getSession(),
                        param.getCs_api_url(), logMsg);
            }
        }
        
        String rt = null;
        List<String> previewList = new ArrayList<String>();
        int previewCount = 1;
        for(File thumbnail:preview.listFiles()) {
            rt = UploadFileToCS(thumbnail, param.getTarget_location()+"/transcode/previews", thumbnail.getName(), param.getSession(),
                    param.getCs_api_url(), logMsg);
            if(thumbnail.getName().contains("frame1")) {
                previewList.add(param.getTarget_location()+"/transcode/previews/" + thumbnail.getName());
            } else {
                previewList.add(param.getTarget_location()+"/transcode/previews/"+previewCount+".jpg");
                ++previewCount;
            }
        }
        File coverImage = new File(coverDir+File.separator+"1.jpg");
        if(coverImage.exists()) {
            rt = UploadFileToCS(coverImage, param.getTarget_location()+"/transcode/previews", "cover.jpg", param.getSession(),
                    param.getCs_api_url(), logMsg);
            result.setCover(param.getTarget_location()+"/transcode/previews/cover.jpg");
        } else {
            result.setCover("");
        }
        
        FileUtils.deleteQuietly(new File(zipFileTempDir+File.separator+id));
        
        LOG.info("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        
        result.setHref("${ref-path}"+param.getTarget_location()+"/transcode/videos/"+defaultKey+"/"+targetFileName);
        result.setPreviews(previewList);
        Map<String,String> metaMap = new HashMap<String,String>();
        metaMap.put("source", ObjectUtils.toJson(srcMetadata));
        metaMap.put(extParam.get("targetFmt"), ObjectUtils.toJson(targetsMetadata.get(defaultKey)));
        Map<String,String> locations = new HashMap<String,String>();
        locations.put(defaultKey, result.getHref());
        for(String key : targetsMetadata.keySet()) {
           metaMap.put(key, ObjectUtils.toJson(targetsMetadata.get(key)));
           locations.put(key,"${ref-path}"+param.getTarget_location()+"/transcode/videos/"+key+"/"+targetFileName);
        }
        result.setLocations(locations);
        result.setMetadata(metaMap);
        result.setStatus(1);
        
        return result;
    }
    
    private static String filterMutiTargetCmds(List<String> cmds, Map<String,String> metaMap,
            Map<String,String> targetsMap, String destDir, String targetFileName) throws IOException {

        int height = 0;
        int width = 0;
        if(StringUtils.isNotEmpty(metaMap.get("Height"))
                && StringUtils.isNotEmpty(metaMap.get("Width"))) {
            height = new BigDecimal(metaMap.get("Height")).intValue();
            width = new BigDecimal(metaMap.get("Width")).intValue();
        }
        
        int rotation = 0;
//        if(StringUtils.isNotEmpty(metaMap.get("Rotation"))) {
//            rotation = new BigDecimal(metaMap.get("Rotation")).intValue();
//        }
        
        int maxHeight = 0;
        String origCmd = null;
        ListIterator<String> iter = cmds.listIterator();
        while(iter.hasNext()) {
            String command = iter.next();
            if(command.startsWith("ffmpeg")) {
                String [] strArgs = command.split("\\s+");
                for(int j=0; j<strArgs.length; ++j) {
                    if("-s".equalsIgnoreCase(strArgs[j])) {
                        if(null == origCmd) {
                            List<String> listArgs = new LinkedList<String>(Arrays.asList(strArgs));
                            listArgs.set(j+1, width+"x"+height);
                            origCmd = StringUtils.join(listArgs, " ");
                        }
                        String [] strScales = strArgs[j+1].toLowerCase().split("x");
                        int argWidth = Integer.parseInt(strScales[0]);
                        int argHeight = Integer.parseInt(strScales[1]);
                        if(argWidth>width || argHeight>height) {
                            iter.remove();
                        } else {
                            if(argHeight>maxHeight) {
                                maxHeight=argHeight;
                            }
                            String target = destDir+File.separator+argHeight+"p"+File.separator+targetFileName;
                            iter.set(command.replace("#target#", target));
                            targetsMap.put(argHeight+"p", target);
                            FileUtils.forceMkdir(new File(destDir+File.separator+argHeight+"p"));
                        }
                    }
                }
                
//                if(rotation!=0) {
//                    
//                }
            }
        }
        
        if( (height<360||width<640) && !cmds.get(0).startsWith("ffmpeg") ) {
            String target = destDir+File.separator+"360p"+File.separator+targetFileName;
            cmds.add(0, origCmd.replace("#target#", target));
            targetsMap.put("360p", target);
            FileUtils.forceMkdir(new File(destDir+File.separator+"360p"));
            maxHeight=360;
        }
        
        return maxHeight+"p";
    }
    
    static class MultiTargetThread implements Callable<MultiTargetThread> {
        private String command; 
        private Map<String,String> extParam;
        private String toolPath;
        private StringBuffer logMsg;
        private StringBuffer output;
        private int result;
        
        public StringBuffer getLogMsg() {
            return logMsg;
        }
        
        public StringBuffer getOutput() {
            return output;
        }
        
        public int getResult() {
            return result;
        }
        
        MultiTargetThread(String command, Map<String,String> extParam,
                String toolPath) {
            this.command = command;
            this.extParam = extParam;
            this.toolPath = toolPath;
            this.logMsg = new StringBuffer();
            this.output = new StringBuffer();
        }
        
        public MultiTargetThread call() throws Exception{
            command = parseParam(command, extParam);
            result = RunCommand(command,output,toolPath,logMsg);
            return this;
        }
    }
    
    private static void executeTranscodeCmds(List<String> cmds, Map<String,String> extParam,
            String path, StringBuffer logMsg) throws Exception{
        StringBuffer output = new StringBuffer();
        List<Callable<MultiTargetThread>> tasks = new ArrayList<Callable<MultiTargetThread>>();
        List<String> otherCmds = new ArrayList<String>();
        for(int i=0; i<cmds.size(); ++i) {
            String [] strArgs = cmds.get(i).split("#");
            String finalCommand = parseParam(cmds.get(i), extParam);
            
//            if(finalCommand.startsWith("ffmpeg")) {
//                tasks.add(new MultiTargetThread(finalCommand, extParam, path));
//            } else {
                otherCmds.add(finalCommand);
//            }
        }
        
//        List<Future<MultiTargetThread>> results = executorService.invokeAll(tasks);
//        for(Future<MultiTargetThread> result : results) {
//            try {
//                MultiTargetThread rt = result.get();
//                String out = rt.getOutput().toString();
//                int rtValue = rt.getResult();
//                if(rtValue!=0) {
//                    int msgStartIndex=out.length()-100<0 ? 0 : out.length()-100;
//                    if(out.lastIndexOf("Please")!=-1) {
//                        msgStartIndex = out.lastIndexOf("Please");
//                    }
//                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                            "LC/MEDIA_TRANSCODE_FAIL","转码工具执行错误："+out.substring(msgStartIndex));
//                }
//            } catch (ExecutionException e1) {
//                LOG.error("转码工具执行错误：",e1);
//                if(e1.getCause()!=null) {
//                    throw (Exception)e1.getCause();
//                }
//            }
//        }
        
        for(String command : otherCmds) {
            int resultValue = RunCommand(command, output, path, logMsg);
            if(resultValue!=0) {
                int msgStartIndex=output.length()-100<0 ? 0 : output.length()-100;
                if(output.lastIndexOf("Please")!=-1) {
                    msgStartIndex = output.lastIndexOf("Please");
                }
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/MEDIA_TRANSCODE_FAIL","转码工具执行错误："+output.substring(msgStartIndex));
            }
        }
    }
    
    private static String parseParam(String cmd, Map<String,String> extParam) {
        String [] strArgs = cmd.split("#");
        String finalCommand = "";
        for(int j=0; j<strArgs.length; ++j) {
            if (j%2!=0) {
                String newString = extParam.get(strArgs[j]);
                if(null == newString) {
                    String msg = "Commands arguments missing："+strArgs[j];
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "LC/MEDIA_TRANSCODE_FAIL",msg);
                }
                strArgs[j] = newString;
            }
            finalCommand += strArgs[j];
        }
        
        return finalCommand;
    }
    
    public static long getMediaMetadata(String file, String toolPath, StringBuffer logMsg,
            Map<String,String> mediaInfoMap) throws Exception {
        StringBuffer mediaInfo = new StringBuffer();
        int result = 0;
        String mediaInfoCmd = "mediainfo --Inform=\"file://" + toolPath + "templatejson.txt\" \"" + file +"\"";
        result = RunCommand(mediaInfoCmd, mediaInfo, toolPath, logMsg);
        if(result!=0) {
            logMsg.append("  获取目标文件metadata失败：" + mediaInfo.toString()+System.getProperty("line.separator"));
            LOG.error("获取目标文件metadata失败：" + mediaInfo.toString());
            return 0;
        }
        
        String srcMetadata = mediaInfo.toString();
        Map<String,String> srcMap = ObjectUtils.fromJson(srcMetadata, Map.class);
        BigDecimal decimal = null;
        if(StringUtils.isEmpty(srcMap.get("Duration"))) {
            decimal = new BigDecimal(0);
        } else {
            decimal = new BigDecimal(srcMap.get("Duration"));
        }
        long srcDuration = decimal.longValue()/1000;
        
        int srcHeight = 0;
        if(StringUtils.isNotEmpty(srcMap.get("Height"))) {
            srcHeight = new BigDecimal(srcMap.get("Height")).intValue();
        }
        int srcWidth = 0;
        if(StringUtils.isNotEmpty(srcMap.get("Width"))) {
            srcWidth = new BigDecimal(srcMap.get("Width")).intValue();
        }
        int finalHeight = srcHeight;
        int finalWidth = srcWidth;

        List<Map<String,String>> videoInfos = ObjectUtils.fromJson(ObjectUtils.toJson(srcMap.get("Video")), List.class);
        
        if((srcHeight<=0 || srcWidth<=0) && CollectionUtils.isNotEmpty(videoInfos)) {
            Map<String,String> videoInfo = videoInfos.get(0);
            if(StringUtils.isNotEmpty(videoInfo.get("Height")) &&  StringUtils.isNotEmpty(videoInfo.get("Width"))) {
                finalHeight = new BigDecimal(videoInfo.get("Height")).intValue();
                finalWidth = new BigDecimal(videoInfo.get("Width")).intValue();
            }
            if(StringUtils.isNotEmpty(videoInfo.get("WidthOriginal")) && StringUtils.isNotEmpty(videoInfo.get("HeightOriginal"))) {
                int heightOriginal = new BigDecimal(videoInfo.get("HeightOriginal")).intValue();
                int widthOriginal = new BigDecimal(videoInfo.get("WidthOriginal")).intValue();
                if(heightOriginal>finalHeight && widthOriginal>finalWidth) {
                    finalHeight=heightOriginal;
                    finalWidth=widthOriginal;
                }
            }
        }
        
        boolean bVideoNoScale = (finalHeight<=0 || finalWidth<=0) && CollectionUtils.isNotEmpty(videoInfos);
        Map<String,String> probeMap = null;
        if(srcDuration <= 0 || bVideoNoScale) {
            String probeCmd = "ffprobe -v quiet -print_format json -show_streams -show_format -i " + file;
            StringBuffer probeData = new StringBuffer();
            int rt = RunCommand(probeCmd, probeData, toolPath, logMsg);
            if(rt!=0) {
                logMsg.append("  获取目标文件metadata失败：" + probeData.toString()+System.getProperty("line.separator"));
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/MEDIA_TRANSCODE_FAIL","获取目标文件metadata失败");
            }
            probeMap = ObjectUtils.fromJson(probeData.toString(), Map.class);
        }
        if(srcDuration <= 0) {
            long probeDuration = 0;
            if(probeMap.containsKey("format")) {
                String formatStr = ObjectUtils.toJson(probeMap.get("format"));
                Map<String,String> formatMap = ObjectUtils.fromJson(formatStr, Map.class);
                if(StringUtils.isNotEmpty(formatMap.get("duration"))) {
                    BigDecimal duaration = new BigDecimal(formatMap.get("duration")).multiply(new BigDecimal(1000));
                    probeDuration = duaration.longValue();
                }
            }
            srcDuration = probeDuration/1000;
        }
        if(bVideoNoScale) {
            if(probeMap.containsKey("streams")) {
                List<Map<String,String>> streams = ObjectUtils.fromJson(ObjectUtils.toJson(probeMap.get("streams")), List.class);
                if(streams!=null) {
                    for(Map<String,String> stream:streams) {
                        if(stream.get("codec_type")!=null && stream.get("codec_type").equals("video")) {
                            String h = ObjectUtils.toJson(stream.get("height"));
                            String w = ObjectUtils.toJson(stream.get("width"));
                            if(StringUtils.isNotEmpty(h)) {
                                finalHeight = new BigDecimal(h).intValue();
                            }
                            if(StringUtils.isNotEmpty(w)) {
                                finalWidth = new BigDecimal(w).intValue();
                            }
                            break;
                        }
                    }
                }
            }
        }
        srcMap.put("Duration", durationFormat(srcDuration));
        srcMap.put("Height", String.valueOf(finalHeight));
        srcMap.put("Width", String.valueOf(finalWidth));
        
        mediaInfoMap.putAll(srcMap);

        return srcDuration;
//        String probeCmd = "ffprobe -v quiet -print_format json -show_streams -show_format -i " + file;
//        StringBuffer probeData = new StringBuffer();
//        result = RunCommand(probeCmd, probeData, toolPath, logMsg);
//        if(result!=0) {
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "LC/MEDIA_TRANSCODE_FAIL","获取目标文件metadata失败！");
//        }
//        
//        MediaInfo meta = ObjectUtils.fromJson(mediaInfo.toString(), MediaInfo.class);
//        Map<String,String> probeMap = ObjectUtils.fromJson(probeData.toString(), Map.class);
//        
//        int probeDuration = 0;
//        if(probeMap.containsKey("format")) {
//            Map<String,String> formatMap = ObjectUtils.fromJson(probeMap.get("format"), Map.class);
//            if(formatMap.containsKey("tags")) {
//                String tags = formatMap.get("tags");
//                if(tags.contains("hasMetadata")) {
//                    meta.setHasMetadata(true);
//                }
//                if(tags.contains("hasKeyframes")) {
//                    meta.setHasKeyframes(true);
//                }
//                if(tags.contains("hasVideo")) {
//                    meta.setHasVideo(true);
//                }
//                if(tags.contains("hasAudio")) {
//                    meta.setHasAudio(true);
//                }
//            }
//            
//            BigDecimal duaration = new BigDecimal(formatMap.get("duration"));
//            probeDuration = duaration.intValue();
//        }
//        meta.setDuration(meta.getDuration()/1000);
//        if(meta.getDuration()<=0) {
//            meta.setDuration(probeDuration);
//        }
//        
//        List<Map<String,String>> videoInfos = new ArrayList<Map<String,String>>();
//        for(int i=0; i<meta.getVideos().size(); ++i) {
//            Map<String,String> videoInfo = meta.getVideos().get(i);
//            BigDecimal duaration = new BigDecimal(videoInfo.get("Duration"));
//            videoInfo.put("Duration", String.valueOf(duaration.intValue()/1000));
//            BigDecimal bitRate = new BigDecimal(videoInfo.get("BitRate"));
//            if(bitRate.longValue()<=0) {
//                videoInfo.put("BitRate", videoInfo.get("BitRateNominal"));
//            }
//
//            int finalHeight = Integer.parseInt(videoInfo.get("Height"));
//            int finalWidth = Integer.parseInt(videoInfo.get("Width"));
//            if(StringUtils.isNotEmpty(videoInfo.get("WidthOriginal")) && StringUtils.isNotEmpty(videoInfo.get("HeightOriginal"))) {
//                int heightOriginal = Integer.parseInt(videoInfo.get("HeightOriginal"));
//                int widthOriginal = Integer.parseInt(videoInfo.get("WidthOriginal"));
//                if(heightOriginal>finalHeight && widthOriginal>finalWidth) {
//                    finalHeight=heightOriginal;
//                    finalWidth=widthOriginal;
//                }
//            }
//            
//            if(finalHeight<=0 || finalWidth<=0) {
//                if(probeMap.containsKey("streams")) {
//                    List<Map<String,String>> streams = ObjectUtils.fromJson(probeMap.get("streams"), List.class);
//                    for(Map<String,String> stream:streams) {
//                        if(stream.get("codec_type").equals("video")) {
//                            finalHeight = Integer.parseInt(stream.get("height"));
//                            finalWidth = Integer.parseInt(stream.get("width"));
//                        }
//                    }
//                }
//            }
//            videoInfo.put("Height", String.valueOf(finalHeight));
//            videoInfo.put("Width", String.valueOf(finalWidth));
//            
//            videoInfos.add(videoInfo);
//        }
//        meta.setVideos(videoInfos);
//        
//        List<Map<String,String>> audioInfos = new ArrayList<Map<String,String>>();
//        for(int i=0; i<meta.getAudios().size(); ++i) {
//            Map<String,String> audioInfo = meta.getAudios().get(i);
//            
//            BigDecimal duaration = new BigDecimal(audioInfo.get("Duration"));
//            if(duaration.intValue() == 0) {
//                audioInfo.put("Duration", String.valueOf(meta.getDuration()));
//            } else {
//                audioInfo.put("Duration", String.valueOf(duaration.intValue()/1000));
//            }
//        }
//        
//        return metadata;
    }
    
    private static String durationFormat(long durationInSec) {
        StringBuffer result = new StringBuffer("PT");
        long sec = durationInSec%60;
        long min = durationInSec/60;
        long hour = min/60;
        min = min%60;
        if(hour>0) {
            result.append(hour+"H");
        }
        if(min>0) {
            result.append(min+"M");
        }
        result.append(sec+"S");
        return result.toString();
    }


    /**
     * argument
     * {identifier:xxx,service:transcoding-ppt-to-html-service,location:xxxx}
     */
    @Override
    public void run(String identifier, String argument, TaskTraceResult taskResult,
            ExtFunService extFunService) {

        StringBuffer logMsg = new StringBuffer();
        /**
         * 完全确认相信对方传输
         */
        String id = identifier;
        TranscodeParam transcodeParam = null;
        TranscodeResult result = new TranscodeResult();
        result.setTranscodeType(SUBTYPE_VIDEO);
        try {
            transcodeParam = ObjectUtils.fromJson(argument,
                    TranscodeParam.class);
        } catch (Exception e1) {
            LOG.error("解析打包json参数错误", e1);
            taskResult.setExitCode(ERR_JSONPARAM);
            taskResult.setTracemsg(e1.getMessage());
            extFunService.callTraceLog(taskResult);
            extFunService.callFail(taskResult);
            return;
        }
        String failCallBackUrl = transcodeParam.getCallback_api()
                + "?identifier=" + id
                + "&status=0";
        String url = transcodeParam.getLocation();
        if (url == null) {
            LOG.error("下载源文件url为空");
            taskResult.setExitCode(ERR_PACK_PATH);
            taskResult.setTracemsg("下载源文件url为空");
            extFunService.callTraceLog(taskResult);
            result.setErrMsg("下载源文件url为空");
            result.setStatus(0);
            taskResult.setCallBackPara(ObjectUtils.toJson(result));
            taskResult.setCallBackUrl(failCallBackUrl);
            extFunService.callFail(taskResult);
            return;
        }

        
        try {
            if(transcodeParam.getExt_param().get(TRANSCODE_SUBTYPE)==null 
                    || transcodeParam.getExt_param().get(TRANSCODE_SUBTYPE).equals(SUBTYPE_VIDEO)) {
                result = transcode(id, transcodeParam, logMsg);
                result.setTranscodeType(SUBTYPE_VIDEO);
            } else {
                result = transcodeAudio(id, transcodeParam, logMsg);
                result.setTranscodeType(SUBTYPE_AUDIO);
            }
        } catch (Exception e) {
            FileUtils.deleteQuietly(new File(zipFileTempDir+File.separator+id));
            taskResult.setExitCode("转码失败");
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            logMsg.insert(0, out.toString());
            taskResult.setTracemsg(logMsg.toString());
            extFunService.callTraceLog(taskResult);
            String errMsg = e.getMessage();
            if(null!=errMsg && errMsg.length()>10000) {
                errMsg = errMsg.substring(0, 5000)+"..."+errMsg.substring(errMsg.length()-5000);
            }
            result.setErrMsg(errMsg);
            result.setStatus(0);
            taskResult.setCallBackPara(ObjectUtils.toJson(result));
            LOG.error(logMsg.toString());
            taskResult.setCallBackUrl(failCallBackUrl);
            extFunService.callFail(taskResult);
            return;
        }
        
        if(null != result) {
            String callBackUrl = transcodeParam.getCallback_api()
                    + "?identifier=" + id
                    + "&status=1";
            taskResult.setExitCode("转码完成");
            logMsg.insert(0, "成功回调："+callBackUrl);
            taskResult.setTracemsg(logMsg.toString());
            extFunService.callTraceLog(taskResult);
            LOG.info(logMsg.toString());
            taskResult.setCallBackUrl(callBackUrl);
            taskResult.setCallBackPara(ObjectUtils.toJson(result));
            extFunService.callSucceed(taskResult);
        }

    }
    
    
    public static void main(String[] args) {
        
        String userDir = System.getProperty("user.dir");
        String path = TranscodeServiceImpl.class.getClassLoader().getResource("tools").getPath();
        
        String paramStr = "{\"ext_param\":{\"targetFmt\":\"mp4\",\"coverNum\":\"16\",\"subtype\":\"video\"},\"target_location\":\"/edu/esp/assets/638b0286-18f5-45c3-bbea-b326241b50b5.pkg\",\"task_execute_env\":\"product\",\"session\":\"f3213692-aefa-4fcc-a14b-1268d5858a14\",\"location\":\"http://cs.101.com/v0.1/download?path=/edu/esp/assets/638b0286-18f5-45c3-bbea-b326241b50b5.pkg/1452252623030.mp4\",\"cs_api_url\":\"http://cs.101.com/v0.1\",\"commands\":[\"ffmpeg -i \\\"#src#\\\" -y -s 720x480 -ab 48k -vcodec libx264 -c:a libvo_aacenc -ar 44100 -qscale 4 -f #targetFmt# -movflags faststart -map 0:v:0 -map 0:a? -ac 2 \\\"#target#\\\"\",\"ffmpeg -i \\\"#src#\\\" -y -s 640x360 -ab 48k -vcodec libx264 -c:a libvo_aacenc -ar 44100 -qscale 4 -f #targetFmt# -movflags faststart -map 0:v:0 -map 0:a? -ac 2 \\\"#target#\\\"\",\"ffmpeg -i \\\"#src#\\\" -y -s 1920x1080 -ab 48k -vcodec libx264 -c:a libvo_aacenc -ar 44100 -qscale 4 -f #targetFmt# -movflags faststart -map 0:v:0 -map 0:a? -ac 2 \\\"#target#\\\"\",\"ffmpeg -i \\\"#src#\\\" -y -s 1280x720 -ab 48k -vcodec libx264 -c:a libvo_aacenc -ar 44100 -qscale 4 -f #targetFmt# -movflags faststart -map 0:v:0 -map 0:a? -ac 2 \\\"#target#\\\"\",\"thumbnail -in \\\"#src#\\\" -picint #intervalTime# -s 160x120 -out \\\"#targetCover#\\\" -join 4x4\",\"ffmpeg -y -ss 5 -i \\\"#src#\\\" -frames 1 -f image2 #targetPreview#/frame1.jpg\"],\"callback_api\":\"http://esp-lifecycle.web.sdp.101.com/v0.6/assets/transcode/videoCallback\"}";
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
        TranscodeParam param = ObjectUtils.fromJson(paramStr,
                TranscodeParam.class);
        //param.setLocation("http://sdpcs.beta.web.sdp.101.com/v0.1/download?path=/dev_content_edu/test_media/wildlife.wmv");
//        param.setCs_api_url("http://sdpcs.beta.web.sdp.101.com/v0.1");
//        param.setSession("7d641af8-57ca-400b-a7a2-0fb8e8ddadd0");
//        param.setTarget_location("/dev_content_edu/esp/assets/1117678e-e65a-4b3e-aede-d026d54ef222.pkg");
//        param.setCallback_api("http://esp-lifecycle.dev.web.nd/v0.6/assets/transcode/videoCallback");
//        List<String> cmds = new ArrayList<String>();
//        cmds.add("ffmpeg -i #src# -y -ab 48k -s 480x270 -vcodec libx264 -c:a libvo_aacenc -ar 44100 -qscale 4 -f #targetFmt# -movflags faststart -map 0:v -map 0:a:0 -ac 2 #target#");
//        cmds.add("thumbnail -in #target# -picint 6 -s 128x72 -out #targetPreview#");
//        param.setCommands(cmds);
//        Map<String,String> extParams = new HashMap<String,String>();
//        extParams.put("targetFmt", "mp4");
//        param.setExt_param(extParams);

        
        StringBuffer errMsg = new StringBuffer();
        try {
            transcode("1000", param, errMsg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
//        File targetFile = new File("C:\\Users\\Administrator\\AppData\\Local\\Temp\\lifecircle\\transcode_temp\\1000\\src\\397f0cd10ca1421aa36a764647aec6d1.mp4");
//        StringBuffer logMsg = new StringBuffer();
//        String rt = UploadFileToCS(targetFile, param.getTarget_location()+"/transcode/videos/test",
//                targetFile.getName(), param.getSession(),
//                param.getCs_api_url(), logMsg);
        
//        System.out.println(rt);

    }
}