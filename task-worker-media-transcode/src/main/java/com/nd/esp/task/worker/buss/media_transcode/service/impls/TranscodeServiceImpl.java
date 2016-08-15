package com.nd.esp.task.worker.buss.media_transcode.service.impls;

import java.io.*;
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


    private static String zipFileTempDir = System.getProperty("java.io.tmpdir");
    
    public final static long chunkMax = 50L*1024*1024;//文件超过chunkMax(Byte)就要进行分块上传
    public final static long chunkNumMax = 20;//最多并行发送的分块数
    public final static long chunkSize = 5L*1024*1024;//文件分块上传时每块的大小(Byte)
    
    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

    public static final String ERR_JSONPARAM = "err_jsonparam";
    public static final String ERR_PACK_PATH = "err_pack_path";
    
    public static final String TRANSCODE_SUBTYPE = "subtype";
    public static final String SUBTYPE_VIDEO = "video";
    public static final String SUBTYPE_AUDIO = "audio";
    
    static {
        if(!zipFileTempDir.endsWith(File.separator)) {
            zipFileTempDir+=File.separator; 
        }
        zipFileTempDir = zipFileTempDir + "lifecircle" + File.separator + "transcode_temp" ;
    }

    
    public TranscodeServiceImpl(){

    }

    public void finalize() throws Throwable {

    }



    
    /**
     * 从cs下载文件到本地
     *
     * @param url 文件所在下载路径
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
//        String targetFileName = NameWithoutEx+"."+extParam.get("targetFmt");
        String srcDir = zipFileTempDir+File.separator+id+File.separator+"src";
        String destDir = zipFileTempDir+File.separator+id+File.separator+"targets";
        FileUtils.forceMkdir(new File(srcDir));
        FileUtils.forceMkdir(new File(destDir));

        List<String> cmds = param.getCommands();
        extParam.put("src", srcDir+File.separator+srcFileName);
        extParam.put("destDir", destDir);
        extParam.put("fileNameNoEx", NameWithoutEx);
        
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
        
        Map<String,String> targetsMap = new HashMap<String,String>();
        if(!cmds.isEmpty() && StringUtils.isNotEmpty(cmds.get(0))) {
            for(ListIterator<String> iter = cmds.listIterator(); iter.hasNext(); ) {
                String cmd=iter.next();
                if(cmd.contains("oggenc2")) {
                    targetsMap.put("href-ogg", destDir+File.separator+NameWithoutEx+".ogg");
                    iter.set(cmd.replace("#target#", targetsMap.get("href-ogg")));
                } else {
                    targetsMap.put("href", destDir+File.separator+NameWithoutEx+".mp3");
                    iter.set(cmd.replace("#target#", targetsMap.get("href")));
                }
            }
            executeTranscodeCmds(cmds, extParam, path, logMsg);
        } else {
            targetsMap.put("href", extParam.get("src"));
        }
        
        LOG.info("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();

        result.setHref("${ref-path}"+param.getTarget_location()+"/transcode/audios/"+NameWithoutEx+".mp3");
        Map<String,Map<String,String>> targetsMetadata = new HashMap<String,Map<String,String>>();
        for(String key : targetsMap.keySet()) {
            String localFilePath = targetsMap.get(key);
            String targetFileName = localFilePath.substring(localFilePath.lastIndexOf(File.separator)+File.separator.length());
            
            if(!extParam.get("src").equals(localFilePath)) {
                Map<String,String> targetMetadata = new HashMap<String,String>();
                long targetDuration = srcDuration;
                try {
                    targetDuration = getMediaMetadata(localFilePath, path, logMsg, targetMetadata);
                } catch (Exception e) {
                    LOG.info("获取目标文件信息失败！");
                }
                targetsMetadata.put(key, targetMetadata);
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
                if (targetFile.isFile()) {
                    UploadFileToCS(targetFile, param.getTarget_location()+"/transcode/audios",
                            targetFileName, param.getSession(),
                            param.getCs_api_url(), logMsg);
                }
            } else {
                targetsMetadata.put("href", srcMetadata);
                result.setHref("${ref-path}"+param.getLocation().replaceAll(".*path=", ""));
            }
        }
        
        FileUtils.deleteQuietly(new File(zipFileTempDir+File.separator+id));
        
        LOG.info("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        
        
        Map<String,String> metaMap = new HashMap<String,String>();
        metaMap.put("source", ObjectUtils.toJson(srcMetadata));
        Map<String,String> locations = new HashMap<String,String>();
        for(String key : targetsMetadata.keySet()) {
            metaMap.put(key, ObjectUtils.toJson(targetsMetadata.get(key)));
            String target = targetsMap.get(key);
            String name =  target.substring(target.lastIndexOf(File.separator)+File.separator.length());
            locations.put(key,"${ref-path}"+param.getTarget_location()+"/transcode/audios/"+name);
         }
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
        String srcDir = zipFileTempDir+File.separator+id+File.separator+"src";
        String destDir = zipFileTempDir+File.separator+id+File.separator+"targets";
        String previewDir = zipFileTempDir+File.separator+id+File.separator+"previews";
        String coverDir = zipFileTempDir+File.separator+id+File.separator+"cover";
        File preview = new File(previewDir);
        FileUtils.forceMkdir(preview);
        FileUtils.forceMkdir(new File(coverDir));
        
        List<String> cmds = param.getCommands();
        extParam.put("src", srcDir+File.separator+srcFileName);
        extParam.put("tempMp4", destDir+File.separator+NameWithoutEx+".mp4");
        extParam.put("destDir", destDir);
        extParam.put("fileNameNoEx", NameWithoutEx);
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
        String defaultKey = filterMutiTargetCmds(cmds, srcMetadata, targetsMap, destDir, NameWithoutEx);
        
        executeTranscodeCmds(cmds, extParam, path, logMsg);
        
        LOG.info("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("转码过程消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();
        Map<String,Map<String,String>> targetsMetadata = new HashMap<String,Map<String,String>>();
        for(String key : targetsMap.keySet()) {
            String localFilePath = targetsMap.get(key);
            String targetFileName = localFilePath.substring(localFilePath.lastIndexOf(File.separator)+File.separator.length());
            Map<String,String> targetMetadata = new HashMap<String,String>();
            long targetDuration = srcDuration;
            try {
                targetDuration = getMediaMetadata(localFilePath, path, logMsg, targetMetadata);
            } catch (Exception e) {
                LOG.info("获取目标文件信息失败！");
            }
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
        
        List<String> previewList = new ArrayList<String>();
        int previewCount = 1;
        for(File thumbnail:preview.listFiles()) {
            UploadFileToCS(thumbnail, param.getTarget_location()+"/transcode/previews", thumbnail.getName(), param.getSession(),
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
            UploadFileToCS(coverImage, param.getTarget_location()+"/transcode/previews", "cover.jpg", param.getSession(),
                    param.getCs_api_url(), logMsg);
            result.setCover(param.getTarget_location()+"/transcode/previews/cover.jpg");
        } else {
            result.setCover("");
        }
        
        FileUtils.deleteQuietly(new File(zipFileTempDir+File.separator+id));
        
        LOG.info("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("上传消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        
        String defaultTarget = targetsMap.get(defaultKey);
        if(null != defaultTarget) {
            String defaultName =  defaultTarget.substring(defaultTarget.lastIndexOf(File.separator)+File.separator.length());
            result.setHref("${ref-path}"+param.getTarget_location()+"/transcode/videos/"+defaultKey+"/"+defaultName);
        }
        result.setPreviews(previewList);
        Map<String,String> metaMap = new HashMap<String,String>();
        metaMap.put("source", ObjectUtils.toJson(srcMetadata));
        Map<String,String> locations = new HashMap<String,String>();
        for(String key : targetsMetadata.keySet()) {
            String finalKey = "href-"+key;
            if(key.contains(defaultKey)) {
                finalKey = finalKey.replace("-"+defaultKey, "");
            } 
            metaMap.put(finalKey, ObjectUtils.toJson(targetsMetadata.get(key)));
            String target = targetsMap.get(key);
            String name =  target.substring(target.lastIndexOf(File.separator)+File.separator.length());
            locations.put(finalKey,"${ref-path}"+param.getTarget_location()+"/transcode/videos/"+key+"/"+name);
        }
        result.setLocations(locations);
        result.setMetadata(metaMap);
        result.setStatus(1);
        
        return result;
    }
    
    private static String filterMutiTargetCmds(List<String> cmds, Map<String,String> metaMap,
            Map<String,String> targetsMap, String destDir, String nameWithNoEx) throws IOException {

        int height = 0;
        int width = 0;
        if(StringUtils.isNotEmpty(metaMap.get("Height"))
                && StringUtils.isNotEmpty(metaMap.get("Width"))) {
            height = new BigDecimal(metaMap.get("Height")).intValue();
            width = new BigDecimal(metaMap.get("Width")).intValue();
        }
        
        if(height<360||width<640) {
            height=360;
            width=640;
        }
        
        
        int maxHeight = 0;
        ListIterator<String> iter = cmds.listIterator();
        while(iter.hasNext()) {
            String command = iter.next();
            if(command.startsWith("ffmpeg")) {
                List<Integer> scale = new ArrayList<Integer>();
                GetScaleParam(command, scale);
                if(!scale.isEmpty()) {
                    int argWidth = scale.get(0).intValue();
                    int argHeight = scale.get(1).intValue();
                    if(argWidth>width || argHeight>height) {
                        iter.remove();
                    } else {
                        if(argHeight>maxHeight) {
                            maxHeight=argHeight;
                        }
                        String targetKey = argHeight + "p";
                        String finalFilename = nameWithNoEx;
                        if(command.startsWith("ffmpeg2theora")) {
                            finalFilename += ".ogv";
                            targetKey += "-ogv";
                        } else {
                            finalFilename += ".mp4";
                        }
                        String target = destDir+File.separator+targetKey+File.separator+finalFilename;
                        targetsMap.put(targetKey, target);
                        FileUtils.forceMkdir(new File(destDir+File.separator+targetKey));
                    }
                }
            }
        }
        
        return maxHeight+"p";
    }
    
    
    private static void GetScaleParam(String cmd, List<Integer> scale) {
        String [] strArgs = cmd.split("\\s+");
        for(int j=0; j<strArgs.length; ++j) {
            if("-s".equalsIgnoreCase(strArgs[j])) {
                String [] strScales = strArgs[j+1].toLowerCase().split("x");
                scale.add(0, Integer.valueOf(strScales[0]));
                scale.add(1, Integer.valueOf(strScales[1]));
            }
            
            if("--width".equalsIgnoreCase(strArgs[j])) {
                scale.add(0, Integer.valueOf(strArgs[j+1]));
            }
            if("--height".equalsIgnoreCase(strArgs[j])) {
                scale.add(1, Integer.valueOf(strArgs[j+1]));
            }
        }
    }
    
    
    private static void executeTranscodeCmds(List<String> cmds, Map<String,String> extParam,
            String path, StringBuffer logMsg) throws Exception{
        StringBuffer output = new StringBuffer();
        List<String> otherCmds = new ArrayList<String>();
        for(int i=0; i<cmds.size(); ++i) {
            String [] strArgs = cmds.get(i).split("#");
            String finalCommand = parseParam(cmds.get(i), extParam);
            otherCmds.add(finalCommand);
        }
        
        for(String command : otherCmds) {
            output.delete(0, output.length());
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
    
    public static void main(String[] args) throws Exception {

        String path = URLDecoder.decode(TranscodeServiceImpl.class.getClassLoader().getResource("tools").getPath().substring(1).
                replace("/", File.separator), "UTF-8");

        String uuidStr = "[\"dedb23cc-d68f-43f8-8a9e-56268a8a85ba\",\"c7d8e6e8-0985-47ca-aa1a-800a5c199085\",\"521e0ddd-1d59-49e5-8fff-c988d30b6cb0\",\"4ea60272-0d68-4774-a47f-7ee5c5c1b9f9\",\"015e1855-3c5e-473c-8744-d3ab87097206\",\"d8191d41-dd41-4c24-a012-80eed566465f\",\"fff4eae0-c0a4-482b-a387-c0d27d6d71d2\",\"770769ad-fa10-4f7d-bc97-4014a4424599\",\"4b5b8fea-8e3a-42de-a0e3-dff1c93cf866\",\"18789aa6-960a-4bb4-9389-be3e57bc23de\",\"e77543bc-0876-4908-88e6-f13ccf4ff3c1\",\"0aacb722-55a0-43bb-8b43-428ffb33ab0c\",\"e2c33e78-908b-4e97-8c3d-3ba57531bf22\",\"e97943bf-cbbb-4864-b22f-8d70d0f440b7\",\"1715616b-4fb5-4835-891c-09e79e1b53b0\",\"ebaaf7f5-8620-4688-b4be-fca87fd1dfce\",\"1004c7a1-0516-48ef-912c-fb227ff3e6d4\",\"cd06feb6-fdc6-4f74-9fa1-465e7661e034\",\"8fea7da5-a8e8-4c32-a4ec-2b8071ba8309\",\"9d4af667-55ae-4d45-9956-cee9001d9732\",\"85dc4d2b-ba0b-464c-a4ea-f9956dd9b01d\",\"f53e2178-50c4-4c93-923f-5f1b86d71bca\",\"9673a5a6-218a-4644-b641-451019deb1f8\",\"38369471-70eb-46c4-90c3-37f3bef21c7f\",\"7d3af97f-3b24-4d34-98a2-042371746f48\",\"01eec0cf-edcc-4068-9b6e-5df7760a727b\",\"8c1b880a-8b39-499a-afdb-7d53499f8738\",\"1d64fcaa-c7ca-4952-8848-3c112eda29b0\",\"11c10925-6b2b-4336-91b8-fffaf9a63fe4\",\"6cfb58d1-1c4c-4fc4-a895-52516574bd26\",\"afd92375-5805-401f-82b9-a09bc728f44d\",\"b48b689f-cb34-4ee2-9d60-ffc0c059586e\",\"5343b7a2-b38d-443b-b89a-f0022c49ea25\",\"b8ae3588-5c62-4cbd-b7b0-678284061246\",\"6c8d9a8c-ed75-4e75-b005-1378bc73ade3\",\"af9ac2a5-9453-4642-906f-f6a6af6a05ba\",\"c135ec9d-73cc-4489-ad16-edeaadb606f5\",\"5aa0029b-72b4-49b8-bb7b-099c2001f878\",\"2d47df9b-47a7-43aa-8fa2-a0c92eeeae9e\",\"7b4ad86a-4872-429a-ad20-560662ddb8a4\",\"b49152a0-1cfb-4856-b531-e1367c347f5d\",\"bb25c4f1-1c2f-4bd6-b782-65d2af3efd37\",\"2225fa8d-283c-4829-9441-bc418053394a\"]";
        List<String> uuids = ObjectUtils.fromJson(uuidStr, List.class);


//        uuidStr = "[\"2fd6e2dc-8aa7-4f95-aeb8-44be6b0077bb\",\"6a08ec21-7339-4419-a33e-0488d925bd3a\",\"05878cb8-b42b-42ad-9c95-a7c2aa1595c3\",\"3aadc4f7-5684-4ba1-ad2e-2bdb606598e9\",\"2dfb9ae9-1777-47fb-a62f-23688cfe9ca6\",\"1d614142-c7ee-48db-8bdc-9f366ac3ad66\",\"3d10aaba-54f5-4eaa-a50a-1c9107bc4eaf\",\"d99a4b79-3d96-4e00-ad6e-4ea23f78ab1c\",\"d1ef84d0-8e87-4962-8969-afab95b2641a\",\"207fe3f4-09e0-4986-abed-49d17a4dd939\",\"a7d8a917-5f8c-4a3c-9c4e-dc8ed6707bb5\",\"f42f7956-09b8-479a-8bc4-bc47d929e98e\",\"274e8f7b-c315-4aa2-9a14-49da0f9352e4\",\"7f8774ec-3a97-4c10-b6d8-283f8fc4f87c\",\"52096cd5-7b61-4302-b111-81ec9c5c5178\",\"22780590-7a3c-45a3-bc54-5983f46613a2\",\"b344c03d-7ced-4b10-9e0a-6606016f724b\",\"1688bd98-b051-438b-8270-258bc8713554\",\"70411c58-65b4-4af4-bca3-158ae672e347\",\"ce6a43ed-e4d7-4edb-89de-4d274e7e85af\",\"0dc190ad-8990-4d94-84ae-58de5c2b66fc\",\"544b8855-91cf-4f9a-bce7-e38350f2b382\",\"3015157c-c89c-4217-bf08-609df06aeea9\",\"2fb54632-b124-466c-be6b-4a0b3f5e90aa\",\"12b37b87-0ef0-4e03-8685-a1bb6be17365\",\"4b5b26dc-cfb5-4be7-83dc-302b8c0b8bcb\",\"3da3637b-a6e4-481b-9f1f-fea6f72ec9d2\",\"c7c0172f-d9f0-4160-bb57-bb4520dde167\",\"d73d8bd3-e6ee-417f-a1d7-3bbdc6fa9e5d\",\"eb8a175d-46de-4071-babc-f56ec5e7e773\",\"90c9b5ce-aecf-49ba-b4ac-1ad35dfe2e62\",\"8f1bcbfb-51c0-4b41-a728-f30d72ded33b\",\"09a19794-6273-4822-939b-d9c927ae9edb\",\"d60e2ecb-9cfd-45fe-90b2-949455dd731b\",\"56f73439-e541-4097-8943-a6055c639624\",\"f25f230c-ce6c-4ea5-88a5-0372d4211b1f\",\"3a8b85a7-49af-4b5d-a7cb-5797ed802f6d\",\"ec3fa94f-7757-45b6-9de5-a8530acccd04\",\"9adc09a8-da97-409d-9e19-a4f47a18835f\",\"44a33f8d-01e4-4af9-bb29-ecad1d3c35cb\",\"503cb7c5-319f-4509-bdbd-d5a8492a70f0\",\"c5d1b75e-e32b-4fd7-aeea-535ffc926e3a\",\"8bbfc6fb-7784-4703-a2de-1de051370048\",\"ffa14120-b642-4667-b3ea-15f52b58a9ba\",\"88512c1c-a1e4-4931-b7b1-69454c67829a\",\"3846a1a5-e4f0-49e0-9b6e-7e218bcb5ba0\",\"ffe6a461-f4c3-49c4-bde8-504a2a45bf7f\",\"7e13faa6-898b-4489-b240-2ca748963473\",\"3a5cea63-00f5-430f-aecb-f76e4a15ebb6\",\"3fd43b15-ab7d-4297-b775-0cfd70389452\",\"594949ec-b8b7-40d2-ae7e-bbf24f81b4c6\",\"d5b3d16e-9a86-4672-a5a9-933969bc3e22\",\"72ad7804-1c49-46b0-a725-6559fff161a2\",\"3b1e175c-a9b1-494f-a345-d0687ba90691\",\"4843048e-3689-4092-9f26-f61918c0e7d5\",\"aab257e5-c567-49bd-9e64-9df47f31dec4\",\"356e3d25-8dcf-49b2-999d-3211a9e6e978\",\"20cea809-dcf3-4052-884a-f4a25b7a2ce3\",\"4e9d6e62-aac3-4417-91c6-1736f51aea85\",\"5b0da201-580b-4853-accd-1f8300350f42\",\"ae210e49-5900-4dd0-a21d-661a287a6c30\",\"9a056db9-76aa-46de-ae3e-3b157ef20962\",\"ddb2b00a-8727-4f35-9040-2db583f73e1c\",\"a4439347-7b24-463f-8b3d-74d30efce91e\",\"b6d80227-5bec-46cb-b11f-b8c9cd4a776c\",\"33b2c17b-e9a4-4dc7-af83-8a8906b40366\",\"d19e0718-b9aa-447d-845c-d87c0f941259\",\"d4936621-d826-4ad4-859d-defc8139030e\",\"e1a450ed-4828-4548-9571-60621a1704a4\",\"4b778dcf-a4fe-4b25-9bc3-957172926da3\",\"a6286e67-f948-4d74-9771-67121d62ee03\",\"950fec26-2c61-4964-809a-d4923e67281f\",\"16ce28da-e52a-40d4-a14b-a4416fa97b6e\",\"45985f8b-34f9-486a-81b9-7b029664f6e2\",\"305df614-bb7b-4a57-a8b3-bfd3e363d95d\",\"e6438a6d-940e-48ba-abfc-4635187c4ce5\",\"3e676420-340e-422f-a545-f76ba959248d\",\"48fb8fed-2f07-4ba5-8224-951dab868780\",\"731a0808-5fe4-4c65-966a-5c23ff6bafc9\",\"ed575004-10b2-4db3-85cf-264390909a1e\",\"24dddeb2-7711-4d17-8d92-a8c09753cb1d\",\"8c772356-9917-4729-becc-31ae95da3b35\",\"acbec7f0-89b3-46a8-abab-1feb14bf2dd1\",\"18a147e3-8551-4549-9fd7-4fed6f63a2a8\",\"cb705668-6fff-4c76-88af-c9f29c6af666\",\"1c838585-a62b-4f48-9716-e26a1d4c9395\",\"cffe6775-ea8f-47dd-ae86-d3e6132d81d7\",\"a1f3a5ef-d29c-446b-bdac-0414526752ce\",\"d74a6bec-ff4f-42b4-ac6e-2f235e6157bf\",\"15139271-9d1c-44ec-8894-214455c1dca7\",\"9f46f954-0d97-4ec1-b3e9-5715f5f13bbe\",\"3bf2ccbb-72b5-489f-8a01-12a4bb9f914a\",\"1d3853c5-21c0-4533-b3a2-d8a7ac74ed5b\",\"23d7cd47-fd7a-45dd-b19b-2b8a31c44534\",\"2616f87f-eed9-4bd5-9ca9-dce478e5a222\",\"4f1d68c5-d592-4d03-a906-c1429d8aacc0\",\"dba9af28-98ce-43b3-8b0c-7eedbc186754\",\"df25ea52-8650-4572-8faf-c682e5c3a372\",\"6ce0dcb2-4011-4bd6-ba1b-b62f9b37bcd7\",\"18b44ed6-f68e-4afd-b375-ae30364ca253\",\"c4e3fb97-c7dd-4bbd-aa46-cb9f110c6de4\",\"630e2148-b2ff-4ea2-b612-b1d8853ce55a\",\"ae3316de-7d69-4550-bb16-7dc28cac51e1\",\"3b51bbbb-d211-413d-92fa-014169ad6024\",\"1036637d-9783-4514-92fb-8dca30de1f83\",\"0c9b8326-cb69-465c-8047-6ba0a6112e24\",\"74cd7995-f3bd-478d-8a81-aef32c4c5a04\",\"46ef775a-016b-4dd6-88b8-73708bbd9741\",\"41c7ee3a-676b-4283-b1c4-a954e42832ea\",\"4f4860cd-c647-41d3-b1eb-3b4e137c5b49\",\"87801b82-5e3c-489a-82bf-4a4aa4356ff9\",\"a6419faf-10f5-4999-b3f8-5c7b14023e79\",\"e825f107-a8ab-4ad5-a005-f380f6c06bf8\",\"49f29580-d877-4221-896b-794e4b52dee8\",\"a067851b-ab28-4673-b68f-5c454814187e\",\"aea4058d-3803-4d92-9ca1-42f388fdd639\",\"07f8e36e-471b-4431-bc16-fde0b0857bf9\",\"18fe520e-76c2-486b-b354-4a81b0d83bb2\",\"fe8122e2-fce9-4216-b3d3-a90edd45eb06\",\"8d4536f5-a5e3-4521-be32-186330ff3402\",\"2a0c3f9a-b84c-4bab-8fc5-1185abf7445c\",\"ce600ca6-2242-4a28-a88e-079291587f8c\",\"ae704f8b-1bd7-4070-8dfd-9e84f9b204e6\",\"15f6ba98-1e22-460e-a611-3c0b35c6a441\",\"18036631-2592-4ccd-ba4b-833a967f1de5\",\"6c596f4a-7176-4e9a-80ca-6ae4a84c1b02\",\"4d64c1fc-1388-4ffa-8d31-5a014c414460\",\"62cb9752-a4d4-4f09-aa56-087b9135ee88\",\"25827243-dd54-476a-898e-b50116be74a1\",\"77499f98-2bcd-48be-9c82-507b0b265989\",\"b22a6084-a722-4c0b-a739-039c043a61c6\",\"3cc33d43-0464-47b8-96be-2232ca66a751\",\"a0d85d74-078e-497d-902f-1a6787fd5d4e\",\"e93154ce-5fea-428a-8471-047711c58015\",\"09961ec8-c7d2-448f-9cf0-1dda23f0c881\",\"6135c048-e7bd-43bb-b9c9-0f3ef8238332\",\"ce2d097b-c3e5-4026-9ded-16b97c74dea4\",\"744afbad-d42d-432c-bb8f-60b81b67ac36\",\"0f66069c-61e5-4a19-b7bd-122453854a81\",\"34ca384a-fa32-4f9a-ba62-293c49e9171d\",\"a567806b-9ccb-4d44-bcb2-8bb5c569bd7e\",\"5667d57c-66ae-4bbf-976b-a7982a20e0f8\",\"93eace4e-5fbd-4065-ad9d-a204834c2f2d\",\"cef5a7be-619d-4e6b-9151-cb6cf6807a29\",\"e15919ef-f19c-4272-bc5e-0353158b4bb5\",\"4072c8d8-d200-49da-be9e-395f8cea855b\",\"4e2b355f-d0e9-485d-928a-9af35cda5daa\",\"e259183d-1008-4708-a6a0-c5b62f297482\",\"6079ab67-55b2-4c6f-a610-9bd206f5c846\",\"e15089a3-15f7-4285-b1c0-8a3f4c016faa\",\"e35711d4-3037-40d1-b3f0-ff1fec817908\",\"bde178c6-e364-4a38-9e4d-75d614db524f\",\"566c6de1-5e82-4674-ba13-be2d9a98d05a\",\"856b978c-528c-4581-a402-8b0628a17a67\",\"ef42832b-4d71-4b9a-b141-4045b3639bd8\",\"a7903bdc-8f8c-47c7-ae41-b7c37f8672aa\",\"67829c48-4b70-46d5-a900-1f28851af251\",\"ff0314d7-cbca-4120-9557-c42ecd031561\",\"3ee39658-cf96-470f-be37-bf560e86c2e0\",\"42223f56-e3f5-47ba-83c4-ff110f33d862\",\"74835884-8c76-408c-835b-8bc13edcd2f8\",\"02a59ab2-31c0-41bc-b136-353eb44bb570\",\"38d6c6c2-1426-42de-bdc3-79f5a0ffd231\",\"fc019031-7141-491c-a744-c5a49918c59d\",\"95051c08-3611-44c4-8e11-fa7b5a2989fb\",\"777f1ddb-8e39-4678-b15f-e0d7cd03b914\",\"8113e56a-bdf7-4ea0-83b6-003c9c456f9d\",\"9a222c91-65f6-4a0f-9605-02eee836d024\",\"d98e068a-bd05-460c-b3b1-3ced5319c0ac\",\"8aa054a1-8408-41d9-90fd-8c907d7907ff\",\"07963f70-93b2-455a-8326-dece3d3f2dc7\",\"91cdc8d4-cd8d-472d-bfac-8116238df332\",\"ccdf937b-970b-45b6-af6f-9abf4173581d\",\"63f85bab-f1d0-4832-95b5-b8a4377a042e\",\"ec150a6e-a93a-45d1-97f5-13df338e6d77\",\"aeb4e46d-c37a-4c2b-b6b7-925c2a69c3fc\",\"86ad3fe9-3e1d-47c6-a5ee-38353435ba3b\",\"b98feff4-87af-447a-9b08-cf5788bfe694\",\"5798f7d2-d4e5-406c-ba93-889931b30637\",\"d48684ee-8bfe-4fbf-865b-309d10976ba7\",\"2cc86e9f-77d6-4446-af51-6c749409f7a3\",\"1399e75a-b20e-4a9c-aa97-d128771ee9c5\",\"07438e2b-14b9-4864-ad8e-0a8c662e745b\",\"3bc99c73-c7d1-4f9d-9b34-1e203f128238\",\"9adda02e-221e-48d0-a729-c4bb4e2db717\",\"258eaa6d-40ce-4ce8-a4ee-bb9ad23a59ec\",\"acae62a5-ad13-49b7-aade-307d47c65b94\",\"f79b1819-ae3b-41ce-82de-2a61b3448843\",\"2911696d-f45d-4d95-a853-fa9c578fde22\",\"e5fb49ba-f2e0-4114-9aa7-743aaf36dd07\",\"771eea77-135f-4a83-b252-2ae6c4324f8c\",\"bf3949e9-16cb-4df4-91b2-dfeec2f70597\",\"ec7ff98c-e757-4fd4-97f4-ed9684ab3444\",\"833ecfaf-936c-4db7-8467-0c4ddbc4fd85\",\"1935cc6a-13c9-4287-99dc-d650dfe8fe2b\",\"b583d06a-015e-402c-af25-148f14624df6\",\"65067276-6915-43e0-9a2e-51245e486192\",\"592e22d4-36cb-4738-bc66-654431b8ebb0\",\"b27fcfb2-b081-40ee-a75e-9c1c4c92dd58\",\"b8139d96-5726-4de4-b5da-ae37d87bf06c\",\"4ea60272-0d68-4774-a47f-7ee5c5c1b9f9\",\"abf3e1a4-9802-43b8-8949-6b4c1a2ef8bf\",\"7358005e-448c-49db-bbfa-bdf1e661154e\",\"75350519-619c-406a-8428-f74742c879c5\",\"624e77d8-7ff5-4e23-bae6-487ee36d0114\",\"a6edbeaa-89f3-4bc6-ad7b-de49a802c693\",\"2e8fe454-c9f2-4d88-bd57-60530fa337c3\",\"3e2ed052-debc-46b4-ab1d-36a61427fb83\",\"fe0d2019-8e12-4067-ad5e-0be8f92190c5\",\"b1848945-e94a-45fb-8c7f-f50f3615d219\",\"42fa1b29-9d7b-4e7f-98bc-15edc4c3185c\",\"753954a4-7739-48ad-a8bc-df70df358ea3\",\"f138f0c4-b372-4755-a3eb-4154caf7625a\",\"a4402423-9cd6-4d9e-9e16-db79e82f18be\",\"77cea310-4d9b-4f35-b515-797a929ad42d\",\"a0566b78-d560-4f66-b7f6-271b91b5c96f\",\"29b189a6-6b5d-435a-800f-06377a557245\",\"5f4e7f8f-7de1-415f-a501-d30e92d6d308\",\"1076684d-5e06-4705-be3b-a0a5224953a4\",\"3dc1a9fa-a7c4-4b23-ab50-b01c79c35774\",\"20b99991-177e-42dd-b4d5-1f60c5ab3c95\",\"ee62e819-caaa-458d-99c9-8abd59eaa6a8\",\"402d01ee-2dfd-4c73-a57c-980735e7c8cb\",\"12d08452-b04e-4805-915e-d7aa13d6a933\",\"511c3eeb-e324-4e38-8dcb-06d6c4f12aef\",\"eb7acd23-eb66-4000-a54c-c662967985cd\",\"76678ee8-4783-485d-b7c0-482e782cbf02\",\"3c53381b-0eea-4a47-83b3-0bfb33967697\",\"9122fdb7-675d-4259-97e4-c5301233eca0\",\"88b071f7-9c79-44c2-a92f-b21cf2f16a82\",\"91142d2a-be83-4d30-9c56-d1bea5e269b2\",\"a1a66256-f27a-4e58-b2a0-a831f09c350d\",\"bb7fe3ce-ef6d-4784-b629-088f81903654\",\"97d13307-092c-4c63-93c6-366ba7a9587e\",\"a75751cc-7c8a-47d1-962e-2bedc2e9616e\",\"4e0c2361-aaa8-43cd-bfd1-ea51f9e15e98\",\"d2e3851d-22f8-4131-be0a-4cc3b92dd203\",\"817980d6-c407-43b1-9589-ebed80b9444e\",\"da9d9f08-4049-4b25-b779-bccc8a52e814\",\"53e74814-634d-4cfc-ae75-3ee6c8b3d4a7\",\"2d5c978e-bb87-4196-9e3d-7efb2c1a7002\",\"628ed5d6-dbb9-485a-859c-8780624c7339\",\"cfe199aa-ec5a-4779-a688-948b0dc70135\",\"301c288f-36e4-4aeb-9fe1-1a5556ee2996\",\"b5e92805-cbfa-47fd-a3cc-65233ef82677\",\"0aa44bf1-53c2-40b8-b4a4-fa2ee3163c95\",\"f0639164-604d-46b2-bcb4-fcef2b8be163\",\"3e9d3e20-af23-45b0-8e01-84918253db5d\",\"7ab75275-87eb-4320-ad2b-10dad41e301d\",\"1941986d-4a94-41b7-ac9f-4179755fc2ba\",\"8d55d46a-795b-4e02-9c4f-864018d692cf\",\"f072d625-e749-4163-bf70-121ab7d21db0\",\"76b6aeea-428c-4799-9df5-d1867b0657a5\",\"1cd49409-346b-44d7-a736-01f5f7366c63\",\"4f6eb06e-2d29-4fbd-88b1-525ef4221bf6\",\"8856c048-c1a9-4236-8300-f07758f910fb\",\"79e5d147-add3-40c8-8722-6354c5394898\",\"d6b48e79-8c32-4d8c-9b27-e072e6499213\",\"26a957a0-93bb-4e43-9dcc-a333af375846\",\"c5fddacc-21eb-4419-b6f3-8017ddfae1b6\",\"f857be60-ea51-4799-9dca-9093dd346080\",\"443adc90-171a-4524-a97f-6c0d8493c9b7\",\"a32bab00-0b76-44a5-b87a-186ce8fe9e2d\",\"58acaf8c-0f5e-47ff-b4e1-ff997b5d7cda\",\"74b61986-c9a0-4e30-89a7-2ff5814eb752\",\"83b5d812-a505-4558-9b5a-3748eabc0f98\",\"704366f6-5669-4026-9fbd-c126efafb9f6\",\"67e5e7ed-61d5-452c-b9eb-1a6dc3aa81bc\",\"9cfb2698-2617-4402-98ef-e3b5d08d650a\",\"6f44d2a4-00e5-44fe-a107-ad4443e5bf15\",\"cd194168-ca26-48fc-b353-fce10b7bbe14\",\"b54c13e5-7a51-4059-bfde-06d1afd6723d\",\"920c2a69-9f73-4597-991b-8f3ee11a13ab\",\"af5ce4c1-3302-49f5-9515-bd7e77da320d\",\"fa906819-4ee7-4f33-b937-32439db78d6e\",\"937f7901-2ba7-4224-863a-c22fb2958423\",\"4d9bd526-ea58-49b5-b2e2-534ba7c828d5\",\"39af4574-e677-4c48-8d5a-5003552e885f\",\"a17e7bfc-c7f8-45c0-8251-ca5ee0af273f\",\"009ee3c4-17f4-46a0-8758-8c18f91c5c0d\",\"abb63fe7-f380-46b6-ab43-e80a8807127a\",\"a28e4326-ca12-4769-b988-e5b0aead618b\",\"0e69b182-5e13-4ab5-8299-6d6b00eb44be\",\"3e125d19-8357-48a5-ad03-5f0a1f1a893c\",\"fe6e8b24-5260-4fea-847c-147f657cc923\",\"7451d4d6-bd58-459b-a27b-8ca2e12af8ec\",\"bb488ac3-4bef-43cc-becf-34d3d81f1015\",\"77185bee-43c5-439a-9ced-b926186c02f3\",\"55d4e683-9af5-4044-8da1-672a78a06dbd\",\"6c29bbf3-05bd-4f6a-84f1-82caeed20ac7\",\"2c3f78b3-7a65-46df-a462-715572154323\",\"3f2983c2-948f-4fab-8312-15fe4122f710\",\"81dcbc04-1941-4d82-81c1-122f2fdbd6b8\",\"b0392738-5c8c-48dc-a9fb-42bafb790d92\",\"164cb164-84d1-4043-9a15-4cd873f691cd\",\"19a19e96-43b4-4a24-8c8b-b915697496f9\",\"6f42bc37-c874-4e90-8219-3a7a62564cc2\",\"e46911e9-665c-430c-8961-24faaf21f3ef\",\"678632ef-9877-493a-9651-03b6cbc900eb\",\"15924171-d2f6-4541-8669-bff8ce29a54a\",\"4ec104e6-6674-430e-aeb9-046dbd7ddfc7\",\"f8d7705b-68f2-4a50-8d21-d1bfbf1fe8b4\",\"3ab8b757-4caa-4c02-9624-b41a2a165990\",\"9067dbbb-b489-4080-8d83-f8867fe214bf\",\"0f4d2e4b-5b3e-470c-a60f-68577f6620cd\",\"48697c13-f4bf-4ea1-8b7b-8837ec4bfcca\",\"b8c7dfda-e277-4480-ad0d-ab79854a49c2\",\"1a50f4bd-e025-4a78-82a7-5eae19fadd30\",\"5d1eacfa-1048-4dd2-9338-a06fdbb5c7a4\",\"e265d07a-58b2-4b19-84b2-b95d796de51f\",\"019c2dee-a75c-4cc9-8f8c-db163937ee6f\",\"37552088-97ae-4649-bc0e-9c599e27e88e\",\"5adc6ba5-2710-4ffa-b845-dd3cfe90e385\",\"a3c18567-9561-443a-813d-2b333e6b9c1a\",\"efb771a0-5527-4781-b457-ac0a0318d6ff\",\"d519d84a-2b4e-44da-9b31-dddfbe9eef02\",\"cc899008-9494-4ab6-8b04-17247d9b5ebb\",\"12c2f4c2-0905-4b8f-9e11-a530120d3cc3\",\"a5373fc6-5241-4d1b-8bb9-656ec85d710c\",\"b630804d-b521-4915-8940-07163f85897d\",\"4ca3e56b-4b25-4e57-a886-fe92a00294b6\",\"8fc997ce-df1c-40da-b23e-3de939b22128\",\"f1294d2c-75a1-4e67-8ab8-b697fdb4054c\",\"ed184faa-8e78-4442-a7f2-76ae94ba3c4c\",\"f94d1121-5ecb-4438-950e-631d975f5b3d\",\"3373c3ea-3959-436f-ad18-615a4a61c080\",\"081ab938-5b2c-484d-9b94-3085382ab639\",\"d2067cff-f1c6-471e-951a-d1431a98de58\",\"fd9488db-2161-4a7a-908c-0a005130f252\",\"b7969050-8934-4e1c-b23b-117ea3e7bce6\",\"4b0c38c9-43fa-4662-a4e1-ddb9e1ef1e96\",\"e5f8a795-2e87-4c19-a9d2-7edc3254539a\",\"12722105-27c9-4ba8-a3b7-629df895cdd0\",\"6fadc30a-ef8c-4cf4-bfdc-e63bb1dc533c\",\"0bbc7b28-a5ca-4113-9978-4f545ff20528\",\"d1de3812-949c-4d2a-a50f-1ada254e5fe3\",\"214385c4-2288-4c84-9c84-16389bdaf09a\",\"ae63f651-1cc0-4f84-80a4-beb34c6e6762\",\"5f07eec4-862f-4209-a5f4-5cdf9190e2f1\",\"9587d8ca-5cc7-45a5-916a-4163e6daa281\",\"4c0f87d1-c8de-445f-865f-03ea2825a524\",\"a87443b5-7675-4cbf-9f16-246dc95c06d1\",\"b46f03be-0a81-49b4-a8db-97538a168f48\",\"615a19d5-a7fd-49f4-a687-f15fdac1b192\",\"ac1a85ca-2ec1-44f7-b3ba-e41213aead87\",\"52869b87-329d-4735-95d0-d3d03b4666da\",\"e5439640-1b48-475b-b812-66ae6595961e\",\"1f04b8ff-2464-4ee4-aa4e-484d4837a136\",\"10f3a046-67e7-40da-9f90-5359f73822b0\",\"ab210f59-8a96-4191-bdb3-7259cd5d9478\",\"8ea56ddf-3871-4eed-9213-e2bd1d233883\",\"34af4c60-13d4-4b5f-8d26-bd9009f33bb3\",\"49fcee7f-cd2b-4986-b375-ca48aa48f1b8\",\"48932633-77df-4b4d-9cf7-573df1459f4b\",\"1778d703-7152-4dfb-9087-fc8993870292\",\"f9f91bfb-c0e3-4d68-a53b-a2a07b68dc78\",\"6d424c86-d147-4dc4-8085-4e058561d43e\",\"024bec4b-8d4b-4dda-95a8-5f9b821c37bb\",\"19e816c8-f56b-4995-91d2-b83f002598a8\",\"15f77533-1597-4113-ac57-8259508a7894\",\"b0d1c132-7127-4624-bd7e-a50f3221ad86\",\"a7de9326-c838-40ce-9eae-07fcfa224ece\",\"3f3a563c-30dc-4943-a0da-20ca685822c9\",\"b753d836-0563-4fe7-a365-bc911e35bbf3\",\"cc5e0293-7cf9-40eb-83e3-36e781148db1\",\"13cd9b20-3524-4bff-80a0-e7b5e5a31e34\",\"00f7e2e0-9656-40ff-ba08-05efa3ee3e75\",\"544d9b3b-3123-481c-b9ee-c0b0e0971249\",\"7cc1eb0f-4ae9-4e19-85d5-ce1b96bebe84\",\"c63db4aa-42ac-4011-9479-347a0fe2488b\",\"a9d48fe8-7b44-487c-ba7e-0576cca19fe9\",\"16fd626a-6ad9-46f6-94b4-a492af03a323\",\"3b7157e6-721d-4f18-8544-42f266a55d79\",\"3c50e5fd-c047-4aee-ada1-1740b2b77fec\",\"009d883b-d68c-492c-811c-035d8a896b4b\",\"7ed1993b-3ad2-409d-8137-2bfdcd1edab8\",\"e1424b07-987a-4c47-9aed-1785d2a36b67\",\"e5986dae-4448-4c11-943e-daa02614d5e7\",\"3a69433a-9c6b-4920-a6df-c91fbf367148\",\"c6b22d3f-027d-426d-9dca-3b6a91acc84a\",\"d98fa2d1-54a6-4d20-882f-9ccb7c3a2fc1\",\"b8f34f3f-6879-42e2-bafe-aea6c2479f51\",\"11f2ad8e-3356-4d6f-9fcb-846e57d66820\",\"bba29152-e96e-4ef2-9f5f-129262c8ab8c\",\"5d2cc2a4-7f9d-4233-a549-50421b268a3a\",\"80396e3f-73ce-417b-92ac-f67bc2b031a2\",\"14f21d9e-5636-4e89-b7a5-e26218d047cd\",\"264882d3-c1a4-4d24-8a28-d4eb36b1c8c5\",\"a8fe9576-54a8-4256-b8a6-ff7831215a11\",\"470dec9b-3a38-42f7-97be-42e21da9d299\",\"1f4ce3c2-c23f-4e08-80eb-f4f6e2794f87\",\"7bf65740-c946-4a0b-bdbc-7666f3805688\",\"068ffff7-df65-49a7-bc69-6f2004c372bd\",\"f03c9189-2ed2-4a19-9f86-8e870af8f7c8\",\"f7fe3658-119d-4e1d-abcd-814402c51e9e\",\"3208dafa-faa6-41ee-bbb6-1bb29caaafc6\",\"4ef02e30-1277-4986-a050-f5f4c3dc6c16\",\"b2f9cc0a-bf89-44f7-bb93-513d808ab365\",\"d5a1c643-2767-497e-8b08-88fc3a243610\",\"0a63c16e-e95e-41a5-83f0-b33c312a73dd\",\"25e6906e-7900-49bb-a63b-2d75095bb1f0\",\"b0d228cc-a184-425f-8f63-8b6fc47e93b4\",\"163420f4-566d-4954-a54d-decda2ee410f\",\"1127d3f4-58dd-4df3-9d06-443b08ee8fa1\",\"a35671d1-c40a-440e-9b87-c3b759d119cd\",\"7184a1ec-f566-492a-b411-e7a11218262c\",\"1628b15f-3ac6-4552-9588-e3d00d22f204\",\"b6a620c4-9c2d-4b65-ae27-3b56b12b7993\",\"9fcbf7d4-d233-40ad-b608-3c1db5d69589\",\"f1365c48-d40b-404f-9be1-6b0d2e6d632f\",\"a71102a8-9297-4e8b-b3c6-5c4efd06bf24\",\"c46b3300-8823-4e02-b236-7954842eda31\",\"bc38fdf1-a334-406c-80a1-c0bbaf0e3a00\",\"67897124-f79e-4e7b-8d00-5b03f0309d89\",\"65b6ac47-6282-446c-a97b-2bcfbb904498\",\"0aaa685b-2afe-4189-8a1a-00a1ca379516\",\"3e2a67dd-a7e9-452e-a2f3-9a5c73159c0d\",\"746a581a-f08c-41e9-92cc-4258e4985786\",\"9500d314-96a9-4724-a24e-17ee2430eb0b\",\"1c166f60-21c2-4603-b6f8-4b659ded64a7\",\"e06405f5-d582-4a62-9dce-3eeb15877145\",\"cd8a6d33-1bb4-44c6-abed-80b75e632ad5\",\"72673e0d-af49-491d-b2d2-10ffb2a4adbb\",\"25b58202-dc69-4265-a2f6-d205c9751150\",\"246ef5f1-e9b1-4bdb-bba9-da00dcdfc886\",\"06e73059-54d2-4d27-ae07-d66cbbc4ca32\",\"d74b91c5-6f08-4e1b-a167-8cbebf398f9d\",\"531db1b8-6f62-41fc-b51d-078b399c2186\",\"4fecf098-eab6-437a-a407-c34752eacc5c\",\"81d34130-8075-408e-a5a2-115a05684af6\",\"d45bb591-3761-442a-badf-138cb64f2f4e\",\"96d34f46-ad0c-460c-a13d-0153dfaab592\",\"357f8b3d-12f6-495c-9a25-cd3690367534\",\"93f8ebeb-184b-4aff-965d-aa2ae91c481d\",\"0335148d-a68a-4ba4-a1a6-1e0f638ad8ab\",\"36136aa8-a50b-4aae-bbdb-3128c3252fb4\",\"1f77e38d-10db-4a9a-8ad3-d1b0ecbe9d8b\",\"53ecf6fa-0d49-4cc2-accb-f67553d51d68\",\"1e7b0a54-1973-496d-8221-179c94904efe\",\"4a4a02ab-19fa-4826-9f4b-b4aaeae1b4b7\",\"692fb42b-3fe1-4351-81fb-560e1e47b75e\",\"f9023c2a-8cb4-46af-871d-3e5407b0876a\",\"8f823ac2-9fb5-41ac-9212-2eb13ed4b861\",\"1904421f-7bac-4df4-8df3-ffb061aa9159\",\"f010ca83-1805-43e2-9be3-c246b75862b4\",\"f0e4cd96-7a40-451c-9d4d-41a0b9bedba6\",\"270c7df7-ee29-4e82-b30f-bf19cdb831b6\",\"3cdf1147-0b6b-4b10-bd09-7c91707d9ccf\",\"d80d9930-eb25-45e5-b73a-73dbb665594c\",\"ab2086d0-2883-40e6-8c5e-2a20f4e0f9b6\",\"f78b74ba-e3df-40a6-8642-d77c1e5a38ee\",\"078a507b-f13e-4b0b-a212-291c58b03722\",\"160655f6-0c59-46c6-9cd2-2dadd64a9c36\",\"376e8f0d-ccbb-498e-a142-a789218f02a3\",\"eef0caa6-9b58-4880-a4dc-b95ac4692c34\",\"cf09f2ee-c219-4b76-9607-5a0d6a6c4745\",\"e2ac4578-d67c-4183-b178-ad48f233ac22\",\"056cb46a-8b28-4a58-b6e1-8fe935b067a5\",\"23f7f4e7-078d-4a2c-8303-a46060a06221\",\"1962f408-d9c5-42e3-9224-fad0a3b5ac3f\",\"782f9403-66e0-4860-9ce2-5e3d685e2358\",\"b558aeb8-fdbd-4e81-a26f-a4cbff0c81e5\",\"88b3e29a-0015-42f9-ace3-d75db0fbff85\",\"b40f95e6-059c-400c-81d3-79d96a8764c4\",\"0241cca2-e779-40d1-ba5e-378643eab72c\",\"1e43aad0-e32f-4da1-8374-571a07ae4904\",\"3c9a98dc-1833-454f-ada7-2d0dad49da4c\",\"4152b45c-cfbc-4b3e-a8b5-15b1c0837f29\",\"fd4bbbc3-722e-4cb3-9a63-09d2e4606dea\",\"840460cf-4e4e-4680-8f4b-150aa9618dde\",\"9a44c7f5-b7e6-4c34-9105-55fe69b59da4\",\"9be9ebc7-b20d-400a-96fc-e601885ea561\",\"d12c44c5-7c6b-4a29-9ea8-f5ed4f0b8891\",\"e91186b8-1bbf-4652-a4ec-6d21e311b96c\",\"a292556e-2890-4041-a589-786f0aaf913d\",\"6c927394-8202-44b9-988a-bc0fe682907e\",\"91b0e565-a0c5-4e00-a667-ca54f0a4fa0a\",\"1e9c707d-94e1-45a4-b23c-2e60a9e4196f\",\"b96db22a-f052-4c04-b181-024f33bf6a85\",\"55589e38-1462-43a2-8f57-26fceb2f6b1e\",\"b17f294a-3f60-480a-98b9-71215fb7d164\",\"28df53be-05e9-4842-b051-81cc9f77ca9b\",\"e44a73a5-e646-494d-98b2-295dc33671db\",\"da009fd5-5f8d-405c-b151-9dd76badb86c\",\"015e1855-3c5e-473c-8744-d3ab87097206\",\"d8191d41-dd41-4c24-a012-80eed566465f\",\"fff4eae0-c0a4-482b-a387-c0d27d6d71d2\",\"770769ad-fa10-4f7d-bc97-4014a4424599\",\"4b5b8fea-8e3a-42de-a0e3-dff1c93cf866\",\"18789aa6-960a-4bb4-9389-be3e57bc23de\",\"e77543bc-0876-4908-88e6-f13ccf4ff3c1\",\"0aacb722-55a0-43bb-8b43-428ffb33ab0c\",\"e2c33e78-908b-4e97-8c3d-3ba57531bf22\",\"e97943bf-cbbb-4864-b22f-8d70d0f440b7\",\"1715616b-4fb5-4835-891c-09e79e1b53b0\",\"ebaaf7f5-8620-4688-b4be-fca87fd1dfce\",\"82e1539f-a8ff-4bba-974c-b56889b1de27\",\"53f1346c-48ca-4d38-b9fe-e5061a40b424\",\"f6786f0f-d53b-414c-96ed-cc7750c6d760\",\"b3f14e97-669c-4494-9f57-c2dbfc129945\",\"07d3b77a-dcbf-4ae6-8a6e-b8e0d570d091\",\"7e9fd4d1-a0c4-4ee6-af9b-a80cc79ae2d6\",\"1f88b9f9-aea5-4759-96b7-4c5dcdff6918\",\"0b6fc495-24f1-4ae1-b588-edd50515c806\",\"e98c0450-eb5e-4aa2-885c-43b50a54cdeb\",\"1f3e42b2-2f8e-43f1-89b0-48423b03126e\",\"4f2d9754-71e0-4e86-ab52-02def492e70e\",\"6fbb650d-90d4-4043-9744-d00447e6cd2e\",\"352640bd-eeec-4790-9b93-1456ed5f3184\",\"b49456fd-282c-4295-9d7b-f8c76216d32b\",\"5cf51648-9f4b-4d3f-ad28-87990c10a893\",\"8e2be1b7-6851-4c8f-b85f-c0071e572685\",\"91d1c532-5771-4222-8988-526f64fcbef3\",\"4cbefe5b-e39b-4b0f-8b0e-5cc824e071e2\",\"ca0c3328-24bf-4bb7-90d1-ce40d714874c\",\"5e179f75-44f3-4ea9-8638-abdc81a9bd8d\",\"e40a6d39-9b2f-45d1-bee0-c330e1bae46e\",\"3114df98-1699-4ba4-956f-043df21aa9f2\",\"430373ec-0658-4465-b869-76da552a8be7\",\"8f1df20e-93be-4d5c-8468-9e011f4c9f66\",\"7db1826d-d5c3-44a3-b325-12ffdbd902e9\",\"35ffae2c-9c84-4a56-882e-b054be387f00\",\"982755bb-1da4-41ee-b40c-095b013c5479\",\"b4176d0e-9c90-4e04-91b9-d1f2713e8565\",\"c21633b1-4f6f-4369-8d39-aecfe5d018db\",\"c1614a02-d1a1-492e-b95b-9939b87f719a\",\"deb48120-c1e2-4de6-bcc6-010c03311e2b\",\"1a3aacd6-79de-4910-902f-e1571d6cfdb2\",\"a7bb7c66-3579-407f-9c78-53e81a489f8b\",\"dde19e90-a3d4-4574-8d91-62f9799022d6\",\"6861c843-703f-4992-b511-ff139f069417\",\"021f1a0e-0aca-4246-a0f5-d198873f41dd\",\"9bc80e36-e263-484c-b216-0b84319c85ec\",\"b904ae1b-6f55-41ef-bed1-058f5889bd3a\",\"c63c2e3a-7d41-4caf-adcc-407e83204039\",\"9f269c63-5db0-415f-a9c9-73c741daccaa\",\"44056635-2b4b-4980-be6b-3d5a37710e0f\",\"70765de2-d674-4d55-bab8-829061af5a09\",\"bbaf9a21-eb5a-4af5-b0c2-ad822633e1fe\",\"43207458-5ea5-427a-96f2-df71415e1537\",\"5e5c1770-f729-408c-a88d-e21a1a8acb04\",\"c17906cc-c735-48d6-9b10-621052d19c76\",\"1060afe7-6dbf-4bea-a7d6-8b1a06240cc1\",\"8442b12a-42ac-4701-a6a0-554c549b014d\",\"2a1ca7a5-cb4b-4270-9895-b840fead8e96\",\"84f56b21-59e7-421d-8449-4d1c53d0d0fc\",\"baabe223-314f-4730-9944-1f178109f2b3\",\"048e4061-e5f3-48ec-b4ac-c4b53cb427e6\",\"10ef7692-de7a-4273-b55e-acb64fa1a794\",\"dae85c8f-c59b-4200-9147-dd45bed3ec5c\",\"a3500889-020d-4c9f-9004-3e83d428d01c\",\"86e103bf-7759-4169-9115-1eff5e9bbd63\",\"336b6f91-1765-4731-8176-5de62953c234\",\"facb1625-13e2-474a-a4b6-4f2da57a85f9\",\"04ec8713-870e-42c5-9a3b-0dfe2b37cc1b\",\"c9c7c71a-866f-4b4d-a894-bf6c8a95054f\",\"d5db6444-cdb9-40d3-9e20-b4c9989ad16f\",\"10e0bd66-af08-4be0-8840-247bba2ba139\",\"deba0d40-9061-4982-a967-3468bbe2396a\",\"3c2837c0-ec90-4fa5-9c49-5c61428c4815\",\"d67871ef-d0ec-4cff-8872-434d3f1af917\",\"02d55060-28b0-4882-8526-9a662724c08b\",\"db098377-cc18-4744-9fd6-209fb591866c\",\"ea6ae519-39e6-4dfa-a65c-c76820883fbe\",\"d70dfdeb-7134-43db-8f84-15ba433ed30c\",\"1ec4357a-9138-436a-a4ef-1b6b3b23fb7b\",\"312e68be-9464-4a59-a404-82aa0c2d7911\",\"fc081139-29b7-4cbc-b3e0-b3278baeff4d\",\"a3bdc839-6ab5-4027-8128-bc2a0904bced\",\"f400aa0f-46a8-4977-a550-5b5824038699\",\"cd1818a6-1c5a-4495-9d9e-8292532dd28c\",\"ad79fb70-298b-4b47-87e0-93e980539db5\",\"d86aed87-7ca1-4c2c-936d-220793e05484\",\"ec46dfc4-617b-4165-a9e0-a02008c9e718\",\"4628afa4-d273-46fa-b0a8-b3c98287a424\",\"7c5776b9-a862-40a6-b43b-b9a02d8622a5\",\"913a1a92-2f35-483c-bd32-02b58cebef74\",\"be8690aa-796b-4d96-82de-028aed2b956f\",\"e4a85103-5d6d-4768-8e36-cc35074db86b\",\"d59350fd-61f4-4565-9159-d87d819f765b\",\"7aab066c-242d-4c4a-9472-c53ebf46832e\",\"2d7cdd9b-b5d8-4603-97e0-c62310664bbe\",\"3c500c41-f377-45b3-a3ba-02cf02dcebac\",\"101f1230-c5ee-4451-81d1-d4aa671b0899\",\"5d3a05ce-d7e2-4675-8f41-6d07d0d54280\",\"1f7ea28a-fb23-4896-b467-28a61b901058\",\"ad08e364-b00c-4ce4-9509-e4db77a59728\",\"9fb35a57-17ad-4de0-9368-240a24603ad8\",\"94a76355-2271-4116-a829-5ef795feb823\",\"ce6f1610-c691-4ba6-ad75-62576c7335ad\",\"4468975b-5768-4be7-8868-e85da235fc55\",\"892c8cec-1cce-4a11-a07f-3d8ddedb1f90\",\"7580d4aa-065f-485f-ac9a-3ecfb550434e\",\"fcbbcf4a-a226-4964-8331-a92ffc337a2f\",\"a9751f61-5a88-4fe3-8a34-b7b6b3e385fc\",\"a449844d-8b5d-4006-a040-e6928828791d\",\"60652dc4-239e-460f-bb2c-8714422c5e0e\",\"d629d45b-f9dc-43b4-93d7-8e283c44cdb0\",\"0859fade-8194-4d85-b32b-2df896ef1eff\",\"3bfc3f37-2c1c-4040-aaa8-13f367b167b2\",\"bcda5f68-29c4-4e03-bc03-975739f15f08\",\"bb8dd048-a190-4d8d-a989-8e44bb6c7959\",\"b75cec38-ab0e-472e-8683-0b934d57facc\",\"7ea981d1-c3ed-4b5f-8d89-1f02e2a64680\",\"69bd73cc-33ca-4d61-824a-bd3b9a9eee58\",\"27fe9081-8aba-43ec-bb9b-33454ce619f7\",\"e873aa9c-4f07-4962-a976-63208f9657a7\",\"2e30fe38-54a6-49d8-8577-654f978f9acf\",\"1d571ab0-2444-4c52-9d76-625be85155b6\",\"5e131246-5c14-474a-9569-1a14d64269ac\",\"5ed107b6-2854-4516-bdf6-d01e01dbcd5b\",\"4455af57-96f7-4ca0-8cd4-a79e509f7895\",\"319f726d-c98d-4f8a-ba0a-d42d938a7eb3\",\"110ee578-6231-429e-b1b0-b3fe24cbecf6\",\"bed97615-2ab4-4ffd-bb29-0251b7d7549c\",\"d0fc49b5-b9b0-4919-9569-bd48662f545c\",\"c4a20ac8-ce68-4cae-8556-1cf3d40d7986\",\"3e61c4b5-3764-4c7f-a692-0d18de8cd4c0\",\"672a9a61-a2eb-4a4b-8a37-aca740e9e900\",\"00a58f50-440b-4f80-93d4-0e29d0ef6b90\",\"acb8fa89-15fb-44b0-8eb5-bb6f474510a0\",\"41468808-0d4b-424c-9712-e809aaf00c67\",\"440918ac-11f4-45b7-89a5-7e1b3bdb6ebd\",\"d549e591-25b5-43bc-8a5b-40b20ebd648f\",\"16373f64-f736-412b-82a8-57a625176a9e\",\"7688b768-e374-46f7-b050-9724764cb471\",\"7ce3a1d4-06b0-4575-813d-bb51dc788121\",\"d4f2d365-bcc1-45d7-ba42-688522b929e8\",\"60bd1384-1be9-4330-b340-adae5b12cdd0\",\"7797cf79-c534-4cc8-87f1-55b48fde5037\",\"3663cab3-8573-4cd0-8707-9b0978f92fa1\",\"ac742481-4697-4299-b742-ed70514d4e11\",\"82ee71a4-438a-47b0-b4fe-dea08b0440ef\",\"b28638ba-643e-42c6-8988-a33fa6d1af7f\",\"54f412bb-a957-4092-94dd-ff4fe92f9fbc\",\"23ba205e-a407-49c1-8a3e-a07d9256c0ad\",\"1f3cf851-3e1b-4282-929a-6a9d790277ff\",\"3a2c9154-6a10-4524-a0f1-0239a3cd7c37\",\"1594c1cc-64c7-4c10-a160-7f24cc718bcd\",\"27061a56-456b-43cf-8356-b9a9adcfc949\",\"5d99fd44-dbde-4dfb-896c-aebbdfa29dc3\",\"d8eb8dfb-ba83-4e46-8007-6907c6226cfa\",\"6ec10577-75da-40a0-84ab-abcf727cbc7a\",\"f6b64dba-53a7-4fa3-a1b1-f2b472c4b204\",\"75a95106-66e3-4add-bd7f-30dc827dea38\",\"e4bcb1d1-f819-46f0-a6dc-50bbd67cb8df\",\"2eea9591-9bb9-4d56-924f-3c0d2ba96f68\",\"bb543fef-8d72-4923-84de-2a1df1b9e726\",\"ccf666e3-98b8-449b-bfe0-4f787ad81a83\",\"d0139b22-3e54-41cd-80d9-5d88c3d70446\",\"325a50b8-ca69-45e4-a937-be7d936de9d1\",\"1fb4ba0f-d155-4902-8338-a5681a917038\",\"27e9059c-5a91-4d4d-9708-57295cca422e\",\"121ee3c0-18d1-486d-9cf1-c6966f73a81a\",\"cc0a5ec4-13b5-403d-98fa-c04cf2c68a6c\",\"2647cf0e-6c51-4aa3-a8a5-5d04bb8fa116\",\"b9a366bd-0e32-4102-bff5-cc551cf1a2d8\",\"48bc4298-b30f-42c8-ae13-f0b2cd0317a5\",\"062f9fa4-0315-4f61-bd46-14857f1e3853\",\"0a8c9e32-2566-434f-ae3a-4d146c0aede5\",\"2ece8e90-394a-485e-b5ed-fcbca0707749\",\"8cf97351-f03b-4a35-bb5f-787bab0f735b\",\"2454e1df-1abb-4728-be2a-0f27d064eda1\",\"d0d58345-48f7-4f94-b6e8-e4e599434f42\",\"683008c9-d952-418e-8f3b-279d3b4dbd02\",\"efd31526-725e-4ff9-a73f-e6ce06f98122\",\"6dd86f02-a146-4ed0-827e-55a19dee5055\",\"8b361502-c8db-4cdc-a47c-00a2f1f21335\",\"9271a13a-52f8-470b-9fd9-76dfa3c4910d\",\"54114c5d-f6c2-48c1-96b5-c439ebfcc97a\",\"9d4b6d28-8881-4943-8dfe-6362eba08472\",\"8e0b932c-06ac-4d6b-aba1-7ce9395199cd\",\"cde2229b-964a-4a65-8207-211b8a080313\",\"d6142bf3-670f-441c-a2ac-56cb3f42dd0b\",\"2dc28e0b-d503-4b98-bc98-fab6ed5bbace\",\"bc0e97f8-3c3a-4ccd-a013-d4d090a744e1\",\"b0a72fdc-79f5-495a-b8c9-61bca8cb29d8\",\"732779b5-7a2f-412a-94ce-e2775deaf945\",\"1dff2feb-1f80-48ec-a1b1-13f96e418ff3\",\"f5da9d4f-a9aa-4681-b440-e523fc35198a\",\"791bdf18-1e46-4159-ae47-65b0d281f6b1\",\"98b0fb1c-69df-4899-980c-8b929bf0fd15\",\"82cf4ae9-3543-4faf-b3cd-b01b0e6c6366\",\"53ab63a5-2e59-4d95-b586-2d87cf7b4157\",\"603d166e-701d-4fe0-9f70-7dacd77d538f\",\"545f7388-752d-4438-97c4-a1daf2ff60ac\",\"9ecb4026-1a94-4830-ba19-d01d603d5d6e\",\"eee276a2-55f7-4c59-8860-70e42c02285c\",\"f1658798-4a54-402c-806e-d0d348171bf6\",\"379fcc40-3b0a-4173-9e20-842d506d8369\",\"22f91d86-12e2-471e-9039-efae8b7fd57e\",\"8bd781d5-b9e2-4492-ac1a-92b3b2cd6626\",\"ea289926-8247-4c00-992c-d58ba60bc7a3\",\"52f03225-5336-4c7a-b536-9bb1f149b6e8\",\"2e95e43c-e688-44be-b198-e340a3921f1f\",\"9e63a12f-72c9-4ae4-91a2-636a4f6b0d6e\",\"6efe3493-ab20-4e74-8fb0-51f61c0c1376\",\"259c4d81-a4cf-4e75-9932-66b8d69aedaf\",\"7976a499-eaa0-4179-a674-03f4b7e4957d\",\"9c715494-db05-49d4-b391-7c2042420836\",\"978394af-7b9a-4f91-aff1-a7aa23e39f88\",\"c13fd510-b0a7-47ac-b93e-35d6dac949ac\",\"7bd5238d-293b-49ac-a9af-1a41b6c54c5e\",\"0dccd803-0333-4355-b898-32dfac897074\",\"b9af786e-e520-4654-be35-fc92c070e881\",\"bae38511-7673-4b0d-9955-8f251f23d5a0\",\"ba8428f4-9e14-4052-9e53-5bbbf792c9e5\",\"c7dfab2a-5298-4139-9b33-21eb6afa53bf\",\"66f39aab-42a5-4cc2-9daf-e08b72805d60\",\"bdd9bf7a-1a60-4103-b51b-8d63a9d4b5a2\",\"2018ea87-6702-4847-9136-b06af81a7981\",\"6a95eb45-9cf3-46a9-8cb2-59770604b867\",\"77a379c8-2d94-4918-b206-601b8585a43e\",\"96ac8044-6c0b-40f4-8b4e-0c40a28064d6\",\"92003962-6335-4edd-bdbe-2d386761ee2a\",\"46545978-f08b-4ff7-84c7-2126a3c73e52\",\"2c7a161f-a6ce-495f-b503-cdce63d03db5\",\"1004c7a1-0516-48ef-912c-fb227ff3e6d4\",\"cd06feb6-fdc6-4f74-9fa1-465e7661e034\",\"70844f7e-a34f-44a1-996e-4e90ee68d608\",\"26a278df-527b-4ec7-9141-dc22a1609780\",\"a71786d0-a210-4fa4-96d9-8ba574de09fb\",\"ae179c27-b918-4920-ae65-09519b8ac9e3\",\"66b5db63-ded6-4d07-ab40-091251c82aad\",\"b36290dd-9af4-42e6-9e2d-bec9325cf860\",\"c92743d4-0484-4ee7-b976-34cefe5e58d5\",\"601ee4b9-2157-4d09-b70c-f221f4ac7447\",\"e993424a-08a3-43b6-b304-d1d00aa58819\",\"f443b1d6-ef33-41fb-b752-a92fe6ad081e\",\"86daf43e-ef81-46cd-b421-c793a8adbee9\",\"7f36e216-4c6e-4777-a2bc-8bb5b8a11fba\",\"75829849-3a13-47d6-8426-9861ef10fbca\",\"6125368a-2e14-47a4-81a4-19d3af1eeb9b\",\"35aa24c6-d5a1-4bce-8911-9c01f571ac10\",\"41e32941-2d14-45ad-b97b-83cf0d64c1b5\",\"7b864c21-6dfc-4245-a09c-3b3a6c769453\",\"ad95ea34-6ea8-48f8-8068-7cbd42d292c7\",\"8fea7da5-a8e8-4c32-a4ec-2b8071ba8309\",\"9d4af667-55ae-4d45-9956-cee9001d9732\",\"26ead4e1-4404-4f96-adf2-08d88b3f84fe\",\"ee8c36eb-a8f3-49d3-b8bc-2d55608f758c\",\"85dc4d2b-ba0b-464c-a4ea-f9956dd9b01d\",\"010fd3fb-61a1-43c5-a65c-cf2108fd4a45\",\"cfaa475a-8f73-4ccf-9c5c-b5cbed3452b3\",\"5c397285-e292-4789-8d1b-1ecb0d9abf5f\",\"f53e2178-50c4-4c93-923f-5f1b86d71bca\",\"5bafd36d-c8a5-4755-9c78-a720cdf22ea4\",\"ff6ee572-40a6-4b63-91b0-6879806bf06e\",\"24304d17-1c39-4ed9-9e18-136833f697b4\",\"180555ac-6734-434e-8009-7c1c7e602ef9\",\"bfcb7d22-084d-4129-9866-ba534a904162\",\"148bd450-6faf-4f99-84dc-78dd065e9605\",\"d798ef08-17eb-4610-bfef-02f0ae2ff21a\",\"0f86034f-78a4-4b2f-b968-258c4b1404ab\",\"8c4cf517-f219-414e-b701-886aaff9c40f\",\"9673a5a6-218a-4644-b641-451019deb1f8\",\"67d7c362-a274-4c90-9ebd-83b70db6290d\",\"38369471-70eb-46c4-90c3-37f3bef21c7f\",\"7d3af97f-3b24-4d34-98a2-042371746f48\",\"01eec0cf-edcc-4068-9b6e-5df7760a727b\",\"8c1b880a-8b39-499a-afdb-7d53499f8738\",\"1d64fcaa-c7ca-4952-8848-3c112eda29b0\",\"f933a760-6b47-4877-825e-f4add836a49d\",\"1627b1ee-4a4d-46a5-bad0-ba6a49d2b886\",\"855d0a22-b2c0-43d3-90f5-44d0a3ab347f\",\"b294028f-5720-4dac-b21c-d8f6c1398735\",\"7d7e8452-40db-4377-8084-a1f563f2ed53\",\"11c10925-6b2b-4336-91b8-fffaf9a63fe4\",\"b53dd3c8-9bf5-46aa-9699-0c0322a830a8\",\"2b6edeac-ab72-4ab7-801a-e5405ebb7c7a\",\"34e33b93-96db-4764-8b20-18e1983bc781\",\"3b9c279a-040b-4c2f-a7aa-dd2977f0e854\",\"5dd54ad9-7236-4969-9b95-46b4dd25dba1\",\"46b07213-2579-4ebf-b688-7caaf5cebeab\",\"6af44f8c-8c4d-48be-8968-d6cd1f111f28\",\"6cfb58d1-1c4c-4fc4-a895-52516574bd26\",\"2ad4cf0f-e2da-41d4-b6c6-7edd44cb32a7\",\"48a6cdb3-f1ee-4860-bc08-26b4cf309ae9\",\"8f68c031-32ef-414b-b6ae-ceca71264675\",\"e8d1415e-0f14-40e0-a1c4-a33c628b5a33\",\"600c02c3-ed43-4785-9614-248498a0de48\",\"0a880266-0609-4d93-a490-4352ffa285c2\",\"d27d6f0a-1911-41f3-9732-2da11e4a0ff1\",\"8e572072-8ffc-4435-a82d-f0bf9a74dec6\",\"adba6afa-f86c-4f0e-a85b-6078341418e5\",\"57c8375a-21dd-45e4-9bbe-878d839ca836\",\"daab3201-8ae5-40c9-aca7-6adc2ea465c7\",\"aa3abc51-dd40-49dd-ae95-5666eb38f404\",\"f728c441-06ea-4fb8-9b5a-8dae0a756a2b\",\"8ff2ba87-3aaa-4328-8144-81397bb30dc2\",\"d0c885a0-0c3b-498f-b2da-402a7b5923ae\",\"8ef2c2e5-7a0c-4890-8713-062872065a5f\",\"87dc9606-74d5-492a-816a-2a6942575571\",\"fd480877-0368-4fac-a4ca-72d0697ba9d2\",\"338c3197-e7b5-456b-ac55-ea6695b02ce8\",\"fda1d914-c031-4cf8-9d77-c6923642d2a9\",\"f958ba38-80e2-482f-bbf1-cbc6abdae660\",\"19ba2007-78be-4e4d-88f2-afe14b54f130\",\"3bf4bac2-7d37-4b4b-9203-cbc6c340f195\",\"1bb952e4-0b45-41b7-aa7e-501640c57364\",\"6668091c-d457-4427-a82f-64278edbaabd\",\"ea45df49-5a0b-42ba-a89a-66fb638028b3\",\"afbd3113-0e68-4549-8c48-bf8f7a8a4778\",\"842f63b7-b74f-4866-a75e-63c3b020744b\",\"6166d90a-05c9-413f-b231-2b6e5b259848\",\"224f05c3-6421-4a88-9bb2-a66d54aa9034\",\"fb8386a9-e0d4-4948-a40b-a3d5d7975513\",\"0b5ac8b9-c28c-4524-bca0-986339255892\",\"9b108436-bafb-4285-9608-7ea3e4bcdf01\",\"95794c06-3054-4487-9fcf-1c29ab37eea3\",\"aab3f848-8e96-478e-96e2-700ed9d8354f\",\"0369c828-61b3-4953-8c5e-b47eeef3eed4\",\"0d985290-fe14-47dd-a734-3f7d89d8c5b6\",\"783e7f00-2d5d-4056-a5e3-1d53730b7577\",\"3592609c-3860-466c-b1f7-c3865116a0bf\",\"66041166-57dd-4c37-8995-7c685d9507bc\",\"2fe90355-c3f4-4e14-b37a-c5736030f0ab\",\"f0042c97-4741-4dc6-a009-f7054967ea5e\",\"2ba08178-7c4b-43a0-b77a-6b8e007d6b1f\",\"9149ef05-0a96-41e1-891e-8b2cf529ba6a\",\"e84a35ef-0e45-4796-8b6b-704729cce9b5\",\"abca669c-65a9-445e-82ea-9a2c07b56d9f\",\"ddbbc217-1db2-4a98-9326-b7517f504ae4\",\"3c8cdd72-70dd-4848-9940-69aa2c710446\",\"1a6490c4-b375-4e8b-9da8-9e35e76b21c1\",\"6ed5f20e-c1ee-43b5-bb10-a850b6aca9f0\",\"21de2aa0-354a-4e46-b249-2884c53c4fb9\",\"5554b19d-cbca-4832-90e4-55ab1050ae08\",\"c5c59f1e-6486-43e2-9612-c73bf3311d65\",\"ba6a0bf3-a797-45f1-b01d-c7505ccaab5c\",\"2495e3ce-7988-42b6-a33b-f305a8148b01\",\"2f15b19d-5664-472c-8812-c0db3e8f17ca\",\"91860309-c6f3-4c7b-a930-828aa4cefed8\",\"51e856c1-f1c9-42f0-bd60-b0e308a8e025\",\"afd92375-5805-401f-82b9-a09bc728f44d\",\"06e6d03a-4f94-4483-a2c0-6e8303804b44\",\"9886a207-0aeb-4ed9-a757-934f12982204\",\"c62a33f7-5e51-4a8f-81e6-d842d652426a\",\"c977bfb1-1bf8-49fb-97b0-c00550fd8490\",\"7157d853-3674-4b26-a294-2ec7d927b6bb\",\"b48b689f-cb34-4ee2-9d60-ffc0c059586e\",\"5343b7a2-b38d-443b-b89a-f0022c49ea25\",\"d080dab1-19e5-4667-ba9b-8a1b6bfa99b3\",\"1b1ac383-c122-4f6f-bbf4-29581d96c619\",\"0f05cd2e-89be-4605-804f-4d1583fddc6b\",\"e14e56a1-c870-4ca5-a962-b62abf0fd055\",\"261edc3e-1220-4a38-88b5-b527f15ca70b\",\"c9e6a050-f246-45b5-ac62-90a07c024c2d\",\"abd226f6-727b-4674-b2ce-33015c8d486d\",\"39e57997-d913-44b5-b568-bd8ace330bfe\",\"7f50dfd4-8622-448e-8827-390df4d43db8\",\"449b5a6a-9388-498b-9050-a69783a6f7be\",\"421530d1-9b4c-4153-9381-502fbe4e4dcb\",\"fb2e2375-9785-45fb-b5b6-0a9619b15ee8\",\"5415d213-b943-4765-901e-8afe5fcd3ee6\",\"befadccd-11f4-4e12-80d7-b8dc6a954c37\",\"9e664124-6e42-42b8-af0f-2e216e4d5865\",\"86f8b29c-523a-495f-944c-dd543ff22699\",\"05795f91-4086-4e70-b8a0-51316a176d1c\",\"1d7d39b6-1e95-46af-9b15-28c1cfb482d4\",\"12732e2e-51d8-41d8-863f-11b99147c1ce\",\"4c11463b-9bcc-44ee-ae35-7db676fcedd7\",\"7e55d638-3fde-4f67-96b2-791c19949554\",\"cbbe2749-095a-45fc-bba1-49645288bd9b\",\"5885c57b-2a11-4bae-a116-8f486cb33af8\",\"10a86a2d-aaa2-458e-815a-09ded55d4d9f\",\"bc5215fb-fb4d-4c13-9055-4525504b38e8\",\"f84d94f2-3fb9-4d38-a98b-8fef8ae2d438\",\"11c1ac33-0eb6-4002-8c10-04c0e8ba6022\",\"9498b6c4-25c4-4ee2-a6ef-72f472f11dff\",\"85ea4f06-e5c8-4ef1-8548-c09c3c898a3c\",\"6c371fa7-5b64-4f71-b6d5-8fec845f89a7\",\"06cb8aad-deff-49ea-a48e-937e3004198b\",\"b71da640-3da1-4d04-b7e0-3ef0ef0dfa2a\",\"b4dca41f-82b7-40ae-a328-9867b7612b21\",\"2c4df567-61a1-4c6a-a0c5-9c9230cf3c2c\",\"37603f81-4c4f-46ca-8b22-045121694c57\",\"9cfdc14d-b899-4b45-aa73-892659ee19bb\",\"6196f7cf-8c0e-4521-8971-d4d592e9ca0f\",\"97088982-6de7-49c2-a02b-debac0e9b3de\",\"2c0b8baa-03ee-4f7e-aa5b-9fe75474df1a\",\"3a8e3720-e43f-4fc9-8dbf-2141a844cacf\",\"0e9c5d40-8e27-4f99-9ce1-5c139e891e60\",\"c172d0bd-69e7-4c13-8995-c25cefd44928\",\"9baa0aac-43fa-4c31-b95c-e50360b9fa1e\",\"1e6f040a-5a76-46f8-a776-77e9fb99747f\",\"ed748c79-4792-4d59-a100-c225c2da0bf2\",\"efb61b40-adc4-415c-9c20-931279b68419\",\"d3c25942-8f40-4707-bb4e-b1b8e4f5cf01\",\"e738db39-b244-4e82-bb7c-7d39f80d0f16\",\"85042785-ef63-4735-bba6-c3bf67b3e6ce\",\"c08c6f81-f874-45bb-a74e-2cf107bf8167\",\"96de3406-341b-4f17-ad99-aac69ebd9bb4\",\"2d2941a0-63b5-443a-ad26-c5d764eb7765\",\"6d85b5fe-f9d7-4b87-807f-0bf38dffde81\",\"c6dfdbb2-1c1c-45db-bcdc-064faaa0dfca\",\"26b6f015-b1c5-4eef-9830-b77b9949211c\",\"920d1235-3ea9-40fb-afb5-b2b615ac4187\",\"9120ae1e-c317-4ee3-9083-b41556028c70\",\"f41ac2f9-b24c-4f30-8875-94ba1713ea7e\",\"8f72bb3e-6151-4638-9220-3864421f50c1\",\"14741ae4-6d31-4a4a-bedc-0aa1e1c6c957\",\"5de0b4ce-3b5d-4695-9513-e02e6ebee6b5\",\"509999ff-1a5b-4724-90cc-30872054306d\",\"f5efe1d7-9d04-4420-a273-d869b067be14\",\"bf97d02b-1806-48bf-9a5b-4ae0b9e202c2\",\"1233a384-7dc9-40e0-ba86-d21d6db36d58\",\"f6563ad5-836e-4f02-9529-4240cc8c823f\",\"0aeea8f2-70c4-4f36-b0eb-66f77c54cb83\",\"a9b7677b-1528-438e-96ab-f3a8f06b0428\",\"37157299-44a5-4aed-b709-3e06b5d1db17\",\"8a69b117-abf8-4bf8-afd1-58904b799877\",\"72ef0280-7754-4ca3-9287-f4ea37582e5b\",\"0c8be5f0-b87c-451d-86ad-c51eb148fa25\",\"68029e7f-4af8-455f-96df-9791fd44384e\",\"111b3ba8-8e75-435d-a579-4f5854d2915e\",\"314807f3-bebb-4087-b736-e3edd2c8e9ef\",\"08ecf2e8-095d-4768-876d-a289947de552\",\"141fb6ca-d8e1-4318-8667-112c57ca7af6\",\"d73cace1-ec7d-4cd7-979f-e73229f9cb7d\",\"02995a4b-5c97-4f5a-8ad4-2fff6a83e19e\",\"7be20a9f-cacd-466a-9e7a-e57c8b035e41\",\"49e2ec5e-f1f9-48d6-b30d-978fb80fcb86\",\"5f0885ff-2d21-44c9-88c8-7b6d5ff0f671\",\"e8dedc44-747e-4e6f-9a46-fd2956ba3cc8\",\"247a70ad-1448-45f2-a158-20b284a05563\",\"82f0a389-f4ee-4309-a269-010054e064de\",\"ad8cf4bd-e4bc-4f71-b327-badf3ecd7a4f\",\"3bcd83b6-9b24-4b70-ac8d-0d1dc0d03a71\",\"8c6fcbaf-2154-4746-b7cc-88478b231a23\",\"19c86146-017b-41c0-9b12-28a02c6cf156\",\"cccc2f95-f67c-4770-8f25-2f2925e94f11\",\"46c6e8a4-12d7-49ef-a785-bd7a7ef43bf3\",\"d7c5f6a9-bcb7-4450-8293-9fe801f520f1\",\"6f037173-8b0e-44d3-be8d-8d64e86575e6\",\"34c5fe5c-eaca-4e5e-a2ae-79e1ca1b26ec\",\"ce5a0ef1-2c9f-4145-a650-8ccd91254113\",\"1256988c-422e-4b90-ba11-44b7d26afbfc\",\"166b3568-6569-4e41-b9d8-b9dd4fa053e3\",\"01d532c9-cc5b-46c9-8f2e-e29a583d17d0\",\"3c1b87ae-8451-421e-8b69-3e55ceb1371f\",\"988f9895-5081-4fe8-a0ab-b5acaf07dc6b\",\"35345c43-d968-4d57-aa3d-933b0cf47aae\",\"8a1d086a-c2d8-42e9-aa98-0bf79094dd94\",\"878b2820-b3e6-4163-8d97-667a7c924648\",\"c37451cd-9fb1-4ed8-8b58-497ecf216292\",\"c7b90a02-22bb-4a98-8e44-9889d66fe187\",\"fb725500-0a6d-4af7-bf49-d5eeccefe8c4\",\"b5b29497-9aba-4982-be53-98f3b3e2c65f\",\"7cdfdc18-4afc-40c0-a1a6-ae9c462a3eac\",\"06b9395d-1e99-4d38-b3af-8a50d91b3a53\",\"0feba478-f234-4618-9900-bf75910538ee\",\"df6050de-cb51-44e1-be40-a1d534f0a976\",\"9c344b36-982c-4625-8952-546d84c319e9\",\"8c2bd77b-5de0-4e13-b6c8-e6d893cbc50a\",\"8979b4eb-051e-4403-b6cc-de22a0910033\",\"2517f66b-1e84-4bb2-9ec0-e3d4b9e86500\",\"83a3c030-a6af-40c5-838c-9342c9654fcb\",\"c78a8f46-c53b-491a-a878-1aa594128d7d\",\"ef720524-5e78-4312-bd10-c5047564aa0e\",\"9b192b2b-c5c6-4bdd-9497-556111f640eb\",\"66140811-0042-4f7f-976d-e2e240e17d9f\",\"51373914-eb6f-4fc1-875e-e9334383a786\",\"3c696571-d0dd-49e6-b1de-e2013a48a9ee\",\"50fbb76e-0938-4e71-b645-1651b1ed58c6\",\"0bc0af6f-a961-4a30-975b-7cb8a79833a2\",\"44c3a5bf-a3e4-42c3-87c9-3fd7abfee984\",\"380c90f1-44e6-4e3f-b492-bd07226dbe7f\",\"188e8f1d-b05a-465d-ad71-442c35916d6b\",\"b5db5da5-ae50-4e53-a43a-f9e5d6f58616\",\"cc461052-109d-42e1-8217-cfc8c2a5538b\",\"386579c8-3c85-4e86-a84e-a0f79286bacd\",\"47903e5e-7a27-4fe8-ab77-740c2675f39e\",\"0810f87f-1f71-45f1-945d-7fc4ad13aadf\",\"39ada6de-3c4a-4720-99aa-0a3c5f7717f8\",\"4fcf5d49-086a-4080-89db-ae7ceb2a2139\",\"e4528f60-dcaf-437c-9da1-aa0dcbe531f8\",\"d4fa5592-bf8e-437a-87b4-206e1edfdc8e\",\"9ee5c443-f55c-4553-8c4c-d50b2ce4cc9d\",\"b4b7cfae-8a22-4fbb-8026-3bd36d0057ba\",\"e036f56e-e1c9-450d-9fa7-d7c7c0f2b560\",\"02a86afb-a6da-405a-8d89-39dcba592bbd\",\"c2590dea-137f-4a08-bc80-899bf39e5702\",\"44b6821e-b88b-47d8-803e-1d9f0b114a5d\",\"21b13ddc-7a81-4229-abcc-425b1553e795\",\"4fc31dcc-f26a-4355-8e6f-cf370f5b6737\",\"8fd7d738-6ed1-4bc8-97e5-593244ec3d55\",\"cabf3c68-e4aa-47ff-a50f-6279d25c453c\",\"691a73f5-5b32-4da4-ae50-e3dfb9045eb0\",\"5e5234eb-08be-4cb6-a91b-a6167db946e5\",\"a5133902-4701-415b-802c-ccb41a255de5\",\"326d149f-ad45-49ce-bf35-b30557d62004\",\"bb16b491-a00c-4ce3-80c8-d4e398b4126c\",\"84c661a8-7a15-4217-860d-ced151b3def6\",\"5eb95603-74b4-4b40-a8fd-419d076b0049\",\"33e2f966-a4e9-4009-afc1-3fc9cb1bb882\",\"cd2bbb31-17c3-47a4-820a-56dd3fa40a4c\",\"399f052c-28f5-4a44-9c7f-71408c5dc7a8\",\"877a3f8c-bc10-4488-a2ce-2f1c8027c471\",\"181aefaa-a23e-4402-981a-eaa4674a9d8d\",\"c6c5efdb-f45e-46aa-ba97-ea29b0e76626\",\"7f7f40e1-e23c-459a-a85a-a1addddefe04\",\"e1830132-33a1-4ad4-ac39-4bf7049c0fba\",\"d0e7e4aa-96fc-473b-9993-b386f3e3ed70\",\"54837d74-d650-4096-b7c4-d3447b9570b1\",\"7940422c-285f-439e-8a1c-7bfb5cc1c834\",\"2fae0729-d11b-4528-8bbb-8fdc567efbe2\",\"f5e75dd9-6919-4db8-a50b-6504e42f44b3\",\"b346fd64-c24e-4871-85f3-71a4cc7a55a6\",\"25fabde1-9081-4e2f-9e9d-00ac70751a88\",\"808f3381-f1f4-4c38-be47-d7fe05edd7bc\",\"d671dc5c-1189-4657-8220-a414dcacc83b\",\"ce8b5297-2e3b-4a4c-8b9f-1dd5855ace4e\",\"d1de850d-908b-4180-b650-e45b6249ea88\",\"cdf224d2-c4d6-42d1-9281-882d29a18bd1\",\"596bee81-11a7-4947-810c-6bb44145be96\",\"11fd60a8-a27b-4b15-b317-0e1969de22ee\",\"d2e5af67-25b2-48a4-b504-c4486b223a98\",\"92d219fc-7802-4c94-aaf6-816b8c5f21e0\",\"c0307264-3eab-4de9-80f4-f14266b94be1\",\"171214d8-5ea5-4053-b436-a3b9daabfbde\",\"acb1f996-ea7d-4a63-93ab-19d3dbc0cebd\",\"465f4652-fb7a-45e1-b40c-0b2c700f7436\",\"87bad967-7896-47c1-94cf-796034482f78\",\"a6536ece-329b-49df-8002-d4986b0df28a\",\"ad6c287d-a76b-42d2-897a-ab00fccc4067\",\"f7a79bca-4533-44a8-853b-709a1cc32e72\",\"899917ec-86c7-45aa-b0e1-0c1084f28c8d\",\"fc872598-90b0-4eac-8295-4e78615702db\",\"3889eb69-4f46-4895-b566-d7bdc8cc08de\",\"15f34b75-751c-4ff6-8263-4ef7b1d7be83\",\"9201f934-0f60-4a64-9764-b4d1b99c75ec\",\"f5a7a116-e135-4942-a515-d7f24bc36410\",\"b8ae3588-5c62-4cbd-b7b0-678284061246\",\"960b45d5-f8a5-4152-a711-97148bebe9cc\",\"6c8d9a8c-ed75-4e75-b005-1378bc73ade3\",\"5d8413d4-1d13-483a-9290-8a63234ff1de\",\"20fca874-f8a9-446a-bfbd-413cb2cab935\",\"9fd25f9a-1800-4cf5-9eca-7a41942dd276\",\"5472833d-a74b-4b5a-8940-9814518421f9\",\"1b49d066-a653-4f07-aad6-713d3f20b320\",\"8ba7ab99-5b17-448d-af89-0ff1b2133c72\",\"fe75b37f-6ec9-4459-8382-2e55399abb05\",\"67742943-6dc8-4bf8-a193-2ddeb91683fa\",\"e72f4554-7fab-43c7-8720-aa4651236722\",\"3a5fea56-e540-402f-bc90-2c359a684f55\",\"4588ec5a-a02e-4335-a0b2-aed4d559cc0c\",\"af9ac2a5-9453-4642-906f-f6a6af6a05ba\",\"c9fb8b64-97b9-462f-80ef-cfff5433d570\",\"066ab166-7472-4cb4-b57f-7bfcae2afc00\",\"37825adb-630d-4ec1-b84b-7c7a979d7404\",\"784d0700-5b2d-4945-a861-d3b0c124baa2\",\"720f577f-45ca-4209-b5f3-3f78d19f83a4\",\"9d24f8b9-f2f2-407f-86e3-4bd8c578103e\",\"9d005918-e6db-4c01-b220-e13aa0d4a924\",\"5561f619-ef3a-4611-95b8-52c3979bb791\",\"1ac62555-3157-4b9a-87a0-f76fb1a244f3\",\"d1800e33-6cc8-4172-9244-f69092eb9b79\",\"9e226674-115c-453d-a2f6-1e65f88bc827\",\"18d2ecf7-5519-4329-8d64-03ea3a474f2d\",\"d556d8e9-4b2c-4ee1-9e48-fa4bd6e4a66c\",\"ebdf9d3a-0f6f-4992-8131-af43dc8f46da\",\"c301260d-24a6-4740-a593-f1e65a46579b\",\"40e57308-2249-4284-ae33-1f58fc5eb377\",\"77aaea0e-ca45-41b1-94c7-6473986ece1e\",\"f5704a22-ce0e-4e82-b684-268eb4e02bb2\",\"af565ce0-1928-4793-aab5-7909d03ca745\",\"f9b9e14d-e6f7-49a3-bbf8-d67453ab0a2d\",\"021af03f-29fa-40dd-b613-ccae313c92c7\",\"542674c3-9375-4e5f-92fe-93f0e750d65a\",\"afc47926-cf5d-4454-840b-b187bc6badc1\",\"27ce6c92-53b8-4717-85c1-7120c03a1a5d\",\"6b3dedf6-16cf-49f3-a2b2-5167a805044f\",\"91e240af-2dba-44b0-b76a-9a37ed75f9c8\",\"62804608-e2df-4662-ae6b-5147b1b3098b\",\"f7f6b605-2470-4558-a82c-4a891ff6c8da\",\"e1eb10d1-1059-42cc-aacd-bf28150797ca\",\"fe412444-e6f5-4f14-a3cb-0a795425d334\",\"d11b7958-c13c-4c15-b6e9-4821941bf646\",\"2a5d6d49-a52c-4240-abba-768169f72b25\",\"c8bdb7d1-e379-4d32-84f5-a0a9d7cab6ac\",\"267c1a06-2436-43fe-a305-d3648cd68499\",\"4673acfd-ff07-4223-83be-7a6ad09888fd\",\"c4321973-70c9-49df-9276-93e1b6aa46e2\",\"7b150b93-5a3c-4622-96a6-30cff0b6c7c3\",\"449c18be-52cc-4c5d-983b-673b11548e00\",\"197bbf85-b359-46b3-92e1-9686feca7d6c\",\"38c388aa-0a3c-4763-9415-18e56dc4f131\",\"57d53496-fd38-4c68-ab20-595f4a971a97\",\"3666f074-969a-4517-8fe8-85ffe8d061f7\",\"c9d76543-901c-4a13-987d-55327084d4cb\",\"87d9c509-c185-4507-9e53-986e7a5703e7\",\"4d16396a-33b9-4776-947d-0587f61bf38d\",\"05dfff4b-e1a0-486e-a450-f3ad5d67834c\",\"d1a5a8e5-dd2b-44ca-b343-2293b3496766\",\"88b7571e-1726-49b9-8f3b-d29211389c35\",\"7a05f9c4-d2ab-4b91-a325-4708c287802a\",\"569bc1cd-db3b-4458-a78a-4f5fe73c3c7a\",\"81541dd1-3de7-46a9-9193-88595405d4be\",\"f0ae380c-7f96-46cc-b8a0-ac837f3cc013\",\"68b677be-551e-432c-aefb-76f005775a0d\",\"89c219ce-96ef-413a-ad12-2badb90aba69\",\"e1b18317-b757-49f8-bc95-869df1c01832\",\"75c53c70-0b00-433a-9b15-f975b6f7c4a6\",\"a2154c7b-9b6f-4334-8be0-ce45ae5f8d19\",\"f800348c-b284-43a4-9c83-daea4e094e6d\",\"62e685ab-e624-4a22-a217-a46e7d17e861\",\"33924702-9c6f-4518-9b7b-63c33d349472\",\"11e82aa3-7123-448e-8946-efd37a3bb142\",\"80852606-1451-44b5-ae6c-3b05b1b18396\",\"76af3f80-a543-4edd-a261-0b5ceeced248\",\"fde0c424-1405-4069-a93b-07b08ecce17e\",\"98248ba7-672f-4eaa-8055-e8d4d9c9d663\",\"f52674ac-65c7-4883-95cc-028002d8f9c3\",\"0c0e40f0-6532-49b4-a243-6018dc46a59c\",\"3850698d-ebb6-4181-9fa9-9317e67e81b0\",\"c52dbe08-0f84-43d2-9d35-5580e40940e3\",\"134c8867-78cf-46c9-8fed-63b85d6838b7\",\"c8ce3d8c-0eb3-41ab-accd-d98572159414\",\"2a94d2fe-50bb-4f52-a1ac-884916199e62\"]";
//        uuids.addAll(ObjectUtils.fromJson(uuidStr, List.class));
//
//
//        uuidStr = "[\"f6c00e7a-5053-40bc-860b-20566a9cfe02\",\"06e72e37-4858-4aed-99fb-28d6da873bdb\",\"ed532b76-0744-4d8c-b30a-d8392e6f9579\",\"8776b6ee-ca7a-4c5f-a770-a78dcae80f81\",\"f348a735-854a-40ac-b0c6-47f8adb26944\",\"354aa07e-9c80-49e4-94a9-a42d4ef678e4\",\"0419394d-599f-44ae-9ba3-87b04a36ce01\",\"d6a494fa-91c5-4833-9845-83691442c5a6\",\"39333511-1bfb-431a-816d-f510707a9661\",\"450e2ddb-dc2e-4df7-a9a2-2a1b6424cdae\",\"48476298-47bb-462c-a636-b02fee49d57b\",\"a99f7877-b86e-41df-b284-4df2cbbb6b32\",\"f19bd149-f7e0-45e4-a0d3-c1121106ec5e\",\"7e524f17-9d13-418e-84b6-4f75be70ff35\",\"006b4b78-fc57-43b2-8a22-5a7d4c57e37c\",\"f565472b-8643-449f-9029-fb79dea4c122\",\"38af5152-0ddf-4057-952d-f2859238d61f\",\"ea2b60cb-8661-4c45-b2ba-9a91d5efe7b0\",\"cab534ad-0fdb-4b6e-ab4d-3baf10c96657\",\"a558439d-6c84-4be6-952e-612a70d3de64\",\"0f9cb03b-3c4c-460f-b679-7e91b218ea10\",\"062f3400-6a83-462b-bf2d-d2bf31bccec5\",\"4082c5cc-80f7-4bc1-8dd8-1471e0122647\",\"96a8209b-5b33-4612-8653-9252b8c5c0fb\",\"a849f969-f909-4a54-98bb-641c8fbb68f0\",\"2fe5a483-73ef-4f2e-9819-a32fd0c7fd0c\",\"d796e020-39cf-4842-8696-46738e58c501\",\"031efc64-c852-4008-9958-32f8ebcfedb0\",\"b2cd0227-d163-4198-9cb3-9e3813b1c86f\",\"4de38772-07d5-4291-bddc-576e74e3b8a6\",\"1b69dd8f-a3aa-438a-aa43-8f0c49a38a75\",\"72659f98-0248-4ce0-99a6-0328888054ad\",\"f87881da-9ffb-47fb-883a-fc88b175ea6c\",\"328d7aac-e547-4b6e-8306-45f27de17f38\",\"f9741898-8a2f-48b1-a3fb-7e405c420888\",\"084c705b-09d5-4262-b15f-37f7ac7c6c3f\",\"65eea051-1cae-4311-b12f-fe21fc03abb4\",\"75dfe06c-e502-405e-836d-50c4e7db3574\",\"d3eed93c-45cf-4c8f-9b6c-e2f6b3ebe43e\",\"9735a181-e84f-411a-a449-f34040628f15\",\"5c3c1358-877b-419e-aac4-35b83e6fd5dc\",\"6d610c9b-eb77-4e49-8269-78634858755d\",\"0ada62bb-92e3-4bd2-82a3-de631f3b497b\",\"d72f191c-1251-422c-b503-8c17e8379abf\",\"6f394d30-f03b-4b74-b280-ff0a2dbd4ec0\",\"ce23b983-47c7-4f53-90b9-eeb4405082dd\",\"8ddadccb-d9a9-47b1-83dc-9cf3c80fbf06\",\"c135ec9d-73cc-4489-ad16-edeaadb606f5\",\"1f2997c3-e203-43ad-ba87-6c1e7125a7bd\",\"9438cd91-f7b8-4de7-9394-a1a44ba61424\",\"242f9fae-2c78-4eed-bf26-862f5c4d5193\",\"049ab34a-d011-438c-b4c7-321e72ed9110\",\"dc31d927-b780-4b52-b87a-6e953becc370\",\"242e1eb2-3b1f-4ff8-aadc-02934f56a80b\",\"4bf138db-07a3-491e-99fc-2159abd32ac5\",\"ce73a4f4-ab94-408e-8183-f91f11556db4\",\"7bafb2b9-7a7c-4d2d-b167-6071db270a11\",\"98f21113-d404-442a-9444-7b57a671f9fb\",\"2f8bec5c-851a-4733-bc31-ad0ebdbf7a32\",\"cd81a320-1e05-4a4f-ba0b-8e564e00f1af\",\"42d86651-5a7a-494a-8bd6-5d2043f16da3\",\"c0f5464a-37b5-405b-9a37-6c2ce8e9ae22\",\"0b8b2f3f-deef-4aa8-9619-ceaccc290d61\",\"4705d18a-6cab-42fe-956c-23beee50f74d\",\"5412f482-8d6f-415e-9580-0da2e131ec44\",\"23336ae8-acb9-44a9-a767-fd9db91fa715\",\"3395e4ad-ce44-4500-8f38-ab7cd075b38e\",\"01194475-ced7-40f6-b767-3a3247939266\",\"0b1c72e5-4eda-4541-9b5f-910a1e7991c5\",\"fff8234a-26e1-4deb-911a-f70f005662e2\",\"8d7578d9-5707-400a-9cbd-6fb0a93f6dd4\",\"9cbccc8d-df1d-4c04-97f7-5fce8f42802b\",\"198a7cf1-be4b-49d9-a10d-af14a08ccfae\",\"43d332ec-9c4b-42e9-92c5-b60fa2bdd546\",\"dc45cc5a-51b8-4eec-86c0-7ca5971d34cd\",\"91a9ac15-f1f4-4374-bfb0-351498904ea7\",\"2086b05e-1de8-4fe3-aed0-2d9db1312ed6\",\"bf520c77-42d3-44a6-9c5a-e4e7eb8999ac\",\"33062e47-4865-41bb-a5a8-841dcb3dc889\",\"bce44851-5e8b-4067-9a0c-6ce3ef56ccb0\",\"03954d80-f5f9-4665-b5b8-de9d535970b3\",\"b24f189f-69f1-4ecc-8d09-bbfb1985848b\",\"b581fb56-2903-4afd-80f3-9333f237b76f\",\"9379485a-fb27-49b0-bd9c-89005b16092d\",\"36547ed9-76ec-4530-84d4-b8375531e011\",\"50b358b2-a204-449a-9c4a-ddcce392a405\",\"5bb9f1cd-8a66-45ac-94eb-8f4dac234182\",\"4b9e6543-db62-48cb-a81d-1baf8a17e0da\",\"c7ef6104-e9d0-47bb-b6ba-562ac2f68521\",\"c95c4f78-8bc2-4aed-a989-c45127fb2cca\",\"e4fb04d7-7ce4-494f-999f-060211c96d01\",\"4e2043e9-d5c8-4b24-9f89-c21a8d52b4f6\",\"650e51e1-c7b1-4434-b23b-c1e9e0176e7b\",\"b7a16bcb-f48f-4dd0-bcc2-7c950a849dcc\",\"f2383ecd-7546-45a0-bd49-c986d0d63de0\",\"d78cdc2f-984c-4fdb-8110-fdab576ea371\",\"2ac556ce-0215-4cb7-af12-038e877150a1\",\"7f2d3b7b-5cdc-4c21-a6e2-8aee4feb116b\",\"d4484ef0-0a86-49ed-9166-8a6c3a4104ec\",\"aa9a6d56-dca3-4509-a07e-20327e193ecb\",\"d64f9d54-3432-4a30-84d0-e00e2ff52ab6\",\"f54dcedc-210c-488c-b619-5054e351f79f\",\"9618368c-6655-4dbf-b167-c96ca64e926a\",\"ed02a800-39cc-42ee-aa4d-b2d49ff91ce2\",\"991dec81-62ab-4e6a-a552-afbb561aff18\",\"a2200e94-8de3-447a-a6d3-2060d5fadf28\",\"c3ad1e49-250e-4e8f-8bec-af0f68f95d47\",\"eeb66e81-923d-47ea-ab3d-31867dc51e02\",\"c5e3efc0-50d7-44bd-aa09-c4b72f208a6b\",\"2380098d-114d-460f-afcc-9156bd5fba51\",\"0304fa28-e070-4f00-a582-c8bcd90b02b1\",\"8f7da24b-07db-477c-a2eb-bb18b25834fc\",\"2edc9154-3540-4ef3-b2bc-893d59a4f130\",\"cba86e8d-e838-4cd0-bc26-a4207ef52016\",\"15a0b416-5109-4ade-9d29-a6f6807dda93\",\"5aa0029b-72b4-49b8-bb7b-099c2001f878\",\"c3a09c26-6291-4f7a-a814-cc41726372ad\",\"bf16b35b-534b-4455-a845-862de6fcdfcf\",\"3973f98a-803d-46ce-9646-944685dcb425\",\"97c355a2-8592-4269-9155-ef6d353c9d51\",\"1dec5689-0f1a-4428-a80c-c7fc1fcefa79\",\"414bfff8-3175-450b-84d2-a7ca80fe8722\",\"0c2e40dc-7d39-4c1e-abc9-2ca3eca51246\",\"8f648c5a-95d0-4459-ba96-5085079cae21\",\"d8e2a67c-f9c4-4a08-9ead-98c4b15463cf\",\"f4e3bde5-9ce2-4432-ad27-2c504dcfc77c\",\"3f94a7ba-d074-4784-8aa7-5dd1319fae9d\",\"130e7194-27bd-4f31-a5bf-e146be397237\",\"3338b821-8d40-4832-a159-d2b94808ef70\",\"bacbdc13-f7c3-4c2f-8a4c-19926fd40654\",\"76c208b0-2928-4d0f-901c-8ea214d74904\",\"2d47df9b-47a7-43aa-8fa2-a0c92eeeae9e\",\"36425ea8-cbe5-4d86-a9f9-cb886a9e8796\",\"934f6738-22c3-4428-9007-e96dd6ecff73\",\"1be511c1-1494-4291-ba91-f069d8de7ebb\",\"959eee37-af5c-4f7a-ba8b-5ded965b4ede\",\"7b4ad86a-4872-429a-ad20-560662ddb8a4\",\"b49152a0-1cfb-4856-b531-e1367c347f5d\",\"6e1cf815-be34-4a78-8ae9-d1a95828962b\",\"5612ef10-33b0-4482-8e8b-3254daf7e351\",\"d33e8722-6f13-4bed-b959-73a70ef0095d\",\"39c571d2-f78a-44c3-bf5a-0b7634ed5034\",\"13f581d9-5dec-4182-92fa-52b12a306bd7\",\"e0b9313f-8490-4a14-a9e0-328eafc69367\",\"2592c87d-e58d-4e67-9154-14555d2d3e09\",\"1e976c56-17ea-43c6-8ce6-6a5843f1a2ce\",\"bb25c4f1-1c2f-4bd6-b782-65d2af3efd37\",\"db581c4d-e5ac-45ca-b153-66f1de1f2110\",\"d1e65d1f-88ae-438c-b498-2b9386fb67ed\",\"d5329322-c33a-47af-8a69-8d047ac10191\",\"8c5bcd3f-ddb1-415e-a1f8-bc8f973bac58\",\"85bf6e9c-e5d7-462f-8abf-25ead753526f\",\"1b0a18e1-a7f1-4496-a43d-7d76871429d2\",\"ecf0fb20-f3c2-4909-b197-6c43469ecab7\",\"eac1ba1a-cd51-479f-9e87-eeb032d207fe\",\"0b06e396-16e1-4cf3-8a2e-da0dff9f2bd5\",\"bf7d96ea-3817-4404-947f-8acd274d39a6\",\"1905c9c1-a661-4f12-8e8a-c499639b78d8\",\"d9e60d13-14f3-4c71-9462-0ce6901ac7eb\",\"d26f7f51-b6ea-4ddc-8d16-3a647bf3c098\",\"b6ac9a51-48f7-4ed6-a576-11c6f1a2ee97\",\"01984bc6-6d4d-4d37-9cc2-11b4fec03671\",\"dd5a5a96-f1eb-459f-a187-87c30fea509b\",\"2823d138-9856-4079-9393-565eebe56723\",\"2225fa8d-283c-4829-9441-bc418053394a\",\"9ef59c37-4164-4d8e-8811-4a5092f1a5cc\",\"ad55c24e-be78-48cd-bcde-2b794b33b432\",\"0ef58fab-9a7e-42b8-a8ce-642bbd118803\",\"c43c5c7b-000e-4210-ab70-81c3286ee9f5\",\"1629130b-8b5b-4370-80d0-eaa959726492\",\"58a93c52-830f-4539-b623-ec266d8e5845\",\"ae3f2253-1526-4d3a-9232-440b79f18dbf\",\"7dbe45f1-0aa5-436f-bb8b-34bb898db9ac\",\"e2afda26-1bae-4565-a05f-c2af489d3a50\",\"9a2f7303-3e2b-45fa-8b6b-902599cbc2e6\",\"8be29914-518f-4b60-bb01-a48529a1e183\",\"8e3acf99-9eae-477e-9873-f99b037dfd3c\",\"39d92d2e-840c-4f10-83d6-97ebdec412d2\",\"f29437ab-8de1-4888-8489-ee7d810492f2\",\"89fadb74-bc3c-402a-b697-b00c60ab77d3\",\"c2ff5c6a-0ca6-495d-9dfe-3528d285ffc8\",\"0556accf-c611-4c46-87d2-d1c24204e851\",\"3a64766f-5b84-46d8-b5a7-c5a6e4b4b51c\",\"6efff68a-20ce-4774-b382-e8f5bdbf1301\",\"bd3a3902-0315-4b3d-bbf4-bc63270f2b82\",\"f50c5a0d-2823-4b21-903e-12fc8e32d0fc\",\"3506c8d0-2a67-4d3a-854b-17f05fedf279\",\"6d6edf1e-2cfa-45c7-9e85-28ad99f6eaa7\",\"9cc16135-b13b-4e2a-ac43-4cb98d7ec438\",\"778c2d0f-08f8-472e-a047-f4e738b03e3f\",\"3b5719a9-b86d-4593-af30-8fa8d76edea4\",\"dbaa0fb5-5357-433c-ade4-c55454fa436a\",\"3a0f2705-fac6-4227-888a-5b754635fba9\",\"b36a416f-614e-4a59-9bb9-4fe99a1de67b\",\"70ff90c0-355b-481b-b604-be6c13d99b4b\",\"80d7654c-f8d2-41c0-b5d9-ed038e7fc66d\",\"2b3c646a-8f63-46eb-9065-d0140140367b\",\"813873af-efd3-4ae2-8e52-0959ad58dcf1\",\"1a49a062-d414-49c6-b46d-062ae86f3116\",\"8e638ac2-9db9-488b-babd-05c42bf24b1c\",\"61ffbdb6-132c-4f0d-b6a0-1abb2725dd70\",\"c0493b6e-0efe-4748-a394-1af4ae3f6fa1\",\"afa70c45-91e3-4f21-a9e9-b6d8dc0acf6a\",\"ccb7e2a3-82b6-4183-b2f2-e96de1d01039\",\"ece6128b-4c53-47b7-815b-d239310fe28f\",\"e8ca6914-15dc-42c5-8e1a-5326411a251e\",\"b2d9fd8d-8fe0-4a2c-8ed6-989c6dd32f2d\",\"d1e326ec-c7b0-48b3-99e7-17f04bc1ba6e\",\"3c752b67-4bbc-4510-814a-3f11ed4c666a\",\"ae812e7b-8dd0-4666-9a0a-2eac2f61a2b0\",\"ca80a9d1-1ad2-46c9-b40c-f972ca25ed8e\",\"8d09f9aa-099f-4a62-899a-b24ef66fe690\",\"93e9957d-507d-4ed7-b0f5-af70f5f0220c\",\"6ccd1662-cb31-4034-9438-445bab2df2b7\",\"54e0770e-2d59-4ba4-b1b6-c378ac381957\",\"6d063f38-239f-47da-b5d2-2a82a32cb7ea\",\"8404474c-51b7-4f65-ad7d-7735cc664f8c\",\"e8d03b5c-dead-4dbd-968e-b1a56c32ab39\",\"c2647895-8a57-4921-a195-27c0655e138d\",\"326bc1fe-3235-4d16-b6f3-29cba0780909\",\"a90d51d6-bf4b-4892-b6ea-59625855493b\",\"1c0ddc0f-d648-41df-b2dc-fe4e5b586eca\",\"cfbe742c-4a0e-4fb6-9eaf-ca40a38c78ab\",\"c5a2b271-fc45-44ad-9bf3-daac73288a04\",\"990322fa-7493-41e0-b37e-fd0e1c8ac330\",\"4221c951-d836-47fc-97fe-3b29ecd3ab93\",\"1386d7cb-fea4-47bf-a64e-f3d62288d5ad\",\"52dff477-d060-4bf5-9c74-2dfcaa0940b2\",\"d3c40023-30f5-44c7-b47b-1a0bf8ba4d38\",\"caa10992-74ed-4300-97b0-bc6022212a4c\",\"b46f9b43-732c-441a-aec1-258f0713527b\",\"a9e8806e-12de-4aad-aaf6-4eac311b801a\",\"aad9e2e5-47c1-4e41-9809-122633ff2509\",\"0304eaaf-9afe-4b37-8244-f81deb823e5a\",\"d55c31db-6ba8-4e99-a81e-d0c5ad95c7b8\",\"cc5d24ac-7199-4695-8c99-1327e5d6ebe5\",\"1324aa7a-f8c9-4dd0-af81-adb7e32bb535\",\"242226cc-bbe1-45ef-a4fe-523b10d784b2\",\"94ade61b-dbd4-44d9-a603-ca469c3a5f88\",\"0beef7cb-08fd-435c-a5f6-4296daddcf95\",\"9fcd4ee3-35bf-4683-bf6e-2e78a628d94d\",\"0d72f035-c252-4ffa-91e7-d8960f9dca73\",\"c108c036-68d1-4752-9240-8b8496810264\",\"1a4de4a9-68e2-4eb1-ad37-9470addb0046\",\"e275580e-fd1c-49f7-9813-3ad2e0cb73b4\",\"662fb8fa-2c3d-436e-b7ae-f286e923dcd6\",\"121f06ff-5ed0-4779-8176-8fbe7b6857b3\",\"b284ba5e-b06e-4b8a-821e-e12a921be6fb\",\"96685527-384b-49c0-aa93-3d9e34289acf\",\"48666921-c768-4194-952c-1ec31cc0c7e9\",\"cddfee6c-c9f5-4503-8b85-06cab9803c4a\",\"b0a9940a-9c29-4e0c-a92c-3d1b9348e4be\",\"a666bccc-1c30-4e2f-95be-7d195cde020b\",\"5d762611-fe45-4ccf-af3d-5cbebfd9dd63\",\"9a723677-6b4f-4c4f-a745-eb63b09ed0b2\",\"39c97cf3-af0e-4edc-99a4-13fa1f5acd5d\",\"c8bffab0-484b-490f-bac8-22bf4e448a67\",\"846260a1-cd0d-4b9f-9b9c-3b431eef6d74\",\"83551fcc-3c6a-49bb-8e09-bb3040a2ab52\",\"67f2599f-0e4f-4625-9c06-c3ce76405ba9\",\"10b200ad-26ca-4b0d-9de1-48c63725b58b\",\"14ef881e-82c2-4821-8422-db127e85d721\",\"6e835651-a33a-4da6-812c-5af681533da0\",\"c417f05a-69cc-43ce-85e8-c28e53de7ac5\",\"6a19ca39-c1e9-4eff-ae0b-a98edf48cc50\",\"70aeeb9a-fbe6-46db-83cd-dcebfd3bf387\",\"bd74f91a-2a9d-49ed-a7fc-559456126e87\",\"dab730b4-4e93-404b-9759-910fae869062\",\"5d95f21e-22fc-49a9-aaa9-744d24ce9b50\",\"b20fb75f-d66b-417e-a322-ee16c272b1c2\",\"829905a9-6683-497d-b470-2e6f99f405ae\",\"9b3e0dd9-4a6e-4786-91c2-9dac334738fa\",\"28c8bfde-af66-4c94-b4f1-eb43dd66be59\",\"17b45e3d-61f9-4a96-8105-4b059c15160f\",\"104a3ee2-6858-4298-a5f5-bfa9a6d39067\",\"e5cc7a04-3999-4c69-ab4b-60fb4023c7f4\",\"daef858c-3761-4561-9534-0a6e5d0f1c66\",\"31390220-c42a-4ff3-8d3e-2470afa06ead\",\"50ee57b9-4170-4367-a138-ab5133717913\",\"bd61a4b4-69e4-460b-9a6d-f4a29d12cec8\",\"4042ab76-4842-48a3-90f2-cac0c425ccc1\",\"ef0602ea-0336-4f04-83af-b6742bf4120a\",\"086e2478-110e-4009-8dcd-1a1ad3df3a22\",\"879c675a-0f22-4d5a-ba5d-9d4457877350\",\"bfa56ae7-1618-4072-99fb-b7adf5252cf5\",\"8d2c9ac2-f50f-45f8-b79f-86f381f1f8cb\",\"af56d4c5-bdab-40d4-9d23-658a868b7407\",\"3d709463-670a-462a-ab1e-69c1419d0744\",\"3b048b03-a6e9-435b-830d-eefbdb71237f\",\"6d4ad1f9-3557-43a0-a4bc-c1927f5d839e\",\"b5bdbcec-eeba-4509-b938-3561c80c4b52\",\"5b4c26ad-e15d-47ec-86c0-ab25ec8a1763\",\"051b653c-8722-4558-9b5b-87c2ff497007\",\"d7008fda-d386-4775-ad84-cb701b90ac6b\",\"778e60b2-e14d-4f7b-8467-287951bdd69f\",\"9f62d099-1ebb-444c-91a4-541b19d60f43\",\"6ff7555d-e48f-4618-9d5f-bf3d778f9a0a\",\"3044d086-c29c-4b9d-882e-55bb0cd5694e\",\"199b9261-1515-41aa-9bd4-ff32a31eb89a\",\"9f26e24e-777c-40a2-8045-e0eace497c03\",\"40c7f848-2095-45b4-b5b7-bbedb52000f9\",\"2efacf9a-40ef-4e21-a5ad-8a3d4fc3d8f5\",\"63229b96-9533-4f47-b691-9cba0de75ca0\",\"1c7717e0-52be-4939-b657-c6c781532963\",\"8df9d435-6327-4e42-a4d4-346e28cbdc8b\",\"c6eed102-0fc8-4f0e-82fb-bd9f2584531c\",\"5c5a4bd4-a6c5-41ac-b8df-fd8b8b6b8269\",\"a733d3f1-2554-42bc-ae84-52ef85239e7f\",\"f89a0c77-34f6-4858-989f-7aea0a5750b9\",\"20cb5855-feb6-4f46-af71-66319ec19230\",\"4e8f1425-4cc5-4725-a652-7d662acfc91b\",\"77eb041f-c450-486f-9684-78e40186d389\",\"bfaacfaa-1c49-4882-853a-d58d747c9ceb\",\"499ba17d-44c9-4f80-b87d-68a1b36a1c88\",\"ba1ebbdf-9479-45dc-91d7-be2518dbd4b2\",\"ce73f525-44dc-402f-b57d-5d0b3af4c8fe\",\"cb76bcb7-bf7b-4702-b6ed-aac8bc4827c7\",\"94519d34-5358-4c84-b227-9c9d1ebbc275\",\"ce3f1768-a740-4fa0-a501-cb773424688e\",\"de1cdc83-b1da-48cf-a783-1b63b126e502\",\"705a5854-9bbf-4e46-9125-ef7ff57af812\",\"c441626f-8cea-43f8-a66e-a5ea7be506f7\",\"dd9fe4c4-cf53-48ab-9f86-9c3fa43389f4\",\"756d5fd0-83e5-4e12-8a4b-061a4e13874f\",\"cda3a8e3-ebea-46d8-b05c-12878c09222f\",\"532af1e3-974c-404a-a8be-80dfc7783a4b\",\"adbd04d3-4df0-4321-8a4a-173a952ea1e9\",\"19955c58-d004-4967-a9e6-723867277fca\",\"dedb23cc-d68f-43f8-8a9e-56268a8a85ba\",\"a7bd7a47-5a3f-40d5-ac79-ecbc411aad74\",\"08e7c740-5975-4f41-b876-6b3242182b92\",\"bfb5cd2d-86c0-4927-b8d8-75f3cddced42\",\"f707a7b9-93a4-4bc6-8976-716ff484fca5\",\"fef8576e-f552-4ad8-ac46-56a61a2d5850\",\"8bea2e87-cd6f-400f-9195-3882151f11c1\",\"1865fbc3-c802-4734-8602-3c2c035a1518\",\"056b081c-2d1e-4d6d-9c9e-ede3499b677b\",\"84dc64fc-ec79-4e9d-9987-5e357a1d3e5a\",\"aac6dd17-83a9-40b3-a66a-cca12f24eb1a\",\"d6e1aa2b-a3c3-41f8-9602-fe4958b3a195\",\"fe9a21fa-4920-407b-b298-b59c4b67c34b\",\"5a9df4a2-1701-4eb8-847e-dd4cf3406da5\",\"3174a0ec-956d-418b-90b3-7b514299f57d\",\"05e16647-812e-40da-a565-ed45a2f77de6\",\"88cf6f96-6b80-4b67-8afb-e9812fbe58bc\",\"db74139a-5d74-45da-bcaa-df629fae572f\",\"39e4abc3-31ee-41c7-96de-1355e189e5a2\",\"599a5e47-7974-4af8-ad1c-4329ff9d941a\",\"2cb60210-94bb-4f29-adea-9cb67ac0a0b0\",\"7b1de241-ed37-4318-b77d-17c98538718c\",\"d92e2bfe-337f-4f73-9249-077125d92ff8\",\"a74c5e98-0c4d-4ade-80d1-0ec458ef85a0\",\"cd1cd7e0-6644-4dd1-a24a-530a04a7bcab\",\"b91230b1-4991-47bc-8ca4-138b97ef26ef\",\"0456f3ef-605d-4705-9816-2a40e573ad30\",\"1b6abf5d-6577-4e68-b4a7-1b6bfc195971\",\"cf68da97-ea30-4ccf-a07d-c2d06d3f27b5\",\"feebd655-c811-408c-9522-d38b69d4dd8c\",\"4b0a49ea-2fc4-4630-a21d-7a0df56d9198\",\"746a1d20-8348-4b2c-afe8-c515a08dc9cd\",\"d8d7345f-95be-4673-be17-b879761f79cf\",\"3700738c-e4ba-43bd-a2f6-ec0e8d56d1fe\",\"71931191-ecb1-4a13-8d90-7918f51215eb\",\"0d58de73-eece-4d73-b477-86e2abc0b672\",\"754ca732-c0d7-43a6-9eed-2f03446cf097\",\"c6f71066-27f1-4a0a-a07b-e97c30dc40d3\",\"bf3f4f41-33b2-4c18-b189-a339e41dee32\",\"bf67a5ef-b4ac-4d34-9e39-a14c58b5154b\",\"434d45c3-9dbb-4c44-9389-b3e3e0c0d64e\",\"c7d8e6e8-0985-47ca-aa1a-800a5c199085\",\"d3854d37-60e6-4e7f-ba14-fba9115af5bd\",\"584271c6-cde3-4db0-836a-8000d8600c71\",\"6431552a-a321-4c44-a6e6-bb2abffefcff\",\"06bf933f-7ec6-4801-8cf9-edfb64d187d6\",\"5489ca02-148a-4c23-9e4c-591dde210a87\",\"7c3188a8-8733-4758-8670-08d39de8af00\",\"2e39232d-ab75-40a1-86f3-24c292a6929b\",\"f5c91e26-95b0-471d-9b39-b0e72baf5f63\",\"167cbdd5-c38f-451b-96a1-c25a832f0b55\",\"99d89f05-1bee-4ed4-9676-4096a8554abc\",\"46486874-f166-4c8e-8981-89a56c68454c\",\"39bd673b-4142-4656-b558-02757a574e5f\",\"7a6199f9-cfaf-44f9-b4f6-8c3d6fdd66e1\",\"1d5b2786-6976-4509-96f6-928b9079d4da\",\"5ad76a9c-ddb8-4313-ba8a-6962a26e9a96\",\"db8c71b1-43e5-461f-a96a-8dd644239d79\",\"ed0ef236-f0c8-4bab-8337-4f7f1b951857\",\"19b3dcd5-c895-4701-91d4-610071cb18f2\",\"05e89b77-e664-47ae-b6f2-b1b74ad28e6c\",\"94b292e7-9152-49be-baff-4215a81e1e8d\",\"089eb5ef-5016-46db-b2a2-55f5af711c96\",\"16641a18-c4c2-427e-9a7f-75fe6aac484f\",\"d1194a8f-06d0-44ad-bf2a-b88e0d6690c0\",\"af200dea-e063-4fc5-b298-544921f2665a\",\"2ff8704a-79b9-4cc3-b126-108fee339de1\",\"01e95917-bf40-4e43-b451-99bcbe568513\",\"56f7605a-9292-4e5c-b488-a91e3961407c\",\"cbba1914-72af-4337-b3cf-e0bbc5bc978b\",\"a53f69cc-995c-4993-a172-2983c2d5844c\",\"553713fa-121c-4574-9dc3-c641d5f383bd\",\"9add919e-08f2-42cb-99a5-22fecd7f23e6\",\"7ecd81de-8f98-400d-a86c-58ad099fc8b3\",\"1b8977e7-cf01-40ab-a8fd-6b316c40e5d9\",\"c46f894e-2675-48e9-87d9-5f7b156c3c16\",\"0324e4e9-079c-4271-8dde-a60ce08df470\",\"357a0e4d-dd05-4171-accb-1c658ebef8fb\",\"c66adc55-91b3-40c0-8b89-87f04701fd26\",\"e6269ead-26f9-4bad-b88f-19f713f79c82\",\"f863a918-467f-415b-baad-183deb58db14\",\"a2c7575a-bfda-47fb-8010-ea040bebddef\",\"c6fec288-048e-4215-bd7f-5f8e61c41dc4\",\"21033827-299f-4a0f-8a4e-67cef1d90059\",\"fba9b40e-ec59-4478-8725-2f17efc36997\",\"e5e73a2f-7fc9-4dbe-a913-840e5aecd808\",\"28222fb8-49a6-481e-b09d-0533f5ab5579\",\"fbf8ed7c-73d4-44ce-a3ff-976c38a6ec36\",\"72690043-9709-4217-9f91-31c93967778e\",\"33aafc10-80bb-4bbd-bcc5-fd84d4b13b86\",\"2c9b1737-6ef6-4804-911e-48ecfe6f4e0e\",\"6875787b-584b-4e5f-8c10-a3495c0a50cf\",\"1c03f635-dea0-4c78-9365-24ad496d037b\",\"1f2af596-5c83-4a88-86cb-4accf5e0a909\",\"fce67518-9356-4d7c-9bba-d78c29d1db63\",\"cb2cc62a-ba94-44c3-af4e-ed8d0debf49c\",\"8d076c35-ed94-4aea-bd06-82bf29491143\",\"e0b0ddf3-e715-4e61-9aea-83fb2234d1a8\",\"08fc80ac-7204-4648-897b-aea694550819\",\"58fe7a82-67cc-4813-9816-3516f514d76d\",\"ffa2ec7c-0a15-40ea-b688-aedf7f9b8d7d\",\"6bf85c52-cb9e-4db2-89f7-340544319ff9\",\"806cf547-005c-43aa-92d6-f5fd120d6707\",\"4f2018fd-a9be-4945-8c5c-41a846d83bfd\",\"31518104-44a0-4916-8e2a-0d7d6bbe8f82\",\"73e09b71-0110-45a9-84fe-e79d24fe9576\",\"f3fe057b-3377-4245-971e-8cbccaefd8e4\",\"572e1955-aee9-4823-8b97-b85f63082dba\",\"5018a403-9631-4ff9-ba54-d285e5141c65\",\"7e40d91d-1094-46dd-86ed-09cc90aef011\",\"6edbb7e3-7d79-4748-93ba-dbd0cf629a33\",\"f320fbd1-f103-4ac1-96fb-f113543b64bc\",\"a4630f2c-b685-4105-b65f-eecaf4f093a0\",\"29bca3ce-581b-4e85-bbfd-a620d191dca8\",\"bf5215fe-da40-485b-ab19-097db742e93b\",\"6bed91de-1760-4629-915f-2deb438d598f\",\"0362a78a-370c-429f-95df-48a005c9b6b5\",\"8887edd1-8489-4b20-8b28-f28eb8768d72\",\"63a794e5-bc2b-4dc8-97c4-ba2eca0b6112\",\"dd4a2923-82e5-4ac7-88ad-afd2be4dd3bd\",\"cb5405be-7535-41c9-b5ff-b9e06157b3b6\",\"a31b392f-caff-4d4d-8f8a-00ecbd64dc47\",\"40f37ab1-79a7-46d8-8724-84c887e0b787\",\"2009da01-45f5-4b2d-9d47-f13334d90d94\",\"2a30ac8d-d6d8-45fb-8eb4-f6f391399a55\",\"b5316c35-f252-4507-942d-bc0015ea85c1\",\"9b56df49-5b1a-453f-91f9-b8e00f042a2c\",\"3c4b1f74-042a-4c08-a71b-62cb4d303738\",\"bfd7cf41-71c8-45a7-b9ac-c5117a475055\",\"422706ee-f227-4be4-a98e-e474c378d0e4\",\"47d1d12c-7e4e-48d7-b0c6-f88c316b4158\",\"122c5b6a-8d33-4ca0-b890-6e74e4a42be0\",\"ba02f101-9f24-4254-a137-00c6e4a3e0d7\",\"2c72071d-a32b-4716-a594-35f7e5f94ee3\",\"9c6d7dcf-b13f-4011-b72d-186ba52fbc8e\",\"4abfc811-2ef1-47ce-aca0-1489df64f93d\",\"d4572694-ebd0-48f3-8b2d-9730f8db89a8\",\"95420aae-071f-4cdc-8445-5c2c8a653524\",\"7aa34dba-7286-4165-91cc-5edbde450026\",\"5922a609-65d2-45d3-9148-1f24db474b60\",\"96f335eb-016c-4e8d-b26a-12ddcba94ecf\",\"d71fbecd-5d26-4074-87ea-f955cfc7e845\",\"fcf3fa5b-4864-4bbb-b617-122904039fc8\",\"c0b2899b-d47b-4370-9b2e-8999b8e83800\",\"dae09411-b23e-4295-ac5f-ba9471757856\",\"eca86fde-26d8-42ec-a9a1-ffad8c2d1225\",\"c997eedb-1317-44a5-bc58-ed8a768d457c\",\"1c43b44b-d6af-427f-b144-c5289732888b\",\"502b0c6f-7e40-4ea1-a35e-750b5f67c800\",\"18595e6e-cea3-4b24-b6a6-0c95aeb75168\",\"90485745-a0fc-44a8-9291-c8a2a507072b\",\"4c8e968a-e84c-4a56-ae69-e6e8313ca09b\",\"9363ec1b-2cac-4975-a671-bb7cde8429b4\",\"44b0ba42-3f90-46bf-8941-5359a7ec3830\",\"44f07ac2-00c2-4f93-a6c0-86d22ddffbc4\",\"ec6a59a3-062b-4bb2-825e-5be1e674c34d\",\"28975c0f-9514-4e2e-81f7-24b86fdbc2a1\",\"947337c0-1db3-4588-ae31-759910ebe6f0\",\"42c2dd4d-7ce2-41e7-bc83-54f082adf0e6\",\"c73cf73c-a12a-4b08-b976-d4cc82e91080\",\"f8b9b209-7085-4142-aa79-67fccf180ce3\",\"a5baa3fa-dc53-47b4-803e-ff651d121633\",\"e08b0d4b-d856-4183-9c42-43806afaf069\",\"695857a6-1d8d-40f2-8617-46886d19adce\",\"3be79f00-fd2e-4c2d-88bb-a7df7b5098eb\",\"17588705-e8bd-4b00-becf-d475e385b110\",\"f0d50559-f324-46f6-bd4b-43c48efe4133\",\"9b497aac-e83f-440d-9b18-ab3b2b7020d2\",\"86e9d270-2dc4-40e5-9518-648ab9b3acb2\",\"39410330-c21c-474f-9d54-a9518336e390\",\"b71584ee-0736-4d83-a0d9-b62f02a4be25\",\"5059c624-4c1b-43f6-a9d7-f1aa1aaf58b6\",\"b076f755-54ee-4ac9-99ee-655702b1d672\",\"8fbe3130-56bc-43cd-be56-06163d6e7c93\",\"54195c63-599e-4df0-b3a4-b8485ffa05b7\",\"e68c1c29-c58a-4e3c-95d8-3cf9123362da\",\"68c1c5c0-22da-4f87-8856-c36655e891d5\",\"bb4567a7-ac81-4e78-a7a5-8546bd4eea3b\",\"a62223ca-f73c-43eb-9e67-6836dd4a1077\",\"8fda62ae-9c9c-434a-8c36-52de55302792\",\"5cd017d1-50aa-4ee8-ac25-53c46d7fbd47\",\"a2d9a334-e9f2-4326-9bb2-30012f3878da\",\"681daff6-db0a-46d1-903b-8f83c79dfdae\",\"fe3f70ee-7c8d-430a-b3f0-2d0f57cf8bb2\",\"448b1e58-2b1a-4cb2-8a97-4e6b09dc27ad\",\"8a6073aa-e4d0-4974-9a17-38a75cabcf2a\",\"ecd6968c-2d30-4661-929d-eea4bcf4bae2\",\"9f31db44-f3ca-4953-9616-3f234369d346\",\"1b5e9420-a3bb-4a2e-9e9b-c878900e64a9\",\"5c57a4c0-6ac6-466b-bbff-b552ea1c7aea\",\"d1a881bf-7219-4d5c-8d6f-6ee49377b181\",\"b73a2557-abd4-4f08-9074-6ff3106ee18e\",\"75298106-0ddd-41b7-aff9-a86531cb6cf7\",\"3d46954f-ff9c-458b-add3-64b4eb40d883\",\"78dba9eb-dd19-400c-8c26-0273ed6529aa\",\"0506be6a-8c2a-423e-a8b6-363977a4a15a\",\"0d764a98-84dc-43d6-bce2-22dd04a076ce\",\"bc98e2e3-081f-4a70-9145-9f35d629f43c\",\"1e614053-dbcd-47dd-b85c-0c4f7483646a\",\"8f40fee6-f928-4bbc-8e03-6ea6e8cc7619\",\"8903612a-c176-4401-9f33-f0fa358fd301\",\"975e39ac-239c-45f4-be7e-9e1e3cfa1b0d\",\"74d36a2c-bed6-47ee-90b3-522da70e04b7\",\"b8bb0ca6-c3b7-4639-a439-1a3b3fd6cf7c\",\"b5baf0ef-36d1-4ad6-ba1a-6a681f68a041\",\"7287540c-e378-4e9a-a915-d2d2acc228a9\",\"5e3e87d5-a38b-4c93-902d-525aeea8d31d\",\"e347429a-1994-4453-a83e-f965cf5bb70c\",\"3d0fc0a2-6471-4556-9ffa-b99c1155d0f3\",\"02634840-9735-4637-902a-d2b997a6c797\",\"9d63af2f-93d7-468d-9454-a9109dcbdfed\",\"a040b8c8-d70a-40e7-b357-bf5b5dca9d9a\",\"2c8e68ae-bd50-4b5d-8e53-f67714935ad4\",\"b60ebfb2-13ad-4d2b-b248-ec02c7aa5518\",\"3297e70a-9dfb-4a86-959c-1ac1e0afea1a\",\"da62a8b5-b6e6-46dc-9fb3-c7b15d477273\",\"663690b2-daae-419b-b08a-44f12391c667\",\"29127824-bb7d-4250-aa7f-a9d3fe29f9e6\",\"6d36c214-5c9f-4bf8-9c41-cd1ab221ad73\",\"01bb9d99-fea7-4470-b5bc-673658231664\",\"66b86a0e-8a50-4e5b-ba33-10cb7104ecf5\",\"c331af33-e8a0-49a5-bad0-417a6e93ee74\",\"f388e548-380e-49a0-8974-0c3a4d6b9b21\",\"c0274a64-cd6f-4a86-bce5-4ff1e860611b\",\"7fccfb7b-9b15-4891-a608-45c05fb14a77\",\"ec8818b8-76f6-4237-a329-6fd237c34a5e\",\"d3589b7e-a662-4294-a787-965fc0f73595\",\"25feffcb-25da-4656-b158-779bc9c0ba94\",\"a2206086-1e3f-4293-b3d9-62d6a1159602\",\"b9aa03f6-49f7-436b-b14b-821a6c633af1\",\"3ef1f976-5b47-4b3c-a7a2-881b0250d46a\",\"d178f080-d1b9-49ba-aa30-c9d5511e5373\",\"4f52134a-43ba-46a1-9a33-b384d42c5cb7\",\"994a107e-40b8-4af6-a2e5-cb5a32d3bbc2\",\"ad531f6c-e9b5-4503-94d2-d7c784a72e01\",\"0660e580-944a-4471-b70d-c5f877f247d7\",\"bb366ffb-a217-4e89-8155-88ed565c98fc\",\"b5c7a6dc-7dd0-4a95-a6ba-f594da9207fb\",\"bf0a3af2-3011-45a8-a730-98abad7359f1\",\"9a2788b9-a011-4088-8587-b0b2bb541307\",\"e3520acd-a402-4d24-9fb6-73837c962a6e\",\"258a75b1-a33b-4306-9298-de0d67d24452\",\"df25de0b-41b1-4b72-8592-7261dcf7cdb3\",\"23a6016a-857b-4881-9ec3-75be0278e05c\",\"875de099-b043-4027-a0c0-914b28aa4a2b\",\"2e0591e2-7ebf-420a-80d8-a058c0fb9434\",\"e51bf806-44d4-4ce0-9f75-1d793d23b404\",\"4567c1ec-de41-4c61-92bb-3df12557dae2\",\"2c1166dd-7b6f-4a81-aa14-36dec1078d4e\",\"03000293-c117-4efa-b975-cb699732e500\",\"06143ea9-e775-4d5a-822e-3f218528da05\",\"99245e17-afd6-4232-a7ec-9f9f13969952\",\"d08a0df2-bc0a-4fc6-a79c-39a8abbd6d74\",\"d2fa9170-06f1-4e46-b449-eb49281713e5\",\"7612b73c-d68e-4b1e-9eb9-85ffa59e5e09\",\"b90c7e6e-a001-4fb1-b1eb-0dc78603b371\",\"b888c42a-309d-4350-8457-41223334d795\",\"f93c859d-d7da-494c-991e-6b7ce3c8909a\",\"8d0cd7a2-3544-4898-a9af-d49fd3c9e0e2\",\"6e3e5d68-93f6-4162-aa8e-207d4897f8df\",\"9e35aa6b-52d8-4b46-a69a-55b381103fec\",\"3f084e64-d8f7-4daa-b07b-6681b51d72b4\",\"f1b02e34-6685-4f40-84c3-7adf673d1133\",\"16280f91-e8a9-4c66-bbd3-21b09330dfb5\",\"15979826-11e3-481c-bfb5-61c75bca3390\",\"4f80050f-11d2-44e8-939c-255db5a7f1a3\",\"01f7fce4-4499-4a9a-9810-44848707b4b1\"]";
//        uuids.addAll(ObjectUtils.fromJson(uuidStr, List.class));

        int count=0;
        List<String> errorIds = new ArrayList<>();
        for (String uuid:uuids) {
            try {
                Map<String, Object> response = null;
                String queryUrl = "http://esp-lifecycle.web.sdp.101.com/v0.6/questions/" + uuid + "?include=TI,CG,LC,CR,EDU";
                WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
                response = wafSecurityHttpClient.getForObject(queryUrl, Map.class);
                Map<String, Object> techInfo = (Map<String, Object>) ((Map) response.get("tech_info")).get("href");
                String loc = String.valueOf(techInfo.get("location"));
                String previewPath = loc.substring(loc.indexOf("/"), loc.lastIndexOf("/")) + "/resources/preview";
                String bigName = uuid + "_question_big.png";
                String smallName = uuid + "_question_small.png";
                String cmd1 = "phantomjs.exe rasterize.js \"http://esp-editor.web.sdp.101.com/resources/player/icplayer/previewqti?id=" + uuid + "&editor_pre=true&hint=0&v=1449045742476\" \"D:\\\\outpic\\" + bigName + "\" 1280 800 10000 1";
                String cmd2 = "phantomjs.exe rasterize.js \"http://esp-editor.web.sdp.101.com/resources/player/icplayer/previewqti?id=" + uuid + "&editor_pre=true&hint=0&v=1449045742476\" \"D:\\\\outpic\\" + smallName + "\" 176 110 10000 0.1375";
                StringBuffer logBuffer = new StringBuffer();
                StringBuffer outBuffer = new StringBuffer();
                RunCommand(cmd1, outBuffer, path, logBuffer);
                RunCommand(cmd2, outBuffer, path, logBuffer);
                File smallPic = new File("D:\\\\outpic\\" + smallName);
                File bigPic = new File("D:\\\\outpic\\" + bigName);
                String out = "";
                if (smallPic.exists()) {
                    out = UploadFileToCS(smallPic, previewPath, smallName, "2c875951-7919-4414-8586-ad96a7285599", "http://cs.101.com/v0.1", logBuffer);
                }
                System.out.println(out);
                if (bigPic.exists()) {
                    out = UploadFileToCS(bigPic, previewPath, bigName, "2c875951-7919-4414-8586-ad96a7285599", "http://cs.101.com/v0.1", logBuffer);
                }
                System.out.println(out);

                String updateUrl = "http://esp-lifecycle.web.sdp.101.com/v0.6/questions/" + uuid;
                Map<String, String> previewMap = new HashMap<>();
                previewMap.put("question_big", "${ref-path}" + previewPath + "/" + bigName);
                previewMap.put("question_small", "${ref-path}" + previewPath + "/" + smallName);
                response.put("preview", previewMap);
                Map<String, Object> rt = wafSecurityHttpClient.putForObject(updateUrl, response, Map.class);
                System.out.println(ObjectUtils.toJson(rt));
                System.out.println(++count);
            }catch (Exception e) {
                errorIds.add(uuid);
                e.printStackTrace();
            }
        }

        System.out.println("error: "+ObjectUtils.toJson(errorIds));

        
//        String paramStr = "{\"ext_param\":{\"targetFmt\":\"mp3\",\"coverNum\":\"16\",\"subtype\":\"audio\"},\"target_location\":\"/edu_product/esp/assets/7243c66c-059c-4074-bcd2-5d463118778b.pkg\",\"task_execute_env\":\"product\",\"session\":\"37034782-05d9-439d-b5fb-8e5241d0c736\",\"location\":\"http://cs.101.com/v0.1/download?path=/edu_product/esp/assets/7243c66c-059c-4074-bcd2-5d463118778b.pkg/a49e0b72468b830d33c5bab067cc8b78.wav\",\"cs_api_url\":\"http://cs.101.com/v0.1\",\"commands\":[\"ffmpeg -i \\\"#src#\\\" -y -ab 128k -c:a libmp3lame -vn \\\"#target#\\\"\",\"ffmpeg -i \\\"#src#\\\" -acodec pcm_s16le -f wav - | oggenc2 -q 2 --raw - -o \\\"#target#\\\"\"],\"callback_api\":\"http://esp-lifecycle.web.sdp.101.com/v0.6/assets/transcode/videoCallback\"}";
//
//        TranscodeParam param = ObjectUtils.fromJson(paramStr,
//                TranscodeParam.class);
//
//
//        StringBuffer errMsg = new StringBuffer();
//        TranscodeResult result = new TranscodeResult();
//        try {
//            result = transcodeAudio("7243c66c-059c-4074-bcd2-5d463118778b", param, errMsg);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        System.out.println("CallbackUrl: "+param.getCallback_api() + "?identifier=7243c66c-059c-4074-bcd2-5d463118778b" + "&status=1");
//
//        System.out.println(ObjectUtils.toJson(result));

    }
}