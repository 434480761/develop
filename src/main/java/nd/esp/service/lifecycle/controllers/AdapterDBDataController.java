package nd.esp.service.lifecycle.controllers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.entity.lifecycle.AdapterTaskResult;
import nd.esp.service.lifecycle.services.AdapterDBDataService;
import nd.esp.service.lifecycle.services.staticdatas.StaticDataService;
import nd.esp.service.lifecycle.services.updatamediatype.UpdateMediatypeService;
import nd.esp.service.lifecycle.services.updatamediatype.model.UpdateMediatypeModel;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.support.UpdateStaticDataTask;
import nd.esp.service.lifecycle.utils.FileUploadUtil;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adapter")
public class AdapterDBDataController {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterDBDataController.class);
    
    @Autowired
    private AdapterDBDataService adapterDBDataService;
    
    @Autowired
    @Qualifier(value = "UpdataMediatypeServiceImpl")
    private UpdateMediatypeService updataMediatypeService;
    
    @Autowired
    private StaticDataService staticDataService;
    
    @RequestMapping(value = "/objective/fix_title", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,Integer> fixKnowledgeObjective() {
        return adapterDBDataService.adapterInstructionalobjectives();
    }
    
    @RequestMapping(value = "/uploadfile",method=RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,Integer> uploadFileToCS(
            @RequestParam(value = "localFileDir", required = false,defaultValue = "D:\\UploadData") String localFileDir,
            @RequestParam String csPath, @RequestParam String filename, @RequestParam String session){
        File targetFile = new File(localFileDir+"\\"+filename);
        StringBuffer logMsg = new StringBuffer();
        String rt = null;
        try {
            rt = FileUploadUtil.UploadFileToCS(targetFile, csPath, filename, session,
                    "http://cs.101.com/v0.1", logMsg);
            System.out.println(rt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 触发资源的转码任务
     *
     *
     *
     * @author:liuwx
     * @date:2015年19月9日
     * @param resType 资源类型
     * @param perCount 每次转码数量 默认10个
     * @return
     */
    @RequestMapping(value = "/{resType}/actions/fix/transcode",method=RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,Integer>triggerResourceTranscode(@PathVariable String resType, 
            @RequestParam(value = "perCount", required = false,defaultValue = "10") int perCount,
            @RequestParam int totCount){

        if(perCount<1){
            LOG.warn("ARITHMETIC/FAIL", "Arithmetic bad for perCount,it must be bigger than zero:" + perCount);
            perCount=10;
        }

//        if(StringUtils.isEmpty(status)){
//            status= LifecycleStatus.TRANSCODE_WAITING.getCode();
//        }

        LOG.info("资源{}:触发转码,每次转码数:perCount={},总转码数：totCount={}",resType,perCount,totCount);


       return this.adapterDBDataService.triggerResourceTranscode(resType,perCount, totCount);

    }
    
    @RequestMapping(value = "/videos/transcode",method=RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,String> triggerVideoTranscode(@RequestParam int totCount, 
            @RequestParam(value = "statusSet", required = false,defaultValue = "ONLINE,AUDIT_WAITING,AUDITING,AUDITED,TRANSCODED,TRANSCODE_ERROR") Set<String> statusSet,
            @RequestParam(value = "onlyOgv", required = false,defaultValue = "false") boolean onlyOgv) {

       LOG.info("视频触发转码,总转码数：totCount={}",totCount);


       return this.adapterDBDataService.triggerVideoTranscode(totCount, statusSet, onlyOgv);

    }
    
    @RequestMapping(value = "/videos/transcode/id",method=RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,String> triggerVideoTranscodeById(@RequestBody List<String> listIds,
            @RequestParam(value = "onlyOgv", required = false,defaultValue = "false") boolean onlyOgv){


       return this.adapterDBDataService.triggerVideoTranscodeByIds(listIds, onlyOgv);

    }
    
    /**
     * 是否进行预打包的切换开关
     * <p>Create Time: 2015年12月29日   </p>
     * <p>Create author: ql   </p>
     * @param turnon
     * @return
     */
//    @RequestMapping(value = "/prepack/switch", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
//    public String switchPrePackEnable(@RequestParam boolean bEnabled){
//        String message = "";
//        
//        if(bEnabled){
//            message = "习题、互动习题等创建、更新时进行预打包";
//        }else{
//            message = "习题、互动习题等创建、更新时不进行预打包（压测使用）";
//        }
//        Constant.ENABLE_PRE_PACK = bEnabled;
//        
//        return message;
//    }
    
    /**
     * 更新媒体资源类型
     * @param type 资源类型
     * @param save 是否保存数据
     * */
    @RequestMapping(value = "/mediatype/{type}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public UpdateMediatypeModel update(@PathVariable String type ,@RequestParam boolean save) {
        return updataMediatypeService.tupdate(type , save);
    }


    /**
     * 触发习题(基础/互动)历史数据打包
     *
     * @param res_type
     *
     * @author liuwx
     *
     * @create 2015.12.21
     *
     */
    @RequestMapping(value = "/{res_type}/pack",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody void triggerResourcePack(@PathVariable String res_type,@RequestParam(required = false) String identifiers,@RequestParam(required = false) String limit ){

        LOG.info("资源{}开始修复历史数据",res_type);
        Constant.ADAPTER_TASK_CHARGE = true;
        this.adapterDBDataService.triggerResourcePack(res_type,identifiers,limit);

    }

    /**
     * 查询触发习题(基础/互动)历史数据打包完成状态  1 完成 0未完成
     *
     * @param res_type
     *
     * @author liuwx
     *
     * @create 2015.12.21
     *
     */
    @RequestMapping(value = "/{res_type}/query/packinfo",method=RequestMethod.GET )
    public @ResponseBody Object queryResourcePackinfo(@PathVariable String res_type ){

        LOG.info("资源{}开始修复历史数据查询",res_type);

        AdapterTaskResult adapterTaskResult = Constant.ADAPTER_TASK_RESULT.get(res_type);

        if(null!=adapterTaskResult){

            return adapterTaskResult;
        }

        return null;

    }

    /**
     * 0触发打包的开关
     * <p>Create Time: 2015年12月21日   </p>
     * <p>Create author: liuwx   </p>
     * @param canquery
     * @return
     */
//    @RequestMapping(value = "/task/switch/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
//    public String changeTaskSwitch(@RequestParam String canquery){
//        String message = "";
//
//        if(canquery.equals("false")){
//            Constant.ADAPTER_TASK_CHARGE = false;
//            message = "中断执行任务";
//        }else{
//            Constant.ADAPTER_TASK_CHARGE = true;
//            message = "重新触发任务";
//        }
//
//        return message;
//    }
    
    
    @RequestMapping(value = "/{res_type}/preview/fix",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, Long> resourcePreviewFix(@PathVariable String res_type){


       return this.adapterDBDataService.fixResourcePreview(res_type);

    }
    
//    /**
//     * 更新习题提供商
//     * <p>Create Time: 2016年2月22日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param upv
//     */
//    @RequestMapping(value = "/questions/provider/update",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
//    public void updateProvider4Questions(){
//        adapterDBDataService.updateProvider4Question();
//    }
    
//    /**
//     * 修复3d半成品数据
//     */
//	@RequestMapping(value = "/3dresource/update", method = RequestMethod.GET)
//	public void update3DResource(@RequestParam String session,@RequestParam String time) {
//		adapterDBDataService.update3DResource(session,Long.parseLong(time));
//	}
    
    /**
     * 触发习题(基础/互动)历史数据打包
     *
     * @param res_type
     *
     * @author qil
     *
     * @create 2016.02.25
     *
     */
    @RequestMapping(value = "/{res_type}/update_pack",method=RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody void triggerResourcePack(@PathVariable String res_type,
            @RequestBody Map<String,String> body,
            @RequestParam(required = false,defaultValue="true") boolean bLowPriority ){

        LOG.info("资源{}开始修复历史数据",res_type);
        String sql = body.get("sql");
        if(StringUtils.hasText(sql)) {
            this.adapterDBDataService.triggerUpdatedResourcePack(res_type,sql,bLowPriority);
        }
    }
    
    /**
     * 修复3D半成品资源维度数据
     * @return
     */
    @RequestMapping(value = "/repair3D",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,Integer> adapter3DResource(){
    	return adapterDBDataService.adapter3DResource();
    }
    
//    @RequestMapping(value = "/repairDJG/lc",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void adapterDJGResource4Lc(){
    	adapterDBDataService.adapterDJGResource4Lc();
    }
    
//    @RequestMapping(value = "/repairDJG/status",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void adapterDJGResource4Status(){
    	adapterDBDataService.adapterDJGResource4Status();
    }
    
//    @RequestMapping(value = "/repair/provider",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void repairProvider(@RequestParam String type,@RequestParam String pre,@RequestParam String now){
    	adapterDBDataService.repairProvider(type,pre,now);
    }
    
    public static boolean REPAIR_SWITCH_1 = true;
    public static boolean REPAIR_SWITCH_2 = true;
    
//    @RequestMapping(value="/repairDJG/switch",method=RequestMethod.GET)
    public String changeRepairSwitch(@RequestParam boolean button,@RequestParam boolean isOne){
    	if(isOne){
    		if(AdapterDBDataController.REPAIR_SWITCH_1 != button){
    			AdapterDBDataController.REPAIR_SWITCH_1 = button;
        		return "开关1:"+ (button?"打开":"关闭");
    		}
    	}else{
    		if(AdapterDBDataController.REPAIR_SWITCH_1 != button){
    			AdapterDBDataController.REPAIR_SWITCH_2 = button;
        		return "开关2:"+ (button?"打开":"关闭");
    		}
    	}
    	
    	return "";
    }
    
    /*******************************静态变量开关相关接口--start************************************/
    /**
     * 修改删除脏数据的控制开关
     * <p>Create Time: 2016年3月2日   </p>
     * <p>Create author: xuzy   </p>
     * @param suspendFlag
     * @return
     */
    @RequestMapping(value="/changeFlag",method=RequestMethod.GET)
    public String changeDeleteDirtyDataFlag(@RequestParam(required = false) boolean suspendFlag){
    	if(suspendFlag){
    		staticDataService.updateNowStatus("suspendFlag",1);
    		staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.suspendFlag = true;
    		return "暂停清理脏数据任务！(最多等待1分钟生效)";
    	}else{
    		staticDataService.updateNowStatus("suspendFlag",0);
    		staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.suspendFlag = false;
    		return "开启清理脏数据任务！(最多等待1分钟生效)";
    	}
    }
    
    /**
     * 06通用查询是否可以查询到QA覆盖范围的开关
     * <p>Create Time: 2015年10月20日   </p>
     * <p>Create author: xiezy   </p>
     * @param onOrOff
     * @return
     */
    @RequestMapping(value = "/coverage/switch/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String changeQaDataSwitch(@RequestParam String onOrOff){
        String message = "";
        
        if(onOrOff.equals("false") && StaticDatas.CAN_QUERY_QA_DATA){
            message = "06通用查询(query)--可以查到QA覆盖范围(最多等待1分钟生效)";
            staticDataService.updateNowStatus("CAN_QUERY_QA_DATA",0);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.CAN_QUERY_QA_DATA = false;
        }else if(onOrOff.equals("true") && !StaticDatas.CAN_QUERY_QA_DATA){
            message = "06通用查询(query)--已屏蔽QA覆盖范围(最多等待1分钟生效)";
            staticDataService.updateNowStatus("CAN_QUERY_QA_DATA",1);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.CAN_QUERY_QA_DATA = true;
        }
        
        return message;
    }
    
    /**
     * 显示QA覆盖范围的开关的当前状态
     * <p>Create Time: 2016年2月29日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    @RequestMapping(value = "/coverage/switch/show", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String showQaDataSwitch(){
        String message = "";
        
        if(!StaticDatas.CAN_QUERY_QA_DATA){
            message = "06通用查询(query)--可以查到QA覆盖范围";
        }else{
            message = "06通用查询(query)--已屏蔽QA覆盖范围";
        }
        
        return message;
    }
    
    /**
     * 06通用查询是否可以查询到QA覆盖范围的开关
     * <p>Create Time: 2015年11月26日   </p>
     * <p>Create author: xiezy   </p>
     * @param canquery
     * @return
     */
    @RequestMapping(value = "/provider/switch/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String changeProviderSwitch(@RequestParam String canquery){
        String message = "";
        
        if(canquery.equals("false") && StaticDatas.CAN_QUERY_PROVIDER){
            message = "06通用查询(query)--不可以查到提供商为智能出题的记录(最多等待1分钟生效)";
            staticDataService.updateNowStatus("CAN_QUERY_PROVIDER",0);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.CAN_QUERY_PROVIDER = false;
        }else if(canquery.equals("true") && !StaticDatas.CAN_QUERY_PROVIDER){
            message = "06通用查询(query)--可以查到提供商为智能出题的记录(最多等待1分钟生效)";
            staticDataService.updateNowStatus("CAN_QUERY_PROVIDER",1);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.CAN_QUERY_PROVIDER = true;
        }
        
        return message;
    }
    
    /**
     * 改变是否推送报表的变量
     * @param canquery
     * @return
     */
    @RequestMapping(value = "/report/switch/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String changeReportSwitch(@RequestParam String canquery){
        String message = "";
        
        if(canquery.equals("false") && StaticDatas.SYNC_REPORT_DATA){
            message = "不同步推送给报表系统(最多等待1分钟生效)";
            staticDataService.updateNowStatus("SYNC_REPORT_DATA",0);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.SYNC_REPORT_DATA = false;
        }else if(canquery.equals("true") && !StaticDatas.SYNC_REPORT_DATA){
            message = "同步推送给报表系统(最多等待1分钟生效)";
            staticDataService.updateNowStatus("SYNC_REPORT_DATA",1);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.SYNC_REPORT_DATA = true;
        }
        
        return message;
    }
    
    /**
     * 访问控制的开关控制
     * <p>Create Time: 2015年11月26日   </p>
     * <p>Create author: xiezy   </p>
     * @param open
     */
    @RequestMapping(value = "/ivc/switch/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String changeIvcSwitch(@RequestParam String open){
        String message = "";
        
        if(open.equals("false") && StaticDatas.IS_IVC_CONFIG_ENABLED){
            message = "访问控制关闭(最多等待1分钟生效)";
            staticDataService.updateNowStatus("IS_IVC_CONFIG_ENABLED",0);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.IS_IVC_CONFIG_ENABLED = false;
        }else if(open.equals("true") && !StaticDatas.IS_IVC_CONFIG_ENABLED){
            message = "访问控制开启(最多等待1分钟生效)";
            staticDataService.updateNowStatus("IS_IVC_CONFIG_ENABLED",1);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.IS_IVC_CONFIG_ENABLED = true;
        }
        
        return message;
    }
    
    /**
     * 通用查询是否优先通用ES查询的开关控制
     * <p>Create Time: 2016年4月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param open
     */
    @RequestMapping(value = "/query/first/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String changeQueryFirstSwitch(@RequestParam String open){
        String message = "";
        
        if(open.equals("false") && StaticDatas.QUERY_BY_ES_FIRST){
            message = "关闭通用查询优先通过ES查询的开关(最多等待1分钟生效)";
            staticDataService.updateNowStatus("QUERY_BY_ES_FIRST",0);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.QUERY_BY_ES_FIRST = false;
        }else if(open.equals("true") && !StaticDatas.QUERY_BY_ES_FIRST){
            message = "开启通用查询优先通过ES查询的开关(最多等待1分钟生效)";
            staticDataService.updateNowStatus("QUERY_BY_ES_FIRST",1);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.QUERY_BY_ES_FIRST = true;
        }
        
        return message;
    }

    @RequestMapping(value = "/titan/switch/change", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public String titanSwitchChange(@RequestParam String open){
        String message = "";

        if(open.equals("false") && StaticDatas.TITAN_SWITCH){
            message = "关闭titan开关(最多等待1分钟生效)";
            staticDataService.updateNowStatus("TITAN_SWITCH",0);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.TITAN_SWITCH = false;
        }else if(open.equals("true") && !StaticDatas.TITAN_SWITCH){
            message = "开启titan开关(最多等待1分钟生效)";
            staticDataService.updateNowStatus("TITAN_SWITCH",1);
            staticDataService.updateLastTime(UpdateStaticDataTask.SWITCH_TASK_ID);
            StaticDatas.TITAN_SWITCH = true;
        }

        return message;
    }

    /*******************************静态变量开关相关接口--end************************************/
}
