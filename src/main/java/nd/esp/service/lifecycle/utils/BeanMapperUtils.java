package nd.esp.service.lifecycle.utils;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;

/**
 * @title 将Object进行mapper操作
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @see com.nd.gaea.rest.o2o.JacksonCustomObjectMapper
 * @create 2015年3月18日 下午6:15:06
 */
public class BeanMapperUtils {
    
 public static void main(String[] args) throws IOException {
        
        AccessModel model =new AccessModel();
        model.setUuid(UUID.randomUUID());
        JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
        System.out.println( mapper.writeValueAsString(model));
        System.out.println(BeanMapperUtils.mapper(model , Map.class));
    }
 
    private static ModelMapper modelMapper = new ModelMapper();
    static{
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
    }
//    static {
//        // 配置教材的扩展属性映射方式
//        PropertyMap<TeachingMaterial, TeachingMaterialModel> orderMap = new PropertyMap<TeachingMaterial, TeachingMaterialModel>() {
//
//            @Override
//            protected void configure() {
//                map().getExtProperties().setIsbn(source.getIsbn());
//                map().getExtProperties().setAttachments(source.getAttachments());
//                map().getExtProperties().setCriterion(source.getCriterion());
//
//            }
//        };
//        modelMapper.addMappings(orderMap);
//    }
    
    static{
    	//资源自定义扩展属性映射转换
    	PropertyMap<ResourceViewModel, ResourceModel> resViewModel2ModelMap = new PropertyMap<ResourceViewModel, ResourceModel>() {
    		@Override
    		protected void configure() {
    			if(source != null && source.getCustomProperties() != null && CollectionUtils.isNotEmpty(source.getCustomProperties().keySet())){
    				map().setCustomProperties(ObjectUtils.toJson(source.getCustomProperties()));
    			}
    		}
		};
    	
//    	PropertyMap<ResourceModel, ResourceViewModel> resModel2ViewModelMap = new PropertyMap<ResourceModel, ResourceViewModel>() {
//    		@Override
//    		protected void configure() {
//    			if(source != null && StringUtils.isNotEmpty(source.getCustomProperties())){
//    				map().setCustomProperties(ObjectUtils.fromJson(source.getCustomProperties(), LinkedHashMap.class));
//    			}
//    		}
//		};
		
		modelMapper.addMappings(resViewModel2ModelMap);
//		modelMapper.addMappings(resModel2ViewModelMap);
    }

	/**mapper成对应的泛型对象
	 * @param origin
	 * @param t
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T> T mapper(Object origin, T t) throws IOException {

		JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
		T mapperT = (T) mapper.readValue(mapper.writeValueAsString(origin),
				t.getClass());

		return mapperT;
	}
	@SuppressWarnings("unchecked")
	public static <T> T mapper(Object origin, Class<T>t) throws IOException {
		
		JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
		T mapperT = (T) mapper.readValue(mapper.writeValueAsString(origin),
				t);
		
		return mapperT;
	}
	@SuppressWarnings("unchecked")
	public static <T> T mapperOnString(String origin, Class<T>t) throws IOException {
		
		JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
		T mapperT = (T) mapper.readValue(origin,
				t);
		
		return mapperT;
	}
	
	public static <T> T beanMapper(Object source,Class<T> target){
		return modelMapper.map(source, target);
	}

}
