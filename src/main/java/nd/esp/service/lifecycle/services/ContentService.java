package nd.esp.service.lifecycle.services;

import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.entity.cs.Dentry;
import nd.esp.service.lifecycle.entity.cs.DentryArray;


/**
 * @title 内容服务接口
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月12日 上午10:06:43
 */
public interface ContentService {
    
    
    /** 
     * @desc:创建目录  
     * @createtime: 2015年6月12日 
     * @author: liuwx 
     * @param path
     * @param dirname
     * @param sessionId
     * @return
     */
    public Dentry createDir(String path, String dirname, String sessionId);
    
    
    /** 
     * @desc:  获取目录项的信息
     * @createtime: 2015年6月12日 
     * @author: liuwx 
     * @param path
     * @param sessionId
     * @return
     */
    public DentryArray getDentryItems(String path, String sessionId);
    
    
    
    /** 
     * @desc:拷贝目录  
     * @createtime: 2015年6月12日 
     * @author: liuwx 
     * @param srcPath 源目录
     * @param descPath 目标目录
     * @param sessionId session 必须是能操作上述两个目录的权限
     * @return
     */
    public boolean copyDir(String srcPath,String descPath,String sessionId);
    
    
    
    /** 
     * @desc:获取目录信息  
     * @createtime: 2015年6月12日 
     * @author: liuwx 
     * @param path
     * @param sessionId
     * @return
     */
    public  Dentry  getDentry(String path, String sessionId);
    
    
    
    /** 
     * @desc:获得顶层的实例session  
     * @createtime: 2015年6月12日 
     * @author: liuwx 
     * @return
     */
    //public String getTopSession();
    
    
    /**	
     * @desc: 获取指定目录的path 
     * @createtime: 2015年6月30日 
     * @author: liuwx 
     * @param path
     * @param arg 可以指定uid,失效时间等 arg[0] uid,arg[1] role,arg[2] expires 
     * @return
     */
    public CsSession getAssignSession(String path,String serviceId,String...arg);
    
    
}
