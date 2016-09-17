package nd.esp.service.lifecycle.support.annotation.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.support.annotation.MapValid;
import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * 分类维度数据的校验
 * 由于@valid无法满足对分类维度里面的字段校验，专门写个校验器
 * 
 * @author xuzy
 * @version 1.0
 */
public class MapValidator implements ConstraintValidator<MapValid, Map<String,List<? extends ResClassificationViewModel>>>{
	private static String TAXONPATH_MAX_LENGTH = "categories.taxonpath不超过200个字符";
	@Override
	public boolean isValid(Map<String,List<? extends ResClassificationViewModel>> value, ConstraintValidatorContext context) {
		if(CollectionUtils.isEmpty(value)){
			return true;
		}
		Set<String> keySet = value.keySet();
		if(CollectionUtils.isNotEmpty(keySet)){
			for (String string : keySet) {
				List<? extends ResClassificationViewModel> v = value.get(string);
				if(CollectionUtils.isNotEmpty(v)){
					for (ResClassificationViewModel resClassificationViewModel : v) {
						//对taxonpath的长度进行校验
						if(resClassificationViewModel.getTaxonpath() != null && resClassificationViewModel.getTaxonpath().length() > 200){
							//动态生成校验结果
							context.disableDefaultConstraintViolation();
							context.buildConstraintViolationWithTemplate(TAXONPATH_MAX_LENGTH).addConstraintViolation();
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void initialize(MapValid constraintAnnotation) {
	}
}
