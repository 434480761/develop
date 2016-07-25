package nd.esp.service.lifecycle.models.v06;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ******************************************
 * <p/>
 * Copyright 2016
 * NetDragon All rights reserved
 * <p/>
 * *****************************************
 * <p/>
 * *** Company ***
 * NetDragon
 * <p/>
 * *****************************************
 * <p/>
 * *** Team ***
 * <p/>
 * <p/>
 * *****************************************
 *
 * @author gsw(806801)
 * @version V1.0
 * @Title KnowledgePathViewModel
 * @Package nd.esp.service.lifecycle.models.v06
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/6/22
 */

public class KnowledgePathViewModel {


    private Set<KnowledgeModel> nodes = new LinkedHashSet<KnowledgeModel>();
    private Set<KnowledgeRelationsModel> relations = new HashSet<KnowledgeRelationsModel>();

    public Set<KnowledgeModel> getNodes() {
        return nodes;
    }

    public void setNodes(Set<KnowledgeModel> nodes) {
        this.nodes = nodes;
    }

    public Set<KnowledgeRelationsModel> getRelations() {
        return relations;
    }

    public void setRelations(Set<KnowledgeRelationsModel> relations) {
        this.relations = relations;
    }
}
