package nd.esp.service.lifecycle.daos.ResLifecycle.v06.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nd.esp.service.lifecycle.repository.sdk.ContributeRepository;
import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.daos.ResLifecycle.v06.ResLifecycleDao;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;


/**
 * 资源lifecycle数据层实现类
 * @author ql
 *
 */
@Service
public class ResLifecycleDaoImpl implements ResLifecycleDao {
    private static final Logger LOG = LoggerFactory.getLogger(ResLifecycleDaoImpl.class);
    
    @Autowired
    private ContributeRepository contributeRepository;
    
    
    /**
     * 根据资源类型、id更新主表中status值，不变更last_update(修改转码状态使用)
     * @param assetId
     * @return
     */
    @Override
    public boolean updatePreview(String resType, String resId, Map<String,String> preview) {
        String sql = "UPDATE ndresource SET preview='" + ObjectUtils.toJson(preview) + "' WHERE identifier = '" + resId + "'";
        
        LOG.info(contributeRepository.getJdbcTemple().toString() + "; preview更新sql:"+sql);
        
//        contributeRepository.getEntityManager().createNativeQuery(sql).executeUpdate();
        contributeRepository.getJdbcTemple().execute(sql);
        return true;
    }
}
