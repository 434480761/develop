package com.nd.esp.task.worker.buss.document_transcode.entity;

/**
 * @title 复制资源状态对象
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午8:52:12
 */
public enum CopyResourceStatus {
    
    
    COPY_NO_CONDITION("0","拷贝不具备条件"),
    COPY_START  ("1","拷贝开始"),
    COPY_SUCCESS("2","拷贝成功"),
    COPY_FAIL("3","拷贝失败");
    
    
    private String code;
    
    private String message;
    CopyResourceStatus(String code,String message){
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
        
        return code+"["+message+"]";
    }
    
    
    

}
