package nd.esp.service.lifecycle.utils;

import java.math.BigDecimal;

/**
 * Created by liuran on 2016/5/19.
 */
public class BigDecimalUtils {
    public static Object toString(Object db){
        if(db instanceof  BigDecimal)
            return ((BigDecimal) db).longValue();
        else
            return db;
    }

    public static BigDecimal toBigDecimal(Object db){
        return new BigDecimal((String) db);
    }

}
