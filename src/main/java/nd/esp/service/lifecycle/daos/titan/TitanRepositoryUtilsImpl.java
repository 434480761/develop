package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TitanSync;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuran on 2016/7/4.
 */
@Repository
public class TitanRepositoryUtilsImpl implements TitanRepositoryUtils{
    private final static Logger LOG = LoggerFactory.getLogger(TitanRepositoryUtilsImpl.class);
    @Autowired
    private TitanSyncRepository titanSyncRepository;
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
            e.printStackTrace();
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
                e.printStackTrace();
                LOG.error("titan数据同步,add异常数据到mysql失败 primaryCategory：{}  errorType:{}  source:{}",primaryCategory,errorType,source);
            }
        } else {
            titanSync.setExecuteTimes(titanSync.getExecuteTimes() + 1);
            try {
                titanSyncRepository.update(titanSync);
            } catch (EspStoreException e) {
                e.printStackTrace();
                LOG.error("titan数据同步,update异常数据到mysql失败 primaryCategory：{}  errorType:{}  source:{}",primaryCategory,errorType,source);

            }
        }
    }

    public void titanSync4MysqlDelete(TitanSyncType errorType, String primaryCategory, String source){
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        example.setType(errorType.toString());
        TitanSync titanSync = null;
        try {
            titanSync = titanSyncRepository.getByExample(example);
        } catch (EspStoreException e) {
            e.printStackTrace();
            LOG.info("");
        }
        if (titanSync != null){
            titanSyncRepository.delete(titanSync);
        }
    }

    public void titanSync4MysqlDeleteAll(String primaryCategory, String source){
        TitanSync example = new TitanSync();
        example.setPrimaryCategory(primaryCategory);
        example.setResource(source);
        try {
            titanSyncRepository.deleteAllByExample(example);
        } catch (EspStoreException e) {
            e.printStackTrace();
            LOG.error("titan数据同步,删除所有异常数据失败 primaryCategory：{}  source:{}",primaryCategory,source);
        }

    }
}
