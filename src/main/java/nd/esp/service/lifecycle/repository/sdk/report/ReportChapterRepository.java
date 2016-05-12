package nd.esp.service.lifecycle.repository.sdk.report;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.report.ReportChapter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportChapterRepository extends ResourceRepository<ReportChapter>,JpaRepository<ReportChapter, String> {

}