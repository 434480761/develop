package nd.esp.service.lifecycle.support.busi.titan;

import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import java.util.*;

/**
 * Created by liuran on 2016/8/12.
 */
public class TitanResourceUtils {
    public static Map<String, List<ResCoverage>> groupCoverage(List<ResCoverage> coverages ){
        Map<String, List<ResCoverage>> coverageMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(coverages)) {
            for (ResCoverage resCoverage : coverages){
                List<ResCoverage> coverageList = coverageMap.get(resCoverage.getResource());
                if(coverageList == null){
                    coverageList = new ArrayList<>();
                    coverageMap.put(resCoverage.getResource(), coverageList);
                }

                coverageList.add(resCoverage);
            }
        }

        return coverageMap;
    }

    public static Map<String, List<ResourceCategory>> groupCategory(List<ResourceCategory> categories){
        Map<String, List<ResourceCategory>> categoryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(categories)) {
            for(ResourceCategory category : categories){
                List<ResourceCategory> categoryList = categoryMap.get(category.getResource());
                if(categoryList == null){
                    categoryList = new ArrayList<>();
                    categoryMap.put(category.getResource(), categoryList);
                }

                categoryList.add(category);
            }
        }

        return categoryMap;
    }

    public static Map<String, List<TechInfo>> groupTechInfo(List<TechInfo> techInfos){
        Map<String, List<TechInfo>> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(techInfos)){
		    for (TechInfo techInfo : techInfos){
		        List<TechInfo> techInfoList = techInfoMap.get(techInfo.getResource());
		        if(techInfoList == null){
		            techInfoList = new ArrayList<>();
		            techInfoMap.put(techInfo.getResource(), techInfoList);
		        }
		
		        techInfoList.add(techInfo);
		    }
        }
        return techInfoMap;
    }

    public static Map<String, List<ResourceStatistical>> groupStatistical(List<ResourceStatistical> statisticalList){
        Map<String, List<ResourceStatistical>> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(statisticalList)){
            for (ResourceStatistical statistical : statisticalList){
                List<ResourceStatistical> techInfoList = techInfoMap.get(statistical.getResource());
                if(techInfoList == null){
                    techInfoList = new ArrayList<>();
                    techInfoMap.put(statistical.getResource(), techInfoList);
                }

                techInfoList.add(statistical);
            }
        }
        return techInfoMap;
    }

    public static List<TechInfo> distinctTechInfo(List<TechInfo> techInfos){
        Map<String, TechInfo> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(techInfos)) {
            for (TechInfo techInfo : techInfos) {
                techInfoMap.put(techInfo.getTitle(), techInfo);
            }
        }
        return new ArrayList<>(techInfoMap.values());
    }

    public static List<ResCoverage> distinctCoverage(List<ResCoverage> coverages){
        Map<String, ResCoverage> coverageMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(coverages)) {
            for (ResCoverage coverage : coverages) {
                String key = coverage.getResType() + coverage.getTarget() + coverage.getTargetType();
                coverageMap.put(key, coverage);
            }
        }
        return new ArrayList<>(coverageMap.values());
    }

    public static List<String> distinctCategoryPath(List<ResourceCategory> categoryList){
        Set<String> path = new HashSet<>();
        if (CollectionUtils.isNotEmpty(categoryList)) {
            for (ResourceCategory resourceCategory : categoryList) {
                if(StringUtils.isNotEmpty(resourceCategory.getTaxonpath())){
                    path.add(resourceCategory.getTaxonpath());
                }
            }
        }

        return new ArrayList<>(path);
    }

    public static Set<String> distinctCategoryCode(List<ResourceCategory> categoryList){
        Set<String> codes = new HashSet<>();
        if(CollectionUtils.isNotEmpty(categoryList)){
            for(ResourceCategory category : categoryList){
                if(StringUtils.isNotEmpty(category.getTaxoncode())){
                    codes.add(category.getTaxoncode());
                }
            }
        }

        return codes;
    }
}
