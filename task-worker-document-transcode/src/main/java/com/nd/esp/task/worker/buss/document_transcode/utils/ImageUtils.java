package com.nd.esp.task.worker.buss.document_transcode.utils;

import com.nd.esp.task.worker.buss.document_transcode.utils.gson.ObjectUtils;
import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.JimiReader;
import com.sun.jimi.core.JimiWriter;
import com.sun.jimi.core.options.JPGOptions;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	public static Map<String,String> toJPG(String filePath, String dest) throws IOException {
		try {
			if(!filePath.contains(".")){
				return null;
			}

			Image srcImg = Jimi.getImage(filePath);
			Map<String,String> meta = new HashMap<>();
			meta.put("width", String.valueOf(srcImg.getWidth(null)));
			meta.put("height", String.valueOf(srcImg.getHeight(null)));
			meta.put("pagesize", "1");
			meta.put("displaymode", "horizontal");
			if(filePath.lastIndexOf(".jpg") > 0) {
				FileUtils.copyFile(new File(filePath), new File(dest));
				return meta;
			}
			String source = filePath;
//			String dest = source.substring(0,source.lastIndexOf(".")) + ".jpg";
			JPGOptions options = new JPGOptions();
	        options.setQuality(100);
	            
			ImageProducer image = Jimi.getImageProducer(source);
			JimiWriter writer = Jimi.createJimiWriter(dest);
            writer.setSource(image);
            writer.setOptions(options);
            writer.putImage(dest);
			return meta;
		} catch (JimiException e) {
			LOG.error("图片转换出错！",e);
		}
		return null;
	}
}
