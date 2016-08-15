package nd.esp.service.lifecycle.support;

import nd.esp.service.lifecycle.support.enums.ResTypeEnum;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>Title: RoleResFilterUrlMap         </p>
 * <p>Description: RoleResFilterUrlMap </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年06月30日           </p>
 * @author lianggz
 */
public class RoleResFilterUrlMap {
	
    //---------------------------------------------------------------------
    // 库管理员--[CoverageAdmin]
    //---------------------------------------------------------------------
    
    /**
     * 库管理员过滤的URL
     * @return
     */
    public static final Map<String,String> getCoverageAdminMap(){
        Map<String,String> m = new LinkedHashMap<String,String>();
        m.putAll(getResCreatorMap());
        m.putAll(getCategoryDataAdminMap());
        m.putAll(getResConsumerMap());
        return m;
    }
    
    //---------------------------------------------------------------------
    // 资源创建者角色--[ResCreator]
    //---------------------------------------------------------------------
    
	/**
	 * 资源创建者角色过滤的URL
	 * @return
	 */
	public static final Map<String,String> getResCreatorMap(){
		Map<String,String> m = new LinkedHashMap<String,String>();

		for (ResTypeEnum e : ResTypeEnum.values()){
			// /v0.3/{res_type} POST
			m.put("/"+ e.getValue() + "/POST", "");
			// /v0.3/{res_type}/* POST
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)" + "/POST", "");
			// /v0.3/{res_type}/* PUT
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)" + "/PUT", "");
			// /v0.3/{res_type}/* GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)" + "/GET", "");
			// /v0.3/{res_type}/list GET
			m.put("/"+ e.getValue() + "/list" + "/GET", "");
			// /v0.3/{res_type}/*/archive POST
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/archive" + "/POST", "");
			// /v0.3/{res_type}/*/archiveinfo GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/archiveinfo" + "/GET", "");
			// /v0.3/{res_type}/*/uploadurl GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/uploadurl" + "/GET", "");
			// /v0.3/{res_type}/*/downloadurl GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/downloadurl" + "/GET", "");
			// /v0.3/{res_type}/*/relations POST
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/relations" + "/POST", "");
			// /v0.3/{res_type}/*/relations/* PUT
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/relations/(\\w+)" + "/PUT", "");
			// /v0.3/{res_type}/*/relations DELETE
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/relations" + "/DELETE", "");
			// /v0.3/{res_type}/*/relations/* DELETE
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/relations/([a-zA-Z0-9-]+)" + "/DELETE", "");
			// /v0.3/{res_type}/*/relations GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/relations" + "/GET", "");
			// /v0.3/{res_type}/*/targets GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/targets" + "/GET", "");
			// /v0.3/{res_type}/*/targets/* GET
			m.put("/"+ e.getValue() + "/([a-zA-Z0-9-]+)/targets/([a-zA-Z0-9-]+)" + "/GET", "");
		}
		return m;
	}
    
    //---------------------------------------------------------------------
    // 维度管理者角色--[CategoryDataAdmin]
    //---------------------------------------------------------------------
    
    /**
     * 维度管理者角色过滤的URL
     * @return
     */
    public static final Map<String,String> getCategoryDataAdminMap(){
        Map<String,String> m = new LinkedHashMap<String,String>();
        // /v0.3/categories/*
        //m.put("/categories" + "/POST", "");              
        m.put("/categories/([a-zA-Z0-9-]+)" + "/PUT", "");
        m.put("/categories/([a-zA-Z0-9-]+)" + "/DELETE", "");
        m.put("/categories/list" + "/GET", "");              
        //m.put("/categories" + "/GET", "");  
        
        // /v0.3/categorypatterns/*
        //m.put("/categorypatterns" + "/POST", "");
        m.put("/categorypatterns/([a-zA-Z0-9-]+)" + "/PUT", "");
        m.put("/categorypatterns/([a-zA-Z0-9-]+)" + "/DELETE", "");
        //m.put("/categorypatterns" + "/GET", "");
        m.put("/categorypatterns/list" + "/GET", "");
        m.put("/categorypatterns/([a-zA-Z0-9-]+)" + "/GET", "");
        
        // /v0.3/categories/ralations
        
        return m;
    }
    
    //---------------------------------------------------------------------
    // 资源消费者角色--[ResConsumer]
    //---------------------------------------------------------------------
    
    /**
     * 资源消费者角色过滤的URL
     * @return
     */
    public static final Map<String,String> getResConsumerMap(){
        Map<String,String> m = new LinkedHashMap<String,String>();
		for (ResTypeEnum e : ResTypeEnum.values()){
			// /v0.3/{res_type}/{uuid}
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)" + "/GET", "");
			// /v0.3/{res_type}/list
			m.put("/"+ e.getValue() +"/list" + "/GET", "");
			// /v0.3/{res_type}/*/archive POST
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)/archive" + "/POST", "");
			// /v0.3/{res_type}/*/archiveinfo GET
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)/archiveinfo" + "/GET", "");
			// /v0.3/{res_type}/*/downloadurl GET
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)/downloadurl" + "/GET", "");
			// /v0.3/{res_type}/*/targets GET
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)/targets" + "/GET", "");
			// /v0.3/{res_type}/*/relations GET
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)/relations" + "/GET", "");
			// /v0.6/{res_type}/*/targets/* GET
			m.put("/"+ e.getValue() +"/([a-zA-Z0-9-]+)/relations/([a-zA-Z0-9-]+)" + "/GET", "");
		}
        return m;
    }   
}