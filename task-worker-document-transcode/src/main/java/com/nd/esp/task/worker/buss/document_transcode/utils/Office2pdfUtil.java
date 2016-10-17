package com.nd.esp.task.worker.buss.document_transcode.utils;


import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Office2pdfUtil {
    private final static Logger LOG = LoggerFactory
            .getLogger(Office2pdfUtil.class);

    private static final int wdFormatPDF = 17;
    private static final int xlTypePDF = 0;
    private static final int ppSaveAsPDF = 32;

    public static void convert2PDF(String inputFile, String pdfFile) throws Exception {
        String suffix = "";
        if(inputFile.lastIndexOf('.')>0 && inputFile.lastIndexOf('.')<inputFile.length()-1) {
            suffix = inputFile.substring(inputFile.lastIndexOf('.')+1);
        }
        File file = new File(inputFile);
        if(!file.exists()){
            System.out.println("文件不存在！");
            return;
        }
        if(suffix.equals("pdf")){
            System.out.println("PDF not need to convert!");
            return ;
        }

        File pdf = new File(pdfFile);
        FileUtils.forceMkdir(pdf.getParentFile());

        if(suffix.equals("doc")||suffix.equals("docx")){
            word2PDF(inputFile, pdfFile);
        }else if(suffix.equals("ppt")||suffix.equals("pptx")||suffix.equals("pptm")){
            ppt2PDF(inputFile, pdfFile);
        }else if(suffix.equals("xls")||suffix.equals("xlsx")||suffix.equals("xlsm")){
            excel2PDF(inputFile, pdfFile);
        }
    }


//    public static void convert2PDF(String[] inputFiles, String[] pdfFiles) {
//        try {
//            for(int i = 0;i<inputFiles.length;i++){
//                String inputFile = inputFiles[i];
//                String pdfFile = pdfFiles[i];
//                if(inputFile==null || inputFile.equals("")) continue;
//                convert2PDF(inputFile,pdfFile);
//            }
//        }catch (Exception ce) {
//
//        }
//    }


    public static void word2PDF(String inputFile,String pdfFile){
        ActiveXComponent app = null;
        Dispatch doc = null;
        try {
//            ComThread.InitSTA();
            app = new ActiveXComponent("Word.Application"); //打开word应用程序
            app.setProperty("Visible", false); //设置word不可见
            Dispatch docs = app.getProperty("Documents").toDispatch(); //获得word中所有打开的文档,返回Documents对象
            //调用Documents对象中Open方法打开文档，并返回打开的文档对象Document
//            doc = Dispatch.call(docs,
//                    "Open",
//                    inputFile,
//                    false,
//                    true
//            ).toDispatch();
//            Dispatch.call(doc,
//                    "ExportAsFixedFormat",
//                    pdfFile,
//                    wdFormatPDF        //word保存为pdf格式宏，值为17
//            );
            doc = Dispatch.invoke(docs, "Open", Dispatch.Method, new Object[] { inputFile, new Variant(false), new Variant(true) }, new int[1]).toDispatch();

            LOG.info("打开文档..." + inputFile);
            LOG.info("转换文档到PDF..." + pdfFile);

            File pdf = new File(pdfFile);
            if (pdf.exists()) {
                pdf.delete();
            }
            Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] { pdf.getAbsolutePath(), new Variant(17) }, new int[1]);
        } catch (ComFailException e) {
            LOG.error("转换文档到PDF错误：", e);
            throw e;
        } catch (Exception e) {
            LOG.error("转换文档到PDF错误：", e);
            throw e;
        } finally {
            if (doc != null) {
                Dispatch.call(doc, "Close", false); //关闭文档
            }
            if (app != null) {
                app.invoke("Quit", new Variant[] {}); //关闭word应用程序
            }
            ComThread.Release();
        }
    }
    public static void excel2PDF(String inputFile,String pdfFile){
        ActiveXComponent app = null;
        Dispatch excel = null;
        try {
//            ComThread.InitSTA();
            app = new ActiveXComponent("Excel.Application");
            app.setProperty("Visible", true);
            Dispatch excels = app.getProperty("Workbooks").toDispatch();
            excel = Dispatch.call(excels,
                    "Open",
                    inputFile,
                    false,
                    true
            ).toDispatch();

            File pdf = new File(pdfFile);
            if (pdf.exists()) {
                pdf.delete();
            }
            Dispatch.call(excel,
                    "ExportAsFixedFormat",
                    xlTypePDF,
                    pdfFile
            );
        } catch (ComFailException e) {
            LOG.error("转换文档到PDF错误：", e);
            throw e;
        } catch (Exception e) {
            LOG.error("转换文档到PDF错误：", e);
            throw e;
        } finally {
            if (excel != null) {
                Dispatch.call(excel, "Close",false);
            }
            if (app != null) {
                app.invoke("Quit");
            }
            ComThread.Release();
        }
    }
    public static void ppt2PDF(String inputFile,String pdfFile){
        ActiveXComponent app = null;
        Dispatch ppt = null;
        try {
//            ComThread.InitSTA();
            app = new ActiveXComponent("PowerPoint.Application");
            app.setProperty("Visible", true);
            Dispatch ppts = app.getProperty("Presentations").toDispatch();
            ppt = Dispatch.call(ppts,
                    "Open",
                    inputFile,
                    true,//ReadOnly
                    true,//Untitled指定文件是否有标题
                    false//WithWindow指定文件是否可见
            ).toDispatch();

            File pdf = new File(pdfFile);
            if (pdf.exists()) {
                pdf.delete();
            }
            Dispatch.call(ppt,
                    "SaveAs",
                    pdfFile,
                    ppSaveAsPDF
            );
        } catch (ComFailException e) {
            LOG.error("转换文档到PDF错误：", e);
//            throw e;
        } catch (Exception e) {
            LOG.error("转换文档到PDF错误：", e);
//            throw e;
        } finally {
//            if (ppt != null) {
//                Dispatch.call(ppt, "Close", false);
//            }
            if (app != null) {
                app.invoke("Quit");
            }
            ComThread.Release();
        }
    }

}

