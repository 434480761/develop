/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.v02.ChapterApi;

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
@Repository("ChapterApi")
public class ChapterApiImpl extends BaseStoreApiImpl<Chapter> implements ChapterApi {

	private static final Logger logger = LoggerFactory
			.getLogger(ChapterApiImpl.class);

	@Autowired
	ChapterRepository  chapterRepository;
	
	@Override
	protected ResourceRepository<Chapter> getResourceRepository() {
		return chapterRepository;
	}

	@Override
	public List<Chapter> getChildList(String chapterId)
			throws EspStoreException {
		Chapter bean = new Chapter();
		bean.setParent(chapterId);
		return chapterRepository.getAllByExample(bean);
	}

	@Override
	public List<Chapter> getChapterByMaterialId(String materialId)
			throws EspStoreException {
		Chapter bean = new Chapter();
		bean.setTeachingMaterial(materialId);
		return chapterRepository.getAllByExample(bean);
	}


}
