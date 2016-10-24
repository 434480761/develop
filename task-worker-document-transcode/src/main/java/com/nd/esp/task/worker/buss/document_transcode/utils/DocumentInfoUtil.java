package com.nd.esp.task.worker.buss.document_transcode.utils;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.nd.esp.task.worker.buss.document_transcode.utils.gson.ObjectUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/22 0022.
 */
public class DocumentInfoUtil {

    private static final String [] HORIZONTAL_TYPES_ARR ={"ppt", "pptx", "img", "bmp", "jpg", "jpeg", "png", "gif"};
    private static final List<String> HORIZONTAL_TYPES = Arrays.asList(HORIZONTAL_TYPES_ARR);

    public static Map<String,String> getDocumentInfo(String pdfPath, String fileType, long size) throws IOException {
        Map<String, String> meta = new HashMap<>();
        PdfReader reader = new PdfReader(pdfPath);
        int nPages = reader.getNumberOfPages();
        int width = 0;
        int height = 0;
        for (int i=1; i<=nPages; ++i) {
            Rectangle rect = reader.getPageSize(i);
            if (null != rect) {
                if(rect.getWidth()>width) {
                    width = (int)Math.ceil((double)rect.getWidth()*4/3);
                }
                if(rect.getHeight()>height) {
                    height =  (int)Math.ceil((double)rect.getHeight()*4/3);
                }
            }
        }
        meta.put("width", String.valueOf(width));
        meta.put("height", String.valueOf(height));
        meta.put("pagesize", String.valueOf(nPages));
        meta.put("FileSize", String.valueOf(size));
        if(HORIZONTAL_TYPES.contains(fileType)) {
            meta.put("displaymode", "horizontal");
        } else {
            meta.put("displaymode", "vertical");
        }

        return meta;
    }
}
