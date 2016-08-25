package nd.esp.service.lifecycle.utils.titan.script.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2016/8/24.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TitanEdgeResource {
    String name() default "";
}
