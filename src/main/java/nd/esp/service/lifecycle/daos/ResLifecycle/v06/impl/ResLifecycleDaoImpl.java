package nd.esp.service.lifecycle.daos.ResLifecycle.v06.impl;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.titan.TitanSyncService;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanRepositoryOperationPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
    @Qualifier("defaultJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("questionJdbcTemplate")
    private JdbcTemplate questionJdbcTemplate;

    @Autowired
    private TitanSyncService titanSyncService;

    
    /**
     * 根据资源类型、id更新主表中status值，不变更last_update(修改转码状态使用)
     * @param assetId
     * @return
     */
    @Override
    public boolean updatePreview(String resType, String resId, Map<String,String> preview) {
        JdbcTemplate jdbcTemplateInUse = jdbcTemplate;
        if (IndexSourceType.QuestionType.getName().equals(resType) || IndexSourceType.SourceCourseWareObjectType.equals(resType)) {
            jdbcTemplateInUse = questionJdbcTemplate;
        }

        String sql = "UPDATE ndresource SET preview='" + ObjectUtils.toJson(preview) + "' WHERE identifier = '" + resId + "'";
        
        LOG.info(jdbcTemplateInUse.toString() + "; preview更新sql:"+sql);
        
//        contributeRepository.getEntityManager().createNativeQuery(sql).executeUpdate();
        jdbcTemplateInUse.execute(sql);

        Map<String, Object> values = new HashMap<>();
        values.put("dbpreview",ObjectUtils.toJson(preview));
        updateEducation(resType,resId,values);

        return true;
    }

    @Override
    public boolean updateLifecycleStatus(String resType, String resId, String status) {
        JdbcTemplate jdbcTemplateInUse = jdbcTemplate;
        if (IndexSourceType.QuestionType.getName().equals(resType) || IndexSourceType.SourceCourseWareObjectType.equals(resType)) {
            jdbcTemplateInUse = questionJdbcTemplate;
        }

        String sql = "UPDATE ndresource SET estatus='" + status + "' WHERE identifier = '" + resId + "'";

        LOG.info(jdbcTemplateInUse.toString() + "; status更新sql:"+sql);

        jdbcTemplateInUse.execute(sql);

        Map<String, Object> values = new HashMap<>();
        values.put("status",status);
        updateEducation(resType,resId,values);

        return true;
    }


    private void updateEducation(String resType, String resId, Map<String, Object> values){
        Education education = new Education();
        education.setIdentifier(resId);
        education.setPrimaryCategory(resType);
        TitanRepositoryOperationPatch patch = new TitanRepositoryOperationPatch();
        patch.setEntity(education);
        patch.setPatchPropertyMap(values);
        titanSyncService.patch(patch);
    }
}
