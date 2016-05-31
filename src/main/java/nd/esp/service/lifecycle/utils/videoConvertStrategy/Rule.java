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
import java.util.List;
import java.util.Set;

/**
 * @author linsm
 * @since 
 *
 */
public class Rule implements ProduceScripts{
    private String id;//惟一
    private Rule nextRuleRef;
    private Command commandRef;
    private Set<SupportVideoType> fileTypeSet;
    private List<Command> commandSet;
    
    public Rule(String id){
        this.id = id;
    }
    
    public Set<SupportVideoType> getFileTypeSet() {
        return fileTypeSet;
    }

    public void setFileTypeSet(Set<SupportVideoType> fileTypeSet) {
        this.fileTypeSet = fileTypeSet;
    }

    public List<Command> getCommandSet() {
        return commandSet;
    }

    public void setCommandSet(List<Command> commandSet) {
        this.commandSet = commandSet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Rule getNextRuleRef() {
        return nextRuleRef;
    }

    public void setNextRuleRef(Rule nextRuleRef) {
        this.nextRuleRef = nextRuleRef;
    }

    public Command getCommandRef() {
        return commandRef;
    }

    public void setCommandRef(Command commandRef) {
        this.commandRef = commandRef;
    }
    
    /**
     * cue,raw,cif,nrg,dat,sfd,bin,toc,ALL等
     * 0<1<2
     * 
     * @param fileType
     * @return
     * @since
     */
    int computeScore(SupportVideoType type){
        if(getFileTypeSet() == null){
            return 0;//ALL
        }
        if(getFileTypeSet().size()==0){
            return 1;
        }
        if(getFileTypeSet().contains(type)){
            return 2;  //perfect specific
        }
        
        return 0;//
    }


    /**
     * id
     */
    @Override
    public int hashCode() {
        if (getId() == null) {
            return 17;
        }
        return getId().hashCode();
    };

    /**
     * id
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        Rule rule = (Rule) object;
        return getId() == null ? rule.getId() == null : getId().equals(rule.getId());
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.utils.videoConvertStrategy.ProduceScripts#produceScripts()
     */
    @Override
    public List<String> produceScripts() {
        List<String> scripts = new ArrayList<String>();
        for(Command command: getCommandSet()){
            scripts.addAll(command.produceScripts());
        }
        return scripts;
    }

    /**
     * 
     * @param commandName
     * @return
     * @since 
     */
    public Command getCommandByName(String commandName) {
        Command mayExistCommand = new Command(commandName);
        if(getCommandSet().contains(mayExistCommand)){
            for(Command command: getCommandSet()){
                if(command.getName().equals(commandName)){
                    return command;
                }
            } 
        }
        return null;
    }

}
