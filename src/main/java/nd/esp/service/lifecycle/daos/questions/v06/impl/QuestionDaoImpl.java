package nd.esp.service.lifecycle.daos.questions.v06.impl;

import java.util.List;

import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.questions.v06.QuestionDao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;

@Repository
public class QuestionDaoImpl implements QuestionDao{
    private final static Logger LOG = LoggerFactory.getLogger(QuestionDaoImpl.class);
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Transactional
    public boolean delBatchAutoQuestions(String chapterid) {
        String sql = String.format("SELECT a.identifier FROM (ndresource a INNER JOIN resource_relations rr ON a.identifier=rr.target AND (rr.enable=1 AND rr.res_type='chapters' AND rr.source_uuid='%s' AND rr.resource_target_type='questions' AND rr.relation_type='ASSOCIATE')) WHERE a.provider='智能出题'",
                chapterid);
        
        LOG.info("查询自动出题下架习题的SQL语句:" + sql);
        
        Query query = questionRepository.getEntityManager().createNativeQuery(sql);
        List<String> listId = query.getResultList();
        
        if(CollectionUtils.isNotEmpty(listId)) {
            String qtiList = "('" + StringUtils.join(listId.iterator(), "','") + "')";
            //主表
            String ndrUpdateSql = "UPDATE ndresource SET enable=0 WHERE identifier IN " + qtiList;
            //关系
            String relationUpdateSql = "UPDATE resource_relations SET enable=0 WHERE (resource_target_type = 'questions' AND target IN " 
                    + qtiList + ") OR (res_type = 'questions' AND source_uuid IN " + qtiList + ")";
            
            LOG.info("自动出题下架习题删除的SQL语句:" + ndrUpdateSql);
            LOG.info("自动出题下架习题删除关系的SQL语句:" + relationUpdateSql);
            
            questionRepository.getEntityManager().createNativeQuery(ndrUpdateSql).executeUpdate();
            questionRepository.getEntityManager().createNativeQuery(relationUpdateSql).executeUpdate();
        }
        
        return true;
    }
}
