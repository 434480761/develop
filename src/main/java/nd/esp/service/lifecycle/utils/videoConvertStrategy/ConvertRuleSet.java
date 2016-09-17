/* =============================================================
 * Created: [2015年10月19日] by Administrator
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.utils.videoConvertStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;

/**
 * @author linsm
 * @since
 */
@SuppressWarnings("deprecation")
public class ConvertRuleSet {
    private Set<Rule> rules = new HashSet<Rule>();

    private static ConvertRuleSet convertRuleSet;

    private static final String videoConvertStrategyXmlName = "simpleVideoConvert.xml"; // 暂时先写死

    private ConvertRuleSet() {

    }

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    /**
     * 实现单例，取得视频转码的策略
     * 
     * @return
     * @since
     */
    public static ConvertRuleSet getConvertRuleSet() {
        synchronized (ConvertRuleSet.class) {
            if (convertRuleSet == null) {
                convertRuleSet = new ConvertRuleSet();
                new ParseXmlToJava().parseXml(convertRuleSet, videoConvertStrategyXmlName);
            }
        }
        return convertRuleSet;
    }

    /**
     * 可能存在多个可以转码的工具（需要找到最适配的，all is least)l
     * 
     * @param fileType 视频类型
     * @return
     * @since
     */
    private Rule findPerfectRule(final SupportVideoType type) {
        Rule perfectRule = Collections.max(getRules(), new Comparator<Rule>() {

            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.computeScore(type) - o2.computeScore(type);
            }
        });
        if (perfectRule == null || perfectRule.computeScore(type) == 0) {
            // FIXME change to lc exception
            throw new WafSimpleException("not support type:" + type);
        }
        return perfectRule;

    }

    /**
     * 根据视频类型（后缀），产生视频转码所需要的脚本
     * 
     * @param fileType 视频类型（后缀）
     * @return 转码所需要的脚本
     * @since
     */
    public List<String> productScript(String fileType) {

        SupportVideoType type = SupportVideoType.fromString(fileType);

        Rule startRule = findPerfectRule(type);

        List<String> scripts = new ArrayList<String>();
        scripts.addAll(startRule.produceScripts());
        while (startRule.getNextRuleRef() != null) {

            if (startRule.getCommandRef() == null) {
                scripts.addAll(startRule.getNextRuleRef().produceScripts());
            } else {
                scripts.addAll(startRule.getCommandRef().produceScripts());
            }
            startRule = startRule.getNextRuleRef();
        }

        return scripts;
    }

    /**
     * 通过ruleId取rule
     * 
     * @param id
     * @return
     * @since
     */
    public Rule getRuleById(String id) {
        Rule mayExistRule = new Rule(id);
        if (getRules().contains(mayExistRule)) {
            for (Rule rule : getRules()) {
                if (rule.getId().equals(id)) {
                    return rule;
                }
            }
        }
        return null;
    }

    /**
     * just for test
     * 
     * @author linsm
     * @param args
     * @since
     */
    public static void main(String[] args) {
        ConvertRuleSet ruleSet = getConvertRuleSet();
        // prinln all command
        // for(Rule rule:ruleSet.getRules()){
        // for(Command command:rule.getCommandSet()){
        // System.out.println(command.getOs().getValue());
        // }
        // }

        for (SupportVideoType supportVideoType : SupportVideoType.values()) {
            System.out.println(supportVideoType.toString() + ":   \n"
                    + ruleSet.productScript(supportVideoType.toString()));
        }
    }

}
