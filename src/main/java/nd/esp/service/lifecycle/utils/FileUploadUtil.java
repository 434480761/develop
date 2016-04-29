package nd.esp.service.lifecycle.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

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
import org.springframework.http.HttpStatus;

import com.google.gson.reflect.TypeToken;
import com.nd.gaea.client.http.WafSecurityHttpClient;

public class FileUploadUtil {
    private final static Logger LOG = LoggerFactory
            .getLogger(FileUploadUtil.class);
    
    public final static long chunkMax = 50L*1024*1024;//文件超过chunkMax(Byte)就要进行分块上传
    public final static long chunkNumMax = 20;//最多并行发送的分块数
    public final static long chunkSize = 5L*1024*1024;//文件分块上传时每块的大小(Byte)
    
    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
    
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
}
