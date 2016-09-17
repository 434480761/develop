package nd.esp.service.lifecycle.repository;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 
 * 项目名字:nd esp<br>
 * 类描述:hibernate 对@Transient不初始化，导致无法写入数据库，采用该注解弥补，在ProxyRepositoryImpl调用<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年5月21日<br>
 * 修改人:<br>
 * 修改时间:2015年5月21日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface DataConverter {
	String target();
	Class<?> type();
}
