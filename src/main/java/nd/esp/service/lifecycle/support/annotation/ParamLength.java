package nd.esp.service.lifecycle.support.annotation;

import java.lang.annotation.*;

/**
 * @title 参数长度限制
 *
 * @desc 默认为500
 *
 * @createtime on 2014/12/2516:50
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamLength {


    String value() default "500";


    boolean required() default true;

}
