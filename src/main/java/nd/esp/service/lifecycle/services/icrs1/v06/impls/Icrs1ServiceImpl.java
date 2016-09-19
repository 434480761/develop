package nd.esp.service.lifecycle.services.icrs1.v06.impls;

import java.util.List;

import nd.esp.service.lifecycle.daos.Icrs1.v06.Icrs1Dao;
import nd.esp.service.lifecycle.models.icrs1.v06.DailyDataModel;
import nd.esp.service.lifecycle.models.icrs1.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs1.v06.TextbookModel;
import nd.esp.service.lifecycle.services.icrs1.v06.Icrs1Service;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("Icrs1ServiceImpl")
@Transactional
public class Icrs1ServiceImpl implements Icrs1Service {

	@Autowired
	private Icrs1Dao icrsDao;

	@Override
	public List<ResourceTotalModel> getResourceTotal(String schoolId,
			String fromDate, String toDate) {
		return icrsDao.getResourceTotal(schoolId, fromDate, toDate);
	}

	@Override
	public List<DailyDataModel> getResourceStatisticsByDay(String schoolId,
			String resType, String fromDate, String toDate) {

		String type = null;
		if (StringUtils.hasText(resType)) {
			if (resType.equals("courseware")) {
				type = "cousewares";
			} else if (resType.equals("multimedia")) {
				type = "assets";
			} else if (resType.equals("basic_question")) {
				type = "questions";
			} else if (resType.equals("funny_question")) {
				type = "coursewareobjects";
			} else {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.ResourceTypeNotFound
								.getCode(),
						LifeCircleErrorMessageMapper.ResourceTypeNotFound
								.getMessage());
			}
		}
		return icrsDao.getResourceStatisticsByDay(schoolId, type, fromDate,
				toDate);
	}

	@Override
	public List<TextbookModel> getTeacherResource(String schoolId, String teacherId,
			String resType) {
		
		return icrsDao.getTeacherResource(schoolId,teacherId,resType);
	}

}
