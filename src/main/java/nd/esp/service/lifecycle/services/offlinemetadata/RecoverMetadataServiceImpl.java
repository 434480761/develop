/* =============================================================
 * Created: [2015年11月18日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.offlinemetadata;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;

/**
 * @author linsm
 * @since
 */
@Service
public class RecoverMetadataServiceImpl implements RecoverMetadataService {

    private static final Logger Log = LoggerFactory.getLogger(RecoverMetadataServiceImpl.class);

    @Autowired
    private CommonServiceHelper commonServiceHelper;

    @Autowired
    private OfflineService offlineService;

    /*
     * (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.RecoverMetadataService#recoverMetadata(java.lang.String,
     * java.util.Set)
     */
    @Override
    public void recoverMetadata(String resourceType, List<String> identifiers) {
        if (CollectionUtils.isNotEmpty(identifiers)) {
            for (String identifier : identifiers) {
                offlineService.writeToCsSync(resourceType, identifier);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.RecoverMetadataService#recoverMetaData(java.lang.String,
     * java.util.Date)
     */
    @Override
    public int recoverMetaData(String resourceType, BigDecimal upToLastUpdate) {
        Item<BigDecimal> item = new Item<BigDecimal>();
        item.setKey("dblastUpdate");
        item.setComparsionOperator(ComparsionOperator.LT);
        item.setLogicalOperator(LogicalOperator.AND);
        item.setValue(ValueUtils.newValue(upToLastUpdate));

        List<Item<? extends Object>> items = new ArrayList<>();
        items.add(item);

        Page<? extends EspEntity> resourcePage = null;
        int page = 0;
        int rows = 500;
        Pageable pageable = null;
        
        int triggeredNum = 0;
        do {
            // 分页查询
            pageable = new PageRequest(page, rows, Direction.DESC, "dblastUpdate");
            try {
                resourcePage = commonServiceHelper.getRepository(resourceType).findByItems(items, pageable);
            } catch (EspStoreException e) {
                throw new LifeCircleException(e.getMessage());
            }
            List<? extends EspEntity> espEntities = resourcePage.getContent();

            if (CollectionUtils.isNotEmpty(espEntities)) {
                for (EspEntity espEntity : espEntities) {
                    offlineService.writeToCsSync(resourceType, espEntity.getIdentifier());
                }

                triggeredNum+= espEntities.size();
                // 记录已经处理到的时间结点;
                Log.error("offlinemetadata,处理资源类型：{},处理到时间:{}",
                          resourceType,
                          ((Education) espEntities.get(espEntities.size() - 1)).getLastUpdate().getTime());
            }

        } while (++page < resourcePage.getTotalPages());

        return triggeredNum;
    }

    /*
     * (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.RecoverMetadataService#recoverMetaData(java.util.Date)
     */
    @Override
    public void recoverMetaData(Timestamp lastUpDate) {
        // TODO Auto-generated method stub

    }

}
