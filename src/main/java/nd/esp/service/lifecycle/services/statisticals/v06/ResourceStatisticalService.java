package nd.esp.service.lifecycle.services.statisticals.v06;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import nd.esp.service.lifecycle.models.statisticals.v06.ResourceStatisticalModel;

public interface ResourceStatisticalService {
    public List<ResourceStatisticalModel> addStatistical(List<ResourceStatisticalModel> sms, String resType, String id);

    public Map<String, List<ResourceStatisticalModel>> getList(List<String> key, List<String> rid);
    
    public void addDownloadStatistical(String bsyskey,String resType,String id);
    
    public void resourceTop(String resType,String uuid,boolean effect);
}
