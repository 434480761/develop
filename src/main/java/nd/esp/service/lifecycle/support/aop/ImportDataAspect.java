package nd.esp.service.lifecycle.support.aop;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.support.enums.SynVariable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 
 * @author linsm
 *
 */
@Aspect
@Component
@Order(9999)
public class ImportDataAspect {
	private static final Logger LOG = LoggerFactory
			.getLogger(ImportDataAspect.class);

	@Autowired
	private CommonServiceHelper commonServiceHelper;

	@Pointcut("@annotation(nd.esp.service.lifecycle.support.annotation.MarkAspect4ImportData)")
	private void aspectjMethod() {
	}

	;

	/**
	 * 
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around(value = "aspectjMethod()")
	public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
		if (commonServiceHelper.queryAndUpdateSynVariable(SynVariable.importDataSync
				.getValue()) == 0) {
			LOG.error("already_has_a_task_running");
			return null;
		}
		LOG.error("before_task_running");
		Object retVal = pjp.proceed();
		LOG.error("after_task_running");
		commonServiceHelper.initSynVariable(SynVariable.importDataSync.getValue());
		return retVal;
	}

}
