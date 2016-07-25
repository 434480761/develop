package nd.esp.service.lifecycle.educommon.vos.constant;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * ******************************************
 * <p/>
 * Copyright 2016
 * NetDragon All rights reserved
 * <p/>
 * *****************************************
 * <p/>
 * *** Company ***
 * NetDragon
 * <p/>
 * *****************************************
 * <p/>
 * *** Team ***
 * <p/>
 * <p/>
 * *****************************************
 *
 * @author gsw(806801)
 * @version V1.0
 * @Title RetrieveFieldsConstant
 * @Package nd.esp.service.lifecycle.educommon.vos.constant
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/7/25
 */
public class RetrieveFieldsConstant {

    public final static String FIELD_TIT  = "TIT";
    public final static String FIELD_KWS  = "KWS";
    public final static String FIELD_TAG = "TAG";
    public final static String FIELD_DES  = "DES";
    public final static String FIELD_EDES  = "EDES";
    public final static String FIELD_CDES  = "CDES";

    /**
     *
     * @return
     */
    public static List<String> getFieldsList(){
        List<String> list = new ArrayList<String>();
        list.add(FIELD_TIT);
        list.add(FIELD_KWS);
        list.add(FIELD_TAG);
        list.add(FIELD_DES);
        list.add(FIELD_EDES);
        list.add(FIELD_CDES);

        return list;
    }

    public static Map<String, String> getFieldsMap() {
        Map<String, String> map = new HashMap<>();
        map.put(FIELD_TIT, "title");
        map.put(FIELD_KWS, "keywords");
        map.put(FIELD_TAG, "tags");
        map.put(FIELD_DES, "description");
        map.put(FIELD_EDES, "edu_description");
        map.put(FIELD_CDES, "cr_description");
        return map;
    }

    /**
     *
     * @param fields
     * @return
     */
    public static List<String> getValidFields(String fields) {
        if (StringUtils.isEmpty(fields)) {
            return new ArrayList<String>();
        }

        Set<String> set = new HashSet<String>(Arrays.asList(fields.split(",")));
        List<String> fieldsList = new ArrayList<String>();
        Map<String, String> fieldsMap = getFieldsMap();
        for (String field : set) {
            if (!fieldsMap.containsKey(field.trim())) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.RetrieveFieldsParamError.getCode(),
                        "fields检索字段中的:" + field + ",不在规定范围内");
            } else {
                fieldsList.add(fieldsMap.get(field.trim()));
            }
        }

        return fieldsList;
    }
}
