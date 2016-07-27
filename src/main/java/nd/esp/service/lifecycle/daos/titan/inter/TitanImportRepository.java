package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;

import java.util.List;

/**
 * Created by liuran on 2016/7/26.
 */
public interface TitanImportRepository {
    boolean importOneData(Education education,
                          List<ResCoverage> resCoverageList,
                          List<ResourceCategory> resourceCategoryList,
                          List<TechInfo> techInfos);
}
