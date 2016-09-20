package nd.esp.service.lifecycle.repository.sdk.icrs;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.icrs.IcrsSyncErrorRecord;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ICRS同步错误记录表仓储
 * @author xiezy
 * @date 2016年9月12日
 */
public interface IcrsSyncErrorRecordRepository extends ResourceRepository<IcrsSyncErrorRecord>,
JpaRepository<IcrsSyncErrorRecord, String> {

}