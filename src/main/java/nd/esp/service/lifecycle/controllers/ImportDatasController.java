/* =============================================================
 * Created: [2015年7月21日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.services.ImportDataService;
import nd.esp.service.lifecycle.services.impls.ImportDataServiceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.category.NdCodePattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import nd.esp.service.lifecycle.repository.model.CategoryData;

/**
 * @author linsm
 * @since
 */
@RestController
public class ImportDatasController {
    @Autowired
    private ImportDataService importDataService;
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportDatasController.class);
    

    /**
     * 导入维度数据，返回 总的行数: 符合格式规范的行数: 合法的分类体系数据（使用数据库）: 成功创建的维度数据: 花的时间：（ms）
     * 
     * @return
     * @since
     */
    @RequestMapping(value = "/v0.6/categories/datas/actions/bulking", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    private @ResponseBody Map<String, Long> importCategoryData() {
        Map<String, Long> importMessage = new LinkedHashMap<String, Long>();
        long beginTime = System.currentTimeMillis();

        // 读取数据
        List<String> sourceLines = readLineToList("CategoryData.txt");// FIXME 是否需要动态文件名
        importMessage.put("文件总的行数", (long) sourceLines.size());
        
        LOG.info("文件总的行数:" + sourceLines.size());

        // 过滤数据
        List<CategoryData> filterLines = filterCategoryData(sourceLines);
        importMessage.put("符合格式规范（详见wiki）的行数", (long) filterLines.size());
        
        LOG.info("符合格式规范（详见wiki）的行数 :"+ filterLines.size());

        importMessage.putAll(importDataService.importCategoryData(filterLines));

        // 花的时间
        long endTime = System.currentTimeMillis();
        importMessage.put("花的时间(ms)", (endTime - beginTime));
        
        LOG.info("花的时间(ms)"+(endTime - beginTime));

        return importMessage;
    }
   
    /**
     * 更新某个模式的关系顺序（保持原来的顺序，但是在同一个路径下，保证不存在相同orderNum值)
     * 
     * @param patternName
     * @return
     * @since
     */
    @RequestMapping(value = "/v0.6/categories/relations/actions/updateOrderNum", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    private @ResponseBody Map<String, Long> updateCategoryRelationOrderNum(@RequestParam(value = "pattern_name", required = true) String patternName) {
        if (StringUtils.isEmpty(patternName)) {
            // 抛出异常：参数不合法
            
            LOG.error(LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage()+patternName);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.InvalidArgumentsError);
        }
        Map<String, Long> importMessage = new LinkedHashMap<String, Long>();
        long beginTime = System.currentTimeMillis();
        importMessage.putAll(importDataService.updateCategoryRelationOrderNum(patternName));
        // 花的时间
        long endTime = System.currentTimeMillis();
        importMessage.put("花的时间(ms)", (endTime - beginTime));
        
        LOG.info("更新关系顺序花的时间(ms)"+patternName+":"+(endTime - beginTime));

        return importMessage;
    }

    /**
     * 过滤数据，通过分割符，判断每行的数据个数
     * 
     * @param lines
     * @return
     * @since
     */
    private static List<CategoryData> filterCategoryData(List<String> lines) {
        
        LOG.info("过滤前的行数："+lines.size());
        
        // 特殊处理pNdCode==ROOT的情况
        List<CategoryData> categoryDatas = new ArrayList<CategoryData>();  //FIXME 使用sdk的模型是否合适(controller)
        Set<String> validCurrentNdCodeSet = new HashSet<String>();// 放入合法行的ndCode (主要为了去重)
        String separator = "\\|";// FIXME 避免写死
        int chunkNum = 5;// FIXME 避免写死

        int lineIndex = 0;//用于标记在文件中的位置（行）
        for (String line : lines) {
            lineIndex++;
            if (StringUtils.isNotEmpty(line)) {
                String[] chunks = line.split(separator);
                if (chunks.length == chunkNum) {
                    String categoryNdCode = chunks[0];
                    String parentNdCode = chunks[1];
                    String currentNdCode = chunks[2];

                    // 检查是否已在合法行中
                    if (validCurrentNdCodeSet.contains(currentNdCode)) {
                        
                        LOG.warn(lineIndex + ":" + line + " 具体原因：重复ndCode");
                       
                        continue;
                    }
                    if (StringUtils.isEmpty(categoryNdCode) || StringUtils.isEmpty(parentNdCode)
                            || StringUtils.isEmpty(currentNdCode)) {
                        
                        LOG.warn(lineIndex + ":" + line+"  错误原因：分类维度、父结点、当前结点至少一个为空");
                       
                        continue;
                    }
                    // 是否现有分类维度
                    NdCodePattern ndCodePattern = NdCodePattern.fromString(categoryNdCode);
                    if (ndCodePattern == null) {
                        
                        LOG.warn(lineIndex + ":" + line+"  错误原因：分类维度不存在");
                        
                        continue;
                    }
                    // 当前维度数据长度
                    if (currentNdCode.length() != ndCodePattern.getLength()) {
                       
                        LOG.warn(lineIndex + ":" + line+"  错误原因：当前结点编码长度不对");
                        
                        continue;
                    }

                    // 父结点维度数据长度
                    if (!ImportDataServiceImpl.TOP_LEVEL_PARENT.equals(parentNdCode)
                            && parentNdCode.length() != currentNdCode.length()) {
                        
                        LOG.warn(lineIndex + ":" + line+"  错误原因：父结点编码长度不对");
                        
                        continue;
                    }

                    // 前缀与分类维度ndCode编码相同
                    if (!categoryNdCode.equals(currentNdCode.substring(0, categoryNdCode.length()))) {
                        
                        LOG.warn(lineIndex + ":" + line+"  错误原因：当前ndCode与分类维度不匹配");
                        
                        continue;
                    }

                    if (!ImportDataServiceImpl.TOP_LEVEL_PARENT.equals(parentNdCode)
                            && !categoryNdCode.equals(parentNdCode.substring(0, categoryNdCode.length()))) {
                        
                        LOG.warn(lineIndex + ":" + line+"  错误原因：父结点与分类维度不匹配");
                        
                        continue;
                    }

                    // 去除右边的"0",父结点是当前结点的前缀
                    if (!ImportDataServiceImpl.TOP_LEVEL_PARENT.equals(parentNdCode)) {
                        String parentPrefix = StringUtils.stripEnd(parentNdCode, "0");
                        String currentPrefix = StringUtils.stripEnd(currentNdCode, "0");
                        if (!currentPrefix.contains(parentPrefix) || parentPrefix.contains(currentPrefix)) {
                           
                            LOG.warn(lineIndex + ":" + line+"  错误原因：父子结点不匹配");
                           
                            continue;
                        }
                    }
                    
                    Integer orderNum = null;
                    if (ImportDataServiceImpl.TOP_LEVEL_PARENT.equals(parentNdCode)) {
                        int minPrefix = ndCodePattern.getMinPrefixLength(); // FIXME 有可能会出现问题
                        orderNum = Integer.valueOf(currentNdCode.substring(minPrefix,
                                                                           minPrefix
                                                                                   + ndCodePattern.getExtendDigit(minPrefix)));
                    } else {
                        orderNum = ndCodePattern.getOrderNum(parentNdCode, currentNdCode);
                    }

                    if (orderNum == null || orderNum == 0) {
                        
                        LOG.warn(lineIndex + ":" + line + "  错误原因： 比较复杂，编码不规范或者粒度太大，暂时处理不了");
                        
                        continue;
                    }

                    // cNdCode,pNdCode,ndCode,title,shortName
                    CategoryData data = new CategoryData();
                    data.setCategory(chunks[0]);
                    data.setParent(chunks[1]);
                    data.setNdCode(chunks[2]);
                    data.setTitle(chunks[3]);
                    data.setShortName(chunks[4]);
                    data.setOrderNum(orderNum);
                    
                    validCurrentNdCodeSet.add(currentNdCode);
                    categoryDatas.add(data);
                } else {
                   
                    LOG.warn(lineIndex + ":" + line + "以" + separator + "模式来分，数目不对，期望：" + chunkNum + "实际："
                            + chunks.length);
                }
            } else {
               
                LOG.warn(lineIndex + ":" + "空行");
            }

        }
        
        LOG.info("过滤后的行数："+categoryDatas.size());
        
        return categoryDatas;
    }

    /**
     * 打开流，得到每一行的数据
     * 
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @since
     */
    private  List<String> readLineToList(String fileName) {
        List<String> lines = new ArrayList<String>();
        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            // 从文件系统中的某个文件中获取字节
            try (   FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis,"utf-8");// InputStreamReader 是字节流通向字符流的桥梁,
                    BufferedReader br = new BufferedReader(isr)  // 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            ) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            
            LOG.info("找不到指定文件");
            
        } catch (IOException e) {
            
            LOG.info("读取文件失败");
            
        }
        
        LOG.info("读取了文件行数："+lines.size());
        
        return lines;
    }
    

}
