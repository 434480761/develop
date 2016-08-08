package nd.esp.service.lifecycle.vos.valid;

import javax.validation.GroupSequence;

/**
 * 针对课时、知识点等的修改校验组
 * 
 * @author caocr
 *
 */
@GroupSequence({ UpdateKnowledgeDefault.class, LifecycleDefault4Update.class, CategoriesDefault.class,CopyrightDefault.class })
public interface ValidUpdateLessPropertiesGroup {

}
