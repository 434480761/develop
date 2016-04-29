package nd.esp.service.lifecycle.support.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @title mysql数据库日志工具类
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/8 16:39
 */
public class DBLogUtil {

    private final static Logger DB_LOG= LoggerFactory.getLogger("DBlog");




    private DBLogUtil(){}

    /**
     * 获取数据库日志
     *
     * @return Logger
     */
    public static Logger getDBlog(){
       // throw new ArithmeticException("先不开放使用啦");
        return DB_LOG;

    }
}
