package com.nd.esp.task.worker.buss.packaging.service.impls;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nd.esp.task.worker.buss.packaging.Constant;
import com.nd.esp.task.worker.buss.packaging.entity.CopyResourceStatus;
import com.nd.esp.task.worker.buss.packaging.entity.cs.Dentry;
import com.nd.esp.task.worker.buss.packaging.entity.cs.DentryArray;
import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadParam;
import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadResponse;
import com.nd.esp.task.worker.buss.packaging.model.CoursewareModel;
import com.nd.esp.task.worker.buss.packaging.service.ContentService;
import com.nd.esp.task.worker.buss.packaging.service.CopyResourceService;
import com.nd.esp.task.worker.buss.packaging.service.CoursewareService;
import com.nd.esp.task.worker.buss.packaging.service.PackageService;
import com.nd.esp.task.worker.buss.packaging.support.LifeCircleException;
import com.nd.esp.task.worker.buss.packaging.utils.BeanMapperUtils;
import com.nd.esp.task.worker.buss.packaging.utils.CollectionUtils;
import com.nd.esp.task.worker.buss.packaging.utils.SessionUtil;
import com.nd.esp.task.worker.buss.packaging.utils.StringUtils;
import com.nd.esp.task.worker.container.ext.TaskTraceResult;
import com.nd.esp.task.worker.container.service.task.ExtFunService;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;

/**
 * @title 拷贝资源接口实现
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午7:49:02
 */
@Service("copyResourceService")
public class CopyResourceServiceImpl implements CopyResourceService {
    private Log LOG = LogFactory.getLog(CopyResourceServiceImpl.class);

    @Autowired
    private CoursewareService coursewareService;

    @Autowired
    private ContentService contentService;

    @Autowired
    @Qualifier("packageService")
    private PackageService packageService;

    /**
     *@param identifier 原始课件identifier
     *@param 附加变量
     *
     *
     *@author liuwx
     */
    @Override
    public void run(String identifier, String argument, TaskTraceResult taskTraceResult, ExtFunService extFunService) {
        // todo 暂时未用到

        boolean operationStatus = true;
        String newCoursewareId = "none";
        String callBackPara = "";
        String execute_status = "";
        String execute_message = "";
        try {
            CoursewareModel coursewareModel = coursewareService.get(identifier);
            if (coursewareModel == null || StringUtils.isEmpty(coursewareModel.getIdentifier())) {
                LOG.error("原始课件未找到,无法进行复制");
                taskTraceResult.setExitCode(CopyResourceStatus.COPY_NO_CONDITION.getCode());
                taskTraceResult.setTracemsg("原始课件未找到");
                operationStatus = false;

            }
            else {
                // 调用上传接口
                // UploadResponse uploadResponse= ObjectUtils.fromJson(argument,new TypeToken<UploadResponse>(){});
                UploadResponse uploadResponse = BeanMapperUtils.mapperOnString(argument, UploadResponse.class);
                // UploadResponse uploadResponse = BeanMapperUtils.beanMapper(argument, UploadResponse.class);
                // UploadResponse uploadResponse = coursewareService.getUpload(Constant.lc_upload_placeholder);
                // 获取生成的identifier
                newCoursewareId = uploadResponse.getUuid();

                // 开始拷贝
                taskTraceResult.setExitCode(CopyResourceStatus.COPY_START.getCode());
                taskTraceResult.setTracemsg(CopyResourceStatus.COPY_START.getMessage());
                extFunService.callTraceLog(taskTraceResult);
                // 获取原始课件的上传信息
                UploadParam uploadParam = new UploadParam(identifier);

                String instanceKey = SessionUtil.getHrefInstanceKey(coursewareModel.getHref());
                if (StringUtils.isEmpty(instanceKey)) {
                    LOG.error("未定义的实例:" + instanceKey);
                    execute_status = "0";
                    operationStatus = false;
                    execute_message = "未定义的实例:" + instanceKey;
                }
                else {

                    String coverage = "";
                    if (!Constant.CS_DEFAULT_INSTANCE.equals(instanceKey)) {
                        // todo 解决拷贝中多实例的问题
                        coverage = "666";
                        uploadParam.buildCoverage(coverage);
                    }
                    // UploadResponse lastUploadResponse = coursewareService.getUpload(identifier);
                    UploadResponse lastUploadResponse = coursewareService.getUpload(uploadParam);
                    /*
                     * String refPath = Constant.CS_EDU_DOMAIN_API + uploadResponse.getSessionId() + "/static"; String
                     * href = coursewareModel.getHref().replace("${ref-path}", refPath) .replace(identifier,
                     * newCoursewareId);
                     */
                    // 原始课件的根目录
                    String srcPath = lastUploadResponse.getDistPath();
                    // 拷贝
                    // todo 日志输出
                    /*
                     * 调用CS原生的复制目录接口 try { copyDentry(uploadResponse, lastUploadResponse,srcPath); } catch (Exception e)
                     * { // TODO Auto-generated catch block e.printStackTrace(); }
                     */
                    String parentPath = getParentPath(srcPath);
                    String descPath = uploadResponse.getDistPath();
                    String dirname = descPath.substring(descPath.lastIndexOf("/") + 1);
                    try {
                        // 通过LC接口获取顶层实例的session
                        // todo 做缓存 多实例
                        //String topSession = contentService.getTopSession();
                        String topSession = lastUploadResponse.getSessionId();
                        // 创建目录项
                        contentService.createDir(parentPath, dirname, topSession);

                        contentService.copyDir(srcPath, descPath, topSession);
                        taskTraceResult.setExitCode(CopyResourceStatus.COPY_SUCCESS.getCode());
                        taskTraceResult.setTracemsg(CopyResourceStatus.COPY_SUCCESS.getMessage());
                    } catch (Exception e) {
                        taskTraceResult.setExitCode(CopyResourceStatus.COPY_FAIL.getCode());
                        taskTraceResult.setTracemsg("调用CS失败");
                        operationStatus = false;
                    }
                    CoursewareModel newCoursewareModel = new CoursewareModel();
                    BeanUtils.copyProperties(coursewareModel, newCoursewareModel);
                    newCoursewareModel.setIdentifier(newCoursewareId);
                    // 目录替换
                    // todo 明确下是否是写入到storeinfo的href中
                    newCoursewareModel.setHref(coursewareModel.getHref().replace(identifier, newCoursewareId));
                    // 开始和更新时间 不赋值
                    newCoursewareModel.setLanguage("zh-CN");
                    // 调用创建课件的接口
                    // this.coursewareService.create(newCoursewareModel);
                    JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
                    newCoursewareModel.setCreateTime(new Date());
                    // newCoursewareModel.setStatus("CONVERT_ED");
                    callBackPara = mapper.writeValueAsString(newCoursewareModel);
                    execute_status = "1";
                }

            }
        } catch (LifeCircleException e) {
            LOG.error("调用生命周期接口报错:" + e.getMessage());
            execute_status = "0";
            operationStatus = false;
            execute_message = e.getMessage();

        } catch (Exception e) {
            LOG.error("操作逻辑出错:" + e.getMessage());
            execute_status = "0";
            operationStatus = false;
            execute_message = e.getMessage();
        }

        String callbackUrl = Constant.LIFE_CYCLE_API_URL + Constant.LC_COPY_API_CALLBACK;
        callbackUrl += "?execute_message=%s&execute_status=%s";
        callbackUrl = String.format(callbackUrl, newCoursewareId, execute_message, execute_status);
        taskTraceResult.setCallBackUrl(callbackUrl);
        taskTraceResult.setCallBackPara(callBackPara);
        extFunService.callTraceLog(taskTraceResult);
        if (operationStatus) {
            extFunService.callSucceed(taskTraceResult);
        }
        else {
            extFunService.callFail(taskTraceResult);
        }

    }

    private String getParentPath(String path) {

        return path.substring(0, path.lastIndexOf("/"));
    }

    /**	
     * @desc:拷贝原始课件的目录  
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param descUploadResponse 需要拷贝的课件上传对象
     * @param srcUploadResponse 原始课件的上传对象
     * @param srcPath 原始课件目录
     * @throws Exception
     */
//    private void copyDentry(UploadResponse descUploadResponse, UploadResponse srcUploadResponse, String srcPath)
//            throws Exception {
//        String oldCoursewareId = descUploadResponse.getUuid();
//        String descSession = descUploadResponse.getSessionId();
//        String srcSession = srcUploadResponse.getSessionId();
//        String refPath = Constant.CS_EDU_DOMAIN_API + descSession + "/static";
//        DentryArray dentryArrays = contentService.getDentryItems(srcPath, srcSession);
//
//        // todo 原始目录为空 抛出异常
//        if (CollectionUtils.isEmpty(dentryArrays.getItems())) {
//            return;
//        }
//
//        for (Dentry dentry : dentryArrays.getItems()) {
//
//            if (Dentry.TYPE_DIRECTORY.equals(dentry.getType())) {
//
//                copyDentry(descUploadResponse, srcUploadResponse, dentry.getPath());
//
//            }
//            else if (Dentry.TYPE_FILE.equals(dentry.getType()) || Dentry.TYPE_LINK.equals(dentry.getType())) {
//                // 下载源课件资源文件
//                String realPath = refPath + dentry.getPath();
//                byte[] data = IOUtils.toByteArray(new URI(realPath));
//                int index = dentry.getPath().indexOf(oldCoursewareId + ".pkg");
//                String subpath = index == -1 ? dentry.getPath() : dentry.getPath().substring(
//                        index + (oldCoursewareId + ".pkg").length() + 1);
//                if (subpath.equals(dentry.getName())) {
//                    subpath = "";
//                }
//                String copyPath = interceptPath(subpath, dentry.getName()).replace(oldCoursewareId,
//                        descUploadResponse.getUuid());
//                // 上传.
//                packageService.UploadFileToCS(data, copyPath, dentry.getName(), descSession, Constant.CS_EDU_DOMAIN_API);
//                /*
//                 * pageDao.saveFile(descUploadResponse, interceptPath(subpath,
//                 * dentry.getName()).replace(oldCoursewareId, descUploadResponse.getUuid()), dentry.getName(), data,
//                 * true);
//                 */
//                LOG.info("拷贝[" + dentry.getPath() + "]至[" + copyPath + "]完成");
//            }
//
//        }
//
//    }

    private String interceptPath(String path, String name) {
        String str = path;
        String folder = org.apache.commons.lang3.StringUtils.substring(str, 0, str.length() - name.length() - 1);
        if (folder.endsWith("/")) {
            folder = folder.substring(0, folder.length() - 1);
        }
        return folder;
    }

    public static void main(String[] args) throws IOException {
        String p = "/edu/esp/coursewares";
        String c = "/edu/esp/coursewares/aa.pkg";
        System.out.println(c.substring(c.lastIndexOf("/") + 1));
        CoursewareModel newCoursewareModel = new CoursewareModel();
        newCoursewareModel.setCreateTime(new Date());
        JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
        System.out.println(mapper.writeValueAsString(newCoursewareModel));

    }
}
