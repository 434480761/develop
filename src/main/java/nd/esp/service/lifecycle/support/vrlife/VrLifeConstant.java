package nd.esp.service.lifecycle.support.vrlife;

import java.util.ArrayList;
import java.util.List;

/**
 * VR人生 -- 常量
 * @author xiezy
 * @date 2016年8月3日
 */
public class VrLifeConstant {
	
	//资源组合部件的类型
	public final static String TYPE_HAIRS_TYLE = "hair_style";
	public final static String TYPE_UPPER_BODY = "upper_body";
	public final static String TYPE_HEAD_STYLE = "head_style";
	public final static String TYPE_LOWER_BODY = "Lower_body";
	public final static String TYPE_FOOT       = "foot";
	
	/**
	 * 获取组合部件的全部类型
	 * @author xiezy
	 * @date 2016年8月3日
	 * @return
	 */
	public static List<String> getPartTypeList(){
		List<String> list = new ArrayList<String>();
		list.add(VrLifeConstant.TYPE_HAIRS_TYLE);
		list.add(VrLifeConstant.TYPE_UPPER_BODY);
		list.add(VrLifeConstant.TYPE_HEAD_STYLE);
		list.add(VrLifeConstant.TYPE_LOWER_BODY);
		list.add(VrLifeConstant.TYPE_FOOT);
		
		return list;
	}
}
