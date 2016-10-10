package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.titan.script.model.EducationToTitanBeanUtils;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanSubmitTransactionImpl implements TitanSubmitTransaction {
    private final static Integer BUTCH_UPDATE_RELATION_RED_PAGE_SIZE =50;
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanSubmitTransactionImpl.class);

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    /**
     * 自定义的处理流程可以手动掉用这个方法
     * */
    @Override
    public boolean submit(TitanTransaction transaction) {
        //TODO 可以做事务的重试
        LinkedList<TitanRepositoryOperation> repositoryOperations = transaction.getAllStep();
        boolean success = submit(repositoryOperations);
        //TODO 每个事务中需要获取资源的类型和ID，方案一：在事务名中存放类型和ID；方案二：在需要的时候再进行解析
        if (!success){
            LOG.info("失败");
        } else {
            LOG.info("成功");
        }
        return success;
    }

    private boolean submit(LinkedList<TitanRepositoryOperation> repositoryOperations){
        TitanScriptBuilder  builder = new TitanScriptBuilder();
        Map<String, String> educationIds = new HashMap<>();
        for (TitanRepositoryOperation operation : repositoryOperations) {
            TitanOperationType type = operation.getOperationType();
            EspEntity entity = operation.getEntity();
            switch (type) {
                case add: case update:
                    if (entity instanceof Education){
                        //需要更新冗余字段
                        educationIds.put(entity.getIdentifier(),
                                ((Education) entity).getPrimaryCategory());
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(entity));
                    } else if (entity instanceof ResCoverage) {
                        //需要更新冗余字段
                        educationIds.put(((ResCoverage) entity).getResource(),
                                ((ResCoverage)entity).getResType());
                        //删除边
                        builder.delete(EducationToTitanBeanUtils.toEdge(entity));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(entity));
                        builder.add(EducationToTitanBeanUtils.toEdge(entity));
                    } else if(entity instanceof ResourceCategory){
                        //需要更新冗余字段
                        educationIds.put(((ResourceCategory) entity).getResource(),
                                ((ResourceCategory) entity).getPrimaryCategory());
                        //删除边
                        builder.delete(EducationToTitanBeanUtils.toEdge(entity));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(entity));
                        builder.add(EducationToTitanBeanUtils.toEdge(entity));
                    } else if(entity instanceof  TechInfo){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(entity));
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(entity));
                    }else if (entity instanceof ResourceStatistical){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(entity));
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(entity));
                    } else if (entity instanceof ResourceRelation){
                        builder.delete(EducationToTitanBeanUtils.toEdge(entity));
                        builder.add(EducationToTitanBeanUtils.toEdge(entity));
                        //更新关系冗余字段
                        builder.updateRelationRedProperty(entity.getIdentifier());
                    } else if (entity instanceof KnowledgeRelation){
                        builder.delete(EducationToTitanBeanUtils.toEdge(entity));
                        builder.add(EducationToTitanBeanUtils.toEdge(entity));
                    }
                    break;
                case delete:
                    String getResource = "g.E().hasLabel('has_coverage','has_category_code')" +
                            ".has('identifier',identifier).outV().values('identifier')";
                    Map<String, Object> param = new HashMap<>();
                    param.put("identifier",entity.getIdentifier());
                    String resourceId = null;
                    try {
                        resourceId = titanCommonRepository.executeScriptUniqueString(getResource, param);
                    } catch (Exception e) {
                        LOG.error("获取资源ID失败");
                    }
                    if (resourceId!=null && resourceId.length() > 10){
                        educationIds.put(resourceId,"delete");
                    }

                    builder.deleteEdgeById(entity.getIdentifier());
                    break;
                //更新关系冗余字段
                case update_relation_red_property:
                    if (entity instanceof ResourceRelation){
                        TitanScriptBuilder addAndUpdate = new TitanScriptBuilder();
                        addAndUpdate.addBeforeCheckExist(EducationToTitanBeanUtils.toEdge(entity));
                        addAndUpdate.updateRelationRedProperty(entity.getIdentifier());
                        addAndUpdate.scriptEnd();
                        String relationId = null;
                        try {
                            relationId = titanCommonRepository.executeScriptUniqueString(
                                    addAndUpdate.getScript().toString(), addAndUpdate.getParam());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (relationId == null || relationId.equals("null")){
                            titanSync((ResourceRelation) entity);
                        }
                    }
                    break;
                default:
                    LOG.info("没有对应的处理方法");
            }
        }
        /**
         * 冗余字段更新策略：
         * 1、只有delete操作，冗余字段更新放在delete操作之前
         * 2、有delete操作，同时有update、add操作
         *      update、add中有包括对资源、覆盖范围、维度数据等的操作
         *      不包含对应的操作
         * 3、只有update、add操作，更新冗余字段放在更新之后
         * */
        for (String identifier : educationIds.keySet()){
            builder.updateEducationRedProperty(identifier);
        }
        builder.scriptEnd();

        Map<String, Object> param = builder.getParam();
        StringBuilder script = builder.getScript();
        String result = null;
        if (param != null && param.size() > 0) {
            try {
                long time = System.currentTimeMillis();
                result = titanCommonRepository.executeScriptUniqueString(script.toString(), param);
                System.out.println("执行脚本:"+(System.currentTimeMillis() - time));
            } catch (Exception e) {
                return false;
            }
        }

        updateRelationRedProperty(educationIds);

        if ("2".equals(result)){
            return true;
        } else {
            return false;
        }
    }

    private void updateRelationRedProperty(Map<String, String> educationIds){
        new Thread(new UpdateRelationRedPropertyRunnable(educationIds)).start();
    }

    private void titanSync(ResourceRelation relation){
        if(titanRepositoryUtils.checkRelationExistInMysql(relation)){
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR, relation);
        }
    }

    public static <T> List<List<T>> splitList(List<T> list, int pageSize) {
        int listSize = list.size();
        int page = (listSize + (pageSize-1))/ pageSize;
        List<List<T>> listArray = new ArrayList<List<T>>();
        for(int i=0;i<page;i++) {
            List<T> subList = new ArrayList<T>();
            for(int j=0;j<listSize;j++) {
                int pageIndex = ( (j + 1) + (pageSize-1) ) / pageSize;
                if(pageIndex == (i + 1)) {
                    subList.add(list.get(j));
                }

                if( (j + 1) == ((j + 1) * pageSize) ) {
                    break;
                }
            }
            listArray.add(subList);
        }
        return listArray;
    }

    private class UpdateRelationRedPropertyRunnable implements Runnable{
        Map<String, String> educationIds ;
        public UpdateRelationRedPropertyRunnable(Map<String, String> educationIds){
            this.educationIds = educationIds;
        }
        @Override
        public void run() {
            updateRelationRedProperty(educationIds);
        }


        private void updateRelationRedProperty(Map<String, String> educationIds){
            List<String> edgeIds = new ArrayList<>();
            for (String identifier : educationIds.keySet()) {
                edgeIds.addAll(getAllRelationId(identifier));
            }
            if (edgeIds.size() == 0){
                return;
            }

            LOG.info("需要更新关系冗余字段:{};资源:{}",edgeIds.size(),educationIds.toString());
            for (List<String> edges : splitList(edgeIds, BUTCH_UPDATE_RELATION_RED_PAGE_SIZE)){
                TitanScriptBuilder updateEdgeBuilder = new TitanScriptBuilder();
                updateEdgeBuilder.butchUpdateRelationRedProperty(edges);
                updateEdgeBuilder.scriptEnd();
                try {
                    titanCommonRepository.executeScript(updateEdgeBuilder.getScript().toString(), updateEdgeBuilder.getParam());
                } catch (Exception e) {
                    LOG.info("更新关系冗余字段出现异常 {}",educationIds);
                }
            }
        }

        private List<String> getAllRelationId(String resourceId){
            String script = "g.V().has('identifier',resourceId).union(inE('has_relation'),outE('has_relation')).values('identifier')";
            Map<String, Object> param = new HashMap<>();
            param.put("resourceId",resourceId);
            List<String> result = new LinkedList<>();
            ResultSet resultSet = null;
            try {
                resultSet = titanCommonRepository.executeScriptResultSet(script, param);
                Iterator<Result> iterator = resultSet.iterator();
                while (iterator.hasNext()) {
                    String id = iterator.next().getString();
                    result.add(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
