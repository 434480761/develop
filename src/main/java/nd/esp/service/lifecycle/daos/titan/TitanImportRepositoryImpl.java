package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanImportRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/7/26.
 */
@Repository
public class TitanImportRepositoryImpl implements TitanImportRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanImportRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;
    @Autowired
    private TitanSyncRepository titanSyncRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;

    @Override
    /**
     * @return false 导入数据失败 ；true 导入成功
     * */
    public boolean importOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {
        Map<String,ResCoverage> coverageMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resCoverageList)){
            for(ResCoverage coverage : resCoverageList){
                String key = coverage.getTarget()+coverage.getStrategy()+coverage.getTargetType();
                if(coverageMap.get(key)==null){
                    coverageMap.put(key, coverage);
                }
            }
        }

        Set<String> categoryPathSet = new HashSet<>();
        Map<String, ResourceCategory> categoryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resourceCategoryList)){
            for (ResourceCategory resourceCategory : resourceCategoryList){
                if(StringUtils.isNotEmpty(resourceCategory.getTaxonpath())){
                    categoryPathSet.add(resourceCategory.getTaxonpath());
                }
                if(categoryMap.get(resourceCategory.getTaxoncode())==null){
                    categoryMap.put(resourceCategory.getTaxoncode(), resourceCategory);
                }

            }
        }

        Map<String, TechInfo> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(techInfos)){
            for (TechInfo techInfo : techInfos){
                if(techInfoMap.get(techInfo.getTitle()) == null){
                    techInfoMap.put(techInfo.getTitle(), techInfo);
                }
            }
        }

        List<ResCoverage> coverageList = new ArrayList<>();
        coverageList.addAll(coverageMap.values());
        List<ResourceCategory> categoryList = new ArrayList<>();
        categoryList.addAll(categoryMap.values());
        List<TechInfo> techInfoList = new ArrayList<>();
        techInfoList.addAll(techInfoMap.values());
        List<String> categoryPathList = new ArrayList<>();
        categoryPathList.addAll(categoryPathSet);

        Map<String, Object> result = TitanScritpUtils.buildScript(education,coverageList,categoryList,techInfoList,categoryPathList);
        //校验addVertex中的参数个数过多，个数超过250返回为null
        if(CollectionUtils.isEmpty(result)){
            //TODO 这种错误情况保存到数据库中，返回true
            saveErrorSource(education);
        } else {
            Long educationId = null;
            try {
                String script = result.get("script").toString();
                Map<String, Object> param = (Map<String, Object>) result.get("param");
                educationId = titanCommonRepository.executeScriptUniqueLong(script, param);
            } catch (Exception e) {
                LOG.error("titanImportErrorData:{}" ,education.getIdentifier());
                e.printStackTrace();
                return  false;
            }
            if(educationId == null){
                return false;
            }
        }

        return true;
    }

    private void saveErrorSource(Education education){
        TitanSync titanSync = new TitanSync();
        titanSync.setIdentifier(UUID.randomUUID().toString());
        titanSync.setLevel(0);
        titanSync.setResource(education.getIdentifier());
        titanSync.setExecuteTimes(999);
        titanSync.setCreateTime(System.currentTimeMillis());
        titanSync.setTitle("");
        titanSync.setDescription("");
        titanSync.setPrimaryCategory(education.getPrimaryCategory());
        titanSync.setType(TitanSyncType.IMPORT_DATA_ERROR.toString());
        try {
            titanSyncRepository.add(titanSync);
        } catch (EspStoreException e) {
            e.printStackTrace();
        }
    }
}
