/* =============================================================
 * Created: [2015年11月27日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.busi.tree.preorder;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;


/**
 * @author linsm
 * @since
 */
public enum TreeDirection {

    pre, next;
    
    static final private Map<String, TreeDirection> stringToTreeDirectionMap = new HashMap<String, TreeDirection>();
    static{
        for(TreeDirection treeDirection:TreeDirection.values()){
            stringToTreeDirectionMap.put(treeDirection.toString(), treeDirection);
        }
    }
    
    static public TreeDirection fromString(String string){
        TreeDirection treeDirection = stringToTreeDirectionMap.get(string);
        if(treeDirection == null){
            throw new LifeCircleException(LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),"not support the tree direction: "+string);
        }
        return treeDirection;
    }

}
