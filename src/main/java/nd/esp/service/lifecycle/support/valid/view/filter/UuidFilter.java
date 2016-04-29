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
public class UuidFilter extends InputFilter {
    @Override
    public void doFilter(ResourceViewModel viewModel) {

        if (!CommonHelper.checkUuidPattern(viewModel.getIdentifier())) {
            throw new WafSimpleException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }
        if (getNextFilter() != null) {
            getNextFilter().doFilter(viewModel);

        }

    }


}
