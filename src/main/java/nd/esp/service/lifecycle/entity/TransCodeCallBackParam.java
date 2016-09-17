/* =============================================================
 * Created: [2015年10月20日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.entity;

import java.util.List;
import java.util.Map;

/**
 * @author linsm
 * @since 
 *
 */
public class TransCodeCallBackParam {
    
    private int status;   //转码状态
    private String href;  //FIXME 暂时认为只转出为一种格式
    private String errMsg; //异常信息
    private Map<String, String> metadata;  //转码后文件元数据
    private String transcodeType;  //转码类型
    private List<String> previews;  //预览图地址 ,/edu/esp/assets/.....
    private String cover;   //封面
    private Map<String,String> locations;  //多码率目标地址
    
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    public String getTranscodeType() {
        return transcodeType;
    }
    public void setTranscodeType(String transcodeType) {
        this.transcodeType = transcodeType;
    }
    public List<String> getPreviews() {
        return previews;
    }
    public void setPreviews(List<String> previews) {
        this.previews = previews;
    }
    public Map<String, String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    public Map<String,String> getLocations() {
        return locations;
    }
    public void setLocations(Map<String,String> locations) {
        this.locations = locations;
    }

}
