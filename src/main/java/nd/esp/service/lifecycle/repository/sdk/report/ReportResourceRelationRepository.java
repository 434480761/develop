package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportNdResource;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceRelation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportResourceRelationRepository extends ResourceRepository<ReportResourceRelation>,JpaRepository<ReportResourceRelation, String> {

}