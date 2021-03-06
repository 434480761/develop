package nd.esp.service.lifecycle.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Create Time: 2015年7月9日           </p>
 * @author xiezy
 */
public interface AdapterDBDataService {
    /**
     * 触发旧资源转码
     *
     * @author:ql
     * @date:2015年12月10日
     * @return
     * @throws Exception
     */
    Map<String,String> triggerTranscodeByIds(String resType, List<String> listIds, boolean bOnlyOgv);

    /**
     * 触发资源转码
     * @param resType
     * @author liuwx
     * @return
     */
    public Map<String,Integer> triggerResourceTranscode(String resType,int perCount,int totCount);
    
    /**
     * 触发旧视频转码
     * 
     * @author:ql
     * @date:2015年12月10日
     * @return
     */
    public Map<String, String> triggerVideoTranscode(int totCount, Set<String> statusSet, boolean bOnlyOgv);
    
    /**
     * 资源preview修复
     * 
     * @author:ql
     * @date:2015年12月23日
     * @return
     */
    public Map<String, Long> fixResourcePreview(String resType);


    /**
     * @author liuwx
     * @param resType
     * @createtime 20151221
     */
    public void triggerResourcePack(String resType,String identifiers,String limit);
    
//    /**
//     * 更新习题提供商
//     * <p>Create Time: 2016年2月22日   </p>
//     * <p>Create author: xiezy   </p>
//     */
//    public void updateProvider4Question();
    
//    /**
//     * 修复3d半成品数据
//     */
//    public void update3DResource(String session,long endTime);

    void triggerUpdatedResourcePack(String resType, String sql, boolean bLowPriority);
    
    /**
     * 修复3D半成品资源的维度数据
     * 增加一个三级分类数据
     * @return
     */
    public Map<String,Integer> adapter3DResource();
    
    public void adapterDJGResource4Lc();
    
    public void adapterDJGResource4Status();
    
    public void repairProvider(String type,String pre,String now);
    
    public Map<String,Integer> adapterInstructionalobjectives();

    public Map<String,String> adapterCoverage(String oldUserId,String newUserId);

    /**
     * 设置国际化编码统一为zh-CN(主要是categorys，category_datas，category_patterns表)
     * @param 
     * @author yuzc
     * @param 
     * */
	public boolean setGbCode(String gbCode);
}
