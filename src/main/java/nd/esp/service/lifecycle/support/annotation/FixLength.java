package nd.esp.service.lifecycle.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import nd.esp.service.lifecycle.support.annotation.impl.FixLengthImpl;

/**
 * @title 自定义校验注解------------暂时不使用
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月21日 下午2:17:52
 */
@Target( { ElementType.FIELD  })  
@Retention(RetentionPolicy.RUNTIME)  
@Constraint(validatedBy = FixLengthImpl.class)  
public @interface FixLength {  
  
    int length();  
    String message() default "{model.href.value.errormsg}";  
  
    Class<?>[] groups() default {};  
  
    Class<? extends Payload>[] payload() default {};  
}  