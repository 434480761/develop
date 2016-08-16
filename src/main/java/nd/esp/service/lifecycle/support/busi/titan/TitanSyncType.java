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
    UPDATE_DATA_OTHER;

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
