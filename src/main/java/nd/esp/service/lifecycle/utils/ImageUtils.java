package nd.esp.service.lifecycle.utils;

import java.awt.image.ImageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.JimiWriter;
import com.sun.jimi.core.options.JPGOptions;
/**
 * 图片转换工具类
 * @author xuzy
 *
 */
public class ImageUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ImageUtils.class);
	
	/**
	 * 将图片转换为JPG格式
	 * @param filePath	图片路径
	 * @return			转换后的jpg图片路径
	 */
	public static String toJPG(String filePath){
		try {
			if(filePath.lastIndexOf(".jpg") > 0 || !filePath.contains(".")){
				return null;
			}
			String source = filePath;
			String dest = source.substring(0,source.lastIndexOf(".")) + ".jpg";
			JPGOptions options = new JPGOptions();
	        options.setQuality(100);
	            
			ImageProducer image = Jimi.getImageProducer(source);
            JimiWriter writer = Jimi.createJimiWriter(dest);
            writer.setSource(image);
            writer.setOptions(options);
            writer.putImage(dest);
            return dest;
		} catch (JimiException e) {
			LOG.error("图片转换出错！",e);
		}
		return null;
	}
}
