package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
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
    private final static Integer BUTCH_UPDATE_RELATION_RED_PAGE_SIZE = 50;
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanSubmitTransactionImpl.class);

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    /**
     * 自定义的处理流程可以手动掉用这个方法
     */
    @Override
    public void submit(TitanTransaction transaction) {
        //TODO 可以做事务的重试
        LinkedList<TitanRepositoryOperation> repositoryOperations = transaction.getAllStep();
        if (CollectionUtils.isEmpty(repositoryOperations)) {
            return;
        }
        boolean success = submit(repositoryOperations);
        //TODO 每个事务中需要获取资源的类型和ID，方案一：在事务名中存放类型和ID；方案二：在需要的时候再进行解析
        if (!success) {
            Map<String, String> map = getAllEducation(transaction.getAllStep());
            for (String identifier : map.keySet()){
                titanSync(identifier, map.get(identifier), TitanSyncType.SAVE_OR_UPDATE_ERROR);
            }
            LOG.info("unsuccessful:{}",map.toString());
        } else {
//            LOG.info("成功");
        }
    }

    @Override
    public boolean submit4Sync(TitanTransaction transaction) {
        LinkedList<TitanRepositoryOperation> repositoryOperations = transaction.getAllStep();
        if (CollectionUtils.isEmpty(repositoryOperations)) {
            return true;
        }
        return submit(repositoryOperations);
    }

    /**
     * 1、educationRed更新冗余字段：修改资源、维度数据、覆盖范围、删除维度数据、删除覆盖范围，
     * 会触发生成更新资源冗余字段和关系冗余字段的脚本
     * */
    private boolean submit(LinkedList<TitanRepositoryOperation> repositoryOperations) {
        TitanScriptBuilder builder = new TitanScriptBuilder();
        Map<String, String> educationRed = new HashMap<>();
        for (TitanRepositoryOperation operation : repositoryOperations) {
            TitanOperationType type = operation.getOperationType();
            EspEntity entity = operation.getEntity();
            switch (type) {
                case add: case update:
                    if (entity instanceof Education){
                        //需要更新冗余字段
                        educationRed.put(entity.getIdentifier(),
                                ((Education) entity).getPrimaryCategory());
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(entity));
                    } else if (entity instanceof ResCoverage) {
                        //需要更新冗余字段
                        educationRed.put(((ResCoverage) entity).getResource(),
                                ((ResCoverage)entity).getResType());
                        //删除边
                        builder.delete(EducationToTitanBeanUtils.toEdge(entity));
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(entity));
                        builder.add(EducationToTitanBeanUtils.toEdge(entity));
                    } else if(entity instanceof ResourceCategory){
                        //需要更新冗余字段
                        educationRed.put(((ResourceCategory) entity).getResource(),
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
                        educationRed.put(resourceId,"delete");
                    }

                    builder.deleteEdgeById(entity.getIdentifier());
                    break;
                //增加执行自定义脚本
                case script:
                    if (operation instanceof TitanRepositoryOperationScript){
                        builder.script(((TitanRepositoryOperationScript) operation).getCustomScript(),
                                ((TitanRepositoryOperationScript) operation).getCustomScriptParam());
                    }
                    break;
                //patch需要配置支持的对象
                case patch:
                    if (operation instanceof TitanRepositoryOperationPatch){
                        if (entity instanceof Education){
                            builder.patch(EducationToTitanBeanUtils.toVertex(entity),
                                    ((TitanRepositoryOperationPatch) operation).getPatchPropertyMap());
                        } else if(entity instanceof  TechInfo){
                            builder.patch(EducationToTitanBeanUtils.toVertex(entity),
                                    ((TitanRepositoryOperationPatch) operation).getPatchPropertyMap());
                            builder.patch(EducationToTitanBeanUtils.toEdge(entity),
                                    ((TitanRepositoryOperationPatch) operation).getPatchPropertyMap());
                        }
                    }
                    break;
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

                        return true;
                    }
                    break;
                case update_relation_edu_red_property:
                    if (entity instanceof Education) {
                        //更新冗余字段
                        educationRed.put(entity.getIdentifier(), ((Education) entity).getPrimaryCategory());
                    }
                    break;
                default:
                    LOG.info("没有对应的处理方法");
            }
        }

        //添加更新资源冗余字段的脚本
        for (String identifier : educationRed.keySet()) {
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
                System.out.println("脚本执行时间:"+ (System.currentTimeMillis() - time));
            } catch (Exception e) {
                return false;
            }
        }

        //启动批量更新关系冗余字段脚本
        updateRelationRedProperty(educationRed);

        if ("2".equals(result)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前事务中所有的资源ID
     * */
    private Map<String, String> getAllEducation(LinkedList<TitanRepositoryOperation> repositoryOperations) {
        Map<String, String> education = new HashMap<>();
        for (TitanRepositoryOperation operation : repositoryOperations) {
            EspEntity entity = operation.getEntity();
            if (entity instanceof Education) {
                education.put(entity.getIdentifier(), ((Education) entity).getPrimaryCategory());
            } else if (entity instanceof ResCoverage) {
                education.put(((ResCoverage) entity).getResource(),((ResCoverage) entity).getResType());
            } else if (entity instanceof ResourceCategory) {
                education.put(((ResourceCategory) entity).getResource(),((ResourceCategory) entity).getPrimaryCategory());
            } else if (entity instanceof TechInfo) {
                education.put(((TechInfo) entity).getResource(),((TechInfo) entity).getResType());
            } else if (entity instanceof ResourceStatistical) {
                education.put(((ResourceStatistical) entity).getResource(),((ResourceStatistical) entity).getResType());
            } else if (entity instanceof KnowledgeRelation) {

            } else if (entity instanceof ResourceRelation) {
                //关系中有两个资源，单独做处理
                titanSync((ResourceRelation) entity);
            }
        }

        return education;
    }

    private void updateRelationRedProperty(Map<String, String> educationIds) {
        new Thread(new UpdateRelationRedPropertyRunnable(educationIds)).start();
    }

    private void titanSync(ResourceRelation relation){
        if(titanRepositoryUtils.checkRelationExistInMysql(relation)){
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR, relation);
        }
    }

    private void titanSync(String identifier, String primaryCategory, TitanSyncType type){
        titanRepositoryUtils.titanSync4MysqlImportAdd(type, primaryCategory, identifier);
    }

    /**
     * TODO
     * 后期处理
     * 以资源为中心够造线程
     * 有需要再次更新覆盖范围的时候（取消上次的更新，并重新更新，更新结束后检查关系的条数是否和开始的时候一样）
     *
     * */
    private class UpdateRelationRedPropertyRunnable implements Runnable {
        Map<String, String> educationIds;

        public UpdateRelationRedPropertyRunnable(Map<String, String> educationIds) {
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

            Collections.sort(edgeIds);
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

        private <T> List<List<T>> splitList(List<T> list, int pageSize) {
            int listSize = list.size();
            int page = (listSize + (pageSize - 1)) / pageSize;
            List<List<T>> listArray = new ArrayList<List<T>>();
            for (int i = 0; i < page; i++) {
                List<T> subList = new ArrayList<T>();
                for (int j = 0; j < listSize; j++) {
                    int pageIndex = ((j + 1) + (pageSize - 1)) / pageSize;
                    if (pageIndex == (i + 1)) {
                        subList.add(list.get(j));
                    }
                    if ((j + 1) == ((j + 1) * pageSize)) {
                        break;
                    }
                }
                listArray.add(subList);
            }
            return listArray;
        }
    }
}
