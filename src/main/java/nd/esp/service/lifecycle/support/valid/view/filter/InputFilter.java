package nd.esp.service.lifecycle.support.valid.view.filter;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;

/**
 * @title 视图模型输入过滤器
 * @desc
 * @atuh lwx
 * @createtime on 2015/10/6 19:49
 */
public abstract class InputFilter {

    private  InputFilter nextFilter;

    /**
     * 过滤器方法
     *
     * @param viewModel
     */
    public abstract  void doFilter(ResourceViewModel viewModel);


    public InputFilter getNextFilter() {
        return nextFilter;
    }

    public void setNextFilter(InputFilter nextFilter) {
        this.nextFilter = nextFilter;
    }
}
