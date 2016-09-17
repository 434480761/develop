package nd.esp.service.lifecycle.repository.sdk;


/**
 * 类描述:ExaminationPaperRepository
 * 创建人: xuzy
 * @version
 */
  
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ExaminationPaper;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExaminationPaperRepository extends  ResourceRepository<ExaminationPaper>,
JpaRepository<ExaminationPaper, String> {

}