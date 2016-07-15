package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TitanSync;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.services.titan.TitanSyncService;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by liuran on 2016/7/11.
 */
@Component
public class TitanSyncTimerTask {
    private final static Logger LOG = LoggerFactory.getLogger(TitanSyncTimerTask.class);
    public static int MAX_REPORT_TIMES = 200;
    public static boolean LOCKED = false;

    @Autowired
    private TitanSyncService titanSyncService;

    @Autowired
    private TitanSyncRepository titanSyncRepository;

    @Autowired
    @Qualifier(value = "defaultJdbcTemplate")
    private JdbcTemplate jdbcTemplate;


//    @Scheduled(fixedRate=60000)
    public void syncTask(){
        if(LOCKED){
            LOG.info("正在同步数据....");
            return;
        }
        if (checkHaveData()){
            LOCKED = true;
            LOG.info("titan sync start");
            try{
                syncData();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                LOCKED = false;
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
