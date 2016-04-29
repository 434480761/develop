/* =============================================================
 * Created: [2015年10月19日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.utils.videoConvertStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linsm
 * @since
 */
public class Command implements ProduceScripts{
    private String name;// 惟一
    private Os os;  //这里后期可能会需要扩展成集合（对应于XML）
    
    public Command(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Os getOs() {
        return os;
    }

    public void setOs(Os os) {
        this.os = os;
    }
    
    /**
     * name
     */
    @Override
    public int hashCode() {
        if (getName() == null) {
            return 17;
        }
        return getName().hashCode();
    };

    /**
     * name
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
        Command rule = (Command) object;
        return getName() == null ? rule.getName() == null : getName().equals(rule.getName());
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.utils.videoConvertStrategy.ProduceScripts#produceScripts()
     */
    @Override
    public List<String> produceScripts() {
        List<String> scripts = new ArrayList<String>();
        scripts.add(getOs().getValue());
        return scripts;
    }

}
