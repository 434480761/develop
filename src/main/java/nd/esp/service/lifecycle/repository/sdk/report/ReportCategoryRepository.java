package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportCategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCategoryRepository extends ResourceRepository<ReportCategory>,JpaRepository<ReportCategory, String> {

}