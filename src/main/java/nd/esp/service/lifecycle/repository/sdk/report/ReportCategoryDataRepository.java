package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportCategoryData;
import nd.esp.service.lifecycle.repository.model.report.ReportNdResource;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCategoryDataRepository extends ResourceRepository<ReportCategoryData>,JpaRepository<ReportCategoryData, String> {

}