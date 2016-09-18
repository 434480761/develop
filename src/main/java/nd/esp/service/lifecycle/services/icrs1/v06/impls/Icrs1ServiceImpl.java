package nd.esp.service.lifecycle.services.icrs1.v06.impls;

import java.util.List;

import nd.esp.service.lifecycle.daos.Icrs1.v06.Icrs1Dao;
import nd.esp.service.lifecycle.models.icrs1.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs1.v06.TextbookModel;
import nd.esp.service.lifecycle.services.icrs1.v06.Icrs1Service;
import nd.esp.service.lifecycle.vos.icrs1.v06.DailyDataViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<DailyDataViewModel> getResourceStatisticsByDay(String schoolId,
			String resType, String fromDate, String toDate) {

		return icrsDao.getResourceStatisticsByDay(schoolId, resType, fromDate,
				toDate);
	}

	@Override
	public List<TextbookModel> getTeacherResource(String schoolId,
			String teacherId, String resType) {

		return icrsDao.getTeacherResource(schoolId, teacherId, resType);
	}

}
