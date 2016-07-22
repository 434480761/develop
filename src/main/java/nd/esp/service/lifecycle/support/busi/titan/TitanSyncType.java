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
    IMPORT_DATA_ERROR;

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
