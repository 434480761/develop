package com.nd.esp.task.worker.buss.document_transcode.utils;

import info.monitorenter.cpdetector.io.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class Txt2htmlUtil {
    public static void transferTxt2Html(String inputFilePath, String outDirPath) throws IOException {
        File input = new File(inputFilePath);

        String strIn = FileUtils.readFileToString(input, getEncoding(inputFilePath));
        String strOut = txtToHtml(strIn);
        File outDir = new File(outDirPath);
        if(!outDir.exists()) {
            FileUtils.forceMkdir(outDir);
        }
        FileUtils.cleanDirectory(outDir);

        FileUtils.writeStringToFile(new File(outDir, "1.html"), strOut, "utf-8");
    }

    private static Charset getEncoding(String inputFile) {
        CodepageDetectorProxy detector =
        CodepageDetectorProxy.getInstance();
        /*-------------------------------------------------------------------------
          ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
          指示是否显示探测过程的详细信息，为false不显示。
        ---------------------------------------------------------------------------*/
        detector.add(new ParsingDetector(false));
        /*--------------------------------------------------------------------------
          JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
          测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
          再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         ---------------------------------------------------------------------------*/
        detector.add(JChardetFacade.getInstance());
        //ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        //UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());
        Charset charset = null;
        File f=new File(inputFile);
        try {
            charset = detector.detectCodepage(f.toURI().toURL());
        } catch (Exception ex) {ex.printStackTrace();}
        return charset;
    }

    public static String txtToHtml(String s) {
        StringBuilder builder = new StringBuilder();
        builder.append("<meta charset=\"utf-8\" />");
        boolean previousWasASpace = false;
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                if (previousWasASpace) {
                    builder.append(" ");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch (c) {
                case '<':
                    builder.append("<");
                    break;
                case '>':
                    builder.append(">");
                    break;
                case '&':
                    builder.append("&");
                    break;
                case '"':
                    builder.append("\"\"");
                    break;
                case '\n':
                    builder.append("<br>");
                    break;
                // We need Tab support here, because we print StackTraces as HTML
                case '\t':
                    builder.append("     ");
                    break;
                default:
                    builder.append(c);

            }
        }
        String converted = builder.toString();
        String str = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?«»“”‘’]))";
        Pattern patt = Pattern.compile(str);
        Matcher matcher = patt.matcher(converted);
        converted = matcher.replaceAll("<a href=\"$1\">$1</a>");
        return converted;
    }
}
