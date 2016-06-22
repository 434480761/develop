/* =============================================================
 * Created: [2015年7月8日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controllers;

import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.services.CategoryService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.category.NdCodePattern;
import nd.esp.service.lifecycle.vos.CategoryDataApplyForNdCodeViewModel;
import nd.esp.service.lifecycle.vos.QueryRelationAllViewModel;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linsm
 * @since
 */
@RestController
@RequestMapping("/v0.6")
public class CategoryControllerV06 {
    
    private final Logger LOG = LoggerFactory.getLogger(CategoryControllerV06.class);
    
    @Autowired
    @Qualifier("CategoryServiceImpl")
    private CategoryService categoryService;
    
    /**
     * 指定关系条件，查询满足条件的关系个数
     * 
     * @param patternPath 路径
     * @param enable 关系是否可用
     * @param ndCode 目标ndCode
     * @return
     * @since
     */
    @RequestMapping(value = "/categories/relations/actions/count", method = RequestMethod.GET,produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody int requestQueryRelationDataNum(
            @RequestParam(value = "patternPath",required=true) String patternPath,
            @RequestParam(value = "enable",required=false,defaultValue = "true") Boolean enable,
            @RequestParam(value = "nd_code",required=false) String ndCode){
        
        return categoryService.countRelation(patternPath, enable, ndCode);
        
    }

    /**
     * 申请新的NDcode-输入和输出
     * 
     * @param ndCode,1、当存在父结点时，值为父结点（维度数据）的ndCode,2、不存在父结点（即是顶层，父结点是ROOT）时，则值为分类维度的ndCode。
     * @return
     * @since
     */
    @RequestMapping(value = "/categories/datas/actions/apply", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody CategoryDataApplyForNdCodeViewModel extendCategoryData(@RequestParam(value = "nd_code", required = true) String ndCode) {
        String categoryNdCode = "";  //分类维度ndCode  如 $E
        String parentNdCode = "";    //父结点ndCode   如$E001000
        // 判断是否为分类维度ndCode(通过长度)
        if (ndCode.length() > NdCodePattern.CATEGORY_LENGTH) {
            
            LOG.debug("nd_code:{} is a parentNdCode",ndCode);
            
            //输入参数是parentNdCode
            categoryNdCode = ndCode.substring(0, NdCodePattern.CATEGORY_LENGTH);
            parentNdCode = ndCode;
        } else if (ndCode.length() == NdCodePattern.CATEGORY_LENGTH) {
            
            LOG.debug("nd_code:{} is a categoryNdCode",ndCode);
            
            //输入参数是对应的分类维度ndCode
            categoryNdCode = ndCode;
        } else {
            // 异常：
            
            LOG.error(LifeCircleErrorMessageMapper.CheckNdCodeRegex.getMessage()+ndCode);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex);
        }
        NdCodePattern pattern = NdCodePattern.fromString(categoryNdCode);
        if (pattern == null) {
            // 抛出暂时没有设计对应的分类维度
            
            LOG.error("暂时没有设计对应的分类维度: " + categoryNdCode);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "暂时没有设计对应的分类维度: " + categoryNdCode);
        }
        String prefix = pattern.getPrefix(ndCode); // 若不符合规范，则会抛出异常
        
        LOG.debug("维度数据编码前缀:"+prefix);
        
        if (prefix.length() < NdCodePattern.CATEGORY_LENGTH) {
            
            LOG.error(LifeCircleErrorMessageMapper.CheckNdCodeRegex.getMessage()+"前缀长度小于分类维度编码长度");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex);
        }
        
        // 获取父结点id
        String parentId = "ROOT";
        if (StringUtils.isNotEmpty(parentNdCode)) {
            CategoryDataModel resultModel = null;
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryDataByNdCode(parentNdCode);
            } catch (EspStoreException e) {
               
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            if (resultModel == null) {
                
                LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage());
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CategoryDataNotFound);
            }
            parentId = resultModel.getIdentifier();
        } 

        // 获取分类维度的id(主要是为了验证是否已存在于数据库，不然可以通过维度数据中的字段取得)
        CategoryModel categoryModelResult = null;
        // 调用service 接口
        try {
            categoryModelResult = categoryService.loadCategoryByNdCode(categoryNdCode);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }

        if (categoryModelResult == null) {
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryNotFound);
        }
        String categoryId = categoryModelResult.getIdentifier();

        int excludeOtherorderNum = getMaxOrderNum(prefix,pattern);
        String excludeOtherNdCode =produceNdCode(pattern, prefix, excludeOtherorderNum);
        String maxNdCode = categoryService.getMaxLevelNdCodeExcludeOther(categoryId, parentId,excludeOtherNdCode);
        
        LOG.debug("当前粒度下:{},可能存在的最大的合法ndCode编码:{}",ndCode,maxNdCode);
        
        int orderNum = 1; // 在一个粒度下的位置（顺序）
        if (StringUtils.isNotEmpty(maxNdCode)) {
            Assert.assertEquals(pattern.getLength(), maxNdCode.length()); // 符合长度要求
            orderNum = Integer.valueOf(maxNdCode.substring(prefix.length(),
                                                           prefix.length() + pattern.getExtendDigit(prefix.length()))) + 1;
        }
        if (orderNum >= excludeOtherorderNum) {
            
            LOG.error("超出范围，分配不了:"+ndCode);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "超出范围，分配不了");
        }
        
        LOG.debug("维度数据编码序号:"+orderNum);
        
        String newNdCode = produceNdCode(pattern, prefix, orderNum);
        
        LOG.debug("生成申请的ndCode:"+newNdCode);

        // 创建维度数据参数
        CategoryDataApplyForNdCodeViewModel viewModelResult = new CategoryDataApplyForNdCodeViewModel();
        viewModelResult.setShortName(newNdCode); //portal 导入教材要求有值，暂时返回ndCode
        viewModelResult.setParent(parentId);
        viewModelResult.setNdCode(newNdCode);
        viewModelResult.setOrderNum(orderNum);
        viewModelResult.setCategory(categoryId);
        return viewModelResult;
    }
    
    
    @RequestMapping(value = {"/categories/relations/all"}, method = RequestMethod.GET,produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody QueryRelationAllViewModel requestQueryRelationData(
			@RequestParam(value = "patternPath",required=true) String patternPath,
			@RequestParam(value = "enable",required=false,defaultValue="true") boolean enable,
			@RequestParam(value = "levelParent",required=false) String levelParent){
		// 校验入参: patternName
		// 调用service 接口
		QueryRelationAllViewModel viewListResult = null;
		try {
			viewListResult = categoryService.queryCategoryRelationAll(levelParent,
			 enable,  patternPath);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}
		 return viewListResult;
	}

    /**
     * 取得某一粒度的预留ndCode
     * @param prefix 前缀 如$E002
     * @param pattern $E
     * @return $E002999
     * @since 
     */
    private int getMaxOrderNum(String prefix, NdCodePattern pattern) {
        Assert.assertNotNull(pattern);
        Assert.assertNotNull(prefix);
        int prefixLength =prefix.length();
        int extendDigit = pattern.getExtendDigit(prefixLength);
        Assert.assertNotEquals(0, extendDigit);
        return Integer.valueOf(String.format("1%0"+extendDigit+"d", 0))-1;
        
    }


    /**
     * 用于有序获取ndCode
     * 
     * @param pattern 对应于分类维度
     * @param prefix 已知前缀
     * @param orderNum 生成的位置
     * @return 维度数据ndCode编码
     * @since
     */
    private String produceNdCode(NdCodePattern pattern, String prefix, int orderNum) {
        String newNdCode = "";
        int extendDigit = pattern.getExtendDigit(prefix.length());
        Assert.assertNotEquals(0, extendDigit); // 在获取前缀时，已经对是否可扩展做出判断（抛出异常）
        newNdCode = prefix + String.format("%0" + extendDigit + "d", orderNum);// 保证占用该层所有位数
        if (newNdCode.length() < pattern.getLength()) {
            newNdCode = newNdCode + String.format("%0" + (pattern.getLength() - newNdCode.length()) + "d", 0);// 用0补到一定的长度（非生成最细粒度的维度数据）
        }
        return newNdCode;
    }

}
