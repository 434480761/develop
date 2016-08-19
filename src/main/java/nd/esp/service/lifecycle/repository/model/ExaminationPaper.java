package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 试卷
 * 
 * @version
 */

@Entity
@Table(name = "examination_papers")
public class ExaminationPaper extends Education {
	private static final long serialVersionUID = 1L;

	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.ExaminationPapersType.getName());
		return IndexSourceType.ExaminationPapersType;
	}
}