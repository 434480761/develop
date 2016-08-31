package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuran on 2016/6/30.
 */
public enum  TitanSyncType {
    //真删资源失败
    DROP_RESOURCE_ERROR,
    //任何和资源相关的修改
    SAVE_OR_UPDATE_ERROR,
    //导入数据失败
    IMPORT_DATA_ERROR,
    FIELD_LENGTH_TOO_LONG,
    SYNC_SUCCESS,
    UPDATE_DATA_RESOURCE,
    UPDATE_DATA_RELATION,
    UPDATE_DATA_TECH,
    UPDATE_DATA_COVERAGE,
    UPDATE_DATA_CATEGORY,
    CHECK_NOT_EXIST,
    CHECK_REPEAT,
    CHECK_TI_NOT_EXIST,
    CHECK_TI_REPEAT,
    CHECK_CG_NOT_EXIST,
    CHECK_CG_REPEAT,
    CHECK_RC_NOT_EXIST,
    CHECK_RC_REPEAT,
    CHECK_STA_NOT_EXIST,
    CHECK_STA_REPEAT,
    CHECK_RR_NOT_EXIST,
    CHECK_RR_REPEAT,
    CHECK_NR_NOT_EXIST,
    CHECK_NR_REPEAT,
    UPDATE_DATA_OTHER,
    DELETE_CATEGORY_ERROR,
    DELETE_COVERAGE_ERROR,
    DELETE_TECH_INFO_ERROR,
    //不同版本数据库差量修复数据
    VERSION_REPAIR;

    public static Map<String, TitanSyncType> map = new HashMap<>();
    static {
        for (TitanSyncType value : values()){
            map.put(value.toString(),value);
        }
    }

    public static TitanSyncType value(String key){
        return map.get(key);
    }

}
