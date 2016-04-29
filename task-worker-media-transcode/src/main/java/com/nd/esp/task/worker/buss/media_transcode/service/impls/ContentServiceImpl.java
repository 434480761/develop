package com.nd.esp.task.worker.buss.media_transcode.service.impls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nd.esp.task.worker.buss.media_transcode.Constant;
import com.nd.esp.task.worker.buss.media_transcode.entity.cs.Dentry;
import com.nd.esp.task.worker.buss.media_transcode.entity.cs.DentryArray;
import com.nd.esp.task.worker.buss.media_transcode.service.ContentService;
import com.nd.esp.task.worker.buss.media_transcode.utils.CollectionUtils;
import com.nd.gaea.client.http.WafSecurityHttpClient;
@Service("contentService")
public class ContentServiceImpl implements ContentService{
    
    
private Log LOG=LogFactory.getLog(ContentServiceImpl.class);

    @Override
    public Dentry createDir(String path, String dirname, String sessionId) {

        Dentry req = new Dentry();
        req.setPath(path);
        req.setName(dirname);
        req.setScope(Dentry.SCOPE_PUBLIC);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Dentry>entity=new HttpEntity<Dentry>(req,headers);
        /*HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);*/
        RestTemplate template=new RestTemplate();
        Dentry resp = template.postForObject(Constant.CS_EDU_DOMAIN_API + "/dentries?session={session}", entity, Dentry.class, sessionId);
        return resp;
    }

    /*
     * (non-Javadoc)
     * @see com.nd.esp.editor.service.CsService#getDentryItems(java.lang.String, java.lang.String)
     */
    @Override
    public DentryArray getDentryItems(String path, String sessionId) {
        // TODO Auto-generated method stub
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        DentryArray result = wafSecurityHttpClient.get(Constant.CS_EDU_DOMAIN_API + "/dentries?path={path}&session={sessionId}&$limit=999",
                                                   DentryArray.class,
                                                   path,
                                                   sessionId);

        return result;
    }

    @Override
    public boolean copyDir(String srcPath,String descPath,String sessionId){
        
        Dentry srcDentry=getDentry(srcPath,sessionId);
        Dentry descDentry=getDentry(descPath,sessionId);
        DentryArray dentryArray=  getDentryItems(srcPath,sessionId);
        List<Dentry>dentries=dentryArray.getItems();
        List dentry_ids=new ArrayList();
        if(CollectionUtils.isNotEmpty(dentries)){
         for(Dentry dentry:dentries)   {
             dentry_ids.add(dentry.getDentryId());
         }
        }
        //单个目录拷贝,会重名
        //String url =Constant.CS_EDU_DOMAIN_API + "/dentries/{dentryId}/actions/copy?session={sessionId}";
       /* String url =Constant.CS_EDU_DOMAIN_API + "/dentries/actions/copy?session={sessionId}";
        RestTemplate template=new RestTemplate();
        Map<String, Object>map=new HashMap<String, Object>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map>entity=new HttpEntity<Map>(map,headers);
        map.put("dst_dentry_id", descDentry.getDentryId());
        map.put("parent_id", srcDentry.getDentryId());
        map.put("dentry_ids", dentry_ids.toArray());*/
        boolean copyFlag=true;
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
            String url =Constant.CS_EDU_DOMAIN_API + "/dentries/actions/copy?session="+sessionId;
            requestBody.put("dst_dentry_id", descDentry.getDentryId());
            requestBody.put("parent_id", srcDentry.getDentryId());
            requestBody.put("dentry_ids", dentry_ids.toArray());
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(requestBody, httpHeaders);
            
            wafSecurityHttpClient.executeForObject(url, HttpMethod.PATCH, entity, Map.class);
           // wafSecurityHttpClient.exchange( url,HttpMethod.PATCH, requestBody, Map.class,httpHeaders);
            
        } catch (Exception e) {
            LOG.error("调用CS拷贝失败:"+e.getMessage());
            copyFlag=false;
        }
        return copyFlag;
    }

    @Override
    public Dentry getDentry(String path, String sessionId) {
        Dentry result=new Dentry();
     /* CsRestTemplate template=new CsRestTemplate();
      String url =Constant.CS_EDU_DOMAIN_API + "/dentries?session={sessionId}";
      Map<String, Object>map=new HashMap<String, Object>();
      map.put("paths", new String[]{path});
     
      try {
          result=   template.patchForObject(url, Dentry.class, map,sessionId);
    } catch (Exception e) {
        // TODO: handle exception
    }*/
          
          Map<String, Object> requestBody = new HashMap<String, Object>();


          WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
          String url = Constant.CS_EDU_DOMAIN_API + "/dentries?session="+sessionId;

          String paths[]={path};

          requestBody.put("paths",paths);

          Map<String, List<Map>> re= wafSecurityHttpClient.exchange( url,HttpMethod.PATCH, requestBody, Map.class);
          if(CollectionUtils.isNotEmpty(re)){
              String dentryId= re.get("items").get(0).get("dentry_id").toString();
              result.setDentryId(dentryId);
          }
      return result;
      
      
      
      
      
      
    }

    @Override
    public String getTopSession() {
        RestTemplate template=new RestTemplate();
       return template.getForObject(Constant.LIFE_CYCLE_API_URL + "/coursewares/create_session/777",  String.class);
    }

}
