package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanSyncTimerTask;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TitanSync;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.services.titan.TitanSyncService;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/7/11.
 */
@Repository
public class TitanSyncTimerTaskImpl implements TitanSyncTimerTask {
    private final static Logger LOG = LoggerFactory.getLogger(TitanSyncTimerTaskImpl.class);
    public static int MAX_REPORT_TIMES = 1000;

    @Autowired
    private TitanSyncService titanSyncService;

    @Autowired
    private TitanSyncRepository titanSyncRepository;

    @Autowired
    @Qualifier(value = "defaultJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public void sync(){
        while (true){
            if (checkHaveData()){
                LOG.info("titan sync start");
                syncData();
            } else {
                LOG.info("titan sync sleeping...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void syncData() {
        int page = 0;
        Page<TitanSync> resourcePage;
        do {
            String fieldName = "createTime";
            int row = 10;
            List<TitanSync> entitylist;
            List<Item<? extends Object>> items = new ArrayList<>();
            Sort sort = new Sort(Sort.Direction.ASC, fieldName);
            Pageable pageable = new PageRequest(page, row, sort);
            try {
                resourcePage = titanSyncRepository.findByItems(items, pageable);
            } catch (EspStoreException e) {
                e.printStackTrace();
                return;
            }
            if (resourcePage == null) {
                return;
            }
            entitylist = resourcePage.getContent();
            if (entitylist == null) {
                continue;
            }
            for (TitanSync titanSync : entitylist) {
                if(titanSync.getExecuteTimes() < MAX_REPORT_TIMES){
                    if (TitanSyncType.DROP_RESOURCE_ERROR.equals(TitanSyncType.value(titanSync.getType()))) {
                        titanSyncService.deleteResource(titanSync.getPrimaryCategory(), titanSync.getResource());
                    } else if (TitanSyncType.SAVE_OR_UPDATE_ERROR.equals(TitanSyncType.value(titanSync.getType()))) {
                        titanSyncService.reportResource(titanSync.getPrimaryCategory(), titanSync.getResource());
                    }
                }
            }

        } while (++page < resourcePage.getTotalPages());
    }

    private boolean checkHaveData(){
        String script = "select count(*) from titan_sync";

        Long total = jdbcTemplate.queryForLong(script);

        if (total > 0){
            return true;
        }

        return false;
    }
}
