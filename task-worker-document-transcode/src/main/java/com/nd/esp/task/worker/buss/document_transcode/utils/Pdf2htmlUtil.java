package com.nd.esp.task.worker.buss.document_transcode.utils;

import com.nd.esp.task.worker.buss.document_transcode.utils.CommandlineUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class Pdf2htmlUtil {
    private final static Logger LOG = LoggerFactory
            .getLogger(Pdf2htmlUtil.class);

    private static final String FontsDircetoryName = "fonts";
    private static final String ShareCssDircetoryName = "sharedcss";
    private static final String ShareCssFileName = "shared.css";
    private static final String Jpedal = "<div id=\"jpedal\" style=\"position:relative;\">";
    private static final String ReplaceJpedal = "<div id=\"jpedal%s\" style=\"position:relative;\">";
    private static final String OverlayBackgroundColor = "background-color:rgba(0,0,0,0);";
    private static final String ReplaceOverlayBackgroundColor = "background-color: #fff; opacity: 0; filter:alpha(opacity=0);";
    private static final String PgOverlay = "<div id=\"pg%sOverlay\"";
    private static final String ReplacePgOverlay = "<div id=\"pg%sOverlay\" class=\"overlay\"";
    private static final String DefaultCss = "\n.t {\n\tposition: absolute;\n\t-webkit-transform-origin: top left;\n\t-moz-transform-origin: top left;\n\t-o-transform-origin: top left;\n\t-ms-transform-origin: top left;\n\t-webkit-transform: scale(0.25);\n\t-moz-transform: scale(0.25);\n\t-o-transform: scale(0.25);\n\t-ms-transform: scale(0.25);\n\tz-index: 1;\n\tposition:absolute;\n\twhite-space:nowrap;\n\toverflow:visible;\n}\n";
    
    private static final String RegexSharedJS = "<!-- Begin shared JS -->([\\s\\S]*)<!-- End shared JS -->";
    private static Pattern RegexSharedCSS = Pattern.compile("<!-- Begin shared CSS values -->([\\s\\S]*)<!-- End shared CSS values -->");
    private static final String ReplaceSharedCSS = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + ShareCssDircetoryName + "/" + ShareCssFileName + "\" />";
    private static Pattern RegexSharedCSSContent = Pattern.compile("<style type=\"text/css\" >([\\s\\S]*)</style>");
    private static final String RegexFontUrl = "url[(]\"([\\d]*/)fonts/";
    private static final String ReplaceFontUrl = "url(\"" + FontsDircetoryName + "/";
    private static final String RegexLtIE9 = "<!--\\[if lt IE 9\\]>([\\s\\S]*)</script><!\\[endif\\]-->";
    private static final String RegexFormData ="<!-- Begin Form Data -->([\\s\\S]*)<!-- End Form Data -->";

    private static FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if(pathname.isFile()) {
                return true;
            }
            return false;
        }
    };

    private static FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if(pathname.isDirectory()) {
                return true;
            }
            return false;
        }
    };

    public static void transferPdf2Html(String inputFilePath, String outDirPath) throws Exception {
        File outDir = new File(outDirPath);
        if(!outDir.exists()) {
            FileUtils.forceMkdir(outDir);
        }
        FileUtils.cleanDirectory(outDir);

        LOG.info("Convert to Html Begin.");
        String command = "java -jar jpdf2html.jar " + inputFilePath + " " + outDirPath;
        StringBuffer output = new StringBuffer();
        StringBuffer logMsg = new StringBuffer();
        CommandlineUtil.RunCommand(command, output, logMsg);
        LOG.info("Convert to Html End.");

        LOG.info("Deal Html Begin.");
        File htmlResultDir = new File(outDirPath+File.separator+"pdf");
        FileUtils.copyDirectory(htmlResultDir, outDir);
        FileUtils.deleteQuietly(htmlResultDir);
        for(File file : outDir.listFiles(fileFilter)) {
            String filename = getFileNameNoEx(file.getName());
            String content = FileUtils.readFileToString(file, "utf-8");

            content = content.replace(Jpedal, String.format(ReplaceJpedal, filename));
            content = content.replace(OverlayBackgroundColor, ReplaceOverlayBackgroundColor);
            content = content.replace(String.format(PgOverlay, filename), String.format(ReplacePgOverlay, filename));

            content = content.replaceAll(RegexSharedJS, "");

            boolean isDefaultCss = false;
            String shareCssContent = null;
            Matcher rangeMatch = RegexSharedCSS.matcher(content);
            if (rangeMatch.find())
            {
                rangeMatch = RegexSharedCSSContent.matcher(rangeMatch.group(0));
                if (rangeMatch.find())
                {
                    shareCssContent = rangeMatch.group(1);
                    isDefaultCss = shareCssContent == DefaultCss;
                }
                content = content.replaceAll(RegexSharedCSS.pattern(), isDefaultCss ? "" : ReplaceSharedCSS);
            }

            content = content.replaceAll(RegexFontUrl, ReplaceFontUrl);

            content = content.replaceAll(RegexLtIE9, "");

            content = content.replaceAll(RegexFormData, "");

            FileUtils.writeStringToFile(file, content, "utf-8");

            LOG.info("Deal Html Font、Css Begin.");
            if (!isDefaultCss && StringUtils.isNotEmpty(shareCssContent))
            {
                File destShareCssDircetory = new File(outDir, ShareCssDircetoryName);
                FileUtils.forceMkdir(destShareCssDircetory);
                FileUtils.writeStringToFile(new File(destShareCssDircetory, ShareCssFileName), shareCssContent);
            }

            File destFontDircetory = new File(outDir, FontsDircetoryName);
            for (File dir : outDir.listFiles(dirFilter))  //循环子目录
            {
                copyFontFiles(dir, destFontDircetory);
            }

            int index = 0;
            for (File fontFile : destFontDircetory.listFiles())
            {
                String name = fontFile.getName();
                String newName = name.equals(name.getBytes("ASCII").toString()) ? name.toLowerCase() : getNewName(fontFile.getName(), index++);
                for (File htmlFile : outDir.listFiles(fileFilter))
                {
                    String fileContent = FileUtils.readFileToString(htmlFile, "utf-8");
                    if(fileContent.contains(name)) {
                        fileContent = fileContent.replace(name, newName);
                    }
                    FileUtils.writeStringToFile(htmlFile, fileContent, "utf-8");
                }
                fontFile.renameTo(new File(destFontDircetory, newName));
            }

            LOG.info("Deal Html Font、Css End.");
        }
        LOG.info("Deal Html End.");
    }

    private static String getNewName(String filename, int index)
    {
        String extension = null;
        if (StringUtils.isNotEmpty(filename)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                extension = filename.substring(dot);
            }
        }

        String name = String.format("%s%d%s", "abcdee-font", index, extension);
        return name;
    }

    private static void copyFontFiles(File sourceDircetory, File destFontDircetory) throws IOException {
        if (sourceDircetory.getAbsolutePath().equals(destFontDircetory.getAbsolutePath()))
        {
            return;
        }
        if (sourceDircetory.getName().equals(FontsDircetoryName))
        {
            for (File file : sourceDircetory.listFiles(fileFilter))       //循环文件
            {
                File destFile = new File(destFontDircetory, file.getName());
                if (!destFile.exists())
                {
                    FileUtils.copyFile(file, destFile);
                }
            }
            FileUtils.deleteQuietly(sourceDircetory);
            return;
        }
        for (File dir : sourceDircetory.listFiles(dirFilter))  //循环子目录
        {
            copyFontFiles(dir, destFontDircetory);
        }
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }


}
