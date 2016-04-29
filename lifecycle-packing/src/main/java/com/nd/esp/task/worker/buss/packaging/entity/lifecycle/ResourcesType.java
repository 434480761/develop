package com.nd.esp.task.worker.buss.packaging.entity.lifecycle;

/**
 * @title 资源类型
 * @desc
 * @atuh lwx
 * @createtime on 2015年7月06日 下午21:52:12
 */
public enum ResourcesType {
    
    
    COURSEWARES("1","coursewares");
    
    
    
    private String code;
    
    private String message;
    ResourcesType(String code,String message){
        this.code=code;
        this.message=message;
    }
    
    
    
    public String getCode() {
        return code;
    }



    public String getMessage() {
        return message;
    }



    @Override
    public String toString() {
        
        return message;
    }
    
    
    

}
