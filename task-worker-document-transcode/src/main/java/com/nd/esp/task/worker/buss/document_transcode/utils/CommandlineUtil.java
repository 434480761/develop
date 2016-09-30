package com.nd.esp.task.worker.buss.document_transcode.utils;

import com.nd.esp.task.worker.buss.document_transcode.support.LifeCircleException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLDecoder;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Administrator on 2016/9/14 0014.
 */
public class CommandlineUtil {

    private final static Logger LOG = LoggerFactory
            .getLogger(CommandlineUtil.class);

    public static int execute(File executable, List<String> args, StringBuffer outputMsg, StringBuffer logMsg) throws Exception {
        String path = URLDecoder.decode(CommandlineUtil.class.getClassLoader().getResource("tools").getPath().substring(1).
                replace("/", File.separator), "UTF-8");

        CommandLine cmdLine = new CommandLine(executable);
        for (String arg:args) {
            cmdLine.addArgument(arg);
        }

        logMsg.append("  Run command:"+cmdLine.toString()+System.getProperty("line.separator"));
        LOG.info("Run command:"+cmdLine.toString());

        DaemonExecutor executor = new DaemonExecutor();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(bos);
        executor.setStreamHandler(psh);
        executor.setWorkingDirectory(new File(path));
        int result = executor.execute(cmdLine);
        String messages = bos.toString();
        if(result!=0) {
            throw new LifeCircleException("ERROR: Run command error\nExit code:"+result+"\nExec: " +cmdLine.toString() + "\nOutput:\n" + messages);
        }
        outputMsg.append(messages);

        logMsg.append("command output:"+outputMsg);
        LOG.info("command output:"+outputMsg);

        return result;
    }

    public static int runCommand(String cmd, StringBuffer outputMsg, StringBuffer logMsg) throws Exception {
        String path = URLDecoder.decode(CommandlineUtil.class.getClassLoader().getResource("tools").getPath().substring(1).
                replace("/", File.separator), "UTF-8");
        logMsg.append("  Run command:"+cmd+System.getProperty("line.separator"));
        LOG.info("Run command:"+cmd);

        ProcessBuilder builder = null;
        if(SystemUtils.IS_OS_LINUX){
            builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        } else if(SystemUtils.IS_OS_WINDOWS){
            builder = new ProcessBuilder("cmd", "/c", cmd);
            builder.directory(new File(path));

            LOG.info("Win Current Path:"+path);
        }

        //both read inputstream and errstream
        builder.redirectErrorStream(true);
        Process process = builder.start();
        Scanner scanner = new Scanner(process.getInputStream(), "GBK");
        StringBuilder rt = new StringBuilder();
        while (scanner.hasNextLine()) {
            rt.append(scanner.nextLine());
            rt.append(System.getProperty("line.separator"));
        }
        scanner.close();
        outputMsg.append(rt.toString() + System.getProperty("line.separator"));

        logMsg.append("command output:"+outputMsg);
        LOG.info("command output:"+outputMsg);

        int resultValue = process.waitFor();

        return resultValue;
    }
}
