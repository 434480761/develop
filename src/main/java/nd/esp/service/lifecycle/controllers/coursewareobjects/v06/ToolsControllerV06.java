package nd.esp.service.lifecycle.controllers.coursewareobjects.v06;

import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;
import nd.esp.service.lifecycle.services.coursewareobjects.v06.ToolsServiceV06;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.coursewareobjects.v06.CourseWareObjectViewModel;
import nd.esp.service.lifecycle.vos.valid.ValidCoursewareObject4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidCoursewareObjectGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学科工具V0.6 API
 * 
 * @author linsm
 */
@RestController
@RequestMapping("/v0.6/tools")
public class ToolsControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(ToolsControllerV06.class);

    private static final int CONTROLLER_CREATE_TYPE = 0;// 新增学科工具操作
    private static final int CONTROLLER_UPDATE_TYPE = 1;// 修改学科工具操作
    @Autowired
    @Qualifier("toolsServiceV06")
    private ToolsServiceV06 toolsServiceV06;

    /**
     * 学科工具创建
     * 
     * @param viewModel 学科工具对象
     * @param validResult BindingResult
     * @param id 学科工具id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CourseWareObjectViewModel create(@Validated(ValidCoursewareObjectGroup.class) @RequestBody CourseWareObjectViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id) {
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_CREATE_TYPE);

        viewModel = toolsApi(viewModel, CONTROLLER_CREATE_TYPE);

        return viewModel;
    }

    /**
     * 学科工具修改
     * 
     * @param viewModel 学科工具对象
     * @param validResult BindingResult
     * @param id 学科工具id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CourseWareObjectViewModel update(@Validated(ValidCoursewareObject4UpdateGroup.class) @RequestBody CourseWareObjectViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id) {
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_UPDATE_TYPE);

        viewModel = toolsApi(viewModel, CONTROLLER_UPDATE_TYPE);

        return viewModel;
    }

    /**
     * 对入参进行校验
     * 
     * @param viewModel 学科工具对象
     * @param validResult BindingResult
     * @param id 学科工具id
     * @param type 业务类型
     * @since
     */
    private void checkParams(CourseWareObjectViewModel viewModel,
                             BindingResult validResult,
                             String id,
                             int type) {
    	viewModel.setIdentifier(id);
        // 入参合法性校验
        if (type == CONTROLLER_CREATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_TOOL_PARAM_VALID_FAIL",
                                    "TOOLControllerV06",
                                    "create");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.CREATE);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_TOOL_PARAM_VALID_FAIL",
                                    "TOOLControllerV06",
                                    "update");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.UPDATE);
        }
    }

    /**
     * 业务统一方法
     * 
     * @param viewModel 学科工具对象
     * @param type 业务类型
     * @return TOOLViewModel
     * @since
     */
    private CourseWareObjectViewModel toolsApi(CourseWareObjectViewModel viewModel, int type) {
        CourseWareObjectModel model = CommonHelper.convertViewModelIn(viewModel,
                                                CourseWareObjectModel.class,
                                                ResourceNdCode.tools);
        
        if (type == CONTROLLER_CREATE_TYPE) {
            // 创建学科工具
            
            LOG.info("学科工具V06创建学科工具操作，业务逻辑处理");
            
            model = toolsServiceV06.createTools(model);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            // 修改学科工具
            
            LOG.info("学科工具v06更新学科工具操作，业务逻辑处理");
            
            model = toolsServiceV06.updateTools(model);
        }

        viewModel = CommonHelper.convertViewModelOut(model, CourseWareObjectViewModel.class);

        return viewModel;
    }

}
