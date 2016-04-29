package com.nd.esp.task.worker.buss.media_transcode.service;

import com.nd.esp.task.worker.buss.media_transcode.model.ResourceModel;

/**
 * @title 资源相关接口
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午8:07:47
 */
//todo  需要引入一个默认实现 所有的service需要继承这个默认实现,减少通用方法的操作
public interface ResourceService<T extends ResourceModel> {
    
    /**	
     * @desc:  创建资源
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param model
     * @return
     */
    public T  create(T model);
    
    /**	
     * @desc:  获取资源
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param identifier
     * @return
     */
    public T get(String identifier);
    
    
    /**	
     * @desc: 修改资源
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param model
     * @return
     */
    public T update(T model);
    
    
    /**	
     * @desc:  删除资源
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param model
     * @return
     */
    public boolean delete(T model);

}
