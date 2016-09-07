package nd.esp.service.lifecycle.services.impls;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.entity.cs.Dentry;
import nd.esp.service.lifecycle.entity.cs.DentryArray;
import nd.esp.service.lifecycle.services.ContentService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.ConnectionPoolUtil;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.nd.gaea.client.http.BearerAuthorizationProvider;
import com.nd.gaea.client.http.WafSecurityHttpClient;
import com.nd.gaea.client.support.DeliverBearerAuthorizationProvider;

@Service("contentService")
public class ContentServiceImpl implements ContentService {
	private static final Logger LOG = LoggerFactory.getLogger(ContentServiceImpl.class);
	private BearerAuthorizationProvider authorProvider = new DeliverBearerAuthorizationProvider();
	private static CloseableHttpClient httpClient = ConnectionPoolUtil.getHttpClient();
	

    @Override
    public Dentry createDir(String path, String dirname, String sessionId) {
        String url = Constant.CS_API_URL + "/dentries?session=" + sessionId;
        Dentry req = new Dentry();
        req.setPath(path);
        req.setName(dirname);
        req.setScope(Dentry.SCOPE_PUBLIC);
        
        Dentry resp = null;
        CloseableHttpResponse response = null;
        
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader("Authorization", authorProvider.getAuthorization());
            EntityBuilder builder = EntityBuilder.create();
            builder.setText(ObjectUtils.toJson(req));
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
                        resp = BeanMapperUtils.mapperOnString(rtBody, Dentry.class);
                    }
                } finally {
                    if (in != null)
                        in.close();
                }
            }
            if(statusLine == null || statusLine.getStatusCode()!=201) {
                LOG.error("资源拷贝出错，调用CS创建目录失败");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + rtBody);
            }
        } catch (Exception e) {
            //处理异常
            LOG.error("资源拷贝出错，调用CS创建目录失败:", e);
        } finally {
            if(response != null) { 
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    LOG.error("关闭连接错误:", e);
                }
            }
        }
                
/*
        Dentry req = new Dentry();
        req.setPath(path);
        req.setName(dirname);
        req.setScope(Dentry.SCOPE_PUBLIC);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Dentry> entity = new HttpEntity<Dentry>(req, headers);
        /*
         * HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.MULTIPART_FORM_DATA);
         */
//        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//        Dentry resp = wafSecurityHttpClient.executeForObject(Constant.CS_API_URL + "/dentries?session={session}", HttpMethod.POST, entity,  Dentry.class,sessionId);
       // ResponseEntity<Dentry> resp = wafSecurityHttpClient.postForEntity(Constant.CS_API_URL + "/dentries?session={session}", entity, Dentry.class,
                //sessionId);
       // RestTemplate template = new RestTemplate();
        /*Map map = template.postForObject(Constant.CS_API_URL + "/dentries?session={session}", entity, Map.class,
                sessionId);*/
        //Dentry resp = new Dentry();
        return resp;
    }

    @Override
    public DentryArray getDentryItems(String path, String sessionId) {
        // TODO Auto-generated method stub
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient(Constant.WAF_CLIENT_RETRY_COUNT);
        DentryArray result = wafSecurityHttpClient.getForObject(Constant.CS_API_URL
                + "/dentries?path={path}&session={sessionId}&$limit=999", DentryArray.class, path, sessionId);

        return result;
    }

    @Override
    public boolean copyDir(String srcPath, String descPath, String sessionId) {

        Dentry srcDentry = getDentry(srcPath, sessionId);
        Dentry descDentry = getDentry(descPath, sessionId);
        DentryArray dentryArray = getDentryItems(srcPath, sessionId);
        List<Dentry> dentries = dentryArray.getItems();
        List dentry_ids = new ArrayList();
        if (CollectionUtils.isNotEmpty(dentries)) {
            for (Dentry dentry : dentries) {
                dentry_ids.add(dentry.getDentryId());
            }
        }
        boolean copyFlag = true;
        CloseableHttpResponse response = null;
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            String url = Constant.CS_API_URL + "/dentries/actions/copy?session=" + sessionId;
            requestBody.put("dst_dentry_id", descDentry.getDentryId());
            requestBody.put("parent_id", srcDentry.getDentryId());
            requestBody.put("dentry_ids", dentry_ids.toArray());
            
            HttpPatch httpPatch = new HttpPatch(url);
            httpPatch.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
            httpPatch.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            httpPatch.addHeader("Authorization", authorProvider.getAuthorization());
            EntityBuilder builder = EntityBuilder.create();
            builder.setText(ObjectUtils.toJson(requestBody));
            builder.setContentType(ContentType.APPLICATION_JSON);
            HttpEntity reqEntity = builder.build();

            httpPatch.setEntity(reqEntity);
            response = httpClient.execute(httpPatch);

        } catch (Exception e) {
            LOG.error("调用CS拷贝失败:" + e.getMessage());
            copyFlag = false;
        } finally {
            if(response != null) { 
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    LOG.error("关闭连接错误:", e);
                }
            }
        }
        return copyFlag;
    }

    @Override
    public Dentry getDentry(String path, String sessionId) {
        Dentry result = new Dentry();
        Map<String, Object> requestBody = new HashMap<String, Object>();
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        String url = Constant.CS_API_URL + "/dentries?session=" + sessionId;
        String paths[] = { path };
        requestBody.put("paths", paths);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // httpHeaders.set("x-http-method-override", "PATCH ");
        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<Map<String, Object>>(requestBody, httpHeaders);
        // Map<String, List<Map>> re = wafSecurityHttpClient.getRestTemplate().postForObject(url, entity, Map.class);
        Map<String, List<Map>> re = wafSecurityHttpClient.executeForObject(url, HttpMethod.PATCH, entity, Map.class);
        // Map<String, List<Map>> re = wafSecurityHttpClient.exchange(url, HttpMethod.PATCH, requestBody, Map.class);
        if (CollectionUtils.isNotEmpty(re)) {
            String dentryId = re.get("items").get(0).get("dentry_id").toString();
            result.setDentryId(dentryId);
        }
        return result;
    }

    /**
     * 
     * arg[0] uid,arg[1] role,arg[2] expires
     * */

    @Override
    public CsSession getAssignSession(String path, String serviceId, String... arg) {
        Assert.assertNotNull("path不能为空", path);
        Assert.assertNotNull("serviceId不能为空", serviceId);
        // todo 未去校验path的合法性 暂时交给cs去控制
        Map<String, Object> requestBody = CsSession.getDefaultSessionParam();
        requestBody.put("path", path);
        requestBody.put("service_id", serviceId);
        extraSessionParam(requestBody, arg);
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        String url = Constant.CS_API_URL + "/sessions";
        CsSession session = wafSecurityHttpClient.postForObject(url, requestBody, CsSession.class);
        LOG.info(String.format("获取到的session:[%s],对应的path:[%s]", session.getSession(), session.getPath()));
        return session;
    }

    /**	
     * @desc: session额外参数配置 
     * @createtime: 2015年7月1日 
     * @author: liuwx 
     * @param requestBody
     * @param arg
     */
    private void extraSessionParam(Map<String, Object> requestBody, String... arg) {
        switch (arg.length) {
        case 1:
            requestBody.put("uid", arg[0]);
            break;
        case 2:
            requestBody.put("uid", arg[0]);
            requestBody.put("role", arg[1]);
            break;
        case 3:
            requestBody.put("uid", arg[0]);
            requestBody.put("role", arg[1]);
            requestBody.put("expires", arg[2]);
            break;

        default:
            break;
        }
    }
}
