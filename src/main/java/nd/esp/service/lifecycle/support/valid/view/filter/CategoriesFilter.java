package nd.esp.service.lifecycle.support.valid.view.filter;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * @title what to do .
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/6 19:51
 */
public class CategoriesFilter extends InputFilter {


    private static final Logger LOG = LoggerFactory.getLogger(CommonHelper.class);
    @Override
    public void doFilter(ResourceViewModel viewModel) {

        Map<String, List<? extends ResClassificationViewModel>> categories = viewModel.getCategories();
        if(categories != null){
            for(String key : categories.keySet()){
                List<? extends ResClassificationViewModel> cList = categories.get(key);
                if(cList != null && !cList.isEmpty()){
                    for (ResClassificationViewModel c : cList) {
                        if(StringUtils.isNotEmpty(c.getTaxonpath())){
                            if(!CommonHelper.checkCategoryPattern(c.getTaxonpath())){
                                LOG.error("taxonpath不对，{}",c.getTaxonpath());
                                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckTaxonpathFail);
                            }
                        }
                    }
                }
            }
        }
        if (getNextFilter() != null) {
            getNextFilter().doFilter(viewModel);

        }

    }


}
