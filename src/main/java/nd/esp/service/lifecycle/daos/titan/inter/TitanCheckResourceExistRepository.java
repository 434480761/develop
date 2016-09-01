package nd.esp.service.lifecycle.daos.titan.inter;

import java.util.List;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.busi.titan.CheckResourceModel;

public interface TitanCheckResourceExistRepository{

    void checkOneResourceInTitan(Education education, List<ResCoverage> resCoverages,
            List<ResourceCategory> resourceCategories, List<TechInfo> techInfos,
            List<ResourceRelation> resourceRelations, List<ResourceStatistical> statistic);

    void checkResourcesInTitan(CheckResourceModel checkResourceModel);

    void checkResourceRelations(List<ResourceRelation> existRelation);

}

