package nd.esp.service.lifecycle.vos.valid;

import javax.validation.GroupSequence;

/**
 * 针对课时、知识点等的创建校验组
 * 
 * @author caocr
 *
 */
@GroupSequence({ LessPropertiesDefault.class, LifecycleDefault.class, CategoriesDefault.class, RelationsDefault.class,CopyrightDefault.class })
public interface ValidCreateLessPropertiesGroup {

}
