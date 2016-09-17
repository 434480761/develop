package nd.esp.service.lifecycle.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @title Spring上下文对象工具类
 *   &#064;RestController
 *   &#064;RequestMapping("/url")
 *  * public class MyController {
 *     @RequestMapping(method = RequestMethod.GET)
 *     public void getSpringContext() {
 *         SpringContextUtil.getBean("beanName");//beanName是LC项目中可以被spring扫描的bean
 *     }
 * @desc 用于在controller,service等对象中获取spring上下文对象
 * <p>也可以通过AnnotationConfigApplicationContext 来获取</p>
 * @atuh lwx
 * @createtime on 2015年8月14日 下午10:11:39
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;     //Spring应用上下文环境
   
    /**
    * 实现ApplicationContextAware接口的回调方法，设置上下文环境   
    * @param applicationContext
    * @throws BeansException
    */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      SpringContextUtil.applicationContext = applicationContext;
    }
   
    /**
    * @return ApplicationContext
    */
    public static ApplicationContext getApplicationContext() {
      return applicationContext;
    }
   
    /**
    * 获取对象   
    * @param name
    * @return Object 一个以所给名字注册的bean的实例
    * @throws BeansException
    */
    public static Object getBean(String name) throws BeansException {
      return applicationContext.getBean(name);
    }
   
    /**
    * 获取类型为requiredType的对象
    * 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
    * @param name       bean注册名
    * @param requiredType 返回对象类型
    * @return Object 返回requiredType类型对象
    * @throws BeansException
    */
    public static Object getBean(String name, Class requiredType) throws BeansException {
      return applicationContext.getBean(name, requiredType);
    }
   
    /**
    * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true 
    * @param name
    * @return boolean
    */
    public static boolean containsBean(String name) {
      return applicationContext.containsBean(name);
    }
   
    /**
    * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
    * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）   
    * @param name
    * @return boolean
    * @throws NoSuchBeanDefinitionException
    */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
      return applicationContext.isSingleton(name);
    }
   
    /**
    * @param name
    * @return Class 注册对象的类型
    * @throws NoSuchBeanDefinitionException
    */
    public static Class getType(String name) throws NoSuchBeanDefinitionException {
      return applicationContext.getType(name);
    }
   
    /**
    * 如果给定的bean名字在bean定义中有别名，则返回这些别名   
    * @param name
    * @return
    * @throws NoSuchBeanDefinitionException
    */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
      return applicationContext.getAliases(name);
    }
  }