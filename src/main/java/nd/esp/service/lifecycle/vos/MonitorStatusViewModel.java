/* =============================================================
 * Created: [2015年6月19日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.vos;

/**
 * @author linsm
 * @since 
 *
 */
public class MonitorStatusViewModel {
    
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
   public static final String STATUS_OK = "OK";
   public static final String STATUS_EXCEPTION = "EXCEPTION";
   public static final int SERVICE_NUM = 2;  //暂时只监控了cs 和mySql，不对转码调度处理

}
