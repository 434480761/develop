package nd.esp.service.lifecycle.support.annotation.impl;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.annotation.Reg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;

/**
 * 正则表达式参数校验类
 * 
 * pattern为正则表达式，可以使用占位符，由于"{}"在正则表达式中有实际意义，所以在这里的占位符暂规定使用"{{}}"
 * 占位符中的变量将从{@link Constant}类中读取
 * 变量须有public static 修饰，返回值类型为{@link String}
 * 
 * @author xuzy
 * @version 1.0
 */
public class RegValidator implements ConstraintValidator<Reg, String>{
	private static final Logger LOG = LoggerFactory.getLogger(RegValidator.class);
	//正则表达式
	private String pattern;
	//值为空时，是否还需要校验
	private boolean isNullValid;
	@Override
	public void initialize(Reg constraintAnnotation) {
		this.pattern = replacePlaceholder(constraintAnnotation.pattern());
		this.isNullValid = constraintAnnotation.isNullValid();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(this.isNullValid || (!this.isNullValid && value != null && !value.isEmpty())){
			if(value == null){
				return false;
			}
			Pattern p = Pattern.compile(pattern);
			Matcher matcher = p.matcher(value);
			if(matcher.find()){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
	
	//占位符内容替换
	private static String replacePlaceholder(String value){
		String reg = "\\{\\{[A-Za-z0-9._]*\\}\\}";
		Pattern pp = Pattern.compile(reg);
		Matcher m = pp.matcher(value);
		//循环替换占位符内容
		while (m.find()) {
			String ph = m.group();
			String key = ph.substring(2, ph.length()-2);
			String v = getPropertyValue(key);
			value = value.replaceAll("\\{\\{"+key+"\\}\\}", v);
		}
		return value;
	}

	private static String getPropertyValue(String key){
		//通过反射机制读取静态变量值
		try {
			Field f = Constant.class.getDeclaredField(key);
			return (String) f.get(null);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			LOG.error("正则校验加载属性出错!",e);
			throw new WafSimpleException(LifeCircleErrorMessageMapper.RegValidationFail.getCode(),LifeCircleErrorMessageMapper.RegValidationFail.getMessage());
		}

	}
}
