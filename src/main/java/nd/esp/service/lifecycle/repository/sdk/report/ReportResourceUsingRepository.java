package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceUsing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportResourceUsingRepository extends ResourceRepository<ReportResourceUsing>,JpaRepository<ReportResourceUsing, String> {

}