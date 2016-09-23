package com.nd.esp.task.worker.buss.document_transcode.utils;

import com.nd.esp.task.worker.buss.document_transcode.support.LifeCircleException;
import com.nd.sdp.cs.sdk.Dentry;
import com.nd.sdp.cs.sdk.ExtendUploadData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContentServiceUtils {
    
    
    private static Log LOG=LogFactory.getLog(ContentServiceUtils.class);


    public static void createSync(String path, String name, String session) throws Exception {
        String serviceName = getServiceName(path);
        //参数设置
        Dentry request = new Dentry();
        request.setPath(path);                     //父目录项路径，支持自动创建目录，（path 和 parent_id 二选一）
        request.setName(name);                        //目录项名（文件一般包括扩展名，支持重命名），必选

        //调用
        LOG.info("cs create " + serviceName + " " + session);
        request.create(serviceName, 0, 0, session);
    }

    public static Dentry uploadSync(File localFile, String csPath, String session) throws Exception {
        String name = localFile.getName();

        Dentry request = new Dentry();

        ExtendUploadData requestData = new ExtendUploadData();
        requestData.setFilePath(csPath+"/"+name);
        LOG.info("cs upload " + localFile.getAbsolutePath());

        Dentry upload = request.upload(getServiceName(csPath), localFile.getAbsoluteFile(),
                requestData, session, null);

        return upload;
    }

    public static List<Dentry> uploadDirectory(File localDir, String csPath, String session) throws Exception {
        List<Dentry> response = new ArrayList<Dentry>();

        String name = csPath.substring(csPath.lastIndexOf('/')+1);
        String path = csPath.substring(0, csPath.lastIndexOf('/'));

        createSync(path, name, session);
        if(localDir.exists() && localDir.isDirectory()) {
            File[] fileList = localDir.listFiles();
            for(File file : fileList) {
                if(!file.isDirectory()) {
                    Dentry uploadSync = uploadSync(file, csPath, session);
                    response.add(uploadSync);
                } else {
                    response.addAll(uploadDirectory(file, csPath+"/"+file.getName(), session));
                }
            }
            return response;
        } else {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/DOCUMENT_TRANSCODE_FAIL","Upload fail, file no exist");
        }
    }

    public static void downloadSync(String csPath, String localPath, String direct, String session) throws Exception {

        File local_file = new File(localPath);
        LOG.info("downloadSync " + localPath + " " + local_file.getParent());
        new File(local_file.getParent()).mkdirs();

        String serviceName = getServiceName(csPath);
        Dentry.download(serviceName, localPath, null, csPath, null, null, session);

    }

    public static String getServiceName(String csPath) {
        int index = csPath.indexOf('/', 1);
        if(-1 == index) {
            index = csPath.length();
        }
        return csPath.substring(1, index);
    }

}
