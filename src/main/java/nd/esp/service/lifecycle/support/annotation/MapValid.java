package nd.esp.service.lifecycle.support.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import nd.esp.service.lifecycle.support.annotation.impl.MapValidator;

@Target({ElementType.PARAMETER,ElementType.METHOD,ElementType.FIELD})
@Constraint(validatedBy=MapValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MapValid {
	String message() default "";
	Class<?>[] groups() default {};     
	Class<? extends Payload>[] payload() default {};  
}
