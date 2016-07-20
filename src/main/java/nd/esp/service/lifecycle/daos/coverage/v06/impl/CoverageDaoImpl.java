package nd.esp.service.lifecycle.daos.coverage.v06.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.sdk.ResCoverage4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResCoverageRepository;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CoverageDaoImpl implements CoverageDao{
    
    @Autowired
    private ResCoverageRepository resCoverageRepository;
    
    @Autowired
    private ResCoverage4QuestionDBRepository resCoverage4QuestionDBRepository;
    
    @Override
    public Map<String, List<CoverageViewModel>> batchGetCoverageByResource(String resType, List<String> rids,DbName dbName) {
        //返回的结果集
        Map<String, List<CoverageViewModel>> resultMap = new HashMap<String, List<CoverageViewModel>>();
        Query query = null;
        if(dbName.equals(DbName.DEFAULT)){
        	query = resCoverageRepository.getEntityManager().createNamedQuery("batchGetCoverageByResource");
        }else{
        	query = resCoverage4QuestionDBRepository.getEntityManager().createNamedQuery("batchGetCoverageByResource");
        }
        
        query.setParameter("rt", resType);
        query.setParameter("rids", rids);
        
        List<ResCoverage> result = query.getResultList();
        if(CollectionUtils.isNotEmpty(result)){
            for(ResCoverage rc : result){
                String resource = rc.getResource();

                if (resultMap.containsKey(resource)) {// 已存在
                    List<CoverageViewModel> existList = resultMap.get(resource);
                    existList.add(BeanMapperUtils.beanMapper(rc, CoverageViewModel.class));
                }else {// 新记录
                    List<CoverageViewModel> cvList = new ArrayList<CoverageViewModel>();
                    cvList.add(BeanMapperUtils.beanMapper(rc, CoverageViewModel.class));

                    resultMap.put(resource, cvList);
                }
            }
        }
        
        return resultMap;
    }
}
