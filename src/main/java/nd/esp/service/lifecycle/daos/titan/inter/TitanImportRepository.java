package nd.esp.service.lifecycle.daos.titan.inter;

import java.util.List;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.*;

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
    public void checkCategoryEdges(Education education,List<ResourceCategory> resourceCategoryList);
    
    public void checkCategoryNodes(Education education,List<ResourceCategory> resourceCategoryList);
    
    void checkResourceAllInTitan2(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList);
}
