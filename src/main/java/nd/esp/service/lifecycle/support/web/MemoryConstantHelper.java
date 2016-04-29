package nd.esp.service.lifecycle.support.web;

import nd.esp.service.lifecycle.entity.log.BaseLogModel;

/**
 * @title  内存常量帮助类
 * @desc
 * @atuh lwx
 * @createtime on 2015/9/25 14:38
 */
public class MemoryConstantHelper {

    private BaseLogModel transCodeLog;


    private MemoryConstantHelper(){}

    public static MemoryConstantHelper getInstance( ){

        return new MemoryConstantHelper();
    }


    public BaseLogModel getTransCodeLog() {
        return transCodeLog;
    }

    public void setTransCodeLog(BaseLogModel transCodeLog) {
        this.transCodeLog = transCodeLog;
    }
}





