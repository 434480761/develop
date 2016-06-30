package nd.esp.service.lifecycle.support;

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
		
		// /v0.3/{res_type} POST
		m.put("/(\\w+)" + "/POST", "");              
		//m.put("/(\\w+)/(\\w+)" + "/POST", "");
		
		// /v0.3/{res_type} PUT
		m.put("/(\\w+)" + "/PUT", "");    
		//m.put("/(\\w+)/(\\w+)/actions/move" + "/PUT", "");   
		//m.put("/(\\w+)/(\\w+)/chapters/(\\w+)" + "/PUT", ""); 
		//m.put("/(\\w+)/(\\w+)/chapters/(\\w+)/actions/move" + "/PUT", ""); 
		
		// /v0.3/{res_type} GET
		m.put("/(\\w+)" + "/GET", "");   
		// /v0.3/{res_type}/list GET
        m.put("/(\\w+)/list" + "/GET", "");   
        // /v0.3/{res_type}/*/archive POST
       	m.put("/(\\w+)/(\\w+)/archive" + "/POST", ""); 
       	// /v0.3/{res_type}/*/archiveinfo GET
       	m.put("/(\\w+)/(\\w+)/archiveinfo" + "/GET", ""); 
       	// /v0.3/{res_type}/*/uploadurl GET
       	m.put("/(\\w+)/(\\w+)/uploadurl" + "/GET", ""); 
       	// /v0.3/{res_type}/*/downloadurl GET
       	m.put("/(\\w+)/(\\w+)/downloadurl" + "/GET", "");
       	// /v0.3/{res_type}/*/relations POST
		m.put("/(\\w+)/(\\w+)/relations" + "/POST", ""); 
		// /v0.3/{res_type}/*/relations/* PUT
		m.put("/(\\w+)/(\\w+)/relations/(\\w+)" + "/PUT", ""); 
		// /v0.3/{res_type}/*/relations DELETE
		m.put("/(\\w+)/(\\w+)/relations" + "/DELETE", "");
		// /v0.3/{res_type}/*/relations GET
		// /v0.3/{res_type}/*/targets GET
		m.put("/(\\w+)/(\\w+)/targets" + "/GET", "");
		// /v0.3/{res_type}/*/targets/* GET
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
        m.put("/categories/(\\w+)" + "/PUT", "");
        m.put("/categories/(\\w+)" + "/DELETE", "");   
        m.put("/categories/list" + "/GET", "");              
        //m.put("/categories" + "/GET", "");  
        
        // /v0.3/categorypatterns/*
        //m.put("/categorypatterns" + "/POST", "");
        m.put("/categorypatterns/(\\w+)" + "/PUT", "");
        m.put("/categorypatterns/(\\w+)" + "/DELETE", "");
        //m.put("/categorypatterns" + "/GET", "");
        m.put("/categorypatterns/list" + "/GET", "");
        m.put("/categorypatterns/(\\w+)" + "/GET", "");
        
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
        // /v0.3/{res_type}/list
        m.put("/(\\w+)/list" + "/GET", "");   
        // /v0.3/{res_type}/*/archive POST
        m.put("/(\\w+)/(\\w+)/archive" + "/POST", "");  
        // /v0.3/{res_type}/*/archiveinfo GET
        m.put("/(\\w+)/(\\w+)/archiveinfo" + "/GET", ""); 
        // /v0.3/{res_type}/*/downloadurl GET
        m.put("/(\\w+)/(\\w+)/downloadurl" + "/GET", "");
        // /v0.3/{res_type}/*/targets GET
        m.put("/(\\w+)/(\\w+)/targets" + "/GET", ""); 
        return m;
    }   
}