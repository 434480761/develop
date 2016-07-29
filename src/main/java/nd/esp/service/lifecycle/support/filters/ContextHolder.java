package nd.esp.service.lifecycle.support.filters;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextHolder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ContextHolder.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return ContextHolder.applicationContext;
	}

	public static Object getBean(String beanName) {
		return ContextHolder.applicationContext.getBean(beanName);
	}

	public static <T> T getBean(Class<T> clazz) {
		return ContextHolder.applicationContext.getBean(clazz);
	}

	public static <T> T getBean(String beanName, Class<T> clazz) {
		return applicationContext.getBean(beanName, clazz);
	}
}
