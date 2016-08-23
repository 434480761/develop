package nd.esp.service.lifecycle.daos.titan.inter;

import java.util.List;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TechInfo;

/**
 * Created by liuran on 2016/7/26.
 */
public interface TitanImportRepository {
    boolean importOneData(Education education,
                          List<ResCoverage> resCoverageList,
                          List<ResourceCategory> resourceCategoryList,
                          List<TechInfo> techInfos);

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
    public void checkCategories(Education education,List<ResourceCategory> resourceCategoryList);

    void checkResourceAllInTitan2(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList);
}
