/* =============================================================
 * Created: [2015年11月5日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.daos.offlinemetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.entity.cs.Dentry;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

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
import org.springframework.stereotype.Repository;

import com.nd.gaea.client.http.WafSecurityHttpClient;

/**
 * @author linsm
 * @since 
 *
 */
@Repository
public class OfflineDaoImpl implements OfflineDao{
    
    private final static Logger LOG = LoggerFactory.getLogger(OfflineDaoImpl.class);
//    public final static long chunkMax = 50L * 1024 * 1024;// 文件超过chunkMax(Byte)就要进行分块上传
//    public final static long chunkSize = 5L * 1024 * 1024;// 文件分块上传时每块的大小(Byte)
//    //FIXME
//    private final static String CS_API_URL = "http://sdpcs.beta.web.sdp.101.com/v0.1";
//    private final static String LC_API_URL = "http://localhost:8080/esp-lifecycle/v0.6";
    private final static String URL_SPLITTER="/"; 
    private final static String CS_API_URL = Constant.CS_API_URL;
    private final static String LC_API_URL = Constant.LIFE_CYCLE_DOMAIN_URL+URL_SPLITTER+"v0.6";
    
    
    
    private final static WafSecurityHttpClient WAF_HTTP_CLIENT = new WafSecurityHttpClient();

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.dao.offlineDao#getCsInfo(java.lang.String, java.lang.String)
     */
    @Deprecated
    @Override
    public AccessModel getCsInfo(String resType, String uuid) {
        
        String url = LC_API_URL + URL_SPLITTER + resType + URL_SPLITTER + uuid + URL_SPLITTER + "uploadurl?uid=777";
        
        LOG.info("getUploadMessage  url:{}",url);

        return WAF_HTTP_CLIENT.getForObject(url, AccessModel.class);
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.dao.offlineDao#getDetail(java.lang.String, java.lang.String)
     */
    @Override
    public String getDetail(String resType, String uuid) {
        
        String url = LC_API_URL + URL_SPLITTER + resType + URL_SPLITTER + uuid + "?" + "include=LC,CG,TI,EDU,CR&isAll=true";

        LOG.info("getDetail url:{}",url);

        return WAF_HTTP_CLIENT.getForObject(url, String.class);
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.dao.offlineDao#upFileToCs(byte[], java.lang.String, java.lang.String, java.lang.String)
     * 
     * 备注：该上传文件到CS的方式是使用http api请求的方式,目前已替换为CS SDK的方式,详见：ContentServiceHelper.uploadByByte
     */
    @Deprecated
    @Override
    public Dentry upFileToCs(byte[] content, String path, String fileName, String session) throws Exception {
        return ObjectUtils.fromJson(UploadSmallFileToCS(content, path, fileName, session, CS_API_URL), Dentry.class);
    }
    
//    /**
//     * 上传文件到cs
//     * FIXME 后期可以考虑把分块处理去除（在这里的任务里数据量并没有那么多） (by lsm 2015.11.18),已处理，重新写一个方法。
//     * 
//     * @param bytes 
//     * @param csPath  requestBody.put("path", csPath);
//     * @param fileName requestBody.put("name", fileName); requestBody.put("filePath", csPath + "/" + fileName);
//     * @param session
//     * @param csApiUrl
//     * @return
//     * @throws Exception
//     * @since
//     */
//    public static String UploadFileToCS(byte[] bytes, String csPath, String fileName, String session, String csApiUrl) throws Exception {
//        String rt = null;
//        if (bytes.length >= chunkMax) {
//            int nChunks = (int) (Math.ceil(Double.valueOf(bytes.length) / chunkSize));
//            List<Callable<String>> tasks = new ArrayList<Callable<String>>();
//            for (int i = 0; i < nChunks; ++i) {
//                Map<String, String> requestBody = new HashMap<String, String>();
//                requestBody.put("path", csPath);
//                requestBody.put("name", fileName);
//                requestBody.put("filePath", csPath + "/" + fileName);
//                requestBody.put("scope", "1");
//                requestBody.put("size", String.valueOf(bytes.length));
//                requestBody.put("chunks", String.valueOf(nChunks));
//                int startIndexInclusive = Integer.valueOf(Long.toString(chunkSize * i));
//                int endIndexExclusive = 0;
//                long end = chunkSize * (i + 1);
//                if (bytes.length < end) {
//                    endIndexExclusive = Integer.valueOf(Long.toString(bytes.length));
//                } else {
//                    endIndexExclusive = Integer.valueOf(Long.toString(end));
//                }
//                byte[] chunkBytes = ArrayUtils.subarray(bytes, startIndexInclusive, endIndexExclusive);
//
//                // 设置分块参数
//                requestBody.put("chunk", String.valueOf(i));
//                requestBody.put("chunkSize", String.valueOf(chunkBytes.length));
//                requestBody.put("pos", String.valueOf(startIndexInclusive));
//
//                UploadThread uploadThread = new UploadThread(chunkBytes, requestBody, session, csApiUrl);
//                tasks.add(uploadThread);
//            }
//
//            List<Future<String>> results = executorService.invokeAll(tasks, 10 * 60, TimeUnit.SECONDS);
//
//            for (Future<String> result : results) {
//                String content = "";
//                try {
//                    content = result.get();
//                } catch (ExecutionException e1) {
//                    LOG.error("向CS上传文件时错误:" + e1.getMessage());
//                    if (e1.getCause() != null) {
//                        throw (Exception) e1.getCause();
//                    }
//                }
//                if (content.contains("dentry_id")) {
//                    Map<String, Object> response = ObjectUtils.fromJson(content, new TypeToken<Map<String, Object>>() {
//                    });
//                    int dealTime = 180;
//                    do {
//                        String queryUrl = csApiUrl + "/dentries/" + response.get("dentry_id") + "?session=" + session;
//                        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//                        response = wafSecurityHttpClient.getForObject(queryUrl, Map.class);
//                        String flag = String.valueOf(response.get("flag"));
//                        LOG.error("cs合并返回的flag:" + flag);
//
//                        if (flag.equals("-3")) {
//                            try {
//                                Thread.sleep(10000);
//                            } catch (InterruptedException e) {
//                                LOG.error("等待CS上传文件合并时异常:" + e.getMessage());
//                                throw e;
//                            }
//                        } else if (flag.equals("-1")) {
//
//                            LOG.error("CS合并的文件被删除:");
//                            throw new ArithmeticException("CS合并的文件被删除,返回的flag=" + flag);
//                        }
//                        dealTime--;
//
//                    } while (!String.valueOf(response.get("flag")).equals("1") && dealTime > 0);
//
//                    rt = ObjectUtils.toJson(response);
//                }
//            }
//        } else {
//            Map<String, String> requestBody = new HashMap<String, String>();
//            requestBody.put("path", csPath);
//            requestBody.put("name", fileName);
//            requestBody.put("filePath", csPath + "/" + fileName);
//            requestBody.put("scope", "1");
//            rt = UploadChunkToCS(bytes, requestBody, session, csApiUrl);
//        }
//
//        return rt;
//    }

    /**
     * 上传小文件到cs
     * @author linsm
     * @param bytes
     * @param csPath
     * @param fileName
     * @param session
     * @param csApiUrl
     * @return
     * @throws Exception
     * @since
     */
    public static String UploadSmallFileToCS(byte[] bytes, String csPath, String fileName, String session, String csApiUrl) throws Exception{
        String rt = null;
        Map<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("path", csPath);
        requestBody.put("name", fileName);
        requestBody.put("filePath", csPath + "/" + fileName);
        requestBody.put("scope", "1");
        rt = UploadChunkToCS(bytes, requestBody, session, csApiUrl);
        return rt;
    }
    private static String UploadChunkToCS(byte[] bytes, Map<String, String> requestBody, String session, String csApiUrl) throws Exception {
        String url = csApiUrl + "/upload?session=" + session;
        String filepath = requestBody.get("filePath");
        String fileName = filepath.substring(filepath.lastIndexOf('/') + 1);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String rt = null;
        try {

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient httpclient = httpClientBuilder.build();
            try {

                HttpPost httpPost = new HttpPost(url);

                MultipartEntityBuilder partBuilder = MultipartEntityBuilder.create();
                for (Map.Entry<String, String> entry : requestBody.entrySet()) {
                    partBuilder.addPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.APPLICATION_JSON));
                }
                InputStreamBody inBody = new InputStreamBody(inputStream,
                                                             ContentType.create("application/zip"),
                                                             fileName);
                partBuilder.addPart("file", inBody);

                HttpEntity reqEntity = partBuilder.build();

                httpPost.setEntity(reqEntity);
                CloseableHttpResponse response = httpclient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                try {
                    // 获取返回数据
                    // logger.debug("status line:" + response.getStatusLine());
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
                    if (statusLine == null || statusLine.getStatusCode() != 200) {
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      "LC/REQUEST_ARCHIVING_FAIL",
                                                      "上传文件异常：" + rt);
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

//    static class UploadThread implements Callable<String> {
//        private final byte[] bytes;
//        private final Map<String, String> requestBody;
//        private final String session;
//        private final String csApiUrl;
//
//        UploadThread(byte[] bytes, Map<String, String> requestBody, String session, String csApiUrl) {
//            this.bytes = bytes;
//            this.requestBody = requestBody;
//            this.session = session;
//            this.csApiUrl = csApiUrl;
//        }
//
//        public String call() throws Exception {
//            String result = UploadChunkToCS(bytes, requestBody, session, csApiUrl);
//            return result;
//        }
//    }
}
