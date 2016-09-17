/**
 * 
 */
package nd.esp.service.lifecycle.repository.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:静态获取spring bean<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年5月20日<br>
 * 修改人:<br>
 * 修改时间:2015年5月20日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */


public class SpringContextHolder implements ApplicationContextAware {

	/**
	 * 以静态变量保存ApplicationContext,可在任意代码中取出ApplicaitonContext.
	 */
	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		SpringContextHolder.context = context;
	}

	public static ApplicationContext getApplicationContext() {
		return context;
	}

	/**
	 * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	public static <T> T getBean(String name) {
		return (T) context.getBean(name);
	}

}
