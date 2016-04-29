package nd.esp.service.lifecycle.vos.valid;

import javax.validation.GroupSequence;

@GroupSequence({ BasicInfoDefault.class, LifecycleDefault4Update.class,
		TechInfoDefault.class, CategoriesDefault.class, CoveragesDefault.class,
		RelationsDefault.class, CopyrightDefault.class,
		RequirementDefault.class })
public interface Valid4UpdateGroup {

}
