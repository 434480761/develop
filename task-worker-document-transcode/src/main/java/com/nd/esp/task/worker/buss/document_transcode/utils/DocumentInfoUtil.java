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

    public static String getDocumentInfo(String pdfPath, String fileType) throws IOException {
        Map<String, String> meta = new HashMap<>();
        PdfReader reader = new PdfReader(pdfPath);
        Rectangle rect = reader.getPageSize(1);
        if(null!=rect) {
            meta.put("width", String.valueOf(rect.getWidth()));
            meta.put("height", String.valueOf(rect.getHeight()));
        }
        meta.put("pagesize", String.valueOf(reader.getNumberOfPages()));
        if(HORIZONTAL_TYPES.contains(fileType)) {
            meta.put("displaymode", "horizontal");
        } else {
            meta.put("displaymode", "vertical");
        }

        return ObjectUtils.toJson(meta);
    }
}
