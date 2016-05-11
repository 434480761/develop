package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportNdResource;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportNdresourceRepository extends ResourceRepository<ReportNdResource>,JpaRepository<ReportNdResource, String> {

}