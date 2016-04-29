package nd.esp.service.lifecycle.log;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Enumeration;

/**
 * @title 测试log4j的配置文件输出
 * @desc
 * @atuh lwx
 * @createtime on 2015/8/26 16:04
 */
public class TestLog4j {


    public static void main(String[] args) {
        PropertyConfigurator.configure(TestLog4j.class.getResourceAsStream("/log4j.properties"));
        Logger logger  =  Logger.getLogger(TestLog4j.class);
        Enumeration<Appender> enumerations= logger.getParent().getAllAppenders();
        while (enumerations.hasMoreElements()){
            System.out.println("输出定义的Appender:"+enumerations.nextElement().getName());
        }
        logger.info("测试info级别日志输出");
        logger.warn("测试warn级别日志输出");
        logger.error("测试error级别日志输出");

    }
}
