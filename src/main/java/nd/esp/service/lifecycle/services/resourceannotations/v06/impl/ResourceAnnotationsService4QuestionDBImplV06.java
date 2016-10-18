package nd.esp.service.lifecycle.services.resourceannotations.v06.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.models.v06.ResourceAnnotationModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.ResourceAnnotation;
import nd.esp.service.lifecycle.repository.sdk.ResourceAnnotation4QuestionDBRepository;
import nd.esp.service.lifecycle.services.resourceannotations.v06.ResourceAnnotationsServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by caocr on 2015/11/25 0025.
 */
@Service(value="resourceAnnotationsService4QuestionDBImplV06")
@Transactional(value="questionTransactionManager")
public class ResourceAnnotationsService4QuestionDBImplV06 implements ResourceAnnotationsServiceV06 {
    private final static Logger LOG = LoggerFactory.getLogger(ResourceAnnotationsService4QuestionDBImplV06.class);
    
    @Autowired
    ResourceAnnotation4QuestionDBRepository resourceAnnotationRepository;
    
    @Override
    public ResourceAnnotationModel addResourceAnnotation(ResourceAnnotationModel model, String resType, String resId) {
        // 判断资源是否存在，不存在抛出not found异常
        CommonHelper.resourceExist(resType, resId, ResourceType.RESOURCE_SOURCE);
        
        ResourceAnnotation resourceAnnotation = BeanMapperUtils.beanMapper(model, ResourceAnnotation.class);
        resourceAnnotation.setResType(resType);
        resourceAnnotation.setResource(resId);
        resourceAnnotation.setCreateTime(new Timestamp(System.currentTimeMillis()));
        
        ResourceAnnotation annotation = null;
        try {
            annotation = resourceAnnotationRepository.add(resourceAnnotation);
        } catch (EspStoreException e) {
            
            LOG.error("添加资源评注失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }
        
        if(annotation == null) {

            LOG.error("添加资源评注失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CreateResourceAnnotationFail.getCode(),
                                          LifeCircleErrorMessageMapper.CreateResourceAnnotationFail.getMessage());
        }
        
        return BeanMapperUtils.beanMapper(annotation, ResourceAnnotationModel.class);
    }

    @Override
    public ResourceAnnotationModel updateResourceAnnotation(ResourceAnnotationModel model, String resType, String resId) {
        //判断评注是否存在
        ResourceAnnotation resourceAnnotation = this.getRelationByAid(model.getIdentifier());
        
        // 判断资源是否存在，不存在抛出not found异常
        CommonHelper.resourceExist(resType, resId, ResourceType.RESOURCE_SOURCE);

        ResourceAnnotation annotation = BeanMapperUtils.beanMapper(model, ResourceAnnotation.class);
        if (model.getScore() == null) {
            annotation.setScore(resourceAnnotation.getScore());
        }
        if (model.getScoreLevel() == null) {
            annotation.setScoreLevel(resourceAnnotation.getScoreLevel());
        }
        if(StringUtils.isEmpty(model.getAnnotationFrom())){
            annotation.setAnnotationFrom(resourceAnnotation.getAnnotationFrom());
        }
        annotation.setResType(resourceAnnotation.getResType());
        annotation.setResource(resourceAnnotation.getResource());
        annotation.setCreateTime(resourceAnnotation.getCreateTime());

        ResourceAnnotation result = null;
        try {
            result = resourceAnnotationRepository.update(annotation);
        } catch (EspStoreException e) {

            LOG.error("修改资源评注失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }

        if (result == null) {

            LOG.error("修改资源评注失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.UpdateResourceAnnotationFail.getCode(),
                                          LifeCircleErrorMessageMapper.UpdateResourceAnnotationFail.getMessage());
        }

        return BeanMapperUtils.beanMapper(result, ResourceAnnotationModel.class);
    }
    
    @Override
    public boolean deleteResourceAnnotationByAnnoId(String resType, String resId, String annoId){
        //没有必要判断资源是否存在，只要评注存在就删掉
        // 判断评注是否存在
        this.getRelationByAid(annoId);
        
        try {
            resourceAnnotationRepository.del(annoId);
        } catch (EspStoreException e) {

            LOG.error("删除资源评注失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.DeleteResourceAnnotationFail.getCode(),
                                          e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public boolean deleteResourceAnnotationByResId(String resType, String resId) {
        ResourceAnnotation resourceAnnotation = new ResourceAnnotation();
        resourceAnnotation.setResType(resType);
        resourceAnnotation.setResource(resId);
        
        List<ResourceAnnotation> resourceAnnotationList = new ArrayList<ResourceAnnotation>();
        List<String> delAnnotationIds = new ArrayList<String>();
        try {
            resourceAnnotationList = resourceAnnotationRepository.getAllByExample(resourceAnnotation);
        } catch (EspStoreException e) {

            LOG.error("根据资源id获取资源评注失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        for (ResourceAnnotation annotation : resourceAnnotationList) {
            delAnnotationIds.add(annotation.getIdentifier());
        }
        
        if (CollectionUtils.isEmpty(delAnnotationIds)) {
            return true;
        }
        
        try {
            resourceAnnotationRepository.batchDel(delAnnotationIds);
        } catch (EspStoreException e) {

            LOG.error("批量删除资源评注失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return true;
    }

    @Override
    public boolean deleteResourceAnnotationByEntityId(String resType, String resId, String entityId) {
        ResourceAnnotation resourceAnnotation = new ResourceAnnotation();
        resourceAnnotation.setResType(resType);
        resourceAnnotation.setResource(resId);
        resourceAnnotation.setEntityIdentifier(entityId);
        
        List<ResourceAnnotation> resourceAnnotationList = new ArrayList<ResourceAnnotation>();
        List<String> delAnnotationIds = new ArrayList<String>();
        try {
            resourceAnnotationList = resourceAnnotationRepository.getAllByExample(resourceAnnotation);
        } catch (EspStoreException e) {

            LOG.error("根据用户id获取资源评注失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        for (ResourceAnnotation annotation : resourceAnnotationList) {
            delAnnotationIds.add(annotation.getIdentifier());
        }
        
        if (CollectionUtils.isEmpty(delAnnotationIds)) {
            return true;
        }
        
        try {
            resourceAnnotationRepository.batchDel(delAnnotationIds);
        } catch (EspStoreException e) {

            LOG.error("批量删除资源评注失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public ListViewModel<ResourceAnnotationModel> queryResourceAnnotationsByResId(String resType,
                                                                                  String resId,
                                                                                  String limit) {
        // 判断资源是否存在，不存在抛出not found异常
        CommonHelper.resourceExist(resType, resId, ResourceType.RESOURCE_SOURCE);
        
        // 返回的结果集
        ListViewModel<ResourceAnnotationModel> listModel = new ListViewModel<ResourceAnnotationModel>();
        
        // 检索条件
        AdaptQueryRequest<ResourceAnnotation> queryRequest = new AdaptQueryRequest<ResourceAnnotation>();
        ResourceAnnotation example = new ResourceAnnotation();
        example.setResType(resType);
        example.setResource(resId);
        queryRequest.setParam(example);
        
        //分页参数
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        queryRequest.setOffset(result[0]);
        //最大值不能查过200条记录，超过后最大以200计算
        if(result[1] > 200){
            queryRequest.setLimit(200);
            limit = "(0,200)";
        }  else {
            queryRequest.setLimit(result[1]);
        }
        
        // 调用SDK
        QueryResponse<ResourceAnnotation> resourceAnnotations = null;
        
        try {
            resourceAnnotations = resourceAnnotationRepository.searchByExample(queryRequest);
        } catch (EspStoreException e) {
            
            LOG.error("查询资源评注失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }

        // 返回结果的items
        List<ResourceAnnotationModel> items = new ArrayList<ResourceAnnotationModel>();
        Long total = 0L;
        if (resourceAnnotations != null && resourceAnnotations.getHits() != null) {
            total = resourceAnnotations.getHits().getTotal();
            if(resourceAnnotations.getHits().getDocs() != null) {
                for(ResourceAnnotation resourceAnnotation : resourceAnnotations.getHits().getDocs()){
                    items.add(BeanMapperUtils.beanMapper(resourceAnnotation, ResourceAnnotationModel.class));
                }
            }
        }

        listModel.setItems(items);
        listModel.setLimit(limit);
        listModel.setTotal(total);

        return listModel;
    }
    
    private ResourceAnnotation getRelationByAid(String aid) {
        // 判断aid对应的资源评注是否存在,不存在抛出异常
        ResourceAnnotation resourceAnnotation = null;
        try {
            resourceAnnotation = resourceAnnotationRepository.get(aid);
        } catch (EspStoreException e) {

            LOG.error("获取资源关系失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }

        if (resourceAnnotation == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ResourceAnnotationNotFound.getCode(),
                                          LifeCircleErrorMessageMapper.ResourceAnnotationNotFound.getMessage());
        }

        return resourceAnnotation;
    }

}
