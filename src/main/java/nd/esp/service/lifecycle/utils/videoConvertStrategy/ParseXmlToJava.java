/* =============================================================
 * Created: [2015年10月19日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.utils.videoConvertStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.utils.JDomUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;

/**
 * @author linsm
 * @since
 */
@SuppressWarnings("deprecation")
public class ParseXmlToJava {

    private static final Logger LOG = LoggerFactory.getLogger(ParseXmlToJava.class);

    /**
     * 将name对应的xml 文件转化为java对象（策略）
     * 
     * @param convertRuleSet
     * @param name
     * @since
     */
    @SuppressWarnings("rawtypes")
    public void parseXml(ConvertRuleSet convertRuleSet, String name) {

        String xmlContent = readFile(name);
        if (xmlContent == null) {
            throw new WafSimpleException("读取不了配置文件的内容,name:" + name);
        }

        JDomUtils jdu = new JDomUtils();
        try {
            Document doc = jdu.buildDoc(xmlContent);
            if (doc != null) {
                Element root = doc.getRootElement();
                List rules = root.getChildren("rule");
                int ruleSize = 0;
                while (ruleSize < rules.size()) {
                    for (Object object : rules) {
                        Element elementObject = (Element) object;
                        String id = elementObject.getAttributeValue("id");
                        Rule currentRule = convertRuleSet.getRuleById(id);
                        if (currentRule != null) {
                            continue;// 已经处理过
                        }

                        currentRule = new Rule(id);

                        String nextRuleId = elementObject.getAttributeValue("nextRuleRef");
                        if (StringUtils.isNotEmpty(nextRuleId)) {
                            Rule nextRule = convertRuleSet.getRuleById(nextRuleId);
                            if (nextRule == null) {
                                // 还是存在依赖，暂时处理不了,跳过
                                continue;
                            }
                            // 依赖已经处理
                            currentRule.setNextRuleRef(nextRule);
                            String commandName = elementObject.getAttributeValue("commandRef");
                            // 指定了command
                            if (StringUtils.isNotEmpty(commandName)) {
                                Command commandRef = nextRule.getCommandByName(commandName);
                                if (commandRef == null) {
                                    // 抛出异常 配置存在问题（不应当发生）
                                    throw new WafSimpleException(nextRuleId + "中不存在：" + commandName);
                                }
                                currentRule.setCommandRef(commandRef);
                            }
                        }
                        // String value = elementObject.getText();
                        // System.out.println(value);
                        currentRule.setFileTypeSet(dealWithFileType(getTypeElement(elementObject).getText()));
                        currentRule.setCommandSet(dealWithCommands(elementObject.getChildren("command")));
                        convertRuleSet.getRules().add(currentRule);
                    }
                    if (ruleSize == convertRuleSet.getRules().size()) {
                        // 存在循环依赖，抛出异常
                        throw new WafSimpleException("loop dependent!!");
                    }
                    ruleSize = convertRuleSet.getRules().size();
                }

            }
        } catch (JDOMException e) {

            LOG.warn("解析视频转码配置文件失败name:" + name, e);

        } catch (IOException e) {

            LOG.warn("解析视频转码配置文件失败name:" + name, e);

        }
    }

    /**
     * @param elementObject
     * @return
     * @since
     */
    private Element getTypeElement(Element elementObject) {
        return elementObject.getChild("fileType");
    }

    /**
     * @param name
     * @return
     * @since
     */
    private String readFile(String name) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(name).getFile());
            fis = new FileInputStream(file);// FileInputStream
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            return IOUtils.toString(isr);
        } catch (FileNotFoundException e) {

            LOG.info("找不到指定文件");

        } catch (IOException e) {

            LOG.info("读取文件失败");

        } finally {
            try {
                if(null != isr) {
                    isr.close();
                }
                if(null != isr) {
                    fis.close();
                }
                // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
            } catch (IOException e) {

                LOG.error("连接关闭异常:" + e.getMessage());

            }
        }
        return null;
    }

    /**
     * @param commands
     * @return
     * @since
     */
    @SuppressWarnings("rawtypes")
    private static List<Command> dealWithCommands(List commands) {
        List<Command> commandSet = new ArrayList<Command>();
        for (Object commandObject : commands) {
            Element element = (Element) commandObject;
            String commandName = element.getAttributeValue("name");
            Command command = new Command(commandName);
            Element osElement = element.getChild("os");// FIXME 后期可能需要扩展成list
            if (osElement == null) {
                // 抛出异常
                throw new WafSimpleException(commandName + "中不存在os");
            }
            String osName = osElement.getAttributeValue("name");
            Os os = new Os(osName);
            os.setValue(osElement.getText().trim());
            command.setOs(os);

            commandSet.add(command);
        }
        return commandSet;
    }

    /**
     * @param typeStrings
     * @return
     * @since
     */
    private static Set<SupportVideoType> dealWithFileType(String typeStrings) {
        if (StringUtils.isEmpty(typeStrings)) {
            return null;
        }
        typeStrings = typeStrings.trim();
        // FIXME 与上面合并？
        if (StringUtils.isEmpty(typeStrings)) {
            return null;
        }
        if ("ALL".equals(typeStrings)) {
            return Collections.emptySet(); // represent all
        }

        String[] typeChunks = typeStrings.split(","); // FIXME 写死
        if (typeChunks == null || typeChunks.length == 0) {
            return null;
        }
        Set<SupportVideoType> types = new HashSet<SupportVideoType>();
        for (String type : typeChunks) {
            types.add(SupportVideoType.fromString(type));
        }
        return types;
    }

}
