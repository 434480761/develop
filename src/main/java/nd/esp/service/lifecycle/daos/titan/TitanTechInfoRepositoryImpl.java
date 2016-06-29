package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanTechInfoRepository;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/5/30.
 */
@Repository
public class TitanTechInfoRepositoryImpl implements TitanTechInfoRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanTechInfoRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public TechInfo add(TechInfo techInfo) {
        if(techInfo == null){
            return null;
        }

        StringBuffer scriptBuffer = new StringBuffer("techinfo = graph.addVertex(T.label, type");
        Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer,techInfo);

        scriptBuffer.append(");g.V().has(primaryCategory,'identifier',sourceIdentifier).next().addEdge('has_tech_info',techinfo ,'identifier',edgeIdentifier);");
        graphParams.put("type", "tech_info");
        graphParams.put("primaryCategory",techInfo.getResType());
        graphParams.put("sourceIdentifier",techInfo.getResource());
        graphParams.put("edgeIdentifier",techInfo.getIdentifier());
        titanCommonRepository.executeScript(scriptBuffer.toString(), graphParams);
        return techInfo;
    }

    @Override
    public List<TechInfo> batchAdd(List<TechInfo> techInfos) {
        if(techInfos == null || techInfos.size() == 0){
            return null;
        }

        //FIXME 不是所有的添加都需要删除
        TechInfo ti = techInfos.get(0);
        deleteAll(ti.getResType(), ti.getResource());

        List<TechInfo> techInfoList = new ArrayList<>();
        for(TechInfo techInfo : techInfos){
            if(add(techInfo)!=null){
                techInfoList.add(techInfo);
            }
        }

        return techInfoList;
    }

    @Override
    public TechInfo update(TechInfo techInfo) {

        add(techInfo);

        return null;
    }

    @Override
    public List<TechInfo> batchUpdate(List<TechInfo> techInfos) {
        for(TechInfo techInfo : techInfos){
            update(techInfo);
        }
        return null;
    }

    /**
     * tech_info暂时不需要删除
     * */
    @Override
    public void remove(TechInfo techInfo) {

    }

    @Override
    public void deleteAll(String primaryCategory, String resource) {
        String deleteScriptBuffer = "g.V().has(primaryCategory,'identifier',resource).outE().hasLabel('has_tech_info').inV().drop()";
        Map<String, Object> deleteParam = new HashMap<>();
        deleteParam.put("primaryCategory", primaryCategory);
        deleteParam.put("resource", resource);
        titanCommonRepository.executeScript(deleteScriptBuffer, deleteParam);
    }

}
