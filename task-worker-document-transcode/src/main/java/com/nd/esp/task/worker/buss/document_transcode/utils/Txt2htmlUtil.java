package com.nd.esp.task.worker.buss.document_transcode.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class Txt2htmlUtil {
    public static void transferTxt2Html(String inputFilePath, String outDirPath) throws IOException {
        File input = new File(inputFilePath);
        String strIn = FileUtils.readFileToString(input, "");
        String strOut = txtToHtml(strIn);
        File outDir = new File(outDirPath);
        if(!outDir.exists()) {
            FileUtils.forceMkdir(outDir);
        }
        FileUtils.cleanDirectory(outDir);

        FileUtils.writeStringToFile(new File(input.getName().replace(".txt", ".html")), strOut);
    }

    public static String txtToHtml(String s) {
        StringBuilder builder = new StringBuilder();
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
