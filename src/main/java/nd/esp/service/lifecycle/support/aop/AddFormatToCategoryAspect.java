package nd.esp.service.lifecycle.support.aop;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <p>切面</p>
 * 根据资源的tech_info中format的内容，在媒体类型维度数据表中找到适配的维度编码，将编码内容存储到资源的分类中。
 * @author liur
 * */
@Aspect
@Component
@Order(10000)
public class AddFormatToCategoryAspect {

    private final static String DEFAULT_NDCODE = "$F990000";

    private final static Logger LOG = LoggerFactory.getLogger(AddFormatToCategoryAspect.class);

    @Autowired
    private CategoryDataRepository categoryDataRepository = null;
//    private static List<Class<? extends Object>> classTypeList = new LinkedList<Class<? extends Object>>();



    @Pointcut("@annotation(nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category)")
    public void performanceAnnon() {

    }

    /**
     * 逻辑：遍历所有的参数，并判断参数的类型是否包含在List集合中，如果是,则进行处理
     * */
    @Before("performanceAnnon()")
    public void executeAnnon(JoinPoint point) {

        LOG.info("切面方法--在create或update方法前，利用techInfo.format向categories中添加维度数据");

        List<Object> parms = Arrays.asList(point.getArgs());

        for (Object parm : parms) {
            if (parm instanceof ResourceViewModel) {
                doing(parm);
            }
        }
    }

    /**
     * 把参数类型和参数进行匹配后强制转换
     * */
    private void doing(Object parm) {
        // V06
        if (parm instanceof ResourceViewModel) {

            LOG.info("V06处理" + parm.getClass().getName());

            ResourceViewModel viewModel = (ResourceViewModel) parm;

            dealFormatV06Controller(viewModel.getTechInfo(), viewModel.getCategories());
        } 
    }

    /**
     * 处理V06
     * @param techInfoMap tech_info数据
     * @param categoriesMap categories数据
     * */
    private void dealFormatV06Controller(Map<String, ? extends ResTechInfoViewModel> techInfoMap,
            Map<String, List<? extends ResClassificationViewModel>> categoriesMap) {

        if (techInfoMap == null || categoriesMap == null) {
            return;
        }

        // 遍历所有的techInfo
//        for (String techInfoKey : techInfoMap.keySet()) {
        	
            ResTechInfoViewModel techInfo = techInfoMap.get("source");
            if(techInfo == null){
            	techInfo = techInfoMap.get("href");
            }

            if (techInfo != null && StringUtils.isNotEmpty(techInfo.getFormat())) {
                // 根据format值获取维度编码
                String ndCode = getNdcodeByFormat(techInfo.getFormat());
                
                List<ResClassificationViewModel> list = (List<ResClassificationViewModel>) categoriesMap.get("mediatype") ;
                if(list == null){
                    list = new LinkedList<ResClassificationViewModel>();
                }
                ResClassificationViewModel resClassification = new ResClassificationViewModel();

                resClassification.setTaxonpath("");
                resClassification.setTaxoncode(ndCode);
                list.add(resClassification);

                if(categoriesMap.get("mediatype") == null){
                    categoriesMap.put("mediatype", list);
                }
            }
//        }
    }

    // 获取nd_code
    private String getNdcodeByFormat(String format) {

        CategoryData categoryData = new CategoryData();
        String ndCode = null;
        categoryData.setShortName(format);
        List<CategoryData> categoryDatas = null;
        try {
            categoryDatas = categoryDataRepository.getAllByExample(categoryData);
        } catch (EspStoreException e) {

            LOG.error("切面处理--查询nd_code出错");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }

        // 数据库中查询不到对应的值时指定一个默认的值
        if (categoryDatas != null && !categoryDatas.isEmpty()) {
            boolean hasData = false;
            for (CategoryData category : categoryDatas) {
                String type = category.getNdCode().substring(0, 2);
                if ("$F".equals(type)) {
                    hasData = true;
                    ndCode = category.getNdCode();
                }
            }
            // 查询到的数据都不符合要求
            if (!hasData) {
                ndCode = DEFAULT_NDCODE;
            }
        }
        // 数据库没有查询到数据
        else {
            ndCode = DEFAULT_NDCODE;
        }

        return ndCode;
    }

    /**
     * 处理V03
     * */
    // private void dealFormatV03Controller(String format, List<String> categories) {
    //
    // // 验证format不为null
    // if (StringUtils.isNotEmpty(format)) {
    // String ndCode = getNdcodeByFormat(format);
    //
    // if (!categories.contains(ndCode)) {
    // categories.add(ndCode);
    // }
    // }
    // }

    /*
     * @SuppressWarnings("unused") private void delaFormatV03Adapter(NdResource resource) {
     * 
     * List<CategoryData> categoryDatas = new LinkedList<CategoryData>();
     * 
     * 数据库中查询到的值不正确<br> 解决办法：判断查询出来的结果，如果不正确则抛出异常终止后面的操作
     * 
     * if (categoryDatas != null && !categoryDatas.isEmpty()) { boolean hasData = false; for (CategoryData category :
     * categoryDatas) { int index = Integer.parseInt(category.getNdCode().substring(3, 4)); if (index ==
     * category.getOrderNum()) { hasData = true; String ndCode = category.getNdCode();
     * resource.getCategories().add(ndCode); } } if (!hasData) { System.out.println("媒体类型不合法"); } } else {
     * System.out.println("媒体类型不合法,查询不到对应的媒体类型数据"); } }
     * 
     * // @Pointcut(
     * "execution(* nd.esp.service.lifecycle.controllers.*.v06.*.*creat e*(..)) && args(viewModel , validResult , id) || "
     * // +
     * "execution(* nd.esp.service.lifecycle.controllers.*.v06.*.*update*(..)) && args(viewModel , validResult , id)")
     * 
     * @SuppressWarnings("unused") private void performanceControllerV06(ResourceViewModel viewModel, BindingResult
     * validResult, String id) {
     * 
     * }
     * 
     * // @Before("performanceControllerV06(viewModel , validResult,id )")
     * 
     * @SuppressWarnings("unused") private void executeControllerV06(ResourceViewModel viewModel, BindingResult
     * validResult, String id) {
     * 
     * LOG.info("切面在ControllerV06执行create或update方法前执行");
     * 
     * dealFormatV06Controller(viewModel.getTechInfo(), viewModel.getCategories());
     * 
     * }
     */

    // public void sss(){
    // V03模型
    // 课件颗粒模板
    // else if ("CourseWareObjectTemplateModel".equals(type)) {
    //
    // //可替换
    // CourseWareObjectTemplateModel viewModel = (CourseWareObjectTemplateModel) parm;
    // dealFormatV03Controller(viewModel.getFormat(), viewModel.getCategories());
    // }
    // // 课件颗粒
    // else if ("CourseWareObjectModel".equals(type)) {
    //
    // LOG.info("V03处理" + parm.getClass().getName());
    // //可替换
    // CourseWareObjectModel viewModel = (CourseWareObjectModel) parm;
    // dealFormatV03Controller(viewModel.getFormat(), viewModel.getCategories());
    // }
    // // 课件
    // else if ("CourseWareModel".equals(type)) {
    //
    // LOG.info("V03处理" + parm.getClass().getName());
    // //可替换
    // CourseWareModel viewModel = (CourseWareModel) parm;
    // dealFormatV03Controller(viewModel.getFormat(), viewModel.getCategories());
    // }
    // // 习题
    // else if ("QuestionModel".equals(type)) {
    //
    // LOG.info("V03处理" + parm.getClass().getName());
    // //可替换
    // QuestionModel viewModel = (QuestionModel) parm;
    // dealFormatV03Controller(viewModel.getFormat(), viewModel.getCategories());
    // }
    // }
}
