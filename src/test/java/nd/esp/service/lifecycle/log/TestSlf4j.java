package nd.esp.service.lifecycle.log;

import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @title what to do .
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/7 13:04
 */
public class TestSlf4j {
    private final static Logger LOG= LoggerFactory.getLogger(TestSlf4j.class);
    private final static Logger PF_LOG= LoggerFactory.getLogger("PFlog");
    public static void test(){
        PF_LOG.info("性能日志记录");
        MDC.put("resource", "uuid");
        MDC.put("res_type", "coursewares");
        MDC.put("operation_type", "测试需要");
        MDC.put("remark", "备注说明");
        //NDC.push("130307");
        LOG.info("hello");
        //注意MDC.getContext()返回有可能是null
        //MDC.getContext().clear();
        MDC.clear();
    }
    public static void main(String[] args) {
        System.out.println(ClassLoader.getSystemResource("log4j.properties").getPath());
        PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
        test();
        //自定义
      /*  Properties properties=new Properties();

        properties.setProperty("log4j.rootLogger", "INFO, CONSOLE");
        properties.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        properties.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%d{HH:mm:ss,SSS} [%t] %-5p %C{1} : %m%n");
        PropertyConfigurator.configure(properties);
        test();*/


        //PerformanceLogUtil.getPfLog().info("测试");


        //DBLogUtil.getDBlog().info("数据库操作");
    }
}
