package nd.esp.service.lifecycle.support.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2016/9/12.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TitanTransaction {
}
