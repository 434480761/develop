package nd.esp.service.lifecycle.educommon.services.titanV07;

import com.nd.gaea.client.http.WafSecurityHttpClient;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResRepoInfo;
import nd.esp.service.lifecycle.repository.sdk.ResRepoInfoRepository;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.services.titan.TitanResultParse2;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import nd.esp.service.lifecycle.vos.statics.ResRepositoryConstant;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by liuran on 2016/8/1.
 */
@Repository
public class NDResourceTitanServiceImpl implements NDResourceTitanService {
    private final static Logger LOG = LoggerFactory.getLogger(NDResourceTitanServiceImpl.class);

    //默认路径key
    private static final String DEFAULT_LOCATION_KEY="href";
    private static final String PPT_LOCATION_KEY = "$ppt2html$";
    private static final int ND_AND_PERSON_ROOT_PATH_LENGTH=2;
    private static final String ND_AND_PERSON_DEFAUL_ORG = "esp";
    private static final int OTHER_ORG_ROOT_PATH_LENGTH=3;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private ResRepoInfoRepository resRepoInfoRepository;

    @Override
    public ResourceModel getDetail(String resourceType, String uuid, List<String> includeList, Boolean isAll) {
        Set<String> uuidSet = new HashSet<>();
        uuidSet.add(uuid);
        List<ResourceModel> modelList = batchGetDetail(resourceType,uuidSet,includeList,isAll);
        if(CollectionUtils.isEmpty(modelList)){
            LOG.info("never created");

            LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + " resourceType:" + resourceType
                    + " uuid:" + uuid);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                    LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                            + " resourceType:" + resourceType + " uuid:" + uuid);
        }

        return modelList.get(0);

    }

    @Override
    public List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList) {
        return batchGetDetail(resourceType, uuidSet, includeList ,false);
    }

    /**
     * CS文件上传 <br>
     * Created 2015年5月13日 下午4:06:25
     *
     * @param resourceType
     * @param uuid
     * @param uid
     * @param renew 是否续约
     * @return
     * @author linsm
     */
    @Override
    public AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew) {
        return getUploadUrl(resourceType, uuid, uid, renew, "");
    }

    @Override
    public AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew, String coverage) {
        String rootPath = "";
        // 逻辑校验 uuid,只能是已存在的资源或是none
        if (uuid.equals(Constant.DEFAULT_UPLOAD_URL_ID)) {
            uuid = UUID.randomUUID().toString();
            if(isPersonal(coverage)){
                //个人
                rootPath = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath();
            }else{
                rootPath = assertHasAuthorizationAndGetPath(coverage,uid);
            }

        } else {
            // 非续约，要判断是否存在对应的元数据
            if (!renew) {
                // check exist resource
                ResourceModel resourceModel = getDetail(resourceType, uuid, Arrays.asList(IncludesConstant.INCLUDE_TI),true);

                if (resourceModel == null) {
                    // 不存在对应的资源


                    LOG.error(LifeCircleErrorMessageMapper.CSResourceNotFound.getMessage());

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CSResourceNotFound);
                }
                String location = getLocation(resourceModel);
                rootPath = getRootPathFromLocation(location);
            } else {
                if(isPersonal(coverage)){
                    //个人
                    rootPath = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath();
                }else{
                    rootPath = assertHasAuthorizationAndGetPath(coverage,uid);
                }
            }

        }

        LOG.debug("各个组织cs根目录："+rootPath);

        Constant.CSInstanceInfo csInstance = getCsInstanceAccordingRootPath(rootPath); // 保证不为空;

        // 设置获取session参数
        CSGetSessionParam param = new CSGetSessionParam();
        param.setServiceId(csInstance.getServiceId());
        String path = producePath(rootPath, resourceType, uuid);

        LOG.debug("dis_path:"+path);

        String url = csInstance.getUrl() + "/sessions";
        // param.setPath(path);
        // 当包含其它组织时，与这个地址serviceName/esp 是并行的，导致没有权限
        // param.setPath(csInstance.getPath());// FIXME 应文鑫要求扩大授权范围
        // String sessionPath = csInstance.getPath().replace("/esp", ""); //暂时去除esp
        String sessionPath = org.apache.commons.lang3.StringUtils.removeEnd(csInstance.getPath(), "/esp");
        LOG.info("sessionPath:{}", sessionPath);

        param.setPath(sessionPath);
        param.setUid(uid);
        param.setRole(Constant.FILE_OPERATION_ROLE);
        param.setExpires(Constant.FILE_OPERATION_EXPIRETIME);

        // 获得session
        String sessionid = getSessionIdFromCS(url, param);

        LOG.debug("session:"+sessionid);

        // 设置返回的对象
        AccessModel accessModel = new AccessModel();
        accessModel.setAccessKey(Constant.FILE_OPERATION_ACCESSKEY);
        accessModel.setAccessUrl(csInstance.getUrl() + "/upload");
        accessModel.setPreview(new HashMap<String, String>());
        accessModel.setUuid(UUID.fromString(uuid));
        accessModel.setExpireTime(CommonHelper.fileOperationExpireDate());
        accessModel.setSessionId(sessionid);
        accessModel.setDistPath(path);

        return accessModel;
    }

    @Override
    public AccessModel getDownloadUrl(String resourceType, String uuid, String uid, String key) {
        Constant.CSInstanceInfo csInstanceInfo = null;
        // 逻辑校验 uuid,只能是已存在的资源
        // check exist resource
        ResourceModel resourceModel = getDetail(resourceType, uuid, Arrays.asList(IncludesConstant.INCLUDE_TI),true);
        if (resourceModel == null) {
            // 不存在对应的资源

            LOG.error(LifeCircleErrorMessageMapper.CSResourceNotFound.getMessage());

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSResourceNotFound);
        }
        // 获取到目录
        String location = "";
        boolean isPpt2Html = PPT_LOCATION_KEY.equals(key)&&"coursewares".equals(resourceType);
        if (org.apache.commons.lang3.StringUtils.isEmpty(key)||isPpt2Html) {
            //都使用转码后的实例
            location = getLocation(resourceModel);
        } else {
            location = getLocation(resourceModel, key);
        }

        String rootPath = getRootPathFromLocation(location);
        csInstanceInfo = getCsInstanceAccordingRootPath(rootPath); // 保证不为空，若空会抛出异常
        if (isPpt2Html) {
            location = producePptLocation(rootPath, uuid);
        }
        // String path = producePath(rootPath, resourceType, uuid); //FIXME 到时需要再综合考虑特殊处理ppt2html

        CSGetSessionParam param = new CSGetSessionParam();
        // param.setPath(path); FIXME 需要特殊处理一下路径
        // param.setPath(csInstanceInfo.getPath());// FIXME 应文鑫要求扩大授权范围
        // 见上传
        String sessionPath = org.apache.commons.lang3.StringUtils.removeEnd(csInstanceInfo.getPath(), "/esp");
        LOG.info("sessionPath:{}", sessionPath);
        param.setPath(sessionPath);
        param.setServiceId(csInstanceInfo.getServiceId());
        param.setUid(uid);
        param.setRole(Constant.FILE_OPERATION_ROLE);
        param.setExpires(Constant.FILE_OPERATION_EXPIRETIME);

        // 获得session
        String sessionid = getSessionIdFromCS(csInstanceInfo.getUrl() + "/sessions", param);


        LOG.debug("session:"+sessionid);

        AccessModel accessModel = new AccessModel();
        accessModel.setAccessKey(Constant.FILE_OPERATION_ACCESSKEY);
        accessModel.setAccessMethod(RequestMethod.GET.toString());
        accessModel.setAccessUrl(location);
        accessModel.setPreview(new HashMap<String, String>());
        accessModel.setUuid(UUID.fromString(uuid));
        accessModel.setExpireTime(CommonHelper.fileOperationExpireDate());
        accessModel.setSessionId(sessionid);

        return accessModel;
    }


    /**
     *
     * @param rootPath
     * @return
     * @since
     */
    public static Constant.CSInstanceInfo getCsInstanceAccordingRootPath(String rootPath) {
        Constant.CSInstanceInfo csInfo = null;
        if (org.apache.commons.lang3.StringUtils.isEmpty(rootPath)) {
            // 抛出物理地址错误

            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                    LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);
        }
        String[] pathChunk = rootPath.split("/");
        if (pathChunk.length < 2) {
            // 抛出物理地址错误

            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                    LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);
        }
        String serviceName = pathChunk[1];

        //FIXME 后期需要修改，目前这种做法无法保证抛出正确的”错误信息“ （by lsm 2015.11.18)
        if (Constant.CS_DEFAULT_INSTANCE.contains(serviceName)) {
            csInfo = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE);
        }else if(Constant.CS_DEFAULT_INSTANCE_OTHER.contains(serviceName)){
            csInfo =  Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE_OTHER);
        }

        if (csInfo == null) {

            LOG.error(LifeCircleErrorMessageMapper.CSInstanceKeyNotFound.getMessage());

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSInstanceKeyNotFound); // 抛出异常
        }
        return csInfo;
    }

    /**
     *
     * @param uuid
     * @return
     * @since
     */
    private String producePptLocation(String root,String uuid) {
        StringBuilder stringBuilder = new StringBuilder("${ref-path}");
        stringBuilder.append(root).append("/assets/ppts/").append(uuid).append(".pkg");
        return stringBuilder.toString();
    }

    private String getLocation(ResourceModel resourceModel) {
        return getLocation(resourceModel, DEFAULT_LOCATION_KEY);
    }

    /**
     * @param root
     * @param resourceType
     * @param uuid
     * @return
     * @since
     */
    public static String producePath(String root, String resourceType, String uuid) {
        StringBuilder stringBuilder = new StringBuilder(root);
        stringBuilder.append("/").append(resourceType).append("/").append(uuid).append(".pkg");
        return stringBuilder.toString();
    }

    /**
     * 调用cs接口获取 session <br>
     * Created 2015年5月13日 下午4:07:34
     *
     * @param param post 请求的相关参数
     * @return session
     * @author linsm
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    private String getSessionIdFromCS(String url, CSGetSessionParam param) {
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("path", param.getPath());
        requestBody.put("service_id", param.getServiceId());
        //FIXME uid 在lcms中是String， 在cs中是long,可能会存在问题
        requestBody.put("uid", param.getUid());
        requestBody.put("role", param.getRole());
        if (param.getExpires() != null) {
            requestBody.put("expires", param.getExpires());// optional
        }

        LOG.debug("调用cs接口："+url);

        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        String sessionId="";
        try {
            Map<String, String> session = wafSecurityHttpClient.post(url, requestBody, Map.class);
            sessionId = session.get("session");
        } catch (Exception e) {
            LOG.error(LifeCircleErrorMessageMapper.InvokingCSFail.getMessage(),e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.InvokingCSFail.getCode(),e.getLocalizedMessage());
        }
        return sessionId;
    }

    /**
     * @param resourceModel
     * @param defaultLocationKey
     * @return
     * @since
     */
    private String getLocation(ResourceModel resourceModel, String defaultLocationKey) {
        Assert.assertNotNull(resourceModel);
        String location = "";

        List<ResTechInfoModel> techInfoModels = resourceModel.getTechInfoList();
        if (CollectionUtils.isNotEmpty(techInfoModels)) {
            for (ResTechInfoModel model : techInfoModels) {
                if (model != null && defaultLocationKey.equals(model.getTitle())) {
                    location =model.getLocation();
                    break;
                }
            }
        }

        if (org.apache.commons.lang3.StringUtils.isEmpty(location)) {
            // 抛出异常，没有对应key的地址

            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + "不存在对应于key = "+defaultLocationKey+"的地址");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                    LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + "不存在对应于key = "+defaultLocationKey+"的地址");
        }
        return location;
    }

    public static String getRootPathFromLocation(String location) {
        //${ref-path}/edu/esp/questions/b129f99d-464f-4562-ada1-1a8fff50e981.pkg/item.xml
        //暂时通过是否包含“esp"来判断目录结构（若有esp则为两层，个人，nd,若无，则是组织)
        String[] pathChunk = location.split("/");
        if (pathChunk.length < ND_AND_PERSON_ROOT_PATH_LENGTH + 1) {
            // 抛出目录结构异常

            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                    LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);
        }
        StringBuilder builder = new StringBuilder();
        if (ND_AND_PERSON_DEFAUL_ORG.equals(pathChunk[2])) {
            // 个人或者是nd
            builder.append("/").append(pathChunk[1]).append("/").append(pathChunk[2]);
        } else {
            // 其它组织
            if (pathChunk.length < OTHER_ORG_ROOT_PATH_LENGTH + 1) {
                // 抛出目录结构异常

                LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                        LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);
            }
            builder.append("/").append(pathChunk[1]).append("/").append(pathChunk[2]).append("/").append(pathChunk[3]);
        }
        return builder.toString();
    }

    /**
     * 上传接口，判断是否为个人
     * @author linsm
     * @param coverage
     * @return
     * @since
     */
    private boolean isPersonal(String coverage) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(coverage)){
            return true;
        }

        if(org.apache.commons.lang3.StringUtils.startsWith(coverage, "User")){
            return true;
        }
        return false;
    }

    /**
     * 校验是否授权，检查物理空间是否已申请 更新：2015.10.26 (当没有申请时，自动申请物理空间),添加参数uid
     *
     * @author linsm
     * @param coverage
     * @since
     */
    private String assertHasAuthorizationAndGetPath(String coverage, String uid) {
        String[] coverageChunk = coverage.split("/");
        //update 2015.12.11 放开长度的要求，改成只要不少于2段
        if (coverageChunk == null || coverageChunk.length < 2) {
            // 抛出异常

            LOG.error(LifeCircleErrorMessageMapper.CSUploadCoverageError.getMessage());

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CSUploadCoverageError);
        }

        ResRepoInfo repoInfo = new ResRepoInfo();
        repoInfo.setTargetType(coverageChunk[0]);
        repoInfo.setTarget(coverageChunk[1]);
        repoInfo.setEnable(null);

        try {
            repoInfo = resRepoInfoRepository.getByExample(repoInfo);
        } catch (EspStoreException e) {

            LOG.error("获取物理空间信息失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetRepositoryFail);
        }

        // 将物理空间不存在时抛出未授权，改为申请物理空间
        // if (repoInfo == null || !repoInfo.getEnable()) {
        //
        // LOG.error(LifeCircleErrorMessageMapper.CSUploadCoverageNotExist.getMessage());
        //
        // throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
        // LifeCircleErrorMessageMapper.CSUploadCoverageNotExist);
        // }
        if (repoInfo == null) {
            // 未创建
            // 校验targetType,创建物理空间
            if (!ResRepositoryConstant.isRepositoryTargetType(coverageChunk[0])) {

                LOG.error("targetType不在可选范围内-targetType取值范围为:Org,Group");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.TargetTypeIsNotExist);
            }

            repoInfo = new ResRepoInfo();

            repoInfo.setIdentifier(UUID.randomUUID().toString());
            repoInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            repoInfo.setEnable(true);
            repoInfo.setStatus(ResRepositoryConstant.STATUS_RUNNING);
            repoInfo.setRepositoryAdmin(uid);
            repoInfo.setRepositoryName(coverage);
            repoInfo.setDescription(coverage);
            repoInfo.setTargetType(coverageChunk[0]);
            repoInfo.setTarget(coverageChunk[1]);
            repoInfo.setTitle(coverage);
            repoInfo.setRepositoryPath(Constant.CS_SESSION_PATH + "/" + coverage);

            try {
                repoInfo = resRepoInfoRepository.add(repoInfo);
            } catch (EspStoreException e) {

                LOG.error("申请物理资源存储空间失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CreateRepositoryFail);
            }

            // 将创建记录写在日志中
            LOG.info("申请物理资源存储空间" + coverage);

        } else if (repoInfo.getEnable()==null||!repoInfo.getEnable()) {
            // 已创建，但被禁用
            // 启用
            repoInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            repoInfo.setEnable(true);
            repoInfo.setStatus(ResRepositoryConstant.STATUS_RUNNING);
            try {
                repoInfo = resRepoInfoRepository.update(repoInfo);
            } catch (EspStoreException e) {

                LOG.error("修改资源物理空间信息失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.UpdateRepositoryFail);
            }

            // 将启用记录写在日志中
            LOG.info("启用物理资源存储空间" + coverage);
        }

        if (repoInfo == null) {

            LOG.error("申请物理资源存储空间失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateRepositoryFail);
        }

        return repoInfo.getRepositoryPath();

    }

    /**
     * cs获取 session 相关参数 <br>
     * Created 2015年5月13日 下午4:08:22
     *
     * @version CSFileUploadServiceImpl
     * @author linsm
     */
    private static class CSGetSessionParam {
        /**
         * 路径
         */
        private String path;
        /**
         * 服务id
         */
        private String serviceId;
        /**
         * 用户id
         */
        private String uid;
        /**
         * 角色
         */
        private String role;
        /**
         * 过期时间(秒)
         */
        private Integer expires;

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

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Integer getExpires() {
            return expires;
        }

        public void setExpires(Integer expires) {
            this.expires = expires;
        }
    }

    /**
     * 通过ID和资源类型从titan中批量获取数据
     * */
    private List<ResourceModel> batchGetDetail(String resourceType, Set<String> uuidSet, List<String> includeList, boolean isAll){
        List<ResourceModel> resourceModelList = new ArrayList<>();

        List<String> uuids = new ArrayList<>();
        uuids.addAll(uuidSet);
        //构造查询脚本
        Map<TitanScritpUtils.KeyWords, Object> resultScript = TitanScritpUtils.buildGetDetailScript(resourceType,uuids,includeList,isAll);
        String script = resultScript.get(TitanScritpUtils.KeyWords.script).toString();
        Map<String, Object> params = (Map<String, Object>) resultScript.get(TitanScritpUtils.KeyWords.params);
        ResultSet resultSet = null;
        try {
            resultSet = titanCommonRepository.executeScriptResultSet(script, params);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/TITAN", "submit script and has errors");
        }

        //分组查询返回结
        List<List<String>> resultList = new ArrayList<>();
        Iterator<Result> iterator = resultSet.iterator();
        List<String> resultTemp = new ArrayList<>();
        while (iterator.hasNext()) {
            String line = iterator.next().getString();
            Map<String, String> valueMap = TitanResultParse.toMap(line);
            if(valueMap.containsKey(ES_SearchField.lc_last_update.toString()) ||
                    valueMap.containsKey(ES_SearchField.lc_create_time.toString())){
                resultTemp = new ArrayList<>();
                resultList.add(resultTemp);
            }
            resultTemp.add(line);
        }

        //解析资源
        for (List<String> result : resultList) {
            Map<String, String> mainResult = null;
            List<Map<String, String>> otherLines = new ArrayList<>();
            String taxOnPath = null;
            for (String line : result) {
                Map<String, String> map = TitanResultParse2.toMap(line);

                if (map.containsKey(ES_SearchField.cg_taxonpath.toString())) {
                    taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
                } else if (map.containsKey(ES_SearchField.lc_create_time.toString())) {
                    mainResult = map;
                } else {
                    otherLines.add(map);
                }
            }
            if (CollectionUtils.isEmpty(mainResult)) {
                continue;
            }
            resourceModelList.add(TitanResultParse2.parseResource(
                    resourceType, mainResult, otherLines, taxOnPath, includeList));
        }


        //解析资源
        /*for (List<String> result : resultList){
            String mainResult = null;
            List<String> otherLines = new ArrayList<String>();
            String taxOnPath = null;
            for (String line : result) {
                if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
                    Map<String, String> map = TitanResultParse.toMap(line);
                    taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
                } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
                    mainResult = line;
                } else {
                    otherLines.add(line);
                }
            }
            if(StringUtils.isEmpty(mainResult)){
                continue;
            }
            resourceModelList.add(TitanResultParse.parseResource(
                    resourceType, mainResult, otherLines, taxOnPath,includeList));
        }*/

        return resourceModelList;
    }


    private ResourceModel getDetailOnlyOne(String resourceType, String uuid, List<String> includeList, Boolean isAll){
//        List<String> uuids = new ArrayList<>();
//        uuids.add(uuid);
//        Map<TitanScritpUtils.KeyWords, Object> resultScript = TitanScritpUtils.buildGetDetailScript(resourceType,uuids,includeList,isAll);
//        String script = resultScript.get(TitanScritpUtils.KeyWords.script).toString();
//        Map<String, Object> params = (Map<String, Object>) resultScript.get(TitanScritpUtils.KeyWords.params);
//        System.out.println(script);
//        ResultSet resultSet = null;
//        try {
//            resultSet = titanCommonRepository.executeScriptResultSet(script, params);
//        } catch (Exception e) {
//            LOG.error(e.getLocalizedMessage());
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "LC/TITAN", "submit script and has errors");
//        }
//
//        List<String> result = new ArrayList<String>();
//        Iterator<Result> iterator = resultSet.iterator();
//        while (iterator.hasNext()) {
//            result.add(iterator.next().getString());
//        }
//
//        String mainResult = null;
//        List<String> otherLines = new ArrayList<String>();
//        String taxOnPath = null;
//        for (String line : result) {
//            if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
//                Map<String, String> map = TitanResultParse.toMap(line);
//                taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
//            } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
//                System.out.println(line);
//                mainResult = line;
//            } else {
//                otherLines.add(line);
//            }
//        }
//        if(StringUtils.isEmpty(mainResult)){
//            LOG.info("never created");
//
//            LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + " resourceType:" + resourceType
//                    + " uuid:" + uuid);
//
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
//                    LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
//                            + " resourceType:" + resourceType + " uuid:" + uuid);
//        }
//        return TitanResultParse.parseResource(
//                resourceType, mainResult, otherLines, taxOnPath);
        return null;
    }
}
