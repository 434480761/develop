package nd.esp.service.lifecycle.controllers.v06;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterService;
import nd.esp.service.lifecycle.services.tool.v06.ToolServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
import nd.esp.service.lifecycle.vos.v06.ChapterReuseViewModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.ibm.icu.math.BigDecimal;
import com.nd.gaea.rest.security.authens.UserInfo;

/**
 * 工具类API接口
 * @author xuzy
 *
 */
@RestController
@RequestMapping("/v0.6/resources/tools2")
public class ToolController2 {

	private final static Logger LOG= LoggerFactory.getLogger(ToolController.class);
	
	@Autowired
	private ChapterService chapterService;
	
	@Autowired
	private EducationRelationServiceV06 educationRelationService;
	
	@Autowired
	private ResourceRelationRepository resourceRelationRepository;
	
	@Autowired
	private TeachingMaterialRepository teachingMaterialRepository;
	
	@Autowired
	private ToolServiceV06 toolService;
	
	public static Map<String,String> userIdMap = new HashMap<String, String>();
	

	/**
	 * 处理上传的关系数据
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/reuse/actions/checkdatas",method=RequestMethod.POST,produces={MediaType.APPLICATION_JSON_VALUE})
	public Map<String,Object> dealData(@RequestBody ChapterReuseViewModel crvm,@AuthenticationPrincipal UserInfo userInfo) throws Exception{
		if(userInfo != null){
			LOG.info("用户id:{},用户名：{},操作章节关系复用API接口",userInfo.getUserId(),userInfo.getUserName());
		}
		
		Map<String,Object> returnMap = new HashMap<String, Object>();
		Map<String, List<String>> datas = crvm.getDatas();
		List<String> resTypes = crvm.getResTypes();
		//入参校验
		if(CollectionUtils.isEmpty(resTypes)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_FAIL","res_types值不能为空");
		}
		
		if(CollectionUtils.isEmpty(datas)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_FAIL","datas值不能为空");
		}
		
		//判断章节是否存在
		Map<String,Object> checkMap = checkChapterIds(crvm.getDatas());
		if(checkMap.get("message") != null){
			return checkMap;
		}
		
		//取出所有的章节详情
		Map<String,ChapterViewModel> chaptersMap = (Map<String,ChapterViewModel>)checkMap.get("chapters");
		
		
		//创建关系
		List<ResourceRelation> resourceRelationList = new ArrayList<ResourceRelation>();
	    Set<String> keySet = datas.keySet();
	    Map<String,List<String>> errorMap = new HashMap<String, List<String>>();
	    for (String key : keySet) {
	    	
			List<String> values = datas.get(key);
			List<RelationForQueryViewModel> relations = new ArrayList<RelationForQueryViewModel>();
			
			for (String rt : crvm.getResTypes()) {
				ListViewModel<RelationForQueryViewModel>  relationModel = educationRelationService.queryListByResTypeByDB("chapters", key, "", rt, "","(0,1000)",false, null);
				List<RelationForQueryViewModel> items = relationModel.getItems();
				relations.addAll(items);
			}

			for (String cid : values) {
				if(StringUtils.isNotEmpty(cid)){
					//根据章节id获取教材id
					String teachingMaterialId = null;
					ChapterViewModel cvm = chaptersMap.get(cid);
					if(cvm != null){
						teachingMaterialId = cvm.getTeachingMaterial();
					}
					
					//根据教材id获取维度路径
					if(teachingMaterialId == null){
						LOG.warn("章节id:{},对应的教材不存在",cid);
						throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_ERROR","章节id:"+cid+",对应的教材不存在");
					}
					List<String> pathList = toolService.getTmCategories(teachingMaterialId);
					List<Map<String, String>> resList = new ArrayList<Map<String,String>>();
					
					float sortNum = 5000f;
					for (RelationForQueryViewModel relationForQueryViewModelNew : relations) {
						Map<String,String> resMap = new HashMap<String, String>();
						//目标id
						String targetId = relationForQueryViewModelNew.getIdentifier();
						//关系类型
						String relationType = relationForQueryViewModelNew.getRelationType();
						//目标资源类型
						String targetType = relationForQueryViewModelNew.getTargetType();
						//创建资源对象
						ResourceRelation rr = new ResourceRelation();
						rr.setIdentifier(UUID.randomUUID().toString());
						rr.setEnable(true);
						rr.setRelationType(relationType);
						rr.setResourceTargetType(targetType);
						rr.setTarget(targetId);
						rr.setSourceUuid(cid);
						rr.setSortNum(sortNum);
						rr.setResType("chapters");	
						resMap.put("resId", targetId);
						resMap.put("resType", targetType);
						resList.add(resMap);
						
						try {
						    EducationRelationModel model4Detail = null;
							if(model4Detail == null){
								resourceRelationList.add(rr);
								sortNum += 10;
							}
						} catch (Exception e) {
							if(errorMap.get(key) == null){
								List<String> msgList = new ArrayList<String>();
								msgList.add("判断资源关系是否存在出错了");
								errorMap.put(key, msgList);
							}else{
								if(!errorMap.get(key).contains("判断资源关系是否存在出错了")){
									errorMap.get(key).add("判断资源关系是否存在出错了");
								}
							}
							LOG.warn("判断资源关系是否存在出错了!",e);
						}
					}
					
					try {
						if(!resourceRelationList.isEmpty()){
							resourceRelationRepository.batchAdd(resourceRelationList);
							toolService.copyTmCategories2Res(pathList, resList);
						}
					} catch (Exception e) {
						if(errorMap.get(key) == null){
							List<String> msgList = new ArrayList<String>();
							msgList.add("批量新增资源关系出错了");
							errorMap.put(key, msgList);
						}else{
							if(!errorMap.get(key).contains("批量新增资源关系出错了")){
								errorMap.get(key).add("批量新增资源关系出错了");
							}
						}
						LOG.warn("批量新增资源关系出错了!",e);
					}
				}
				
			}
		}
	    if(CollectionUtils.isNotEmpty(errorMap)){
	    	returnMap.put("results", errorMap);
	    	return returnMap;
	    }
		
		return null;
	}
	
	@RequestMapping(value="/parseDate",method = RequestMethod.GET)
	public Long parseDate(@RequestParam(value="value",required=true) String date,@RequestParam(value="format",required=false) String format) throws ParseException{
		SimpleDateFormat sdf = null;
		if(format != null){
			sdf = new SimpleDateFormat(format);
		}else{
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		
		Date d = sdf.parse(date);
		return d.getTime();
		
	}
	
	@RequestMapping(value="/formatDate",method = RequestMethod.GET)
	public String formatDate(@RequestParam(value="value",required=true) String date,@RequestParam(value="format",required=false) String format) throws ParseException{
		SimpleDateFormat sdf = null;
		if(format != null){
			sdf = new SimpleDateFormat(format);
		}else{
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		
		return sdf.format(new Date(Long.valueOf(date)));
	}
	
	/**
	 * 导入excel(模板：99u家具用户关系对应表.xlsx)
	 * 
	 * @author:xuzy
	 * @date:2015年12月29日
	 * @param request
	 * @throws IOException 
	 */
	@RequestMapping(value="/resolveExcel")
	public void resolveExcel(HttpServletRequest request) throws IOException{
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		if (resolver.isMultipart(request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multiRequest.getFile("material");
			InputStream in = file.getInputStream();
			BufferedInputStream bin = new BufferedInputStream(in);
			// 打开HSSFWorkbook
			Workbook wb = new XSSFWorkbook(bin);
			
			for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
				Sheet st = wb.getSheetAt(sheetIndex);
				for (int rowIndex = 1; rowIndex <= st.getLastRowNum(); rowIndex++) {
					Row row = st.getRow(rowIndex);
					if (row == null) {
						continue;
					}
					if(row.getCell(2) == null){
						continue;
					}
					String code = String.valueOf(new BigDecimal(row.getCell(2).getNumericCellValue()).toBigInteger());
					String userId = String.valueOf(new BigDecimal(row.getCell(1).getNumericCellValue()).toBigInteger());
					userIdMap.put(code, userId);
				}
			}
		}
	}
	
	/**
	 * 校验章节UUID是否存在
	 * @param lists
	 * @return
	 */
	private Map<String,Object> checkChapterIds(Map<String,List<String>> param){
		Map<String,Object> returnMap = new HashMap<String, Object>();
		Map<String,Object> dataMap = new HashMap<String, Object>();
	    Set<String> keySet = param.keySet();
	    Map<String,ChapterViewModel> chapters = new HashMap<String, ChapterViewModel>();
		//用来判断是否所有的数据都是合法的
		boolean flag = true;
	    for (String key : keySet) {
	    	if(StringUtils.isEmpty(key)){
	    		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_FAIL","基准教材章节UUID不能为空");
	    	}
	    	
	    	List<String> errorList = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			for (String v : param.get(key)) {
				if(StringUtils.isNotEmpty(v)){
					values.add(v);
				}
			}
			values.add(key);
			Set<String> set = new HashSet<String>();
			set.addAll(values);
			
			Map<String,ChapterViewModel> ll = chapterService.batchGetChapterList(values); 
			if(ll.size() != set.size()){
				flag = false;
				for (String uuid : values) {
					if(ll.get(uuid) == null){
						//说明uuid在数据库中不存在
						errorList.add(uuid);
					}
				}
			}
			if(CollectionUtils.isNotEmpty(errorList)){
				dataMap.put(key, errorList);
			}else{
				chapters.putAll(ll);
			}
		}
	    
	    
	    if(!flag){
	    	returnMap.put("datas", dataMap);
	    	returnMap.put("message", "以下章节映射有异常，请核实");
	    }else{
	    	returnMap.put("chapters", chapters);
	    }
		return returnMap;
	}
	
}
