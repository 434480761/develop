package com.nd.esp.task.worker.buss.document_transcode.utils;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016/9/19 0019.
 */
public class Pdf2imageUtil {

    public static final int DEFAULT_THUMB_WIDTH = 200;
    public static final int DEFAULT_THUMB_HEIGHT = 200;

    public static void transferPdf2Image(String inputFilePath, String outDirPath, StringBuffer logMsg) throws Exception {
        File outDir = new File(outDirPath);
        if(!outDir.exists()) {
            FileUtils.forceMkdir(outDir);
        }
        FileUtils.cleanDirectory(outDir);

        File pdf = new File(inputFilePath);
        boolean useNonSeqParser = false;
        String password = "";
        String pdfFile = pdf.getAbsolutePath();
        String outputPrefix = null;
        String imageFormat = "jpg";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        int imageType = BufferedImage.TYPE_INT_RGB;
        int resolution = 96;
        try {
            resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        } catch (HeadlessException e) {
            resolution = 96;
        }
        System.out.println(resolution);
        if (outputPrefix == null) {
            outputPrefix = pdfFile.substring(0, pdfFile.lastIndexOf('.'));
        }
        PDDocument document = null;
        try {

            if (useNonSeqParser) {
                document = PDDocument.loadNonSeq(new File(pdfFile), null, password);
            }
            else {
                document = PDDocument.load(pdfFile);
                if (document.isEncrypted()) {
                    try {
                        document.decrypt(password);
                    } catch (CryptographyException e) {
                        throw new IOException("无法转换加密的PDF文档", e);
                    }
                }
            }

            // Make the call
            PDFImageWriter imageWriter = new PDFImageWriter();
            boolean success = imageWriter.writeImage(document, imageFormat, password, startPage, endPage, outDir.getAbsolutePath() + "/", imageType, resolution);
            if (!success) {
                throw new IOException("Error: no writer found for image format '" + imageFormat + "'");
            }
        } finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    public static void makeThumbnails(String imageDirPath, String thumbDirPath) throws IOException {
        File imageDir = new File(imageDirPath);
        if(imageDir.exists() && imageDir.isDirectory()) {
            File thumbDir = new File(thumbDirPath);
            if(!thumbDir.exists()) {
                FileUtils.forceMkdir(thumbDir);
            }
            FileUtils.cleanDirectory(thumbDir);

            for (File file : imageDir.listFiles()) {
                ImgCompress imgCom = new ImgCompress(file.getAbsolutePath());
                imgCom.resizeFix(thumbDirPath+File.separator+file.getName() ,DEFAULT_THUMB_WIDTH, DEFAULT_THUMB_HEIGHT);
            }
        }
    }
}
