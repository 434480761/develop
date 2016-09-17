package nd.esp.service.lifecycle.support.transcode;

import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

import static nd.esp.service.lifecycle.support.busi.TransCodeUtil.*;

/**
 * @title 转码相关上下文
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/22 14:29
 */
public class TranscodeContext {

    //旧状态映射 key=oldStatus value=newStatus
    private static final Map<String,String> OLD_VERSION_STATUS_MAPPING=new HashMap<>();

    //新状态映射 key=newStatus value=oldStatus
    private static final Map<String,String> NEW_VERSION_STATUS_MAPPING=new HashMap<>();



    static {
        //init data
        OLD_VERSION_STATUS_MAPPING.put(CONVERT_STATUS_UNCONVERTED,NEW_CONVERT_STATUS_UNCONVERTED);
        OLD_VERSION_STATUS_MAPPING.put(CONVERT_STATUS_CONVERTING,NEW_CONVERT_STATUS_CONVERTING);
        OLD_VERSION_STATUS_MAPPING.put(CONVERT_STATUS_CONVERTED,NEW_CONVERT_STATUS_CONVERTED);
        OLD_VERSION_STATUS_MAPPING.put(CONVERT_STATUS_CONVERT_ERR,NEW_CONVERT_STATUS_CONVERT_ERR);

        NEW_VERSION_STATUS_MAPPING.put(NEW_CONVERT_STATUS_UNCONVERTED ,CONVERT_STATUS_UNCONVERTED);
        NEW_VERSION_STATUS_MAPPING.put(NEW_CONVERT_STATUS_CONVERTING ,CONVERT_STATUS_CONVERTING);
        NEW_VERSION_STATUS_MAPPING.put(NEW_CONVERT_STATUS_CONVERTED ,CONVERT_STATUS_CONVERTED);
        NEW_VERSION_STATUS_MAPPING.put(NEW_CONVERT_STATUS_CONVERT_ERR ,CONVERT_STATUS_CONVERT_ERR);

    }



    public static String getOldVersionStatus(String status){
        Assert.assertNotNull("status不能为空", status);

        String result= OLD_VERSION_STATUS_MAPPING.get(status);
        return result!=null?result:status;
    }
    public static String getNewVersionStatus(String status){
        Assert.assertNotNull("status不能为空", status);

        String result= NEW_VERSION_STATUS_MAPPING.get(status);
        return result!=null?result:status;
    }







}
