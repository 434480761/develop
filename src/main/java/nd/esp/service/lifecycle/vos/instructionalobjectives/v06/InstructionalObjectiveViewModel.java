package nd.esp.service.lifecycle.vos.instructionalobjectives.v06;

import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.vos.valid.InstructionalObjectiveDefault;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
/**
 * 教学目标V06视图层模型
 * 校验信息
 * @author linsm
 */
public class InstructionalObjectiveViewModel extends ResourceViewModel{
	/**
	 * 学习对象的标题名称
	 */
    @NotBlank(message="{resourceViewModel.title.notBlank.validmsg}",groups={InstructionalObjectiveDefault.class})
    //@Length(message="{InstructionalObjectiveViewModel.title.maxlength.validmsg}",max=1000,groups={InstructionalObjectiveDefault.class})
	private String title;
    
    private String kbId;
    
    private String ocId;
	
    @JsonInclude(Include.NON_NULL)
    @Override
    public Map<String, String> getPreview() {
        return super.getPreview();
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKbId() {
		return kbId;
	}

	public void setKbId(String kbId) {
		this.kbId = kbId;
	}

	public String getOcId() {
		return ocId;
	}

	public void setOcId(String ocId) {
		this.ocId = ocId;
	}
}
