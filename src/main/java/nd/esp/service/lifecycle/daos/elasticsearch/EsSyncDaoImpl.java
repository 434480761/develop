package nd.esp.service.lifecycle.daos.elasticsearch;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.EsSync;
import nd.esp.service.lifecycle.repository.sdk.EsSyncRepository;
import nd.esp.service.lifecycle.support.busi.elasticsearch.EsSyncTimerTask;

@Service
public class EsSyncDaoImpl implements EsSyncDao {

	private final static Logger LOG = LoggerFactory
			.getLogger(EsSyncDaoImpl.class);
	@Autowired
	private EsSyncRepository esSyncRepository;

	@Override
	public void beforeUpdate(Resource resource) throws EspStoreException {

		EsSync result = getByExample(resource);

		if (result == null) {
			// 还不存在对应的记录：插入一条记录
			insertOneRecord(resource, true);
		} else if (result.getSyncType() != null && result.getSyncType()) {
			// 更新
			result.setTryTimes(result.getTryTimes() + 1);
			result.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			esSyncRepository.update(result);
		}

		// 暂时不考虑已经清理过了

	}

	@Override
	public void beforeDelete(Resource resource) throws EspStoreException {
		EsSync result = getByExample(resource);

		// 还不存在对应的记录：插入一条记录
		if (result == null) {
			insertOneRecord(resource, false);
		} else if (result.getSyncType() != null && result.getSyncType()) {
			// 更新
			result.setTryTimes(result.getTryTimes() + 1);
			result.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			result.setSyncType(false);
			esSyncRepository.update(result);
		} else if (result.getSyncType() != null && !result.getSyncType()) {
			// 删除
			result.setTryTimes(result.getTryTimes() + 1);
			result.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
			esSyncRepository.update(result);
		}

	}

	/**
	 * 成功同步es后
	 * 
	 * @throws EspStoreException
	 */
	@Override
	public void afterUpdate(Resource resource) throws EspStoreException {
		EsSync result = getByExample(resource, true);
		if (result == null) {
			LOG.warn(
					"es sync(update) task data may loss or be updated by delete: resource:{},resourceType:{}",
					resource.getIdentifier(), resource.getResourceType());
		} else {
			delete(result);
		}

	}

	@Override
	public void afterDelete(Resource resource) throws EspStoreException {
		EsSync result = getByExample(resource, false);
		if (result == null) {
			LOG.warn(
					"es sync(delete) task data loss: resource:{},resourceType:{}",
					resource.getIdentifier(), resource.getResourceType());
		} else {
			delete(result);
		}

	}

	private EsSync getByExample(Resource resource) throws EspStoreException {
		return getByExample(resource, null);
	}

	private EsSync getByExample(Resource resource, Boolean syncType)
			throws EspStoreException {
		EsSync condition = new EsSync();
		condition.setEnable(true);
		condition.setPrimaryCategory(resource.getResourceType());
		condition.setResource(resource.getIdentifier());
		if (syncType != null) {
			condition.setSyncType(syncType);
		}
		return esSyncRepository.getByExample(condition);
	}

	private void insertOneRecord(Resource resource, Boolean syncType)
			throws EspStoreException {
		BigDecimal bigDecimalTime = new BigDecimal(System.currentTimeMillis());
		EsSync bean = new EsSync();
		bean.setIdentifier(UUID.randomUUID().toString());
		bean.setResource(resource.getIdentifier());
		bean.setPrimaryCategory(resource.getResourceType());
		bean.setCreateTime(bigDecimalTime);
		bean.setLastUpdate(bigDecimalTime);
		bean.setEnable(true);
		bean.setSyncType(syncType);
		bean.setTryTimes(1);
		esSyncRepository.add(bean);

	}

	private void delete(EsSync result) throws EspStoreException {
		// result.setEnable(false);
		// result.setLastUpdate(new BigDecimal(System.currentTimeMillis()));
		// esSyncRepository.update(result);
		esSyncRepository.delete(result.getIdentifier());

	}

	@Override
	public void batchBeforeUpdate(Set<Resource> resourceSet)
			throws EspStoreException {
		for (Resource resource : resourceSet) {
			beforeUpdate(resource);
		}
	}

	@Override
	public void batchAfterUpdate(Set<Resource> resourceSet)
			throws EspStoreException {
		for (Resource resource : resourceSet) {
			afterUpdate(resource);
		}
	}

	@Override
	public void batchBeforeDelete(Set<Resource> resourceSet)
			throws EspStoreException {
		for (Resource resource : resourceSet) {
			beforeDelete(resource);
		}

	}

	@Override
	public void batchAfterDelete(Set<Resource> resourceSet)
			throws EspStoreException {
		for (Resource resource : resourceSet) {
			afterDelete(resource);
		}

	}

	@Override
	public void restartSync() throws EspStoreException {
		String sql = "update es_sync set try_times ="
				+ (EsSyncTimerTask.MAX_TRY_TIMES - 1) + " WHERE try_times >="
				+ EsSyncTimerTask.MAX_TRY_TIMES;
		esSyncRepository.getJdbcTemple().execute(sql);
	}
	
}
