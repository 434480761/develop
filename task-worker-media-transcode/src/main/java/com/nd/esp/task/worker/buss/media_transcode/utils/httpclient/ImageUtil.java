package com.nd.esp.task.worker.buss.media_transcode.utils.httpclient;

/**
 * Created by qil on 2016/9/26 0026.
 */
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageUtil {

    /**
     *
     * 对图片裁剪，并把裁剪完蛋新图片保存 。
     */

    public static void cut(String srcPath, int numPerEdge) throws IOException {
        FileInputStream is = null;
        ImageInputStream iis = null;
        try {
            // 读取图片文件
            is = new FileInputStream(srcPath);

            Iterator<ImageReader> it = ImageIO
                    .getImageReadersByFormatName("jpg");

            ImageReader reader = it.next();

            // 获取图片流
            iis = ImageIO.createImageInputStream(is);

            reader.setInput(iis, true);
            int height = reader.getHeight(0);
            int width = reader.getWidth(0);
            int x = 0;
            int y = 0;
            ImageReadParam param = reader.getDefaultReadParam();
            for(int i=0; i<numPerEdge*numPerEdge; ++i) {
                Rectangle rect = new Rectangle(x, y, width/numPerEdge, height/numPerEdge);
                if((i+1)%numPerEdge!=0) {
                    x += width/numPerEdge;
                } else {
                    y += height/numPerEdge;
                    x=0;
                }
                param.setSourceRegion(rect);
                BufferedImage bi = reader.read(0, param);

                // 保存新图片
                ImageIO.write(bi, "jpg", new File(srcPath+File.separator+"thumb"+(i+1)+".jpg"));
            }
        } finally {
            if (is != null)
                is.close();
            if (iis != null)
                iis.close();
        }

    }

    public static void main(String[] args) throws Exception {
        String name = "d:\\1.jpg";
        ImageUtil.cut(name, 4);
    }

}

