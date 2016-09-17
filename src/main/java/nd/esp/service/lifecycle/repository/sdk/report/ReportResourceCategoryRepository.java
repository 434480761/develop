package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportNdResource;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceCategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportResourceCategoryRepository extends ResourceRepository<ReportResourceCategory>,JpaRepository<ReportResourceCategory, String> {

}