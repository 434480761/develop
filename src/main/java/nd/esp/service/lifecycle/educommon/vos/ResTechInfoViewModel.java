package nd.esp.service.lifecycle.educommon.vos;

import java.util.List;

import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import nd.esp.service.lifecycle.vos.valid.TechInfoDefault;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResTechInfoViewModel {

	/**
	 * 标识
	 */
	//private String identifier;
	@NotBlank(message="{resourceViewModel.techInfo.format.notBlank.validmsg}",groups={TechInfoDefault.class})
	@Length(message="{resourceViewModel.techInfo.format.maxlength.validmsg}",max=100, groups={TechInfoDefault.class})
	private String format;
	@Min(message="{resourceViewModel.techInfo.size.min.validmsg}",value=0,groups={TechInfoDefault.class})
	private long size;
	@NotBlank(message="{resourceViewModel.techInfo.location.notBlank.validmsg}",groups={TechInfoDefault.class})
	@Length(message="{resourceViewModel.techInfo.location.maxlength.validmsg}",max=1000, groups={TechInfoDefault.class})
	private String location;
	@Valid
	private List<? extends TechnologyRequirementViewModel> requirements;
	private String md5;
	private String secureKey;
	@JsonIgnore
	private String title;
	private String entry;
	private boolean printable = false;

	public ResTechInfoViewModel(){

	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<? extends TechnologyRequirementViewModel> getRequirements() {
		return requirements;
	}

	public void setRequirements(
			List<? extends TechnologyRequirementViewModel> requirements) {
		this.requirements = requirements;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getSecureKey() {
		return secureKey;
	}

	public void setSecureKey(String secureKey) {
		this.secureKey = secureKey;
	}

	public boolean isPrintable() {
		return printable;
	}

	public void setPrintable(boolean printable) {
		this.printable = printable;
	}

}