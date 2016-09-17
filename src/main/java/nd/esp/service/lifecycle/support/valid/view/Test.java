package nd.esp.service.lifecycle.support.valid.view;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.valid.view.filter.InputFilter;
import nd.esp.service.lifecycle.support.valid.view.filter.KeywordsFilter;
import nd.esp.service.lifecycle.support.valid.view.filter.UuidFilter;

import java.util.Arrays;
import java.util.UUID;

/**
 * @title what to do .
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/6 19:59
 */
public class Test {

    public static void main(String[] args) {

        InputFilter keywordsFilter =new KeywordsFilter();
        InputFilter uuidFilter =new UuidFilter();
        uuidFilter.setNextFilter(keywordsFilter);
        ResourceViewModel viewModel =new ResourceViewModel();
        viewModel.setIdentifier(UUID.randomUUID().toString());
        viewModel.setKeywords(Arrays.asList("123456789"));
        uuidFilter.doFilter(viewModel);






    }
}
