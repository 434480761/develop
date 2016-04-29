package nd.esp.service.lifecycle.support.valid.view.filter;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.busi.CommonHelper;

/**
 * @title what to do .
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/6 19:51
 */
public class TagsFilter extends InputFilter {
    @Override
    public void doFilter(ResourceViewModel viewModel) {

        if(!CommonHelper.checkListLength(viewModel.getTags(), 1000)){
            throw new WafSimpleException(LifeCircleErrorMessageMapper.CheckTagsLengthFail.getCode(), LifeCircleErrorMessageMapper.CheckTagsLengthFail.getMessage());
        }
        if (getNextFilter() != null) {
            getNextFilter().doFilter(viewModel);

        }

    }


}
