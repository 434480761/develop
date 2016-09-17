package nd.esp.service.lifecycle.vos.coursewareobjects.v06;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.vos.valid.CoursewareObjectBasicInfo;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 课件颗粒视图数据模型（v0.6）
 * 
 * @author caocr
 * @since
 */
public class CourseWareObjectViewModel extends ResourceViewModel {

	@NotBlank(message="{courseWareObjectViewModel.title.notBlank.validmsg}", groups={CoursewareObjectBasicInfo.class})
    @Length(message="{courseWareObjectViewModel.title.maxlength.validmsg}",max=500, groups={CoursewareObjectBasicInfo.class})
	@Override
	public String getTitle() {
		return super.getTitle();
	}
	
}
