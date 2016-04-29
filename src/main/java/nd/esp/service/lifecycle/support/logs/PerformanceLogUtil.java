package nd.esp.service.lifecycle.support.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @title 性能日志工具类
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/8 16:39
 */
public class PerformanceLogUtil {

    private final static Logger PF_LOG= LoggerFactory.getLogger("PFlog");




    private  PerformanceLogUtil(){}

    /**
     * 获取性能日志
     *
     * @return Logger
     */
    public static Logger getPfLog(){

        return PF_LOG;

    }
}
