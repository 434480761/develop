package nd.esp.service.lifecycle.support.annotation.impl;

import nd.esp.service.lifecycle.support.annotation.ParamLength;
import nd.esp.service.lifecycle.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @title 处理参数长度解决者
 * @desc
 * @createtime on 2014/12/2516:58
 */

public class MethodArgumentsLengthResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //只会触发一次
       /* Class<?> paramType = parameter.getParameterType();
        if (parameter.hasParameterAnnotation(ParamLength.class)) {//如果不是这个注解则忽视
            if (String.class.isAssignableFrom(paramType)) {//如果是实现了String接口
                String paramName = parameter.getParameterAnnotation(ParamLength.class).value();
                return StringUtils.hasText(paramName);//判断是否为空 如果value为空 则流程停止
            } else {
                return true;
            }
        }
        return false;*/

        return true;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Class<?> paramType = parameter.getParameterType();
        String paramName = parameter.getParameterAnnotation(ParamLength.class).value();//获取注解中value的值
        int length = Integer.valueOf(paramName);
        Iterator<String> parameterNames =webRequest.getParameterNames();
        String param = parameterNames.next();//参数对应的变量名
        String value = webRequest.getParameter(param);//参数对应的值
        if(value.length() > length){
            //throw new BusinessException("40000","param[" + param + "]'s length bigger than " + length,new ParamLengthException("参数长度异常"));
        }
        return value;
    }
}
