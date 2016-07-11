package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgePathViewModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeRelationsModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanDirection;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
 * @Title TitanKnowledgeResourceServiceImpl
 * @Package nd.esp.service.lifecycle.services.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/6/22
 */

@Service
public class TitanKnowledgeResourceServiceImpl implements TitanKnowledgeResourceService {
    @Autowired
    private TitanCommonRepository titanCommonRepository;


    @Override
    public KnowledgeModel create(KnowledgeModel r) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public KnowledgeModel get(String id) {
        StringBuffer script = new StringBuffer("g.V().hasLabel('knowledges').has('identifier','");
        script.append(id);
        script.append("').valueMap()");
        ResultSet result = titanCommonRepository.executeScriptResultSet(script.toString());
        Iterator<Result> iterator = result.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next().getString();
            System.out.println(line);
        }

        return null;
    }

    @Override
    public KnowledgePathViewModel queryKnowledgePath(String startId, String endId, int minDepth, int maxDepth) {
        System.out.println("开始节点:" + startId + "    结束节点:" + endId);
        String vDirection = TitanDirection.in.toString();
        String eDirection = TitanDirection.out.toString();
        boolean formStart = true;

        StringBuffer script = new StringBuffer("g.V().hasLabel('knowledges')").append(".has('identifier','");
        StringBuffer byEnd = new StringBuffer();

        if (startId != null) {
            System.out.println("从开始节点查询路径");
            script.append(startId);
            if (endId != null) {
                byEnd.append(".has('identifier','").append(endId).append("')");
            }
        } else {
            if (endId != null) {
                formStart = false;
                eDirection = TitanDirection.in.toString();
                vDirection = TitanDirection.out.toString();
                System.out.println("从结束节点查询路径");
                script.append(endId);
            }
        }

        script.append("').emit(has('lc_enable',true)).repeat(");
        script.append(eDirection).append("E().hasLabel('has_knowledge_relation').");
        script.append(vDirection).append("V().has('lc_enable',true)).times(").append(maxDepth).append(")");
        script.append(byEnd);
        script.append(".path().by(valueMap())");
        System.out.println("script:" + script);

        List<List<Map<String, String>>> paths = new ArrayList<>();
        ResultSet result = titanCommonRepository.executeScriptResultSet(script.toString());
        Iterator<Result> iterator = result.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next().getString();
            paths.add(toOnePath(line));
        }

        return changeToKnowledgePathViewModel(paths, formStart);
    }

    @Override
    public KnowledgePathViewModel queryStartNode(String endId, int limit) {
        KnowledgePathViewModel knowledgePathViewModel = new KnowledgePathViewModel();
        Set<String> set = new HashSet<>();
        Set<KnowledgeModel> nodes = new LinkedHashSet<KnowledgeModel>();
        //:> g.V().hasLabel('knowledges').emit().repeat(outE().hasLabel('has_knowledge')).times(2).valueMap().limit(10)
        StringBuffer script = new StringBuffer("g.V().hasLabel('knowledges')");
        if (endId != null) {
            if (!"".equals(endId)) {
                script.append(".has('identifier','");
                script.append(endId);
                script.append("')");
            }
        }
        script.append(".emit(has('lc_enable',true)).repeat(inE().hasLabel('has_knowledge_relation').outV().has('lc_enable',true)).times(4).dedup().valueMap().limit(");
        script.append(limit);
        script.append(")");
        System.out.println("script:" + script);

        ResultSet result = titanCommonRepository.executeScriptResultSet(script.toString());
        Iterator<Result> iterator = result.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next().getString();
            Map<String, String> map = TitanResultParse.toMap(line);
            KnowledgeModel node = new KnowledgeModel();
            TitanResultParse.dealMainResult(node, map);
            if (!set.contains(node.getIdentifier())) {
                // System.out.println(line);
                nodes.add(node);
                set.add(node.getIdentifier());
            }

        }
        knowledgePathViewModel.setNodes(nodes);
        return knowledgePathViewModel;
    }

    @Override
    public KnowledgePathViewModel queryEndNode(String startId, int limit) {
        KnowledgePathViewModel knowledgePathViewModel = new KnowledgePathViewModel();
        Set<String> set = new HashSet<>();
        Set<KnowledgeModel> nodes = new LinkedHashSet<KnowledgeModel>();
        StringBuffer script = new StringBuffer("g.V().hasLabel('knowledges')");
        if (startId != null) {
            if (!"".equals(startId)) {
                script.append(".has('identifier','");
                script.append(startId);
                script.append("')");
            }
        }
        //
        script.append(".emit(has('lc_enable',true)).repeat(outE().hasLabel('has_knowledge_relation').inV().has('lc_enable',true)).times(4).dedup().valueMap().limit(");
        script.append(limit);
        script.append(")");
        System.out.println("script:" + script);

        ResultSet result = titanCommonRepository.executeScriptResultSet(script.toString());
        Iterator<Result> iterator = result.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next().getString();
            Map<String, String> map = TitanResultParse.toMap(line);
            KnowledgeModel node = new KnowledgeModel();
            TitanResultParse.dealMainResult(node, map);
            if (!set.contains(node.getIdentifier())) {
                nodes.add(node);
                set.add(node.getIdentifier());
                // //System.out.println(line);
            }

        }
        knowledgePathViewModel.setNodes(nodes);
        return knowledgePathViewModel;
    }


    private KnowledgePathViewModel resultOrderByPaths(List<List<Map<String, String>>> allPaths, boolean isQueryStart) {
        KnowledgePathViewModel knowledgePathViewModel = new KnowledgePathViewModel();
        Set<KnowledgeModel> nodes = new LinkedHashSet<KnowledgeModel>();
        Map<String, Integer> paths = new TreeMap<>();
        for (List<Map<String, String>> listOnePath : allPaths) {
            int pathNodeSize = listOnePath.size();
            if (pathNodeSize >= 3) {
                Map<String, String> start = listOnePath.get(0);
                Map<String, String> end = listOnePath.get(pathNodeSize - 1);
                String key = start.get(ES_SearchField.identifier.toString()) + "," + end.get(ES_SearchField.identifier.toString());
                if (paths.containsKey(key)) {
                    paths.put(key, paths.get(key) + 1);
                } else {
                    paths.put(key, 1);
                }

            }
        }


        knowledgePathViewModel.setNodes(nodes);
        return knowledgePathViewModel;

    }

    /**
     *
     * @param paths
     * @param formStart
     * @return
     */
    private KnowledgePathViewModel changeToKnowledgePathViewModel(List<List<Map<String, String>>> paths, boolean formStart) {
        KnowledgePathViewModel knowledgePathViewModel = new KnowledgePathViewModel();
        Set<KnowledgeModel> nodes = new LinkedHashSet<>();
        Set<KnowledgeRelationsModel> relations = new HashSet<>();
        Set<String> duplSet = new HashSet<>();
        for (List<Map<String, String>> path : paths) {
            int pathSize = path.size();
            if (pathSize >= 3) {
                for (int i = 0; i + 2 < pathSize; i = i + 2) {
                    KnowledgeRelationsModel relation = null;
                    if (formStart) {
                        // source edge target
                        relation = toKnowledgeRelationsModel(path.get(i), path.get(i + 1), path.get(i + 2));
                    } else {
                        relation = toKnowledgeRelationsModel(path.get(i + 2), path.get(i + 1), path.get(i));
                    }
                    if (relation != null) {
                        if (!duplSet.contains(relation.getIdentifier())) {
                            relations.add(relation);
                            duplSet.add(relation.getIdentifier());

                            KnowledgeModel source = relation.getSourceKnowledgeModel();
                            if (!duplSet.contains(source.getIdentifier())) {
                                nodes.add(source);
                                duplSet.add(source.getIdentifier());
                            }
                            KnowledgeModel target = relation.getTargetKnowledgeModel();
                            if (!duplSet.contains(target.getIdentifier())) {
                                nodes.add(target);
                                duplSet.add(target.getIdentifier());
                            }
                        }
                    }

                }
            }
        }
        knowledgePathViewModel.setNodes(nodes);
        knowledgePathViewModel.setRelations(relations);
        return knowledgePathViewModel;
    }

    /**
     * @param source
     * @param edge
     * @param target
     * @return
     */
    private KnowledgeRelationsModel toKnowledgeRelationsModel(Map<String, String> source, Map<String, String> edge, Map<String, String> target) {
        KnowledgeModel sourceModel = new KnowledgeModel();
        KnowledgeRelationsModel relation = new KnowledgeRelationsModel();
        KnowledgeModel targetModel = new KnowledgeModel();
        //source
        if (source.size() > 0) TitanResultParse.dealMainResult(sourceModel, source);
        // target
        if (target.size() > 0) TitanResultParse.dealMainResult(targetModel, target);

        // relation
        relation.setIdentifier(edge.get(ES_SearchField.identifier.toString()));
        relation.setSource(source.get(ES_SearchField.identifier.toString()));
        relation.setTarget(target.get(ES_SearchField.identifier.toString()));
        relation.setSourceKnowledgeModel(sourceModel);
        relation.setTargetKnowledgeModel(targetModel);
        relation.setContextObject(edge.get("context_object"));
        relation.setContextType(edge.get("context_type"));
        relation.setRelationType("relation_type");

        return relation;
    }

    /**
     * @param line
     * @return
     */
    private List<Map<String, String>> toOnePath(String line) {
        List<Map<String, String>> pathList = new ArrayList<>();
        line = line.substring(1, line.length() - 3).replaceAll("==>", "");
        String[] nodes = line.split("}, ");

        for (String node : nodes) {
            Map<String, String> tmpMap = new HashMap<>();
            node = node.substring(1, node.length());
            // System.out.println(node);

            String[] fields = null;
            if (node.contains("], ")) {
                fields = node.split("], ");
            } else {
                fields = node.split(", ");
            }

            for (String f : fields) {
                String[] t = f.split("=");
                if (t.length == 2) {
                    String v = t[1];
                    if (v.startsWith("[")) {
                        v = v.substring(1, v.length());
                    }
                    //System.out.println(t[0] + ":" + v);
                    tmpMap.put(t[0].trim(), v);
                }
            }
            pathList.add(tmpMap);
        }
        return pathList;
    }

    public static void main(String[] args) {
        Map<String, Integer> paths = new TreeMap<>();
        paths.put("a", 8);
        paths.put("b", 2);
        paths.put("c", 5);
        paths.put("d", 1);
        paths.put("e", 4);


        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(paths.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            //升序排序
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });


        System.out.println(paths);


    }

}
