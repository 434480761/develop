package nd.esp.service.lifecycle.support.valid.view;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.valid.view.filter.*;
import org.junit.Assert;

/**
 * @title 视图校验工具类
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/6 20:05
 */
public class InputValidUtil {

    /**
     *
     *
     * @param viewModel
     */
    public static void checkAll(ResourceViewModel viewModel){
        Assert.assertNotNull(viewModel);
        InputFilter keywordsFilter =new KeywordsFilter();
        InputFilter uuidFilter =new UuidFilter();
        InputFilter tagsFilter =new TagsFilter();
        InputFilter techinfoFilter =new TechinfoFilter();
        InputFilter categoriesFilter =new CategoriesFilter();
        uuidFilter.setNextFilter(keywordsFilter);
        keywordsFilter.setNextFilter(tagsFilter);
        tagsFilter.setNextFilter(techinfoFilter);
        techinfoFilter.setNextFilter(categoriesFilter);
        uuidFilter.doFilter(viewModel);

    }
}
