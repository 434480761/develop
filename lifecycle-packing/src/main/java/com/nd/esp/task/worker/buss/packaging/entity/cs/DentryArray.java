/**
 * 
 */
package com.nd.esp.task.worker.buss.packaging.entity.cs;

import java.io.Serializable;
import java.util.List;

/**
 * @title cs的目录数组
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午10:09:58
 */
public class DentryArray implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private List<Dentry> items;

    /**
     * @return the items
     */
    public List<Dentry> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Dentry> items) {
        this.items = items;
    }
    
}
