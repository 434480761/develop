package com.nd.esp.task.worker.buss.document_transcode.utils;

import com.nd.esp.task.worker.buss.document_transcode.utils.CommandlineUtil;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class Office2pdfUtil {

    public static void transferOffice2Pdf(String inputFilePath, String outputFilePath) throws Exception {
        String cmd = "officetopdf " + inputFilePath + " " + outputFilePath;
        StringBuffer output = new StringBuffer();
        StringBuffer logMsg = new StringBuffer();
        CommandlineUtil.RunCommand(cmd, output, logMsg);
    }
}
