package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TitanSync;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/7/4.
 */
@Repository
public class TitanRepositoryUtilsImpl implements TitanRepositoryUtils{
    private final static Logger LOG = LoggerFactory.getLogger(TitanRepositoryUtilsImpl.class);
    @Autowired
    private TitanSyncRepository titanSyncRepository;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    /**
     * Sync导入失败增加一条数据
     * */
    public void titanSync4MysqlImportAdd(TitanSyncType errorType, String primaryCategory, String source){
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        List<TitanSync> titanSyncList = null;

        try {
            titanSyncList = titanSyncRepository.getAllByExample(example);
        } catch (EspStoreException e) {
//            e.printStackTrace();
            LOG.info("");
        }

        /**
         * 获取Sync数据同步条件，如果新增加的真删除资源，则清空其它的数据同步条件
         * */
        List<TitanSync> titanSyncUpdate = new ArrayList<>();
        if(titanSyncList != null && titanSyncList.size() > 0){
            for(TitanSync ts : titanSyncList){
                if(TitanSyncType.value(ts.getType()).equals(errorType)){
                    titanSyncUpdate.add(ts);
                } else if(TitanSyncType.DROP_RESOURCE_ERROR.equals(errorType)){
                    titanSyncRepository.delete(ts);
                }
            }
        }

        if(titanSyncUpdate.size() == 0){
            TitanSync  titanSync = new TitanSync();
            titanSync.setResource(source);
            titanSync.setPrimaryCategory(primaryCategory);
            titanSync.setIdentifier(UUID.randomUUID().toString());
            titanSync.setCreateTime(new Date().getTime());
            titanSync.setExecuteTimes(0);
            titanSync.setType(errorType.toString());
            titanSync.setDescription("");
            titanSync.setTitle("");
            titanSync.setLevel(0);
            try {
                titanSyncRepository.add(titanSync);
            } catch (EspStoreException e) {
                LOG.error("titan数据同步,add异常数据到mysql失败 primaryCategory：{}  errorType:{}  source:{}",primaryCategory,errorType,source);
            }
        } else {
            for (TitanSync titanSync : titanSyncUpdate){
                titanSync.setExecuteTimes(titanSync.getExecuteTimes() + 1);
                titanSync.setCreateTime(System.currentTimeMillis());
            }
            try {
                titanSyncRepository.batchAdd(titanSyncUpdate);
            } catch (EspStoreException e) {
                LOG.error("titan数据同步,update异常数据到mysql失败 primaryCategory：{}  errorType:{}  source:{}",primaryCategory,errorType,source);

            }
        }
    }

    @Override
    public void titanSync4MysqlAdd(TitanSyncType errorType, String primaryCategory, String source, int executeTimes) {
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        List<TitanSync> titanSyncList = null;
        TitanSync titanSync = null;
        try {
            titanSyncList = titanSyncRepository.getAllByExample(example);
        } catch (EspStoreException e) {
//            e.printStackTrace();
            LOG.info("");
        }

        /**
         * 获取Sync数据同步条件，如果新增加的真删除资源，则清空其它的数据同步条件
         * */
        if(titanSyncList != null && titanSyncList.size() > 0){
            for(TitanSync ts : titanSyncList){
                if(TitanSyncType.value(ts.getType()).equals(errorType)){
                    titanSync = ts;
                } else if(TitanSyncType.DROP_RESOURCE_ERROR.equals(errorType)){
                    titanSyncRepository.delete(ts);
                }
            }
        }

        if(titanSync == null){
            titanSync = new TitanSync();
            titanSync.setResource(source);
            titanSync.setPrimaryCategory(primaryCategory);
            titanSync.setIdentifier(UUID.randomUUID().toString());
            titanSync.setCreateTime(new Date().getTime());
            titanSync.setExecuteTimes(executeTimes);
            titanSync.setType(errorType.toString());
            titanSync.setDescription("");
            titanSync.setTitle("");
            titanSync.setLevel(0);
            try {
                titanSyncRepository.add(titanSync);
            } catch (EspStoreException e) {
                LOG.error("titan数据同步,add异常数据到mysql失败 primaryCategory：{}  errorType:{}  source:{}",primaryCategory,errorType,source);
            }
        }
    }

    /**
     * 数据保存到titan中失败，保存一条数据到同步表中，如果表中已经存在数据则不进行操作
     * */
    @Override
    public void titanSync4MysqlAdd(TitanSyncType errorType, String primaryCategory, String source) {
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        List<TitanSync> titanSyncList = null;
        TitanSync titanSync = null;
        try {
            titanSyncList = titanSyncRepository.getAllByExample(example);
        } catch (EspStoreException e) {
//            e.printStackTrace();
            LOG.info("");
        }

        /**
         * 获取Sync数据同步条件，如果新增加的真删除资源，则清空其它的数据同步条件
         * */
        if(titanSyncList != null && titanSyncList.size() > 0){
            for(TitanSync ts : titanSyncList){
                if(TitanSyncType.value(ts.getType()).equals(errorType)){
                    titanSync = ts;
                } else if(TitanSyncType.DROP_RESOURCE_ERROR.equals(errorType)){
                    titanSyncRepository.delete(ts);
                }
            }
        }

        if(titanSync == null){
            titanSync = new TitanSync();
            titanSync.setResource(source);
            titanSync.setPrimaryCategory(primaryCategory);
            titanSync.setIdentifier(UUID.randomUUID().toString());
            titanSync.setCreateTime(new Date().getTime());
            titanSync.setExecuteTimes(0);
            titanSync.setType(errorType.toString());
            titanSync.setDescription("");
            titanSync.setTitle("");
            titanSync.setLevel(0);
            try {
                titanSyncRepository.add(titanSync);
            } catch (EspStoreException e) {
                LOG.error("titan数据同步,add异常数据到mysql失败 primaryCategory：{}  errorType:{}  source:{}",primaryCategory,errorType,source);
            }
        }
    }

    /**
     * 1、特殊情况：关系的目标资源或源资源在mysql数据库中不存在，属于异常数据需要进行判断，当源资源和目标资源都存在的时候才把关系的对应数据都加入到mysql中
     * */
    @Override
    public void titanSync4MysqlAdd(TitanSyncType errorType, ResourceRelation resourceRelation) {
        //添加目标资源
        //添加源资源
        if(!checkEducationExistInTitan(resourceRelation.getResType(), resourceRelation.getSourceUuid())){
            titanSync4MysqlAdd(errorType, resourceRelation.getResType(), resourceRelation.getSourceUuid());
        }
        titanSync4MysqlAdd(errorType, resourceRelation.getResourceTargetType(), resourceRelation.getTarget());
    }

    /**
     * 删除同步表中的一条数据
     * */
    public void titanSync4MysqlDelete(TitanSyncType errorType, String primaryCategory, String source){
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        example.setType(errorType.toString());
        TitanSync titanSync = null;
        try {
            titanSync = titanSyncRepository.getByExample(example);
        } catch (EspStoreException e) {
            LOG.info(e.getMessage());
        }
        if (titanSync != null){
            titanSyncRepository.delete(titanSync);
        }
    }

    /**
     * 删除一个资源在同步表中的所有数据
     * */
    public void titanSync4MysqlDeleteAll(String primaryCategory, String source){
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        try {
            titanSyncRepository.deleteAllByExample(example);
        } catch (EspStoreException e) {
            LOG.error("titan数据同步,删除所有异常数据失败 primaryCategory：{}  source:{}",primaryCategory,source);
        }
    }

    /**
     * 检查关系在mysql中是否存在
     * */
    @Override
    public boolean checkRelationExistInMysql(ResourceRelation resourceRelation) {
        if(checkEducationExistInMySql(resourceRelation.getResType(), resourceRelation.getSourceUuid())
                && checkEducationExistInMySql(resourceRelation.getResourceTargetType(), resourceRelation.getTarget())){
           return true;
        }
        return false;
    }

    public void titanSyncUpdateErrorType(TitanSyncType errorType, String primaryCategory, String source,TitanSyncType targetType){
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        example.setType(errorType.toString());
        List<TitanSync> titanSyncList = null;
        try {
            titanSyncList = titanSyncRepository.getAllByExample(example);
            for (TitanSync titanSync : titanSyncList){
                titanSync.setType(targetType.toString());
                titanSync.setCreateTime(System.currentTimeMillis());
            }

            titanSyncRepository.batchAdd(titanSyncList);
        } catch (EspStoreException e) {
            LOG.info("");
        }
    }

    private boolean checkEducationExistInMySql(String primaryCategory, String identifier){
        if(ResourceNdCode.fromString(primaryCategory)==null){
            return false;
        }
        String pc = primaryCategory;
        if ("guidancebooks".equals(primaryCategory)) {
            pc = "teachingmaterials";
        }

        EspRepository<?> espRepository = ServicesManager.get(pc);
        List<Education> educations = null;
        Education example = new Education();
        example.setIdentifier(identifier);
        example.setPrimaryCategory(primaryCategory);
        try {
            List<String> ids = new ArrayList<>();
            ids.add(identifier);
            //TODO 以后方法的改造可能对功能有影响
            educations = (List<Education>) espRepository.getAll(ids);
        } catch (EspStoreException e) {
            //抛出异常默认资源存在
            return true;
        }
        if(CollectionUtils.isNotEmpty(educations)){
            for (Education education : educations){
                if(primaryCategory.equals(education.getPrimaryCategory())){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 当且仅当ID>0的时候返回true
     * */
    private boolean checkEducationExistInTitan(String primaryCategory, String identifier){
        Long id;
        try {
           id = titanCommonRepository.getVertexIdByLabelAndId(primaryCategory, identifier);
        } catch (Exception e) {

            LOG.error("titan_repository error:{}" ,e.getMessage());

            return false;
        }

        if(id != null && id > 0){
            return true;
        }

        return false;
    }
}
