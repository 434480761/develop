/* =============================================================
 * Created: [2015年8月20日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.enums;

/**
 * 适配任务状态枚举类
 * 
 * @author linsm
 * @since
 */
public enum AdapterTaskResultStatus {
    UNSTART("未开始"), // 未开始
    RUNNING("执行中"), // 执行中
    FINISH("执行完成"), // 执行完成
    INTERRUPT("执行中断"), // 执行中断
    ;
    private String status;
    AdapterTaskResultStatus(String status){

        this.status=status;
    }


    public String getStatus() {
        return status;
    }


}
