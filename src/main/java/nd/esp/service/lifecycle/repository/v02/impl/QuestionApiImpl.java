/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.Hits;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.Question;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.v02.QuestionApi;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */
@Repository("QuestionApi")
public class QuestionApiImpl extends BaseStoreApiImpl<Question> implements QuestionApi {

	private static final Logger logger = LoggerFactory
			.getLogger(QuestionApiImpl.class);

	@Autowired
	QuestionRepository  questionRepository;
	
	@Autowired
	ResourceRelationRepository resourceRelationRepository;
	
	@Override
	protected ResourceRepository<Question> getResourceRepository() {
		return questionRepository;
	}

	@Override
	public QueryResponse<Question> search(String chapterid, String creatorid,
			String publisher, List<String> questionType, String difficulty,
			QueryRequest query) throws EspStoreException {
		if(StringUtils.isEmpty(chapterid)){
			AdaptQueryRequest<Question> queryRequest= new AdaptQueryRequest<>(query);
			Question bean  = new Question();
			bean.setCreator(creatorid);
			bean.setPublisher(publisher);
			bean.setDifficulty(difficulty);
			queryRequest.setParam(bean);
			queryRequest.and("questionType", questionType);
			
			if (logger.isInfoEnabled()) {
                
			    logger.info("queryRequest is {}", queryRequest);
			    
            }
			        
			return this.searchByExample(queryRequest);
		}else{
			AdaptQueryRequest<ResourceRelation> queryRelationRequest= new AdaptQueryRequest<>(query);
			ResourceRelation relation = new ResourceRelation();
			relation.setSourceUuid(chapterid);
			relation.setResType(IndexSourceType.ChapterType.getName());
			queryRelationRequest.setParam(relation);
			queryRelationRequest.and("questionType", questionType);
			queryRelationRequest.and("difficulty", difficulty);
			queryRelationRequest.and("publisher", publisher);
			queryRelationRequest.and("creatorid", creatorid);
			QueryResponse<ResourceRelation> temp =  resourceRelationRepository.searchByExample(queryRelationRequest);
			List<String> ids = Lists.newArrayList();
			if(temp.getHits()!=null&&temp.getHits().getTotal()>0){
				for(ResourceRelation item : temp.getHits().getDocs()){
					ids.add(item.getIdentifier());
				}
			}
			List<Question> qs = questionRepository.getAll(ids);
			Hits<Question> hits = new Hits<Question>();
			hits.setTotal(temp.getHits().getTotal());
			hits.setDocs(qs);
			QueryResponse<Question> resp = new QueryResponse<>();
			resp.setResponseBody(temp.getResponseBody());
			resp.setResponseHeader(temp.getResponseHeader());
			resp.setHits(hits);
			return resp;
		}
	}
}
