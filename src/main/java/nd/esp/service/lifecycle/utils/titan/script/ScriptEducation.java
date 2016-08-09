package nd.esp.service.lifecycle.utils.titan.script;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.services.elasticsearch.ES_Search;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import java.util.*;

/**
 * Created by liuran on 2016/8/5.
 */
public class ScriptEducation extends ScriptAbstract{
    private List<ResCoverage> coverages;
    private List<ResourceCategory> categories;

    private Education education;
    public ScriptEducation(Education education, List<ResCoverage> coverages, List<ResourceCategory> categories){
        this.education = education;
        this.coverages = coverages;
        this.categories = categories;
    }

    @Override
    String name() {
        return "EDU";
    }

    @Override
    String resourceIdentifier() {
        return education.getIdentifier();
    }

    @Override
    String resourcePrimaryCategory() {
        return education.getPrimaryCategory();
    }

    @Override
    String nodeLabel() {
        return education.getPrimaryCategory();
    }

    @Override
    String edgeLabel() {
        return null;
    }


    @Override
    Map<String, Object> customProperty() {
        Map<String, Object> customMap = new HashMap<>();
        Set<String> resCoverages = new HashSet<>() ;
        Set<String> categoryCodes = new HashSet<>();
        Set<String> paths = new HashSet<>();
        if(CollectionUtils.isNotEmpty(coverages)){
            for(ResCoverage resCoverage : coverages){
                String setValue4 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy()+"/"+education.getStatus();
                String setValue3 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//"+education.getStatus();
                String setValue2 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy()+"/";
                String setValue1 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//";
                resCoverages.add(setValue1);
                resCoverages.add(setValue2);
                resCoverages.add(setValue3);
                resCoverages.add(setValue4);
            }
        }

        if(CollectionUtils.isNotEmpty(categories)){
            for(ResourceCategory category : categories){
                if(StringUtils.isNotEmpty(category.getTaxonpath())){
                    paths.add(category.getTaxonpath());
                }
                if(StringUtils.isNotEmpty(category.getTaxoncode())){
                    categoryCodes.add(category.getTaxoncode());
                }
            }
        }

        if(CollectionUtils.isNotEmpty(resCoverages)){
            String searchCoverageString = StringUtils.join(resCoverages,",").toLowerCase();
            customMap.put("search_coverage_string",searchCoverageString);
            customMap.put("search_coverage",resCoverages);
        }

        if(CollectionUtils.isNotEmpty(categoryCodes)){
            String searchCodeString = StringUtils.join(categoryCodes, ",").toLowerCase();
            customMap.put("search_code_string",searchCodeString);
            customMap.put("search_code",categoryCodes);
        }

        if(CollectionUtils.isNotEmpty(paths)){
            String searchPathString = StringUtils.join(paths, ",").toLowerCase();
            customMap.put("search_path_string",searchPathString);
            customMap.put("search_path",paths);
        }

        return customMap;
    }

    @Override
    EspEntity entity() {
        return education;
    }

    @Override
    Map<String, Object> searchUniqueNodeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_SearchField.lc_enable.toString(),education.getEnable());
        map.put(ES_SearchField.identifier.toString(),education.getIdentifier());
        map.put("primary_category", education.getPrimaryCategory());
        return map;
    }

    @Override
    Map<String, Object> searchUniqueEdgeProperty() {
        return null;
    }
}
