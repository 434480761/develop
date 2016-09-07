package nd.esp.service.lifecycle.daos.ResLifecycle.v06;

import java.util.Map;


/**
 * 资源lifecycle数据层
 * @author ql
 *
 */
public interface ResLifecycleDao {
    /**
     * 根据资源类型、id更新主表中preview值，不变更last_update(转码使用)
     * @param resType
     * @param resId
     * @param preview
     * @return
     */
    public boolean updatePreview(String resType, String resId, Map<String,String> preview);

    public boolean updateLifecycleStatus(String resType, String resId, String status);
}
