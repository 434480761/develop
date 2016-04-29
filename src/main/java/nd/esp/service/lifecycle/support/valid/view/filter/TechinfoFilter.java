package nd.esp.service.lifecycle.support.valid.view.filter;

import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * @title what to do .
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/6 19:51
 */
public class TechinfoFilter extends InputFilter {
    @Override
    public void doFilter(ResourceViewModel viewModel) {

        Map<String,? extends ResTechInfoViewModel> techInfoMap = viewModel.getTechInfo();
        if(techInfoMap == null || !techInfoMap.containsKey("href")){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoFail);
        }
        if (getNextFilter() != null) {
            getNextFilter().doFilter(viewModel);

        }

    }


}
