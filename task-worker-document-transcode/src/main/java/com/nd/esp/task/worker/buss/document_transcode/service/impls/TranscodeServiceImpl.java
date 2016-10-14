package com.nd.esp.task.worker.buss.document_transcode.service.impls;

import com.nd.esp.task.worker.buss.document_transcode.model.TranscodeParam;
import com.nd.esp.task.worker.buss.document_transcode.model.TranscodeResult;
import com.nd.esp.task.worker.buss.document_transcode.service.TranscodeService;
import com.nd.esp.task.worker.buss.document_transcode.support.LifeCircleException;
import com.nd.esp.task.worker.buss.document_transcode.utils.*;
import com.nd.esp.task.worker.buss.document_transcode.utils.gson.ObjectUtils;
import com.nd.esp.task.worker.container.ext.TaskTraceResult;
import com.nd.esp.task.worker.container.service.task.ExtFunService;
import com.nd.sdp.cs.common.CsConfig;
import com.nd.sdp.cs.sdk.Dentry;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;



/**
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:06
 */
@SuppressWarnings("unchecked")
@Service("documentTranscodeService")
public class TranscodeServiceImpl implements TranscodeService {

    private final static Logger LOG = LoggerFactory
            .getLogger(TranscodeServiceImpl.class);


    private static String zipFileTempDir = System.getProperty("java.io.tmpdir");

    public static final String ERR_JSONPARAM = "err_jsonparam";
    public static final String ERR_PACK_PATH = "err_pack_path";

    public static final String REF_PATH_HEADER = "${ref-path}";

    private static final String [] IMAGE_EXTS_ARRAY={"jpg","jpeg","bmp","png","gif"};
    private static final List<String> IMAGE_EXTS = Arrays.asList(IMAGE_EXTS_ARRAY);


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


    
    public static TranscodeResult transcode(String id, TranscodeParam param, StringBuffer logMsg) throws Exception{
        TranscodeResult result = new TranscodeResult();

        String srcFileName = param.getLocation().substring(param.getLocation().lastIndexOf("/")+1);
        String srcDir = zipFileTempDir+File.separator+id+File.separator+"src";
        String destDir = zipFileTempDir+File.separator+id+File.separator+"targets";
        String htmlDir = destDir+File.separator+"html";
        String imageDir = destDir+File.separator+"image";
        String thumbDir = destDir+File.separator+"thumbnail";

        File destDirectory = new File(destDir);
        if(destDirectory.exists()) {
            FileUtils.deleteQuietly(destDirectory);
        }
        FileUtils.forceMkdir(destDirectory);

        long timeStart = System.currentTimeMillis();
        StringBuffer errMsg = new StringBuffer();
        logMsg.append("  Download path:"+param.getLocation()+"&session="+param.getSession()+System.getProperty("line.separator"));
        LOG.info("Download path:"+param.getLocation()+"&session="+param.getSession());
        String srcFilePath = srcDir + File.separator + srcFileName;
        if(!DownloadFile(param.getLocation(), srcFilePath, errMsg, param.getSession())) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/DOCUMENT_TRANSCODE_FAIL",errMsg.toString());
        }

        LOG.info("下载消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms");
        logMsg.append("下载消耗时间: "+(System.currentTimeMillis()-timeStart)+"ms"+System.getProperty("line.separator"));
        timeStart = System.currentTimeMillis();

        Map<String,String> targetsMap = new HashMap<String,String>();
        Map<String,String> targetsMetadata = new HashMap<String,String>();
        List<String> previews = new ArrayList<String>();
        long sizeOfFile = 0;
        Map<String,String> meta = new HashMap<>();
        if(srcFileName.endsWith(".txt")) {
            Txt2htmlUtil.transferTxt2Html(srcFilePath, htmlDir);
            targetsMap.put("html", REF_PATH_HEADER+param.getTarget_location()+"/transcode/html");
            meta.put("FileSize", String.valueOf(FileUtils.sizeOfDirectory(new File(htmlDir))));
            targetsMetadata.put("source", ObjectUtils.toJson(meta));
            targetsMetadata.put("html", ObjectUtils.toJson(meta));
        } else if(srcFileName.endsWith(".jpg") || srcFileName.endsWith(".jpeg") || srcFileName.endsWith(".bmp")
                || srcFileName.endsWith(".png") || srcFileName.endsWith(".gif")) {
            String destFilename = Pdf2htmlUtil.getFileNameNoEx(srcFileName)+".jpg";
            meta = ImageUtils.toJPG(srcFilePath, destDir+File.separator+destFilename);
            targetsMetadata.put("source", ObjectUtils.toJson(meta));
            targetsMap.put("href", REF_PATH_HEADER+param.getTarget_location()+"/transcode/"+destFilename);
            meta.put("FileSize", String.valueOf(FileUtils.sizeOf(new File(destDir + File.separator + destFilename))));
        } else {
            LOG.info("Office转pdf开始");
            String pdfFilePath = destDir + File.separator + "pdf.pdf";
            if (!srcFileName.endsWith(".pdf")) {
                Office2pdfUtil.convert2PDF(srcFilePath, pdfFilePath);
            } else {
                FileUtils.copyFile(new File(srcFilePath), new File(pdfFilePath));
            }
            String suffix = "";
            if(srcFilePath.lastIndexOf('.')>0 && srcFilePath.lastIndexOf('.')<srcFilePath.length()-1) {
                suffix = srcFilePath.substring(srcFilePath.lastIndexOf('.')+1);
            }
            sizeOfFile = FileUtils.sizeOf(new File(pdfFilePath));
            targetsMap.put("pdf", REF_PATH_HEADER + param.getTarget_location() + "/transcode/pdf.pdf");
            meta = DocumentInfoUtil.getDocumentInfo(pdfFilePath, "pdf", sizeOfFile);
            targetsMetadata.put("source", ObjectUtils.toJson(DocumentInfoUtil.getDocumentInfo(pdfFilePath, suffix, FileUtils.sizeOf(new File(srcFilePath)))));
            Pdf2htmlUtil.transferPdf2Html(pdfFilePath, htmlDir, logMsg);
            sizeOfFile = FileUtils.sizeOfDirectory(new File(htmlDir));
            targetsMap.put("html", REF_PATH_HEADER + param.getTarget_location() + "/transcode/html");
            targetsMetadata.put("html", ObjectUtils.toJson(DocumentInfoUtil.getDocumentInfo(pdfFilePath, suffix, sizeOfFile)));
            Pdf2imageUtil.transferPdf2Image(pdfFilePath, imageDir, logMsg);
            sizeOfFile = FileUtils.sizeOfDirectory(new File(imageDir));
            targetsMap.put("image", REF_PATH_HEADER + param.getTarget_location() + "/transcode/image");
            targetsMetadata.put("image", ObjectUtils.toJson(DocumentInfoUtil.getDocumentInfo(pdfFilePath, suffix, sizeOfFile)));
            Pdf2imageUtil.makeThumbnails(imageDir, thumbDir);
            File[] thumbFiles = new File(thumbDir).listFiles();
            Arrays.sort(thumbFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Integer.valueOf(Pdf2htmlUtil.getFileNameNoEx(o1.getName())) - Integer.valueOf(Pdf2htmlUtil.getFileNameNoEx(o2.getName()));
                }
            });
            for (File thumbFile : thumbFiles) {
                previews.add(param.getTarget_location() + "/transcode/thumbnail/" + thumbFile.getName());
            }
        }

        String csHost = param.getCs_api_url().substring(0, param.getCs_api_url().lastIndexOf('/')).replace("http://", "");
        CsConfig.setHost(csHost);
        List<Dentry> response = ContentServiceUtils.uploadDirectory(new File(destDir), param.getTarget_location()+"/transcode", param.getSession());

        if(targetsMap.containsKey("href")) {
            for(Dentry dentry:response) {
                if(dentry.getName().endsWith(".jpg") && dentry.getInode()!=null) {
                    meta.put("md5", dentry.getInode().getMd5());
                    targetsMetadata.put("href", ObjectUtils.toJson(meta));
                    break;
                }
            }
        } else if(targetsMap.containsKey("pdf")) {
            for(Dentry dentry:response) {
                if("pdf.pdf".equals(dentry.getName()) && dentry.getInode()!=null) {
                    meta.put("md5", dentry.getInode().getMd5());
                    targetsMetadata.put("pdf", ObjectUtils.toJson(meta));
                    break;
                }
            }
        }

        result.setStatus(1);
        result.setLocations(targetsMap);
        result.setMetadata(targetsMetadata);
        result.setPreviews(previews);
        
        return result;
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
            result = transcode(id, transcodeParam, logMsg);
        } catch (Throwable e) {
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
        String paramStr = "{\"callback_api\":\"http://esp-lifecycle.pre1.web.nd/v0.6/assets/transcode/document_callback\",\"session\":\"830d00ff-7d79-4d4b-8c5b-a2b7b37a2191\",\"location\":\"http://betacs.101.com/v0.1/download?path=/prepub_content_edu_product/esp/assets/c186535a-a664-486e-bfb7-f31ad3776fb6.pkg/pdf%20Android%e7%bb%88%e7%ab%af%e4%b8%93%e9%a1%b9%e5%ae%9d%e5%85%b8-%e4%b8%8a%e7%af%87%20(%e5%85%88%e8%a1%8c%e7%89%88)%20%20(1).pdf\",\"target_location\":\"/prepub_content_edu_product/esp/assets/c186535a-a664-486e-bfb7-f31ad3776fb6.pkg\",\"cs_api_url\":\"http://betacs.101.com/v0.1\"}";

        TranscodeParam param = ObjectUtils.fromJson(paramStr,
                TranscodeParam.class);


        StringBuffer errMsg = new StringBuffer();
        TranscodeResult result = new TranscodeResult();
        try {
            result = transcode("cdffe", param, errMsg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("CallbackUrl: "+param.getCallback_api() + "?identifier=d5714fd6-0d09-4cb5-9d4b-9627a67f5efe&status=1");

        System.out.println(ObjectUtils.toJson(result));

        String s="";


    }
}