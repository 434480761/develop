package nd.esp.service.lifecycle.support.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import nd.esp.service.lifecycle.support.annotation.impl.RegValidator;

@Target({ElementType.PARAMETER,ElementType.METHOD,ElementType.FIELD})
@Constraint(validatedBy=RegValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Reg {
	String message() default "格式不对";
	//当value值为空时，是否还需要校验
	boolean isNullValid() default true;
	//正则表达式
	String pattern();
	Class<?>[] groups() default {};     
	Class<? extends Payload>[] payload() default {};  
}
