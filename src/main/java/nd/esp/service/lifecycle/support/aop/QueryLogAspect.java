package nd.esp.service.lifecycle.support.aop;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.ArrayUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;
import com.nd.gaea.rest.exceptions.messages.ErrorMessage;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.security.authens.UserInfo;

/**
 * @title API请求日志记录切面功能
 * @Desc order 10001 在AddFormatToCategoryAspect之后执行
 * @author liuwx
 * @version 1.0
 * @see AddFormatToCategoryAspect
 * @create 2015年9月26日 上午11:33:38
 * @update 新增userId操作记录
 */
@Aspect
@Component
@Order(10001)
public class QueryLogAspect {
    private static final Logger LOG = LoggerFactory.getLogger(QueryLogAspect.class);
    private long startTime = System.currentTimeMillis();
    @Autowired
    private HttpServletRequest request;
    private String requestInfo = "";
    //记录用户mac的标志位
    private static final String MAC_USER_ID="UserId";

    /**
     * Pointcut
     * 定义Pointcut，Pointcut的名称为aspectjMethod()，此方法没有返回值和参数
     * 该方法就是一个标识，不进行调用
     */
    //@Pointcut("execution(* nd.esp.service.lifecycle.controllers.*.*(..)) || execution(* nd.esp.service.lifecycle.controllers.v06.*.*(..))")
    //@Pointcut("execution(* *..*Controller*.*(..))")
    @Pointcut("execution(* nd.esp.service.lifecycle.controllers..*Controller*.*(..))")
    private void aspectjMethod() {
    }

    ;

    /**
     * Before
     * 在核心业务执行前执行，不能阻止核心业务的调用。
     *
     * @param joinPoint
     */
    @Before("aspectjMethod()")
    public void beforeAdvice(JoinPoint joinPoint) {
        startTime = System.currentTimeMillis();
        //获取用户的userId
        Object[] args =joinPoint.getArgs();
        String client="the client:[%s]";
        String userId= request.getHeader(MAC_USER_ID);
        if(StringUtils.hasText(userId)){
            LOG.info("从调用方的header中获取到MAC USERID:{}",userId);
            client=String.format(client,userId);

        }else {//使用userInfo标签
            //约定选择最后一个
            if(ArrayUtils.isNotEmpty(args)){
                Object userInfo= args[args.length-1];
                if(userInfo instanceof  UserInfo){
                    UserInfo realUserInfo=(UserInfo)userInfo;
                    client=String.format(client,realUserInfo.getUserId());
                    //MDC.put("userID", realUserInfo.getUserId());
                }
            }
        }

        requestInfo = client + request.getRemoteAddr() + " via " + request.getRequestURL();
        if (StringUtils.hasText(request.getQueryString())) {
            requestInfo += "?" + request.getQueryString();
        }
        requestInfo += " by " + request.getMethod();
    }

    /**
     * After
     * 核心业务逻辑退出后（包括正常执行结束和异常退出），执行此Advice
     *
     * @param joinPoint
     */
    @After(value = "aspectjMethod()")
    public void afterAdvice(JoinPoint joinPoint) {
        // System.out.println("-----afterAdvice().invoke-----");
        //LOG.error(((MethodInvocationProceedingJoinPoint) joinPoint).getSignature().toLongString());
        //LOG.info(((MethodInvocationProceedingJoinPoint) joinPoint).getTarget().toString());
        LOG.info(requestInfo + " consume time:{}", (System.currentTimeMillis() - startTime));
        //MDC.clear();
    }

    /**
     * Around
     * 手动控制调用核心业务逻辑，以及调用前和调用后的处理,
     * <p/>
     * 注意：当核心业务抛异常后，立即退出，转向AfterAdvice
     * 执行完AfterAdvice，再转到ThrowingAdvice
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around(value = "aspectjMethod()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
      //  System.out.println("-----aroundAdvice().invoke-----");
        // 调用核心逻辑
    	try {
    		Object retVal = pjp.proceed();
    		return retVal;
		} catch (Exception e) {
			dealException(e);			
		}
        return null;
    }

    /**
     * AfterReturning
     * 核心业务逻辑调用正常退出后，不管是否有返回值，正常退出后，均执行此Advice
     *
     * @param joinPoint
     */
    @AfterReturning(value = "aspectjMethod()", returning = "retVal")
    public void afterReturningAdvice(JoinPoint joinPoint, String retVal) {
        LOG.info("返回值:{}",retVal);
        requestInfo = "";

    }

    /**
     * 核心业务逻辑调用异常退出后，执行此Advice，处理错误信息
     * <p/>
     * 注意：执行顺序在Around Advice之后
     *
     * @param joinPoint
     * @param ex
     */
    @AfterThrowing(value = "aspectjMethod()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
        if (WafSimpleException.class.isAssignableFrom(ex.getClass())) {
            WafSimpleException wfs = (WafSimpleException) ex;
            ErrorMessage errorMessage = wfs.getErrorMessage();
            LOG.error("调用接口失败,code:{},message:{}", errorMessage.getCode(), errorMessage.getMessage(), ex);
            //CourseWareObjectControllerV03.createCourseCell(..)
            LOG.error(((MethodInvocationProceedingJoinPoint) joinPoint).getSignature().toShortString());
            // public nd.esp.service.lifecycle.vos.LearningObjectViewModel nd.esp.service.lifecycle.controllers.CourseWareObjectControllerV03.createCourseCell(nd.esp.service.lifecycle.models.CourseWareObjectModel,org.springframework.validation.BindingResult,java.lang.String)
            //LOG.info(((MethodInvocationProceedingJoinPoint) joinPoint).getSignature().toLongString());
            //nd.esp.service.lifecycle.controllers.CourseWareObjectControllerV03@2b0e8176
            //LOG.info(((MethodInvocationProceedingJoinPoint) joinPoint).getTarget().toString());
            boolean capture=needCapture();
            //只有Post或则put请求的才需要捕获
            if(capture) {
                for (Object arg : joinPoint.getArgs()) {
                    //也可以通过toLongString()方法动态判断对象的类型
                    if (arg instanceof ResourceViewModel) {
                        JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
                        try {
                            String result = mapper.writeValueAsString(arg);
                            LOG.info("捕获传入的对象参数:{}", result);
                        } catch (Exception e) {
                            LOG.error("捕获对象方法{}异常,参数转换失败:{}", ((MethodInvocationProceedingJoinPoint) joinPoint).getSignature().toShortString(), e.getMessage(), e);
                        }


                    }
                }
            }

        }
        /*System.out.println("-----afterThrowingAdvice().invoke-----");
        System.out.println(" 错误信息：" + ex.getMessage());
        System.out.println(" 此处意在执行核心业务逻辑出错时，捕获异常，并可做一些日志记录操作等等");
        System.out.println(" 可通过joinPoint来获取所需要的内容");
        System.out.println("-----End of afterThrowingAdvice()------");*/
    }

    //判断请求是否是put和post的请求
    private  boolean needCapture(){
        String requestMethod =request.getMethod();
        HttpMethod method=HttpMethod.valueOf(requestMethod);
        boolean capture=(method==HttpMethod.POST||method==HttpMethod.PUT);
        return capture;
    }


    /**
     * 判断是否含有AuthenticationPrincipal标签
     * @see AuthenticationPrincipal
     *
     * @param joinPoint
     * @return
     */
    private boolean hasAuthenticationPrincipalAnnotation(JoinPoint joinPoint){
        boolean has=false;
        MethodInvocationProceedingJoinPoint mj=  ((MethodInvocationProceedingJoinPoint) joinPoint);
        MethodSignature methodSignature= (MethodSignature)mj.getSignature();
        //取出方法参数的所有注解
        Annotation[][] annotations= methodSignature.getMethod().getParameterAnnotations();
            if(ArrayUtils.isNotEmpty(annotations)){
                //获取最后一个参数的注解
                Annotation [] lastParameter=    annotations[annotations.length-1];
                if(ArrayUtils.isNotEmpty(lastParameter)){
                    for(Annotation annotation:lastParameter){
                      if(annotation instanceof  AuthenticationPrincipal){
                            has=true;
                            break;
                        }
                    }
                }
            }
        return has;
    }
    
    /**
     * 对异常的处理
     * 
     * @author:xuzy
     * @date:2016年1月16日
     * @param e
     * @throws Exception
     */
    private void dealException(Exception e) throws Exception{
    	if(e instanceof JpaSystemException){
    		String message = e.getMessage();
    		if(message != null){
    			if(message.contains("'")){
    				int bi = message.indexOf("'");
    				int ei = message.indexOf("'", bi+1);
    				String name = message.substring(bi+1, ei);
    				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/DATA_TOO_LONG", name+"字段值长度过长");
    			}
    		}
    	}else{
    		throw e;
    	}
    }
}
