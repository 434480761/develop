/* =============================================================
 * Created: [2015年7月21日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.repository.model.CategoryData;

/**
 * @author linsm
 * @since 
 *
 */
public interface ImportDataService {
    
    public Map<String, Long> importCategoryData(List<CategoryData> categoryDatas);
    
    public Map<String, Long> updateCategoryRelationOrderNum(String patternName);

}
