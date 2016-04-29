package nd.esp.service.lifecycle.services.repository.v06.impl;

import nd.esp.service.lifecycle.models.v06.ResRepositoryModel;
import nd.esp.service.lifecycle.services.repository.v06.ResRepositoryService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResRepoInfo;
import nd.esp.service.lifecycle.repository.sdk.ResRepoInfoRepository;
/**
 * 物理存储空间Service实现
 * <p>Create Time: 2015年7月16日           </p>
 * @author xiezy
 */
@Service("resRepositoryService")
@Transactional
public class ResRepositoryServiceImpl implements ResRepositoryService{
	private static final Logger LOG = LoggerFactory.getLogger(ResRepositoryServiceImpl.class);
    
    @Autowired
    private ResRepoInfoRepository resRepoInfoRepository;
    
    @Override
    public ResRepositoryModel createRepository(ResRepositoryModel repositoryModel) {
        ResRepoInfo repoInfo = ObjectUtils.fromJson(ObjectUtils.toJson(repositoryModel), ResRepoInfo.class);
        try {
            repoInfo = resRepoInfoRepository.add(repoInfo);
        } catch (EspStoreException e) {
            LOG.error("申请物理资源存储空间失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
        }
        
        if(repoInfo == null){
            LOG.error("申请物理资源存储空间失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateRepositoryFail);
        }
        
        return ObjectUtils.fromJson(ObjectUtils.toJson(repoInfo), ResRepositoryModel.class);
    }

    @Override
    public ResRepositoryModel getRepositoryDetailByCondition(String type, String target) {
        ResRepoInfo repoInfo = new ResRepoInfo();
        repoInfo.setTargetType(type);
        repoInfo.setTarget(target);
        repoInfo.setEnable(null);
        
        try {
            repoInfo = resRepoInfoRepository.getByExample(repoInfo);
        } catch (EspStoreException e) {
            LOG.error("获取物理空间信息失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
        }
        
        if(repoInfo == null){
            return null;
        }
        
        return ObjectUtils.fromJson(ObjectUtils.toJson(repoInfo), ResRepositoryModel.class);
    }

    @Override
    public ResRepositoryModel getRepositoryDetailById(String id) {
        ResRepoInfo repoInfo = null;
        
        try {
            repoInfo = resRepoInfoRepository.get(id);
        } catch (EspStoreException e) {
            LOG.error("获取物理空间信息失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
        }
        
        if(repoInfo == null){
            return null;
        }
        
        return ObjectUtils.fromJson(ObjectUtils.toJson(repoInfo), ResRepositoryModel.class);
    }

    @Override
    public ResRepositoryModel updateRepository(ResRepositoryModel repositoryModel) {
        ResRepoInfo repoInfo = ObjectUtils.fromJson(ObjectUtils.toJson(repositoryModel), ResRepoInfo.class);
        
        try {
            repoInfo = resRepoInfoRepository.update(repoInfo);
        } catch (EspStoreException e) {
            LOG.error("修改资源物理空间信息失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
        }
        
        if(repoInfo == null){
            LOG.error("修改资源物理空间信息失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateRepositoryFail);
        }
        
        return ObjectUtils.fromJson(ObjectUtils.toJson(repoInfo), ResRepositoryModel.class);
    }

    @Override
    public boolean deleteRepository(ResRepositoryModel repositoryModel) {
        ResRepoInfo repoInfo = ObjectUtils.fromJson(ObjectUtils.toJson(repositoryModel), ResRepoInfo.class);
        
        try {
            repoInfo = resRepoInfoRepository.update(repoInfo);
        } catch (EspStoreException e) {
            LOG.error("删除资源物理空间信息失败",e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
        }
        
        if(repoInfo == null){
            return false;
        }
        
        return !repoInfo.getEnable();
    }
}
