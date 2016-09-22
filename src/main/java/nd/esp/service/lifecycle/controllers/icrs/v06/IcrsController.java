package nd.esp.service.lifecycle.controllers.icrs.v06;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.models.icrs.v06.DailyDataModel;
import nd.esp.service.lifecycle.models.icrs.v06.HourDataModel;
import nd.esp.service.lifecycle.models.icrs.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs.v06.TeacherOutputResource;
import nd.esp.service.lifecycle.models.icrs.v06.TextbookModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.icrs.v06.IcrsService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.icrs.IcrsResourceType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.icrs.v06.DailyDataViewModel;
import nd.esp.service.lifecycle.vos.icrs.v06.ResourceTotalViewModel;
import nd.esp.service.lifecycle.vos.icrs.v06.TextbookViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.icu.text.SimpleDateFormat;

@RestController
@RequestMapping("/v0.6/icrs")
public class IcrsController {

	@Autowired
	private IcrsService icrsService;

	/**
	 * 查询本校不同类别资源的产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型
	 * 
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param from_date
	 * @param to_date
	 * @return
	 */
	@RequestMapping(value = "/{school_id}/statistics", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResourceTotalViewModel getResourceStatisticsTotal(
			@PathVariable(value = "school_id") String schoolId,
			@RequestParam(required = false, value = "from_date") String fromDate,
			@RequestParam(required = false, value = "to_date") String toDate) {

		// 校验日期是否合法
		if (StringUtils.hasText(fromDate)) {
			isValidDate(fromDate);
		}
		if (StringUtils.hasText(toDate)) {
			isValidDate(toDate);
		}
		List<ResourceTotalModel> rtm = icrsService.getResourceTotal(schoolId,
				fromDate, toDate);

		return changeToViewModel(rtm);
	}

	/**
	 * 查询本校资源的日产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型。
	 * 
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param from_date
	 * @param to_date
	 * @return
	 */
	@RequestMapping(value = "/{school_id}/statistics/day", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE }, params = {
			"from_date", "to_date" })
	public List<DailyDataViewModel> getResourceStatisticsByDay(
			@PathVariable(value = "school_id") String schoolId,
			@RequestParam(required = false, value = "res_type") String resType,
			@RequestParam String from_date, @RequestParam String to_date) {

		// 校验日期是否合法
		isValidDate(from_date);
		isValidDate(to_date);

		// 校验resType和获取对应NDR资源类型
		if (StringUtils.hasText(resType)) {
			if (!IcrsResourceType.validType(resType)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.ResourceTypeNotFound);
			}

			resType = IcrsResourceType.getCorrespondingType(resType);
		}

		List<DailyDataModel> ddmList = icrsService.getResourceStatisticsByDay(
				schoolId, resType, from_date, to_date);

		List<DailyDataViewModel> ddvmList = new ArrayList<DailyDataViewModel>();
		if (CollectionUtils.isNotEmpty(ddmList)) {
			for (DailyDataModel model : ddmList) {
				DailyDataViewModel ddvm = new DailyDataViewModel();
				ddvm.setData(model.getData());
				ddvm.setDate(model.getDate());
				ddvmList.add(ddvm);
			}
		}

		return ddvmList;
	}

	/**
	 * 取得某一个教师上传的资源，所对应的教材列表
	 * 
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param teacherId
	 * @param resType
	 * @return
	 */
	@RequestMapping(value = "/{school_id}/{teacher_id}/teachingmaterials", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<TextbookViewModel> getTeacherResource(
			@PathVariable(value = "school_id") String schoolId,
			@PathVariable(value = "teacher_id") String teacherId,
			@RequestParam(required = false, value = "res_type") String resType) {

		// 校验resType和获取对应NDR资源类型
		if (StringUtils.hasText(resType)) {
			if (!IcrsResourceType.validType(resType)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.ResourceTypeNotFound);
			}

			resType = IcrsResourceType.getCorrespondingType(resType);
		}

		List<TextbookModel> list = icrsService.getTeacherResource(schoolId,
				teacherId, resType);
		List<TextbookViewModel> tvmlist = new ArrayList<TextbookViewModel>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (TextbookModel model : list) {
				TextbookViewModel tvm = new TextbookViewModel();
				tvm.setUuid(model.getUuid());
				tvm.setTitle(model.getTitle());
				tvmlist.add(tvm);
			}
		}
		return tvmlist;
	}

	/**
	 * List<ResourceTotalModel> 转换为 ResourceTotalViewModel
	 * 
	 * @author yuzc
	 * @date 2016年9月12日
	 * @param rtm
	 * @return rtvm
	 */
	public ResourceTotalViewModel changeToViewModel(List<ResourceTotalModel> rtm) {

		ResourceTotalViewModel rtvm = new ResourceTotalViewModel();
		if (CollectionUtils.isNotEmpty(rtm)) {
			for (ResourceTotalModel model : rtm) {
				if (model.getResType().equals(
						IndexSourceType.AssetType.getName())) {
					rtvm.setTotalMultimedia(model.getResTotal());
				} else if (model.getResType().equals(
						IndexSourceType.SourceCourseWareType.getName())) {
					rtvm.setTotalCourseware(model.getResTotal());
				} else if (model.getResType().equals(
						IndexSourceType.QuestionType.getName())) {
					rtvm.setTotalBasicQuestion(model.getResTotal());
				} else if (model.getResType().equals(
						IndexSourceType.SourceCourseWareObjectType.getName())) {
					rtvm.setTotalFunnyQuestion(model.getResTotal());
				}
			}
		}

		return rtvm;
	}

	/**
	 * 查询本校教师的资源产出数据
	 * 
	 * @author xm
	 * @version
	 * @date 2016年9月14日 下午6:12:04
	 * @method getTeacherResourceOutput
	 * @see
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param grade
	 * @param subject
	 * @param order
	 * @param limit
	 * @return ListViewModel<TeacherOutputResource>
	 * @throws
	 */
	@RequestMapping(value = "/{school_id}/statistics/query", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE }, params = { "limit" })
	public ListViewModel<TeacherOutputResource> getTeacherResourceOutput(
			@PathVariable(value = "school_id") String schoolId,
			@RequestParam(required = false, value = "res_type") String resType,
			@RequestParam(required = false, value = "from_date") String fromDate,
			@RequestParam(required = false, value = "to_date") String toDate,
			@RequestParam(required = false, value = "grade") String grade,
			@RequestParam(required = false, value = "subject") String subject,
			@RequestParam(required = false, value = "order") String order,
			@RequestParam String limit) {

		// limit校验
		ParamCheckUtil.checkLimit(limit);

		// 入参检验
		isValidInput(schoolId, resType, fromDate, toDate, null, order);
		// 转为数据库中reyType类型
		resType = IcrsResourceType.getCorrespondingType(resType);

		ListViewModel<TeacherOutputResource> returnList = icrsService
				.queryTeacherResourceOutput(schoolId, resType, fromDate,
						toDate, grade, subject, order, limit);

		return returnList;
	}

	/**
	 * 查询本校资源一天内各时段的产出数量
	 * 
	 * @author xm
	 * @version
	 * @date 2016年9月14日 下午6:13:01
	 * @method getRecourcesPerHour
	 * @see
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return List<Map<String,Object>>
	 * @throws
	 */
	@RequestMapping(value = "/{school_id}/statistics/hour", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<HourDataModel> getRecourcesPerHour(
			@PathVariable(value = "school_id") String schoolId,
			@RequestParam(required = false, value = "res_type") String resType,
			@RequestParam(required = true, value = "query_date") String queryDate)
			throws java.lang.Exception {

		// 入参检验
		isValidInput(schoolId, resType, null, null, queryDate, null);
		resType = IcrsResourceType.getCorrespondingType(resType);

		return icrsService.queryResourcePerHourOutput(schoolId, resType,
				queryDate);
	}

	/**
	 * 对查询入参检验
	 * 
	 * @author xm
	 * @version
	 * @date 2016年9月14日 下午6:14:08
	 * @method isValidInput
	 * @see
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param queryDate
	 * @param order
	 * @return void
	 * @throws
	 */
	public void isValidInput(String schoolId, String resType, String fromDate,
			String toDate, String queryDate, String order) {

		if (!StringUtils.hasText(schoolId)) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CheckIcrsParamValidFail);
		}
		if (StringUtils.hasText(resType)) {
			if (!IcrsResourceType.validType(resType)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CheckIcrsParamValidFail);
			}
		}
		if (StringUtils.hasText(order)) {
			if (!order.equalsIgnoreCase("desc")
					&& !order.equalsIgnoreCase("asc")) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CheckIcrsParamValidFail);
			}
		}
		if (StringUtils.hasText(fromDate)) {
			isValidDate(fromDate);
		}
		if (StringUtils.hasText(toDate)) {
			isValidDate(toDate);
		}
		if (StringUtils.hasText(queryDate)) {
			isValidDate(queryDate);
		}
	}

	/**
	 * 校验日期格式
	 * 
	 * @author yuzc
	 * @date 2016年9月12日
	 * @param str
	 * @return
	 */
	public boolean isValidDate(String str) {

		boolean convertSuccess = true;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		format.setLenient(false);
		try {
			format.parse(str);
		} catch (java.text.ParseException e) {

			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.DateFormatFail);
		}
		return convertSuccess;
	}

	// /**
	// * 对Date类型进行校验，看是否满足yyyy-MM-dd类型
	// *
	// * @author xm
	// * @version
	// * @date 2016年9月14日 下午6:47:18
	// * @method isValidDate
	// * @see
	// * @param date
	// * @return void
	// * @throws
	// */
	// public void isValidDate(String date) {
	// if (StringUtils.hasText(date)) {
	// try {
	// String formDateString = date;
	// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	// format.setLenient(false);
	// format.parse(formDateString);
	// } catch (Exception e) {
	// throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	// LifeCircleErrorMessageMapper.CheckIcrsParamValidFail
	// .getCode(), e.getMessage());
	// }
	// }
	// }
}
