package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.*;

import java.util.List;

/**
 * Created by liuran on 2016/7/26.
 */
public interface TitanImportRepository {
    boolean importOneData(Education education,
                          List<ResCoverage> resCoverageList,
                          List<ResourceCategory> resourceCategoryList,
                          List<TechInfo> techInfos);

    boolean importStatistical(List<ResourceStatistical> statisticalList);

    boolean batchImportRelation(List<ResourceRelation> resourceRelation);

    boolean checkResourceExistInTitan(Education education);

    boolean checkResourceAllInTitan(Education education, List<ResCoverage> resCoverageList,
                                    List<ResourceCategory> resourceCategoryList,
                                    List<TechInfo> techInfos,
                                    List<ResourceRelation> resourceRelationList);
    boolean checkResourceAllInTitanDetail(Education education, List<ResCoverage> resCoverageList,
                                    List<ResourceCategory> resourceCategoryList,
                                    List<TechInfo> techInfos,
                                    List<ResourceRelation> resourceRelationList);
}
