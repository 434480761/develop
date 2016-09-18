package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;

/**
 * Created by Administrator on 2016/9/14.
 */
public class EducationToTitanBeanUtils {
    public static TitanModel toVertex(EspEntity entity){
        TitanModel model = null;
        if (entity instanceof Asset){
            model = BeanMapperUtils.beanMapper(entity,TitanAssets.class);
        }
        if (entity instanceof ResCoverage){
            model = BeanMapperUtils.beanMapper(entity,TitanResCoverageVertex.class);
        }

        if (entity instanceof ResourceCategory){
            model = BeanMapperUtils.beanMapper(entity,TitanResourceCategory.class);
        }

        if (entity instanceof TechInfo){
            model = BeanMapperUtils.beanMapper(entity,TitanTechInfo.class);
        }

        if (entity instanceof ResourceStatistical){
            model = BeanMapperUtils.beanMapper(entity, TitanResourceStatistical.class);
        }

        return model;
    }

    public static TitanModel toEdge(EspEntity entity){
        TitanModel model = null;
        if (entity instanceof  ResCoverage){
            model = BeanMapperUtils.beanMapper(entity, TitanResCoverageEdge.class);
        }

        if (entity instanceof ResourceCategory){
            model = BeanMapperUtils.beanMapper(entity,TitanResourceCategoryEdge.class);
        }

        if (entity instanceof TechInfo){
            model = BeanMapperUtils.beanMapper(entity,TitanTechInfoEdge.class);
        }

        if (entity instanceof ResourceRelation){
            model = BeanMapperUtils.beanMapper(entity,TitanResourceRelationEdge.class);
        }

        if (entity instanceof ResourceStatistical){
            model = BeanMapperUtils.beanMapper(entity, TitanResourceStatisticalEdge.class);
        }

        return model;
    }
}
